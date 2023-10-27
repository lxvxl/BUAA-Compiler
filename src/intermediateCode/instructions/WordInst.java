package intermediateCode.instructions;

import java.util.List;

import intermediateCode.Inst;

public record WordInst(String name, int size, List<String> initVals) implements Inst {
    @Override
    public String toString() {
        return String.format("@%s = word %s %s", name, size,
                initVals == null ? "default" : String.join(",", initVals));
    }
}

