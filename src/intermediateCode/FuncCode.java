package intermediateCode;

import Writer.MipsGenerator;
import ident.SymbolTable;
import ident.idents.Func;
import ident.idents.Var;
import intermediateCode.instructions.*;
import intermediateCode.optimize.BasicBlock;
import intermediateCode.optimize.RegAllocator;
import intermediateCode.optimize.StackAlloactor;

import java.util.*;

public class FuncCode {
    private final String name;
    private List<Inst> insts;
    private final Func func;
    private boolean inferable;//表示是否可以从参数推断出函数的返回值

    private boolean hasSideEffect;

    private static final boolean peepholeOp = true;

    public FuncCode(String name, List<Inst> insts) {
        this.name = name;
        this.insts = insts;
        this.func = (Func) SymbolTable.searchIdent(name);
    }
    public void optimize() {
        //初始化
        RegAllocator.resetAll();
        StackAlloactor.reset();
        //函数内联
        checkInlinable();
        inlineFuncs();
        //检查副作用
        this.checkSideEffect();
        //基本块划分
        HashMap<String, BasicBlock> labelToBlock = new HashMap<>();
        List<BasicBlock> basicBlocks = new ArrayList<>();
        generateBasicBlocks(basicBlocks, labelToBlock);
        //expandLoop(basicBlocks, labelToBlock);
        //dag优化
        for (BasicBlock block : basicBlocks) {
            block.dagOptimize();
        }
        //活跃变量分析
        boolean isFinished = false;
        while (!isFinished) {
            isFinished = true;
            for (int i = basicBlocks.size() - 1; i >= 0; i--) {
                BasicBlock curBlock = basicBlocks.get(i);
                for (String label : curBlock.getNextBlock()) {
                    if (curBlock.addUsefulVar(labelToBlock.get(label).getInUsefulVar())) {
                        isFinished = false;
                    }
                }
                if (curBlock.generateInUsefulVar()) {
                    isFinished = false;
                }
            }
        }
        basicBlocks.forEach(BasicBlock::removeUnusedVar);

        //生成指令序号
        CodeGenerator.clearInstNum();
        basicBlocks.forEach(b -> b.getInsts().forEach(CodeGenerator::recordInstNum));

        //准备寄存器分配
        basicBlocks.forEach(BasicBlock::allocaReg);
        //额外处理函数参数的状况
        RegAllocator.dealFuncParamConflict(func.getParams().stream().map(Var::getAddrReg).toList());

        insts.clear();
        basicBlocks.forEach(b -> insts.addAll(b.getInsts()));
        RegAllocator.allocateGlobalReg();
        checkInferable();
        output();
        toMips2(basicBlocks);
    }

    public void output() {
        StringBuilder buffer = new StringBuilder();
        for (Var var : func.getParams()) {
            if (!buffer.isEmpty()) {
                buffer.append(' ');
            }
            buffer.append(var.getAddrReg());
        }
        System.out.printf("function %s %s (%s)\n", func.getReturnType(), func.getName(), buffer);
        for (Inst inst : insts) {
            System.out.println((inst instanceof Label ? "" : "\t") + inst);
        }
    }

    public void toMips() {
        FrameMonitor.funcIn();
        MipsGenerator.addInst("func_" + name + ":");
        for (int i = 0; i < func.getParams().size(); i++) {
            FrameMonitor.mapParam(func.getParams().get(i).getAddrReg(), -i);
        }
        for (int i = 0; i < insts().size(); i++) {
            Inst inst = insts.get(i);
            if (name.equals("main") && inst instanceof RetInst) {
                MipsGenerator.addInst("\tli $v0, 10");
                MipsGenerator.addInst("\tsyscall");
                continue;
            }

            if (peepholeOp) {
                if (inst instanceof CmpInst cmpInst
                        && i + 1 < insts().size()
                        && insts.get(i + 1) instanceof BrInst brInst
                        && brInst.reg().equals(cmpInst.getResult())) {
                    i++;
                    cmpInst.toMipsWithBr(brInst);
                    continue;
                }
            }
            inst.toMips();
        }
        MipsGenerator.addInst("\tmove $sp, $fp");
        MipsGenerator.addInst("\tjr $ra");
    }

