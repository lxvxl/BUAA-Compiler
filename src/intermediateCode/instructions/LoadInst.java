package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record LoadInst(String result, String addr, int offset, String arrName) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = load %s %s", result, addr, offset);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        FrameMonitor.getParamVal(addr, "$t0");
        MipsGenerator.addInst(String.format("\tlw $t2, %d($t0)", offset));
        FrameMonitor.initParam(result, "$t2");
    }

    @Override
    public List<String> usedReg() {
        return Stream.of(addr).filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return Stream.of(addr, Integer.toString(offset)).toList();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new LoadInst(result, Inst.getEquivalentReg(regMap, addr), offset, arrName);
    }

    @Override
    public String getResult() {
        return result;
    }


    public boolean isArray() {
        return arrName != null;
    }
}

