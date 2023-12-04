package intermediateCode.optimize;

import intermediateCode.Computable;
import intermediateCode.Inst;
import intermediateCode.instructions.*;

import java.util.*;

public class BasicBlock {
    private final String label;
    private final List<String> nextBlock;
    private List<Inst> insts;
    private final HashMap<String, String> regMap;
    private final Set<ParamAddr> addrRecords = new HashSet<>();
    private HashSet<String> inUsefulVar = new HashSet<>();
    private final HashSet<String> outUsefulVar = new HashSet<>();

    private record ParamAddr(String addr, int offset, String storeReg, boolean isArray) {}

    public BasicBlock(String label, Map<String, String> regMap) {
        this.label = label;
        this.nextBlock = new ArrayList<>();
        this.insts = new ArrayList<>();
        this.regMap = (HashMap<String, String>) regMap;
    }

    public void setNextBlock(String label1, String label2) {
        nextBlock.add(label1);
        nextBlock.add(label2);
    }

    public void setNextBlock(String label) {
        nextBlock.add(label);
    }

    public void addInst(Inst inst) {
        insts.add(inst);
    }

    public void output() {
        System.out.println("==========基本块==========");
        for (Inst inst : insts) {
            System.out.println((inst instanceof Label ? "" : "\t") + inst);
        }
        System.out.println("出口：" + String.join(",", nextBlock));
    }

    public List<Inst> getInsts() {
        return insts;
    }

    public void dagOptimize() {
        List<Inst> newInsts = new ArrayList<>();
        for (Inst inst : this.insts) {
            Inst newInst = inst.generateEquivalentInst(regMap);
            if (newInst instanceof Computable computableInst) {
                if (computableInst.getSpecificResult() != null) {
                    regMap.put(newInst.getResult(), computableInst.getSpecificResult());
                    continue;
                }
                Inst inst1 = newInsts.stream()
                        .filter(e -> e.getClass().equals(newInst.getClass()) && newInst.getParams().equals(e.getParams()))
                        .findFirst()
                        .orElse(null);
                if (inst1 == null) {
                    newInsts.add(newInst);
                    regMap.put(newInst.getResult(), newInst.getResult());
                } else {
                    regMap.put(newInst.getResult(), inst1.getResult());
                }
            } else if (newInst instanceof LoadInst loadInst) {
                if (loadInst.addr().charAt(0) == '@') {
                    newInsts.add(newInst);
                    continue;
                }
                ParamAddr paramAddr = addrRecords.stream()
                        .filter(p -> p.addr().equals(loadInst.addr()) && p.offset == loadInst.offset())
                        .findFirst()
                        .orElse(null);
                if (paramAddr == null) {
                    newInsts.add(newInst);
                    regMap.put(newInst.getResult(), newInst.getResult());
                    addrRecords.add(new ParamAddr(loadInst.addr(), loadInst.offset(), loadInst.result(), loadInst.isArray()));
                } else {
                    regMap.put(newInst.getResult(), paramAddr.storeReg);
                }
            } else if (newInst instanceof StoreInst storeInst) {
                if (storeInst.addr().charAt(0) == '@') {
                    newInsts.add(newInst);
                    continue;
                }
                newInsts.add(newInst);
                if (storeInst.isArray()) {
                    addrRecords.removeIf(e -> e.isArray);
                } else {
                    addrRecords.removeIf(e -> e.addr().equals(storeInst.addr()));
                }
                addrRecords.add(new ParamAddr(storeInst.addr(), storeInst.offset(), storeInst.val(), storeInst.isArray()));
            } else {
                newInsts.add(newInst);
                regMap.put(newInst.getResult(), newInst.getResult());
                regMap.remove(null);
            }
        }
        this.insts = newInsts;
    }

