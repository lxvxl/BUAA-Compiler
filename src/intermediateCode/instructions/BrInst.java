package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record BrInst(String reg, String trueLabel, String falseLabel) implements Inst {
    @Override
    public String toString() {
        return String.format("br %s %s %s", reg, trueLabel, falseLabel);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        FrameMonitor.getParamVal(reg, "$t0");
        MipsGenerator.addInst("\tbne $t0, $0, " + trueLabel);
        MipsGenerator.addInst("\tj " + falseLabel);
    }
}
