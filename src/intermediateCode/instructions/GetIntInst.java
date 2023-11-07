package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record GetIntInst(String result) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = getint()", result);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        MipsGenerator.addInst("\tli $v0, 5");
        MipsGenerator.addInst("\tsyscall");
        FrameMonitor.initParam(result, "$v0");
    }
}
