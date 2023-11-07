package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.Inst;

public record PutCharInst(char c) implements Inst {
    @Override
    public String toString() {
        return String.format("putchar '%s'", c == '\n' ? "\\n" : Character.toString(c));
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        MipsGenerator.addInst(String.format("\tli $a0, %d", (int)c));
        MipsGenerator.addInst("\tli $v0, 11");
        MipsGenerator.addInst("\tsyscall");
    }
}
