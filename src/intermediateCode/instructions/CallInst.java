package intermediateCode.instructions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Writer.MipsGenerator;
import intermediateCode.*;
import intermediateCode.optimize.StackAlloactor;

public record CallInst(String result, String funcName, List<String> params) implements Inst {
    private static int count = 0;
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
        MipsGenerator.addInst("#" + toString());
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
        return params;
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

    @Override
    public int num() {
        return CodeGenerator.getInstNum(this);
    }

    @Override
    public Inst replace(int n, String funcName) {
        return new CallInst(Inst.transformParam(result, n, funcName),
                this.funcName,
                params.stream().map(p -> Inst.transformParam(p, n, funcName)).toList());
    }

    @Override
    public Inst replaceFor(int n) {
        return new CallInst(Inst.transformFor(result, n),
                this.funcName,
                params.stream().map(p -> Inst.transformFor(p, n)).toList());
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    public String getSpecificResult() {
        FuncCode funcCode = CodeGenerator.getFuncCode(funcName);
        if (!funcCode.isInferable()) {
            return null;
        }
        if (params.stream().allMatch(Inst::isInt)) {
            System.out.println("开始推断函数的值");
            count++;
            return Integer.toString(VirtualMachine.runFunc(funcName, params.stream().map(Integer::parseInt).toList()));
        } else {
            return null;
        }
    }
}

