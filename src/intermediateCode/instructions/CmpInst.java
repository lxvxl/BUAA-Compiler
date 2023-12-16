package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.Computable;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;

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
        if (CodeGenerator.OPTIMIZE) {
            toMips2();
            return;
        }
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

    private void toMips2() {
        int num = num();
        String para1Reg = RegAllocator.getParamVal(para1, num);
        String para2Reg = RegAllocator.getParamVal(para2, num);
        String resultReg = RegAllocator.getFreeReg(num, result);
        switch (op) {
            case "==" -> {
                MipsGenerator.addInst(String.format("\tand %s, $0, $0", resultReg));
                MipsGenerator.addInst(String.format("\tbne %s, %s, %s", para1Reg, para2Reg, "TEMP_LABEL" + num));
                MipsGenerator.addInst(String.format("\tori %s, $0, 1", resultReg));
                MipsGenerator.addInst("TEMP_LABEL" + num + ':');
            }
            case "!=" -> {
                MipsGenerator.addInst(String.format("\tand %s, $0, $0", resultReg));
                MipsGenerator.addInst(String.format("\tbeq %s, %s, %s", para1Reg, para2Reg, "TEMP_LABEL" + num));
                MipsGenerator.addInst(String.format("\tori %s, %s, 1", resultReg, "$0"));
                MipsGenerator.addInst("TEMP_LABEL" + num + ':');
            }
            case ">" -> MipsGenerator.addInst(String.format("\tslt %s, %s, %s", resultReg, para2Reg, para1Reg));
            case "<" -> MipsGenerator.addInst(String.format("\tslt %s, %s, %s", resultReg, para1Reg, para2Reg));
            case ">=" -> {
                MipsGenerator.addInst(String.format("\tslt %s, %s, %s", resultReg, para1Reg, para2Reg));
                MipsGenerator.addInst(String.format("\txori %s, %s, 1", resultReg, resultReg));
            }
            case "<=" -> {
                MipsGenerator.addInst(String.format("\tslt %s, %s, %s", resultReg, para2Reg, para1Reg));
                MipsGenerator.addInst(String.format("\txori %s, %s, 1", resultReg, resultReg));
            }
        }
    }

    public void toMipsWithBr(BrInst br) {
        MipsGenerator.addInst('#' + toString());
        MipsGenerator.addInst('#' + br.toString());
        if (CodeGenerator.OPTIMIZE) {
            toMipsWithBr2(br);
            return;
        }
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

    private void toMipsWithBr2(BrInst br) {
        int num = num();
        if (!RegAllocator.isDisposableParam(result)) {
            toMips2();
            br.toMips();
            return;
        }
        String para1Reg = RegAllocator.getParamVal(para1, num);
        String para2Reg = RegAllocator.getParamVal(para2, num);
        switch (op) {
            case "==" -> {
                MipsGenerator.addInst(String.format("\tbne %s, %s, %s", para1Reg, para2Reg, br.falseLabel()));
                MipsGenerator.addInst("\tj " + br.trueLabel());
            }
            case "!=" -> {
                MipsGenerator.addInst(String.format("\tbeq %s, %s, %s", para1Reg, para2Reg, br.falseLabel()));
                MipsGenerator.addInst("\tj " + br.trueLabel());
            }
            case ">" -> {
                MipsGenerator.addInst(String.format("\tslt $at, %s, %s", para2Reg, para1Reg));
                MipsGenerator.addInst(String.format("\tbeq $at, $0, %s", br.falseLabel()));
                MipsGenerator.addInst("\tj " + br.trueLabel());
            }
            case "<" -> {
                MipsGenerator.addInst(String.format("\tslt $at, %s, %s", para1Reg, para2Reg));
                MipsGenerator.addInst(String.format("\tbeq $at, $0, %s", br.falseLabel()));
                MipsGenerator.addInst("\tj " + br.trueLabel());
            }
            case ">=" -> {
                MipsGenerator.addInst(String.format("\tslt $at, %s, %s", para1Reg, para2Reg));
                MipsGenerator.addInst(String.format("\tbne $at, $0, %s", br.falseLabel()));
                MipsGenerator.addInst("\tj " + br.trueLabel());
            }
            case "<=" -> {
                MipsGenerator.addInst(String.format("\tslt $at, %s, %s", para2Reg, para1Reg));
                MipsGenerator.addInst(String.format("\tbne $at, $0, %s", br.falseLabel()));
                MipsGenerator.addInst("\tj " + br.trueLabel());
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

    @Override
    public int num() {
        return CodeGenerator.getInstNum(this);
    }

    @Override
    public Inst replace(int n, String funcName) {
        return new CmpInst(op,
                Inst.transformParam(result, n, funcName),
                Inst.transformParam(para1, n, funcName),
                Inst.transformParam(para2, n, funcName));
    }

    @Override
    public Inst replaceFor(int n) {
        return new CmpInst(op,
                Inst.transformFor(result, n),
                Inst.transformFor(para1, n),
                Inst.transformFor(para2, n));
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}