package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record StoreInst(String val, String addr, int offset) implements Inst {
    @Override
    public String toString() {
        return String.format("store %s, %s, %s", val, addr, offset);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        FrameMonitor.getParamVal(addr, "$t0");
        FrameMonitor.getParamVal(val, "$t2");
        Output.output(String.format("\tsw $t2, %d($t0)", offset));
    }
}

