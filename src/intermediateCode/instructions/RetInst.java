package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record RetInst(String ret) implements Inst {
    @Override
    public String toString() {
        return String.format("ret %s", ret == null ? "" : ret);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        if (ret != null) {
            FrameMonitor.getParamVal(ret, "$v0");
        }
        Output.output("\tmove $sp, $fp");
        Output.output("\tjr $ra");
    }
}

