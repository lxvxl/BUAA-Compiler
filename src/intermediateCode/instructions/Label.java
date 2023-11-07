package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.Inst;

public record Label(String label) implements Inst {
    @Override
    public String toString() {
        return label + ':';
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        MipsGenerator.addLabel(label + ':');
    }
}
