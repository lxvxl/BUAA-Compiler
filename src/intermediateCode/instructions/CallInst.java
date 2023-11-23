package intermediateCode.instructions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.StackAlloactor;

public record CallInst(int num, String result, String funcName, List<String> params) implements Inst {
    public CallInst(String result, String funcName, List<String> params) {
        this(CodeGenerator.getInstNum(), result, funcName, params);
    }

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
        MipsGenerator.addInst("#" + num + " " + toString());
        if (CodeGenerator.OPTIMIZE) {
            StackAlloactor.dealCallInst(this);
            return;
        }
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

    @Override
    public List<String> usedReg() {
        return params.stream().filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return params.stream().filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new CallInst(result,
                funcName,
                params.stream()
                        .map(e -> Inst.getEquivalentReg(regMap, e))
                        .toList());
    }

    @Override
    public String getResult() {
        return result;
    }
}

