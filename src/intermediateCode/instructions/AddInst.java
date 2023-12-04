package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.Computable;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record AddInst(String result, String para1, String para2) implements Inst, Computable {

    @Override
    public String toString() {
        return String.format("%s = add %s %s", result, para1, para2);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (CodeGenerator.OPTIMIZE) {
            toMips2();
            return;
        }
        try {
            int a = Integer.parseInt(para1);
            FrameMonitor.getParamVal(para2, "$t0");
            MipsGenerator.addInst(String.format("\taddi $t2, $t0, %d", a));
            FrameMonitor.initParam(result, "$t2");
            return;
        } catch (Exception ignored) {}
        try {
            int a = Integer.parseInt(para2);
            FrameMonitor.getParamVal(para1, "$t0");
            MipsGenerator.addInst(String.format("\taddi $t2, $t0, %d", a));
            FrameMonitor.initParam(result, "$t2");
            return;
        } catch (Exception ignored) {}
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        MipsGenerator.addInst("\tadd $t2, $t0, $t1");
        FrameMonitor.initParam(result, "$t2");
    }

    private void toMips2() {
        String freeReg = RegAllocator.getFreeReg(num(), result);
        if (Inst.isImmediate(para1)) {
            short a = Short.parseShort(para1);
            String para2Reg = RegAllocator.getParamVal(para2, num());
            MipsGenerator.addInst(String.format("\taddiu %s, %s, %d", freeReg, para2Reg, a));
        } else if (Inst.isImmediate(para2)) {
            short a = Short.parseShort(para2);
            String para1Reg = RegAllocator.getParamVal(para1, num());
            MipsGenerator.addInst(String.format("\taddiu %s, %s, %d", freeReg, para1Reg, a));
        } else {
            String para1Reg = RegAllocator.getParamVal(para1, num());
            String para2Reg = RegAllocator.getParamVal(para2, num());
            MipsGenerator.addInst(String.format("\taddu %s, %s ,%s", freeReg, para2Reg, para1Reg));
        }
    }

    @Override
    public List<String> usedReg() {
        return Stream.of(para1, para2).filter(p -> !Inst.isInt(p)).sorted().toList();
    }

    @Override
    public List<String> getParams() {
        return Stream.of(para1, para2).sorted().toList();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new AddInst(result, Inst.getEquivalentReg(regMap, para1), Inst.getEquivalentReg(regMap, para2));
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public String getSpecificResult() {
        if (Inst.isInt(para1) && Inst.isInt(para2)) {
            return Integer.toString(Integer.parseInt(para1) + Integer.parseInt(para2));
        } else if (Inst.isInt(para1) && Integer.parseInt(para1) == 0) {
            return para2;
        } else if (Inst.isInt(para2) && Integer.parseInt(para2) == 0) {
            return para1;
        } else {
            return null;
        }
    }

    @Override
    public int num() {
        return CodeGenerator.getInstNum(this);
    }

    @Override
    public Inst replace(int n, String funcName) {
        return new AddInst(Inst.transformParam(result, n, funcName),
                Inst.transformParam(para1, n, funcName),
                Inst.transformParam(para2, n, funcName));
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
