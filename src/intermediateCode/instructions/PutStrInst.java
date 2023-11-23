package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record PutStrInst(int num, String label) implements Inst {

    public PutStrInst(String label) {
        this(CodeGenerator.getInstNum(), label);
    }

    @Override
    public String toString() {
        return String.format("putstr %s", label);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        MipsGenerator.addInst(String.format("\tla $a0, %s", label));
        MipsGenerator.addInst("\tli $v0, 4");
        MipsGenerator.addInst("\tsyscall");
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
