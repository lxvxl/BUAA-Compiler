package intermediateCode.instructions;

import intermediateCode.Inst;

public record AllocaInst(String result, int size) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = alloca %d", result, size);
    }
}
