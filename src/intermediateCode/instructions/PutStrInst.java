package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.Inst;

public record PutStrInst(String label) implements Inst {
    @Override
    public String toString() {
        return String.format("putstr %s", label);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        Output.output(String.format("\tla $a0, %s", label));
        Output.output("\tli $v0, 4");
        Output.output("\tsyscall");
    }
}
