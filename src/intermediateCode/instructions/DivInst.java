package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.Computable;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;
import jdk.jfr.Unsigned;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record DivInst(String result, String para1, String para2) implements Inst, Computable {
    @Override
    public String toString() {
        return String.format("%s = div %s %s", result, para1, para2);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (CodeGenerator.OPTIMIZE) {
            if (Inst.isInt(para2)) {
                divisionOptimize();
                return;
            }
            int num = num();
            String resultReg = RegAllocator.getFreeReg(num, result);
            String para1Reg = RegAllocator.getParamVal(para1, num);
            String para2Reg = RegAllocator.getParamVal(para2, num);
            MipsGenerator.addInst(String.format("\tdiv %s, %s", para1Reg, para2Reg));
            MipsGenerator.addInst("\tmflo " + resultReg);
            return;
        }
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        MipsGenerator.addInst("\tdiv $t0, $t1");
        MipsGenerator.addInst("\tmflo $t2");
        FrameMonitor.initParam(result, "$t2");
    }

    private void divisionOptimize() {
        int num = num();
        String para1Reg = RegAllocator.getParamVal(para1, num);
        String resultReg = RegAllocator.getFreeReg(num, result);
        int divider = Integer.parseInt(para2);
        if (divider == -1) {
            MipsGenerator.addInst(String.format("\tsubu %s, $0, %s", resultReg, para1Reg));
            return;
        } else if (divider > 0 && (divider & (divider - 1)) == 0) {
            int k = (int) Math.round(Math.log(divider) / Math.log(2));
            MipsGenerator.addInst(String.format("\tsra $at, %s, %d", para1Reg, k - 1));
            MipsGenerator.addInst(String.format("\tsrl $at, $at, %d", 32 - k));
            MipsGenerator.addInst(String.format("\taddu %s, %s, $at", resultReg, para1Reg));
            MipsGenerator.addInst(String.format("\tsra %s, %s, %d", resultReg, resultReg, k));
            return;
        } else if (divider < 0 && ((-divider) & ((-divider) - 1)) == 0) {
            int k = (int) Math.round(Math.log(-divider) / Math.log(2));
            MipsGenerator.addInst(String.format("\tsubu %s, $0, %s", resultReg, para1Reg));
            MipsGenerator.addInst(String.format("\tsra $at, %s, %d", resultReg, k - 1));
            MipsGenerator.addInst(String.format("\tsrl $at, $at, %d", 32 - k));
            MipsGenerator.addInst(String.format("\taddu %s, %s, $at", resultReg, resultReg));
            MipsGenerator.addInst(String.format("\tsra %s, %s, %d", resultReg, resultReg, k));
            return;
        }
        if (false) {
            String para2Reg = RegAllocator.getParamVal(para2, num);
            MipsGenerator.addInst(String.format("\tdiv %s, %s", para1Reg, para2Reg));
            MipsGenerator.addInst("\tmflo " + resultReg);
            return;
        }

        int absD = Math.abs(divider);
        int l = (int) Math.round(Math.ceil(Math.log(absD) / Math.log(2)));
        int shPost = l;
        long mLow = (1L << (32 + l)) / absD;
        long mHigh = ((1L << (32 + l)) + (1L << (l + 1))) / absD;
        while (mLow / 2 < mHigh / 2 && shPost > 0) {
            mLow = mLow / 2;
            mHigh = mHigh / 2;
            shPost--;
        }

        if (mHigh < (1L << 31)) {
            int m = (int) mHigh;
            MipsGenerator.addInst(String.format("\tli %s, 0x%x", resultReg, m));
            MipsGenerator.addInst(String.format("\tmult %s, %s", resultReg, para1Reg));
            MipsGenerator.addInst(String.format("\tmfhi %s", resultReg));
            MipsGenerator.addInst(String.format("\tsra %s, %s, %d", resultReg, resultReg, shPost));
            MipsGenerator.addInst(String.format("\tsrl $at, %s, 31", resultReg));
            MipsGenerator.addInst(String.format("\taddu %s, %s, $at", resultReg, resultReg));
        } else {
            int m = (int) (mHigh - (1L << 32));
            MipsGenerator.addInst(String.format("\tli %s, 0x%x", resultReg, m));
            MipsGenerator.addInst(String.format("\tmult %s, %s", resultReg, para1Reg));
            MipsGenerator.addInst(String.format("\tmfhi %s", resultReg));
            MipsGenerator.addInst(String.format("\taddu %s, %s, %s", resultReg, resultReg, para1Reg));
            MipsGenerator.addInst(String.format("\tsra %s, %s, %d", resultReg, resultReg, shPost));
            MipsGenerator.addInst(String.format("\tsrl $at, %s, 31", resultReg));
            MipsGenerator.addInst(String.format("\taddu %s, %s, $at", resultReg, resultReg));
        }


        /*int k = (int) (Math.log(Math.abs(divider)) / Math.log(2));
        double mF = (double)(1L << (31 + k)) / divider;

        int m;
        if (divider > 0) {
            m = (int) Math.ceil(mF);
        } else {
            m = (int) Math.floor(mF);
        }

        System.out.println(m - mF);
        System.out.println((Math.pow(2, k)) /divider);
        MipsGenerator.addInst(String.format("\tli %s, 0x%x", resultReg, m));
        MipsGenerator.addInst(String.format("\tmult %s, %s", resultReg, para1Reg));
        MipsGenerator.addInst(String.format("\tmfhi %s", resultReg));
        MipsGenerator.addInst(String.format("\tsra %s, %s, %d", resultReg, resultReg, k - 1));
        MipsGenerator.addInst(String.format("\tsrl $at, %s, 31", resultReg));
        MipsGenerator.addInst(String.format("\taddu %s, %s, $at", resultReg, resultReg));*/
    }

    @Override
    public List<String> usedReg() {
        return Stream.of(para1, para2).filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return Stream.of(para1, para2).toList();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new DivInst(result, Inst.getEquivalentReg(regMap, para1), Inst.getEquivalentReg(regMap, para2));
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public String getSpecificResult() {
        if (Inst.isInt(para1) && Inst.isInt(para2)) {
            return Integer.toString(Integer.parseInt(para1) / Integer.parseInt(para2));
        } else if (para2.equals("1")) {
            return para1;
        } else if (para1.equals("0")) {
            return "0";
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
        return new DivInst(Inst.transformParam(result, n, funcName),
                Inst.transformParam(para1, n, funcName),
                Inst.transformParam(para2, n, funcName));
    }

    @Override
    public Inst replaceFor(int n) {
        return new DivInst(Inst.transformFor(result, n),
                Inst.transformFor(para1, n),
                Inst.transformFor(para2, n));
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}

