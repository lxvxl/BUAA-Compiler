package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record GetIntInst(String result) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = getint()", result);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        Output.output("\tli $v0, 5");
        Output.output("\tsyscall");
        FrameMonitor.initParam(result, "$v0");
    }
}
