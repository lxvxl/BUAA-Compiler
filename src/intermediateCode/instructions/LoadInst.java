package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;
import intermediateCode.optimize.StackAlloactor;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record LoadInst(int num, String result, String addr, int offset, String arrName) implements Inst {

    public LoadInst(String result, String addr, int offset, String arrName) {
        this(CodeGenerator.getInstNum(), result, addr, offset, arrName);
    }

    @Override
    public String toString() {
        return String.format("%s = load %s %s", result, addr, offset);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (CodeGenerator.OPTIMIZE) {
            toMips2();
            return;
        }
        FrameMonitor.getParamVal(addr, "$t0");
        MipsGenerator.addInst(String.format("\tlw $t2, %d($t0)", offset));
        FrameMonitor.initParam(result, "$t2");
    }

    private void toMips2() {
        //判断这个地址是否和一个全局寄存器绑定起来
        if (RegAllocator.bindTempParamWithGlobalReg(addr, result)) {
            return;
        }

        String resultReg = RegAllocator.getFreeReg(num, result);
        //判定这个地址是否是一个全局变量
        if (Inst.isGlobalParam(addr)) {
            MipsGenerator.addInst(String.format("\tlw %s, g_%s + %d", resultReg, addr.substring(1), offset));
            return;
        }

        //判定这个地址是否是一个局部变量
        if (Inst.isStackParam(addr)) {
            StackAlloactor.loadAtAddr(addr, resultReg, offset);
            return;
        }
        //判定这个地址是否是一个临时变量
        String addrReg = RegAllocator.getParamVal(addr, num);
        MipsGenerator.addInst(String.format("\tlw %s, %d(%s)", resultReg, offset, addrReg));
    }

    @Override
    public List<String> usedReg() {
        return Stream.of(addr).filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return Stream.of(addr, Integer.toString(offset)).toList();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new LoadInst(result, Inst.getEquivalentReg(regMap, addr), offset, arrName);
    }

    @Override
    public String getResult() {
        return result;
    }


    public boolean isArray() {
        return arrName != null;
    }
}

