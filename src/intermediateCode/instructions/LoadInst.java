package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record LoadInst(String result, String addr, int offset) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = load %s %s", result, addr, offset);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        FrameMonitor.getParamVal(addr, "$t0");
        Output.output(String.format("\tlw $t2, %d($t0)", offset));
        FrameMonitor.initParam(result, "$t2");
    }
}

