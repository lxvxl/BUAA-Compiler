package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record AllocaInst(String result, int size) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = alloca %d", result, size);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        FrameMonitor.allocaParam(size, result);
    }
}
