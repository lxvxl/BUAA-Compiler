package intermediateCode.instructions;

import intermediateCode.Inst;

public record ModInst(String result, String para1, String para2) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = mod %s %s", result, para1, para2);
    }
}