    private void toMips2(List<BasicBlock> basicBlocks) {
        MipsGenerator.addInst("func_" + name + ":");
        List<String> params = func.getParams().stream().map(Var::getAddrReg).toList();
        RegAllocator.initFuncParam(params);
        for (BasicBlock b : basicBlocks) {
            for (int i = 0; i < b.getInsts().size(); i++) {
                Inst inst = b.getInsts().get(i);
                //System.out.println(b.hashCode() + inst.toString());
                if (name.equals("main") && inst instanceof RetInst) {
                    MipsGenerator.addInst("\tli $v0, 10");
                    MipsGenerator.addInst("\tsyscall");
                    continue;
                }

                if (peepholeOp) {
                    if (inst instanceof CmpInst cmpInst
                            && i + 1 < insts().size()
                            && b.getInsts().get(i + 1) instanceof BrInst brInst
                            && brInst.reg().equals(cmpInst.getResult())) {
                        i++;
                        cmpInst.toMipsWithBr(brInst);
                        continue;
                    }
                }
                inst.toMips();
            }
            RegAllocator.clearTempReg();
        }
        MipsGenerator.addInst("\tmove $sp, $fp");
        MipsGenerator.addInst("\tjr $ra");
    }

    /**
     * 生成基本快
     */
    public void generateBasicBlocks(List<BasicBlock> basicBlocks, HashMap<String, BasicBlock> labelToBlock) {
        Map<String, String> regMap = new HashMap<>();
        func.getParams().forEach(var -> regMap.put(var.getAddrReg(), var.getAddrReg()));
        List<String> funcArrParams = func.getParams().stream()
                .filter(v -> v.getDim() > 0)
                .map(Var::getAddrReg)
                .toList();

        BasicBlock curBlock = new BasicBlock(name, regMap, funcArrParams);
        labelToBlock.put(name, curBlock);
        for (Inst inst : insts) {
            if (inst instanceof Label label) {
                if (curBlock != null) {
                    curBlock.setNextBlock(label.label());
                    basicBlocks.add(curBlock);
                }
                curBlock = new BasicBlock(label.label(), regMap, funcArrParams);
                labelToBlock.put(label.label(), curBlock);
                curBlock.addInst(inst);
            } else if (inst instanceof JumpInst jumpInst) {
                if (curBlock == null) {
                    continue;
                }
                curBlock.setNextBlock(jumpInst.label());
                curBlock.addInst(inst);
                basicBlocks.add(curBlock);
                curBlock = null;
            } else if (inst instanceof BrInst brInst) {
                if (curBlock == null) {
                    continue;
                }
                curBlock.setNextBlock(brInst.trueLabel(), brInst.falseLabel());
                curBlock.addInst(inst);
                basicBlocks.add(curBlock);
                curBlock = null;
            } else {
                if (curBlock == null) {
                    continue;
                }
                curBlock.addInst(inst);
            }
        }
        if (curBlock != null && !curBlock.getInsts().isEmpty()) {
            basicBlocks.add(curBlock);
        }
    }

    private void checkInlinable() {
        for (int i = 0; i < insts.size(); i++) {
            Inst inst = insts.get(i);
            if (inst instanceof CallInst callInst && callInst.funcName().equals(func.getName())
                || inst instanceof Label
                || inst instanceof RetInst && i != insts.size() - 1) {
                func.setInlinable(false);
                return;
            }
        }
        func.setInlinable(true);
    }

