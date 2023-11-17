package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record BrInst(String reg, String trueLabel, String falseLabel) implements Inst {
    @Override
    public String toString() {
        return String.format("br %s %s %s", reg, trueLabel, falseLabel);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        FrameMonitor.getParamVal(reg, "$t0");
        MipsGenerator.addInst("\tbne $t0, $0, " + trueLabel);
        MipsGenerator.addInst("\tj " + falseLabel);
    }

    @Override
    public List<String> usedReg() {
        return Stream.of(reg).filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return Stream.of(reg).toList();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new BrInst(Inst.getEquivalentReg(regMap, reg), trueLabel, falseLabel);
    }

    @Override
    public String getResult() {
        return null;
    }
}
