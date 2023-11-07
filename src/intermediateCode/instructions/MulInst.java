package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record MulInst(String result, String para1, String para2) implements Inst {
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
}

