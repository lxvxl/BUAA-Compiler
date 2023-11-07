package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record AllocaInst(String result, int size) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = alloca %d", result, size);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        FrameMonitor.allocaParam(size, result);
    }
}
