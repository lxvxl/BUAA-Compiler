package intermediateCode;

import ident.idents.Var;
import intermediateCode.instructions.*;
import jdk.jfr.Frequency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualMachine {
    private final List<Inst> insts;
    private final ArrayList<Integer> stack;
    private final Map<String, Integer> stackIndex;
    private final Map<String, Integer> valueMap;
    private final Map<String, Integer> labelMap;
    private int stackTop;
    private int pc;

    public static int runFunc(String funcName, List<Integer> params) {
        FuncCode funcCode = CodeGenerator.getFuncCode(funcName);
        return new VirtualMachine(funcCode.insts(), funcCode.getFunc().getParams(), params).run();
    }

    private VirtualMachine(List<Inst> insts, List<Var> vars, List<Integer> params) {
        this.insts = insts;
        this.stack = new ArrayList<>();
        this.valueMap = new HashMap<>();
        this.stackIndex = new HashMap<>();
        this.labelMap = new HashMap<>();
        stackTop = 0;
        pc = 0;
        for (int i = 0; i < vars.size(); i++) {
            Var var = vars.get(i);
            int param = params.get(i);
            alloca(var.getAddrReg(), 4);
            storeInStack(var.getAddrReg(), 0, Integer.toString(param));
        }
        for (int i = 0; i < insts.size(); i++) {
            if (insts.get(i) instanceof Label labelInst) {
                labelMap.put(labelInst.label(), i);
            }
        }
    }

    private int run() {
        while (true) {
            Inst inst = insts.get(pc);
            if (inst instanceof AddInst addInst) {
                setValue(getValue(addInst.para1()) + getValue((addInst.para2())), addInst.result());
            } else if (inst instanceof AllocaInst allocaInst) {
                alloca(allocaInst.result(), allocaInst.size());
            } else if (inst instanceof BrInst brInst) {
                if (getValue(brInst.reg()) == 0) {
                    pc = labelMap.get(brInst.falseLabel());
                } else {
                    pc = labelMap.get(brInst.trueLabel());
                }
            } else if (inst instanceof CallInst callInst) {
                setValue(runFunc(callInst.funcName(), callInst.params().stream().map(this::getValue).toList()),
                        callInst.result());
            } else if (inst instanceof CmpInst cmpInst) {
                int para1 = getValue(cmpInst.para1());
                int para2 = getValue(cmpInst.para2());
                String result = cmpInst.result();
                switch (cmpInst.op()) {
                    case "==" -> setValue(para1 == para2 ? 1 : 0, result);
                    case ">=" -> setValue(para1 >= para2 ? 1 : 0, result);
                    case "<=" -> setValue(para1 <= para2 ? 1 : 0, result);
                    case "!=" -> setValue(para1 != para2 ? 1 : 0, result);
                    case ">" -> setValue(para1 > para2 ? 1 : 0, result);
                    case "<" -> setValue(para1 < para2 ? 1 : 0, result);
                }
            } else if (inst instanceof DivInst divInst) {
                setValue(getValue(divInst.para1()) / getValue((divInst.para2())), divInst.result());
            } else if (inst instanceof JumpInst jumpInst) {
                pc = labelMap.get(jumpInst.label());
            } else if (inst instanceof LoadInst loadInst) {
                setValue(loadFromStack(loadInst.addr(), loadInst.offset()), loadInst.result());
            } else if (inst instanceof ModInst modInst) {
                setValue(getValue(modInst.para1()) % getValue((modInst.para2())), modInst.result());
            } else if (inst instanceof MulInst mulInst) {
                setValue(getValue(mulInst.para1()) * getValue((mulInst.para2())), mulInst.result());
            } else if (inst instanceof RetInst retInst) {
                return getValue(retInst.ret());
            } else if (inst instanceof StoreInst storeInst) {
                storeInStack(storeInst.addr(), storeInst.offset(), storeInst.val());
            } else if (inst instanceof SubInst subInst) {
                setValue(getValue(subInst.para1()) - getValue((subInst.para2())), subInst.result());
            }
            pc++;
        }
    }

    private void alloca(String addr, int size) {
        stackIndex.put(addr, stackTop);
        stackTop += size / 4;
        for (int i = 0; i < size / 4; i++) {
            stack.add(0);
        }
    }

    private void storeInStack(String addr, int offset, String val) {
        stack.set((getValue(addr) + offset) / 4, getValue(val));
    }

    private int loadFromStack(String addr, int offset) {
        return stack.get((getValue(addr) + offset) / 4);
    }

    private int getValue(String val) {
        if (Inst.isInt(val)) {
            return Integer.parseInt(val);
        } else if (Inst.isTempParam(val)) {
            return valueMap.get(val);
        } else if (Inst.isStackParam(val)) {
            return stackIndex.get(val) * 4;
        }
        throw new RuntimeException();
    }

    private void setValue(int val, String param) {
        valueMap.put(param, val);
    }
}
