package intermediateCode.instructions;

import java.util.List;

import intermediateCode.Inst;

public record CallInst(String result, String funcName, List<String> params) implements Inst {
    @Override
    public String toString() {
        if (result == null) {
            return "call " + funcName + " " + String.join(" ", params);
        } else {
            return result + " = call " + funcName + " " + String.join(" ", params);
        }
    }
}

