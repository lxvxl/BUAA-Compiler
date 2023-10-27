package intermediateCode.instructions;

import intermediateCode.Inst;

public record StoreInst(String val, String addr, int offset) implements Inst {
    @Override
    public String toString() {
        return String.format("store %s, %s, %s", val, addr, offset);
    }
}

