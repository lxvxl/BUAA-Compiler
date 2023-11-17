package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.Computable;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record CmpInst(String op, String result, String para1, String para2) implements Inst, Computable {

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

    @Override
    public List<String> usedReg() {
        switch (op) {
            case "==", "!=" -> {return Stream.of(para1, para2).filter(p -> !Inst.isInt(p)).sorted().toList();}
            default -> {return Stream.of(para1, para2).filter(p -> !Inst.isInt(p)).toList();}
        }
    }

    @Override
    public List<String> getParams() {
        switch (op) {
            case "==", "!=" -> {return Stream.of(para1, para2, op).sorted().toList();}
            default -> {return Stream.of(para1, para2, op).toList();}
        }
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new CmpInst(op, result, Inst.getEquivalentReg(regMap, para1), Inst.getEquivalentReg(regMap, para2));
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public String getSpecificResult() {
        if (Inst.isInt(para1) && Inst.isInt(para2)) {
            int i1 = Integer.parseInt(para1);
            int i2 = Integer.parseInt(para2);
            return switch (op) {
                case "==" -> i1 == i2 ? "1" : "0";
                case "!=" -> i1 != i2 ? "1" : "0";
                case ">" -> i1 > i2 ? "1" : "0";
                case "<"  -> i1 < i2 ? "1" : "0";
                case ">=" -> i1 >= i2 ? "1" : "0";
                case "<=" -> i1 <= i2 ? "1" : "0";
                default -> throw new IllegalStateException("Unexpected value: " + op);
            };
        } else {
            return null;
        }
    }
}