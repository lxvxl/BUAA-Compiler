package intermediateCode.instructions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Writer.Output;
import intermediateCode.Inst;

public record WordInst(String name, int size, List<String> initVals) implements Inst {
    @Override
    public String toString() {
        return String.format("@%s = word %s %s", name, size,
                initVals == null ? "default" : String.join(",", initVals));
    }

    public void toMips() {
        Output.output('#' + toString());
        if (initVals == null) {
            Output.output(String.format("%s: .space %d", name, size));
        } else {
            List<String> reversedVals = new ArrayList<>(initVals);
            //Collections.reverse(reversedVals);
            Output.output(String.format("%s: .word %s", name, String.join(",", reversedVals)));
        }
    }
}

