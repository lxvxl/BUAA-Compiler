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

public record ModInst(String result, String para1, String para2) implements Inst, Computable {
    @Override
    public String toString() {
        return String.format("%s = mod %s %s", result, para1, para2);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (CodeGenerator.OPTIMIZE) {
            int num = num();
            String resultReg = RegAllocator.getFreeReg(num, result);
            String para1Reg = RegAllocator.getParamVal(para1, num);
            String para2Reg = RegAllocator.getParamVal(para2, num);
            MipsGenerator.addInst(String.format("\tdiv %s, %s", para1Reg, para2Reg));
            MipsGenerator.addInst("\tmfhi " + resultReg);
            return;
        }
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        MipsGenerator.addInst("\tdiv $t0, $t1");
        MipsGenerator.addInst("\tmfhi $t2");
        FrameMonitor.initParam(result, "$t2");
    }

    @Override
    public List<String> usedReg() {
        return Stream.of(para1, para2).filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return Stream.of(para1, para2).toList();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new ModInst(result, Inst.getEquivalentReg(regMap, para1), Inst.getEquivalentReg(regMap, para2));
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public String getSpecificResult() {
        if (Inst.isInt(para1) && Inst.isInt(para2)) {
            return Integer.toString(Integer.parseInt(para1) % Integer.parseInt(para2));
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
        return new ModInst(Inst.transformParam(result, n, funcName),
                Inst.transformParam(para1, n, funcName),
                Inst.transformParam(para2, n, funcName));
    }

    @Override
    public Inst replaceFor(int n) {
        return new ModInst(Inst.transformFor(result, n),
                Inst.transformFor(para1, n),
                Inst.transformFor(para2, n));
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}

