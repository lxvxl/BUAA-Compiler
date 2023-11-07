package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record PutIntInst(String n) implements Inst {
    @Override
    public String toString() {
        return String.format("putint %s", n);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        FrameMonitor.getParamVal(n, "$a0");
        MipsGenerator.addInst("\tli $v0, 1");
        MipsGenerator.addInst("\tsyscall");
    }
}
