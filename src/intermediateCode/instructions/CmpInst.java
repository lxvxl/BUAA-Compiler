package intermediateCode.instructions;

import intermediateCode.Inst;

public record CmpInst(String op, String result, String para1, String para2) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = cmp %s %s %s", result, op, para1, para2);
    }
}