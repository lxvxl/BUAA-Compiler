package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.Inst;

public record JumpInst(String label) implements Inst {
    @Override
    public String toString() {
        return "jump " + label;
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        MipsGenerator.addInst("\tj " + label);
    }
}
