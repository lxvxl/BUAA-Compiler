package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.Computable;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record MulInst(String result, String para1, String para2) implements Inst, Computable {
    @Override
    public String toString() {
        return String.format("%s = mul %s %s", result, para1, para2);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        try{
            int n = Integer.parseInt(para1);
            if ((n > 0) && ((n & (n - 1)) == 0)) {
                FrameMonitor.getParamVal(para2, "$t0");
                MipsGenerator.addInst(String.format("\tsll $t2, $t0, %d", (int)(Math.log(n) / Math.log(2))));
                FrameMonitor.initParam(result, "$t2");
                return;
            }
        } catch (Exception ignored) {}
        try{
            int n = Integer.parseInt(para2);
            if ((n > 0) && ((n & (n - 1)) == 0)) {
                FrameMonitor.getParamVal(para1, "$t0");
                MipsGenerator.addInst(String.format("\tsll $t2, $t0, %d", (int)(Math.log(n) / Math.log(2))));
                FrameMonitor.initParam(result, "$t2");
                return;
            }
        } catch (Exception ignored) {}
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        MipsGenerator.addInst("\tmult $t0, $t1");
        MipsGenerator.addInst("\tmflo $t2");
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
        return new MulInst(result, Inst.getEquivalentReg(regMap, para1), Inst.getEquivalentReg(regMap, para2));
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public String getSpecificResult() {
        if (Inst.isInt(para1) && Inst.isInt(para2)) {
            return Integer.toString(Integer.parseInt(para1) * Integer.parseInt(para2));
        } else {
            return null;
        }
    }
}

