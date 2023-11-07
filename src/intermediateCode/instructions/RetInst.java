package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record RetInst(String ret) implements Inst {
    @Override
    public String toString() {
        return String.format("ret %s", ret == null ? "" : ret);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (ret != null) {
            FrameMonitor.getParamVal(ret, "$v0");
        }
        MipsGenerator.addInst("\tmove $sp, $fp");
        MipsGenerator.addInst("\tjr $ra");
    }
}

