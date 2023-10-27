package intermediateCode.instructions;

import intermediateCode.Inst;

public record PutIntInst(String n) implements Inst {
    @Override
    public String toString() {
        return String.format("putint %s", n);
    }
}
