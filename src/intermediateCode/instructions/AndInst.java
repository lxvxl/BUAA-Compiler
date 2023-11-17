package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.Computable;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record AndInst(String result, String para1, String para2) implements Inst, Computable {
    @Override
    public String toString() {
        return String.format("%s = and %s %s", result, para1, para2);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        try {
            int a = Integer.parseInt(para1);
            FrameMonitor.getParamVal(para2, "$t0");
            MipsGenerator.addInst(String.format("\tandi $t2, $t0, %d", a));
            FrameMonitor.initParam(result, "$t2");
            return;
        } catch (Exception ignored) {}
        try {
            int a = Integer.parseInt(para2);
            FrameMonitor.getParamVal(para1, "$t0");
            MipsGenerator.addInst(String.format("\tandi $t2, $t0, %d", a));
            FrameMonitor.initParam(result, "$t2");
            return;
        } catch (Exception ignored) {}
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        MipsGenerator.addInst("\tand $t2, $t0, $t1");
        FrameMonitor.initParam(result, "$t2");
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
        return new AndInst(result, Inst.getEquivalentReg(regMap, para1), Inst.getEquivalentReg(regMap, para2));
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public String getSpecificResult() {
        if (Inst.isInt(para1) && Inst.isInt(para2)) {
            return Integer.parseInt(para1) != 0 && Integer.parseInt(para2) != 0 ? "1" : "0";
        } else {
            return null;
        }
    }
}
