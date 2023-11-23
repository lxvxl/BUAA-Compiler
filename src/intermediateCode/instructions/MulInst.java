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

public record MulInst(int num, String result, String para1, String para2) implements Inst, Computable {

    public MulInst(String result, String para1, String para2) {
        this(CodeGenerator.getInstNum(), result, para1, para2);
    }

    @Override
    public String toString() {
        return String.format("%s = mul %s %s", result, para1, para2);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (CodeGenerator.OPTIMIZE) {
            toMips2();
            return;
        }
        try{
            int n = Integer.parseInt(para1);
            if ((n > 0) && ((n & (n - 1)) == 0)) {
                FrameMonitor.getParamVal(para2, "$t0");
                MipsGenerator.addInst(String.format("\tsll $t2, $t0, %d", (int)(Math.log(n) / Math.log(2))));
                FrameMonitor.initParam(result, "$t2");
                return;
            }
        } catch (Exception ignored) {}
        try{
            int n = Integer.parseInt(para2);
            if ((n > 0) && ((n & (n - 1)) == 0)) {
                FrameMonitor.getParamVal(para1, "$t0");
                MipsGenerator.addInst(String.format("\tsll $t2, $t0, %d", (int)(Math.log(n) / Math.log(2))));
                FrameMonitor.initParam(result, "$t2");
                return;
            }
        } catch (Exception ignored) {}
        FrameMonitor.getParamVal(para1, "$t0");
        FrameMonitor.getParamVal(para2, "$t1");
        MipsGenerator.addInst("\tmult $t0, $t1");
        MipsGenerator.addInst("\tmflo $t2");
        FrameMonitor.initParam(result, "$t2");
    }

    private void toMips2() {
        String resultReg = RegAllocator.getFreeReg(num, result);
        try{
            int n = Integer.parseInt(para1);
            if ((n > 0) && ((n & (n - 1)) == 0)) {
                String para2Reg = RegAllocator.getParamVal(para2, num);
                MipsGenerator.addInst(String.format("\tsll %s, %s, %d", resultReg, para2Reg, (int)(Math.log(n) / Math.log(2))));
                return;
            }
        } catch (Exception ignored) {}
        try{
            int n = Integer.parseInt(para2);
            if ((n > 0) && ((n & (n - 1)) == 0)) {
                String para1Reg = RegAllocator.getParamVal(para1, num);
                MipsGenerator.addInst(String.format("\tsll %s, %s, %d", resultReg, para1Reg, (int)(Math.log(n) / Math.log(2))));
                return;
            }
        } catch (Exception ignored) {}
        String para1Reg = RegAllocator.getParamVal(para1, num);
        String para2Reg = RegAllocator.getParamVal(para2, num);
        MipsGenerator.addInst(String.format("\tmult %s, %s", para1Reg, para2Reg));
        MipsGenerator.addInst("\tmflo " + resultReg);
    }

    @Override
    public List<String> usedReg() {
        return Stream.of(para1, para2).filter(p -> !Inst.isInt(p)).sorted().toList();
    }

    @Override
    public List<String> getParams() {
        return Stream.of(para1, para2).sorted().toList();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new MulInst(result, Inst.getEquivalentReg(regMap, para1), Inst.getEquivalentReg(regMap, para2));
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public String getSpecificResult() {
        if (Inst.isInt(para1) && Inst.isInt(para2)) {
            return Integer.toString(Integer.parseInt(para1) * Integer.parseInt(para2));
        } else {
            return null;
        }
    }
}

