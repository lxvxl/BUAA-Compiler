package intermediateCode;

import Writer.MipsGenerator;
import ident.SymbolTable;
import ident.idents.Func;
import ident.idents.Var;
import intermediateCode.instructions.*;
import intermediateCode.optimize.BasicBlock;
import intermediateCode.optimize.RegAllocator;
import intermediateCode.optimize.StackAlloactor;
import parser.nodes.AddExp;
import parser.nodes.Block;

import java.util.*;

public class FuncCode {
    private final String name;
    private List<Inst> insts;
    private final Func func;

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
        output();
        //基本块划分
        HashMap<String, BasicBlock> labelToBlock = new HashMap<>();
        List<BasicBlock> basicBlocks = new ArrayList<>();
        generateBasicBlocks(basicBlocks, labelToBlock);
        //insts.clear();
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
        /*if (true) {
            throw new RuntimeException();
        }*/

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
        BasicBlock curBlock = new BasicBlock(name, regMap);
        labelToBlock.put(name, curBlock);
        for (Inst inst : insts) {
            if (inst instanceof Label label) {
                if (curBlock != null) {
                    curBlock.setNextBlock(label.label());
                    basicBlocks.add(curBlock);
                }
                curBlock = new BasicBlock(label.label(), regMap);
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
                        areaMap.put(newVarAddr, param);
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

            for (Inst tf : anotherFuncInsts) {
                if (tf instanceof LoadInst loadInst) {
                    String loadInstAddr = Inst.transformParam(loadInst.addr(), n, funcName);
                    if (areaMap.containsKey(loadInstAddr)) {
                        areaMap.put(loadInst.result(), areaMap.get(loadInstAddr));
                    }
                }/* else if (tf instanceof StoreInst storeInst) {
                    String storeInstAddr = Inst.transformParam(storeInst.addr(), n, funcName);
                    if (areaMap.containsKey(storeInstAddr)) {
                        areaMap.put(storeInst.getResult(), areaMap.get(storeInstAddr));
                    }
                }*/
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
                } else {
                    newInsts.add(inst1.replace(n, funcName));
                }
            }
        }
        this.insts = newInsts;
    }

    public String name() {
        return name;
    }

    public List<Inst> insts() {
        return insts;
    }
}
