package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record DivInst(String result, String para1, String para2) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = div %s %s", result, para1, para2);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        Output.output("\tdiv $t0, $t1");
        Output.output("\tmflo $t2");
        FrameMonitor.initParam(result, "$t2");
    }
}

