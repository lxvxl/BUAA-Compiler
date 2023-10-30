package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record OrInst(String result, String para1, String para2) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = or %s %s", result, para1, para2);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        try {
            int a = Integer.parseInt(para1);
            FrameMonitor.getParamVal(para2, "$t0");
            Output.output(String.format("\tori $t2, $t0, %d", a));
            FrameMonitor.initParam(result, "$t2");
            return;
        } catch (Exception ignored) {}
        try {
            int a = Integer.parseInt(para2);
            FrameMonitor.getParamVal(para1, "$t0");
            Output.output(String.format("\tori $t2, $t0, %d", a));
            FrameMonitor.initParam(result, "$t2");
            return;
        } catch (Exception ignored) {}
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        Output.output("\tori $t2, $t0, $t1");
        FrameMonitor.initParam(result, "$t2");
    }
}

