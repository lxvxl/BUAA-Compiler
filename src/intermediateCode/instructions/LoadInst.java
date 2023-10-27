package intermediateCode.instructions;

import intermediateCode.Inst;

public record LoadInst(String result, String addr, int offset) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = load %s %s", result, addr, offset);
    }
}

