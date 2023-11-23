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

public record GetIntInst(int num, String result) implements Inst {

    public GetIntInst(String result) {
        this(CodeGenerator.getInstNum(), result);
    }

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
}
