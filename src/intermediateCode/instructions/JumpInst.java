package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record JumpInst(String label) implements Inst {
    @Override
    public String toString() {
        return "jump " + label;
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        MipsGenerator.addInst("\tj " + label);
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
