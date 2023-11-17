package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

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

    @Override
    public List<String> usedReg() {
        return Stream.of(ret).filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return Stream.of(ret).toList();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new RetInst(Inst.getEquivalentReg(regMap, ret));
    }

    @Override
    public String getResult() {
        return null;
    }
}

