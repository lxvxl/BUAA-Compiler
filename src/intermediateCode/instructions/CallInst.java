package intermediateCode.instructions;

import java.util.List;
import java.util.stream.Stream;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record CallInst(String result, String funcName, List<String> params) implements Inst {
    @Override
    public String toString() {
        if (result == null) {
            return "call " + funcName + " " + String.join(" ", params);
        } else {
            return result + " = call " + funcName + " " + String.join(" ", params);
        }
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        List<String> curEnv = Stream.of("$fp", "$ra").toList();
        int curBottom = FrameMonitor.storeEnv(curEnv);
        for (int i = 0; i < params.size(); i++) {
            FrameMonitor.getParamVal(params.get(params.size() - 1 - i), "$t0");
            MipsGenerator.addInst(String.format("\tsw $t0, -%d($sp)", (curBottom + i) * 4));
        }
        MipsGenerator.addInst("\tmove $fp, $sp");
        MipsGenerator.addInst(String.format("\taddi $sp, $sp, %d", -(curBottom - 1 + params.size()) * 4));
        MipsGenerator.addInst("\tjal " + "func_" + funcName);
        FrameMonitor.restoreEnv(curEnv);
        if (result != null) {
            FrameMonitor.initParam(result, "$v0");
        }
    }
}

