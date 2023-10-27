package intermediateCode.instructions;

import intermediateCode.Inst;

public record PutCharInst(char c) implements Inst {
    @Override
    public String toString() {
        return String.format("putchar '%s'", c == '\n' ? "\\n" : Character.toString(c));
    }
}
