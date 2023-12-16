package intermediateCode.instructions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.Inst;

public record WordInst(String name, int size, List<String> initVals) implements Inst {
    @Override
    public String toString() {
        return String.format("@%s = word %s %s", name, size,
                initVals == null ? "default" : String.join(",", initVals));
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (initVals == null) {
            MipsGenerator.addInst(String.format("%s: .word 0:%d", "g_" + name, size / 4));
        } else {
            List<String> reversedVals = new ArrayList<>(initVals);
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

    @Override
    public int num() {
        return CodeGenerator.getInstNum(this);
    }

    @Override
    public Inst replace(int n, String funcName) {
        return this;
    }

    @Override
    public Inst replaceFor(int n) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}

