package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record BrInst(String reg, String trueLabel, String falseLabel) implements Inst {
    @Override
    public String toString() {
        return String.format("br %s %s %s", reg, trueLabel, falseLabel);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        FrameMonitor.getParamVal(reg, "$t0");
        Output.output("\tbne $t0, $0, " + trueLabel);
        Output.output("\tj " + falseLabel);
    }
}
