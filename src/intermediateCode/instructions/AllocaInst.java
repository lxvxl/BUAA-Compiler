package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record AllocaInst(String result, int size) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = alloca %d", result, size);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
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
}
