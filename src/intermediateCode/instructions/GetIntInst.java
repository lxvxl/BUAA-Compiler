package intermediateCode.instructions;

import intermediateCode.Inst;

public record GetIntInst(String result) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = getint()", result);
    }
}
