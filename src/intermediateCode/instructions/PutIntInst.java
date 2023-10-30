package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record PutIntInst(String n) implements Inst {
    @Override
    public String toString() {
        return String.format("putint %s", n);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        FrameMonitor.getParamVal(n, "$a0");
        Output.output("\tli $v0, 1");
        Output.output("\tsyscall");
    }
}
