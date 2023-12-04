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
        if (CodeGenerator.OPTIMIZE) {
            int num = num();
            MipsGenerator.addInst(String.format("\tmove %s, $v0", RegAllocator.getFreeReg(num, result)));
            return;
        }
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

    @Override
    public int num() {
        return CodeGenerator.getInstNum(this);
    }

    @Override
    public Inst replace(int n, String funcName) {
        return new GetIntInst(Inst.transformParam(result, n, funcName));
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
