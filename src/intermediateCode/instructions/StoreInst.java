package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record StoreInst(String val, String addr, int offset, String arrName, boolean isGlobalArea) implements Inst {
    @Override
    public String toString() {
        return String.format("store %s, %s, %s", val, addr, offset);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        FrameMonitor.getParamVal(addr, "$t0");
        FrameMonitor.getParamVal(val, "$t2");
        MipsGenerator.addInst(String.format("\tsw $t2, %d($t0)", offset));
    }

    @Override
    public List<String> usedReg() {
        return Stream.of(val, addr).filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return new ArrayList<>();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new StoreInst(Inst.getEquivalentReg(regMap, val), Inst.getEquivalentReg(regMap, addr), offset, arrName, isGlobalArea);
    }

    @Override
    public String getResult() {
        return null;
    }

    public boolean isArray() {
        return arrName != null;
    }
}

