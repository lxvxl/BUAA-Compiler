package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;

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
        if (CodeGenerator.OPTIMIZE) {
            String resultReg = RegAllocator.getParamVal(n, num());
            MipsGenerator.addInst("\tmove $a0, " + resultReg);
            MipsGenerator.addInst("\tli $v0, 1");
            MipsGenerator.addInst("\tsyscall");
            return;
        }
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

    @Override
    public int num() {
        return CodeGenerator.getInstNum(this);
    }

    @Override
    public Inst replace(int n, String funcName) {
        return new PutIntInst(Inst.transformParam(this.n, n, funcName));
    }

    @Override
    public Inst replaceFor(int n) {
        return new PutIntInst(Inst.transformFor(this.n, n));
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
