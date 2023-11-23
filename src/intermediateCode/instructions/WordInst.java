package intermediateCode.instructions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.Inst;

public record WordInst(int num, String name, int size, List<String> initVals) implements Inst {

    public WordInst(String name, int size, List<String> initVals) {
        this(CodeGenerator.getInstNum(), name, size, initVals);
    }

    @Override
    public String toString() {
        return String.format("@%s = word %s %s", name, size,
                initVals == null ? "default" : String.join(",", initVals));
    }

    @Override
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

    @Override
    public List<String> usedReg() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getParams() {
        return new ArrayList<>();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return this;
    }

    @Override
    public String getResult() {
        return null;
    }
}