    private void inlineFuncs() {
        List<Inst> newInsts = new ArrayList<>();
        HashMap<String, Integer> inlinedFuncs = new HashMap<>();

        for (int j = 0; j < insts.size(); j++) {
            Inst inst = insts.get(j);
            //如果不是调用函数指令，则忽略
            if (!(inst instanceof CallInst callInst)) {
                newInsts.add(inst);
                continue;
            }

            //检查函数是否是可内联的
            String funcName = callInst.funcName();
            Func newFunc = (Func) SymbolTable.searchIdent(funcName);
            assert newFunc != null;
            if (!newFunc.isInlinable()) {
                newInsts.add(inst);
                continue;
            }
            List<Var> vars = newFunc.getParams();

            //若这个函数是第一次内联，先为其形式参数分配栈上的空间
            if (!inlinedFuncs.containsKey(funcName)) {
                vars.forEach(var -> newInsts.add(new AllocaInst(Inst.transformParam(var.getAddrReg(), 0, funcName),
                        4,
                        false)));
            }

            //维护函数被内联的次数
            int n = inlinedFuncs.getOrDefault(funcName, 0) + 1;
            inlinedFuncs.put(funcName, n);

            //为形参赋值
            List<String> params = callInst.params();
            Map<String, String> areaMap = new HashMap<>();
            for (int i = 0; i < vars.size(); i++) {
                Var var = vars.get(i);
                String param = params.get(i);
                String newVarAddr = Inst.transformParam(var.getAddrReg(), n, funcName);
                newInsts.add(new StoreInst(params.get(i),
                        newVarAddr,
                        0,
                        null,
                        false
                ));
                if (var.getDim() != 0) {
                    if (!Inst.isTempParam(param)) {
                        areaMap.put(var.getAddrReg(), param);
                    } else {
                        String arrReg = null;
                        for (int k = j - 1; k > 0 ; k--) {
                            if (param.equals(insts.get(k).getResult())) {
                                if (insts.get(k) instanceof AddInst addInst) {
                                    arrReg = Inst.isStackParam(addInst.para1()) || Inst.isGlobalParam(addInst.para1()) 
                                            ? addInst.para1() : addInst.para2();
                                    break;
                                } else if (insts.get(k) instanceof LoadInst loadInst) {
                                    arrReg = loadInst.result();
                                }
                            }
                        }
                        areaMap.put(var.getAddrReg(), arrReg);
                    }
                }
            }

            //将被内联函数的中间代码插入到现在的函数中
            List<Inst> anotherFuncInsts = CodeGenerator.getFuncCode(funcName).insts();
            //先维护内存区域的标记
            //TODO 我忘了这里是干啥用的了，好像能删
            for (Inst tf : anotherFuncInsts) {
                if (tf instanceof LoadInst loadInst) {
                    String loadInstAddr = Inst.transformParam(loadInst.addr(), n, funcName);
                    if (areaMap.containsKey(loadInstAddr)) {
                        areaMap.put(loadInst.result(), areaMap.get(loadInstAddr));
                    }
                }
            }

            for (Inst inst1 : anotherFuncInsts) {
                if (inst1 instanceof RetInst retInst) {
                    if (callInst.result() != null) {
                        newInsts.add(new AddInst(callInst.result(), "0", Inst.transformParam(retInst.ret(), n, funcName)));
                    }
                } else if (inst1 instanceof LoadInst loadInst) {
                    newInsts.add(loadInst.replace(n, funcName, areaMap));
                } else if (inst1 instanceof StoreInst storeInst) {
                    newInsts.add(storeInst.replace(n, funcName, areaMap));
                } else if (inst1 instanceof AllocaInst && n > 1) {
                } else {
                    newInsts.add(inst1.replace(n, funcName));
                }
            }
        }
        this.insts = newInsts;
    }

    private class MyException extends RuntimeException {}

