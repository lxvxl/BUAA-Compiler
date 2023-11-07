package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record LoadInst(String result, String addr, int offset) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = load %s %s", result, addr, offset);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        FrameMonitor.getParamVal(addr, "$t0");
        MipsGenerator.addInst(String.format("\tlw $t2, %d($t0)", offset));
        FrameMonitor.initParam(result, "$t2");
    }
}

