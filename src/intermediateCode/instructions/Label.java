package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record Label(int num, String label) implements Inst {

    public Label(String label) {
        this(CodeGenerator.getInstNum(), label);
    }

    @Override
    public String toString() {
        return label + ':';
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        MipsGenerator.addLabel(label + ':');
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