    public void expandLoop(List<BasicBlock> basicBlocks, HashMap<String, BasicBlock> labelToBlock) {
        int expandTimes = 0;
        for (int i = 3; i < basicBlocks.size() - 1; i++) {
            BasicBlock endBlock = basicBlocks.get(i);
            BasicBlock bodyBlock = basicBlocks.get(i - 1);
            BasicBlock cmpBlock = basicBlocks.get(i - 2);
            BasicBlock preBlock = basicBlocks.get(i - 3);
            try {
                if (!endBlock.getNextBlock().get(0).equals(cmpBlock.getLabel())) {
                    throw new Exception();
                }

                //判断cmpBlock是否合法
                List<Inst> cmpInsts = cmpBlock.getInsts();
                LoadInst cmpLoad1 = (LoadInst) cmpInsts.get(1);
                LoadInst cmpLoad2 = (LoadInst) cmpInsts.get(2);
                CmpInst cmpCmp = (CmpInst) cmpInsts.get(3);
                if (cmpInsts.size() != 5 || cmpLoad1.isArray() || cmpLoad1.isGlobalArea()
                        || cmpLoad2.isArray() || cmpLoad2.isGlobalArea()) {
                    throw new Exception();
                }
                List<Inst> preInsts = preBlock.getInsts();
                int maxI = Integer.parseInt(cmpCmp.para2());
                String paramI = cmpLoad1.addr();
                //确保bodyBlock不会修改i的值
                if (bodyBlock.getInsts().stream().anyMatch(inst -> inst instanceof StoreInst storeInst
                        && storeInst.addr().equals(paramI))) {
                    throw new Exception();
                }
                //确保endBlock满足要求
                LoadInst endLoad = (LoadInst) endBlock.getInsts().get(1);
                AddInst endAdd = (AddInst) endBlock.getInsts().get(2);
                StoreInst endStore = (StoreInst) endBlock.getInsts().get(3);
                if (endBlock.getInsts().size() != 5
                    || !endLoad.addr().equals(paramI)
                    || !endAdd.para2().equals("1")
                    || !endStore.addr().equals(paramI)) {
                    throw new Exception();
                }
                //查找i的初始值
                int initI = 0;
                for (int j = preInsts.size() - 1; j >= 0 ; j--) {
                    if (preInsts.get(j) instanceof StoreInst storeInst
                        && storeInst.addr().equals(paramI)) {
                        initI = Integer.parseInt(storeInst.val());
                    }
                    if (j == 0) {
                        throw new Exception();
                    }
                }
                int loopTimes = maxI - initI;
                List<Inst> bodyInsts = new ArrayList<>(bodyBlock.getInsts().subList(1, bodyBlock.getInsts().size() - 1));
                bodyInsts.add(endLoad);
                bodyInsts.add(endAdd);
                bodyInsts.add(endStore);
                preInsts.remove(preInsts.size() - 1);
                for (int j = 0; j < loopTimes; j++) {
                    int finalExpandTimes = expandTimes;
                    preInsts.addAll(bodyInsts.stream().map(inst -> inst.replaceFor(finalExpandTimes)).toList());
                    expandTimes++;
                }
                preInsts.addAll(basicBlocks.get(i + 1).getInsts().subList(1, basicBlocks.get(i + 1).getInsts().size()));
                basicBlocks.remove(i - 2);
                basicBlocks.remove(i - 2);
                basicBlocks.remove(i - 2);
                basicBlocks.remove(i - 2);
                i = Math.max(i - 4, 3);
            } catch (MyException e) {
                throw e;
            } catch (Exception e) {
                continue;
            }
        }
    }

    private void checkInferable() {
        if (func.getReturnType().equals("void")
                || this.func.getParams().stream().anyMatch(v -> v.getDim() > 0)
                || this.insts.size() > 300) {
            this.inferable = false;
            return;
        }
        for (Inst inst : insts) {
            if (inst instanceof GetIntInst
                || inst instanceof PutIntInst
                || inst instanceof PutStrInst
            ) {
                this.inferable = false;
                return;
            }
            if (inst instanceof CallInst callInst) {
                if (!CodeGenerator.getFuncCode(callInst.funcName()).isInferable()
                    && !callInst.funcName().equals(name)) {
                    this.inferable = false;
                    return;
                }
            }
            if (inst.getParams()
                    .stream()
                    .anyMatch(Inst::isGlobalParam)) {
                this.inferable = false;
                return;
            }
        }
        this.inferable = true;
    }

    private void checkSideEffect() {
        this.hasSideEffect = true;
        this.hasSideEffect = this.insts.stream()
                .anyMatch(i -> i instanceof GetIntInst
                || i instanceof PutIntInst
                || i instanceof PutStrInst
                || i instanceof CallInst callInst && CodeGenerator.getFuncCode(callInst.funcName()).hasSideEffect
                || i instanceof StoreInst storeInst && storeInst.isGlobalArea());
    }

    public boolean hasSideEffect() {
        return this.hasSideEffect;
    }


    public boolean isInferable() {
        return inferable;
    }

    public String name() {
        return name;
    }

    public List<Inst> insts() {
        return insts;
    }

    public Func getFunc() {
        return func;
    }

    /**
     * 查找一个变量到底是指向哪个内存区域的，从baseLoc向前回溯
     */
    public static String findArea(String param, List<Inst> insts, int baseLoc) {
        if (Inst.isTempParam(param)) {
            for (int k = baseLoc - 1;; k--) {
                if (insts.get(k) instanceof AddInst addInst && addInst.result().equals(param)) {
                    return  !Inst.isGlobalParam(addInst.para1()) && !Inst.isInt(addInst.para1()) ?
                            addInst.para2() : addInst.para1();
                } else if (insts.get(k) instanceof LoadInst loadInst && loadInst.result().equals(param)) {
                    return loadInst.addr();
                }
            }
        } else {
            return param;
        }
    }
}