    /**
     * 尝试重新生成入口有用变量。返回是否有所改变
     */
    public boolean generateInUsefulVar() {
        HashSet<String> nowUsefulVar = new HashSet<>(outUsefulVar);
        for (int i = insts.size() - 1; i >= 0; i--) {
            Inst inst = insts.get(i);

            if (inst instanceof Computable) {
                if (nowUsefulVar.contains(inst.getResult())) {
                    nowUsefulVar.addAll(inst.usedReg());
                    nowUsefulVar.remove(inst.getResult());
                }
            } else if (inst instanceof BrInst
                    || inst instanceof CallInst
                    || inst instanceof PutIntInst
                    || inst instanceof RetInst) {
                nowUsefulVar.addAll(inst.usedReg());
                if (inst.getResult() != null && Inst.isGlobalParam(inst.getResult())) {
                    nowUsefulVar.remove(inst.getResult());
                }
            } else if (inst instanceof LoadInst loadInst) {
                if (nowUsefulVar.contains(loadInst.result())) {
                    nowUsefulVar.add(loadInst.addr());
                    if (loadInst.isArray()) {
                        nowUsefulVar.add(loadInst.arrName());
                    }
                    nowUsefulVar.remove(inst.getResult());
                }
            } else if (inst instanceof StoreInst storeInst) {
                if (nowUsefulVar.contains(storeInst.addr()) || storeInst.isGlobalArea()) {
                    //如果地址是有用的，那么赋值必定是有用的
                    nowUsefulVar.add(storeInst.val());
                    if (!storeInst.isArray()) {//如果地址不是数组，之前地址上的数据就没用了
                        nowUsefulVar.remove(storeInst.addr());
                    } else {
                        nowUsefulVar.add(storeInst.addr());
                        nowUsefulVar.add(storeInst.arrName());
                    }
                } else if (storeInst.isArray() && nowUsefulVar.contains(storeInst.arrName())) {
                    //如果存储是数组，且数组是有用的
                    nowUsefulVar.add(storeInst.val());
                    nowUsefulVar.add(storeInst.addr());
                }
            }
        }
        nowUsefulVar.removeIf(var -> var == null || !Inst.isStackParam(var));
        if (nowUsefulVar.equals(inUsefulVar)) {
            return false;
        } else {
            this.inUsefulVar = nowUsefulVar;
            return true;
        }
    }

    public void removeUnusedVar() {
        HashSet<String> nowUsefulVar = new HashSet<>(outUsefulVar);
        List<Inst> newInsts = new ArrayList<>();
        for (int i = insts.size() - 1; i >= 0; i--) {
            Inst inst = insts.get(i);
            if (inst instanceof Computable) {
                if (nowUsefulVar.contains(inst.getResult())) {
                    nowUsefulVar.addAll(inst.usedReg());
                    nowUsefulVar.remove(inst.getResult());
                    newInsts.add(inst);
                }
            } else if (inst instanceof BrInst
                    || inst instanceof CallInst
                    || inst instanceof PutIntInst
                    || inst instanceof RetInst) {

                nowUsefulVar.addAll(inst.usedReg());
                nowUsefulVar.remove(inst.getResult());
                newInsts.add(inst);
            } else if (inst instanceof LoadInst loadInst) {
                if (nowUsefulVar.contains(loadInst.result())) {
                    nowUsefulVar.add(loadInst.addr());
                    if (loadInst.isArray()) {
                        nowUsefulVar.add(loadInst.arrName());
                    }
                    nowUsefulVar.remove(inst.getResult());
                    newInsts.add(inst);
                }
            } else if (inst instanceof StoreInst storeInst) {
                if (nowUsefulVar.contains(storeInst.addr()) || storeInst.isGlobalArea()) {
                    //如果地址是有用的，那么赋值必定是有用的
                    nowUsefulVar.add(storeInst.val());
                    newInsts.add(storeInst);
                    if (!storeInst.isArray()) {//如果地址不是数组，之前地址上的数据就没用了
                        nowUsefulVar.remove(storeInst.addr());
                    } else {
                        nowUsefulVar.add(storeInst.addr());
                        nowUsefulVar.add(storeInst.arrName());
                    }
                } else if (storeInst.isArray() && nowUsefulVar.contains(storeInst.arrName())) {
                    //如果存储是数组，且数组是有用的
                    nowUsefulVar.add(storeInst.val());
                    nowUsefulVar.add(storeInst.addr());
                    newInsts.add(storeInst);
                }
            } else {
                newInsts.add(inst);
            }
        }
        Collections.reverse(newInsts);
        this.insts = newInsts;
    }

    /**
     * 记录变量的使用指令
     * 记录栈变量的引用次数以及冲突关系
     */
    public void allocaReg() {
        HashSet<String> nowUsefulVar = new HashSet<>(outUsefulVar);
        for (int i = insts.size() - 1; i >= 0; i--) {
            Inst inst = insts.get(i);
            RegAllocator.recordParamUsage(inst);
            if (inst instanceof LoadInst loadInst && !loadInst.isArray() && Inst.isStackParam(loadInst.addr())) {
                RegAllocator.addParamRef(loadInst.addr());
                nowUsefulVar.add(loadInst.addr());
            } else if (inst instanceof StoreInst storeInst && !storeInst.isArray() && Inst.isStackParam(storeInst.addr())) {
                RegAllocator.addParamRef(storeInst.addr());
                nowUsefulVar.remove(storeInst.addr());
                RegAllocator.recordConflicts(nowUsefulVar, storeInst.addr());
            }
        }
    }

    public boolean addUsefulVar(HashSet<String> usefulVar) {
        return this.outUsefulVar.addAll(usefulVar);
    }

    public HashSet<String> getInUsefulVar() {
        return inUsefulVar;
    }

    public List<String> getNextBlock() {
        return nextBlock;
    }
}
