package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

public record CmpInst(String op, String result, String para1, String para2) implements Inst {
    @Override
    public String toString() {
        return String.format("%s = cmp '%s' %s %s", result, op, para1, para2);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        MipsGenerator.addInst("\tslt $t3, $t0, $t1");//t3 = t0 < t1
        MipsGenerator.addInst("\tslt $t4, $t1, $t0");//t4 = t1 < t0
        switch (op) {
            case "==" -> {
                MipsGenerator.addInst("\tnor $t2, $t3, $t4");
                MipsGenerator.addInst("\tandi $t2, $t2, 1");
            }
            case "!=" -> MipsGenerator.addInst("\tor $t2, $t3, $t4");
            case ">" -> MipsGenerator.addInst("\tmove $t2, $t4");
            case "<" -> MipsGenerator.addInst("\tmove $t2, $t3");
            case ">=" -> MipsGenerator.addInst("\txori $t2, $t3, 1");
            case "<=" -> MipsGenerator.addInst("\txori $t2, $t4, 1");
        }
        FrameMonitor.initParam(result, "$t2");
    }

    public void toMipsWithBr(BrInst br) {
        MipsGenerator.addInst('#' + toString());
        MipsGenerator.addInst('#' + br.toString());
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        switch (op) {
            case "==" -> {
                MipsGenerator.addInst("\tbeq $t0, $t1, " + br.trueLabel());
                MipsGenerator.addInst("\tj  " + br.falseLabel());
            }
            case "!=" -> {
                MipsGenerator.addInst("\tbne $t0, $t1, " + br.trueLabel());
                MipsGenerator.addInst("\tj  " + br.falseLabel());
            }
            case ">" -> {
                MipsGenerator.addInst("\tslt $at, $t1, $t0");
                MipsGenerator.addInst("\tbne $at, $zero, " + br.trueLabel());
                MipsGenerator.addInst("\tj  " + br.falseLabel());
            }
            case "<"  -> {
                MipsGenerator.addInst("\tslt $at, $t0, $t1");
                MipsGenerator.addInst("\tbne $at, $zero, " + br.trueLabel());
                MipsGenerator.addInst("\tj  " + br.falseLabel());
            }
            case ">=" -> {
                MipsGenerator.addInst("\tslt $at, $t0, $t1");
                MipsGenerator.addInst("\tbeq $at, $zero, " + br.trueLabel());
                MipsGenerator.addInst("\tj  " + br.falseLabel());
            }
            case "<=" -> {
                MipsGenerator.addInst("\tslt $at, $t1, $t0");
                MipsGenerator.addInst("\tbeq $at, $zero, " + br.trueLabel());
                MipsGenerator.addInst("\tj  " + br.falseLabel());
            }
        }
    }
}