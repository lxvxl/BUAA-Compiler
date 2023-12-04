package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;
import intermediateCode.optimize.StackAlloactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record AllocaInst(String result, int size, boolean isArray) implements Inst {

    @Override
    public String toString() {
        return String.format("%s = alloca %d", result, size);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (CodeGenerator.OPTIMIZE) {
            StackAlloactor.alloca(result, size);
            return;
        }
        FrameMonitor.allocaParam(size, result);
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
        return new AllocaInst(Inst.transformParam(result, n, funcName), size, isArray);
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
