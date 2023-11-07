package intermediateCode.instructions;

import java.util.ArrayList;
import java.util.List;

import Writer.MipsGenerator;
import intermediateCode.Inst;

public record WordInst(String name, int size, List<String> initVals) implements Inst {
    @Override
    public String toString() {
        return String.format("@%s = word %s %s", name, size,
                initVals == null ? "default" : String.join(",", initVals));
    }

    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (initVals == null) {
            //MipsGenerator.addInst(".align 2");
            MipsGenerator.addInst(String.format("%s: .word 0:%d", "g_" + name, size / 4));
        } else {
            List<String> reversedVals = new ArrayList<>(initVals);
            //Collections.reverse(reversedVals);
            //MipsGenerator.addInst(".align 2");
            MipsGenerator.addInst(String.format("%s: .word %s", "g_" + name, String.join(",", reversedVals)));
        }
    }
}

