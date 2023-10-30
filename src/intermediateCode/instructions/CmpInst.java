package intermediateCode.instructions;

import Writer.Output;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record CmpInst(String op, String result, String para1, String para2) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = cmp '%s' %s %s", result, op, para1, para2);
    }

    @Override
    public void toMips() {
        Output.output('#' + toString());
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        Output.output("\tslt $t3, $t0, $t1");//t3 = t0 < t1
        Output.output("\tslt, $t4, $t1, $t0");//t4 = t1 < t0
        switch (op) {
            case "==" -> {
                Output.output("\tnor, $t2, $t3, $t4");
                Output.output("\tandi, $t2, $t2, 1");
            }
            case "!=" -> Output.output("\tor $t2, $t3, $t4");
            case ">" -> Output.output("\tmove $t2, $t4");
            case "<" -> Output.output("\tmove $t2, $t3");
            case ">=" -> Output.output("\txori $t2, $t3, 1");
            case "<=" -> Output.output("\txori $t2, $t4, 1");
        }
        FrameMonitor.initParam(result, "$t2");
    }
}