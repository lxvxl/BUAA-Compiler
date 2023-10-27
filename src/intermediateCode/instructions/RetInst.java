package intermediateCode.instructions;

import intermediateCode.Inst;

public record RetInst(String ret) implements Inst {
    @Override
    public String toString() {
        return String.format("ret %s", ret);
    }
}

