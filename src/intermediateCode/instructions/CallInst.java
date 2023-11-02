package intermediateCode.instructions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import Writer.Output;
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
        Output.output('#' + toString());
        /*for (int i = 1; i <= 31; i++) {
            Output.output(String.format("\tsw $%d, -%d($sp)", i, i * 4));
        }
        int curBottom = FrameMonitor.getBottom();*/
        List<String> curEnv = Stream.of("$fp", "$ra").toList();
        int curBottom = FrameMonitor.storeEnv(curEnv);
        for (int i = 0; i < params.size(); i++) {
            FrameMonitor.getParamVal(params.get(params.size() - 1 - i), "$t0");
            Output.output(String.format("\tsw $t0, -%d($sp)", (curBottom + i) * 4));
        }
        Output.output("\tmove $fp, $sp");
        Output.output(String.format("\taddi $sp, $sp, %d", -(curBottom - 1 + params.size()) * 4));
        Output.output("\tjal " + funcName);
        FrameMonitor.restoreEnv(curEnv);
        if (result != null) {
            FrameMonitor.initParam(result, "$v0");
        }
        /*for (int i = 1; i <= 31; i++) {
            Output.output(String.format("\tlw $%d, -%d($sp)", i, i * 4));
        }*/
    }
}

