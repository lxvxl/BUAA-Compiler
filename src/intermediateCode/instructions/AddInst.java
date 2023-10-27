package intermediateCode.instructions;

import intermediateCode.Inst;

public record AddInst(String result, String para1, String para2) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = add %s %s", result, para1, para2);
    }
}
