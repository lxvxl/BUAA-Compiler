package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;

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
        if (CodeGenerator.OPTIMIZE) {
            int num = num();
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

    @Override
    public int num() {
        return CodeGenerator.getInstNum(this);
    }

    @Override
    public Inst replace(int n, String funcName) {
        return new RetInst(Inst.transformParam(ret, n, funcName));
    }

    @Override
    public Inst replaceFor(int n) {
        return new RetInst(Inst.transformFor(ret, n));
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}

