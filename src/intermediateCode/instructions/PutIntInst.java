package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

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

    @Override
    public List<String> usedReg() {
        return Stream.of(n).filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return new ArrayList<>();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new PutIntInst(Inst.getEquivalentReg(regMap, n));
    }

    @Override
    public String getResult() {
        return null;
    }
}
