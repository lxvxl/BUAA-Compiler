package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.Inst;

public record Label(String label) implements Inst {
    @Override
    public String toString() {
        return label + ':';
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        Output.output(label + ':');
    }
}
