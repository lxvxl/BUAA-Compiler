package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record DivInst(String result, String para1, String para2) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = div %s %s", result, para1, para2);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        MipsGenerator.addInst("\tdiv $t0, $t1");
        MipsGenerator.addInst("\tmflo $t2");
        FrameMonitor.initParam(result, "$t2");
    }
}

