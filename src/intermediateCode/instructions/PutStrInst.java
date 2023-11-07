package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.Inst;

public record PutStrInst(String label) implements Inst {
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
}
