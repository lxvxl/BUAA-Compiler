package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.Inst;

public record JumpInst(String label) implements Inst {
    @Override
    public String toString() {
        return "jump " + label;
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        Output.output("\tj " + label);
    }
}
