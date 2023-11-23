package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record RetInst(int num, String ret) implements Inst {

    public RetInst(String ret) {
        this(CodeGenerator.getInstNum(), ret);
    }

    @Override
    public String toString() {
        return String.format("ret %s", ret == null ? "" : ret);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (CodeGenerator.OPTIMIZE) {
            if (ret != null) {
                String resultReg = RegAllocator.getParamVal(ret, num);
                MipsGenerator.addInst("\tmove $v0, " + resultReg);
            }
            MipsGenerator.addInst("\tmove $sp, $fp");
            MipsGenerator.addInst("\tjr $ra");
            return;
        }
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

