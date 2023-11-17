package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

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
        return result;
    }
}
