package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.Inst;

public record PutCharInst(char c) implements Inst {
    @Override
    public String toString() {
        return String.format("putchar '%s'", c == '\n' ? "\\n" : Character.toString(c));
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        Output.output(String.format("\tli $a0, %d", (int)c));
        Output.output("\tli $v0, 11");
        Output.output("\tsyscall");
    }
}
