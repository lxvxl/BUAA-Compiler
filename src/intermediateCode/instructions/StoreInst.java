package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;
import intermediateCode.optimize.StackAlloactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public record StoreInst(int num, String val, String addr, int offset, String arrName, boolean isGlobalArea) implements Inst {

    public StoreInst(String val, String addr, int offset, String arrName, boolean isGlobalArea) {
        this(CodeGenerator.getInstNum(), val, addr, offset, arrName, isGlobalArea);
    }

    @Override
    public String toString() {
        return String.format("store %s, %s, %s", val, addr, offset);
    }

    @Override
    public void toMips() {
        MipsGenerator.addInst('#' + toString());
        if (CodeGenerator.OPTIMIZE) {
            toMips2();
            return;
        }
        FrameMonitor.getParamVal(addr, "$t0");
        FrameMonitor.getParamVal(val, "$t2");
        MipsGenerator.addInst(String.format("\tsw $t2, %d($t0)", offset));
    }

    private void toMips2() {
        if (Inst.isGlobalParam(addr)) {
            //先判断是否是全局变量
            MipsGenerator.addInst(String.format("\tsw %s, g_%s + %d", RegAllocator.getParamVal(val, num), addr.substring(1), offset));
            return;
        }
        //判断这个变量是否作为寄存器在使用
        String addrReg = RegAllocator.getStackParamReg(addr);
        if (addrReg != null) {
            MipsGenerator.addInst(String.format("\tmove %s, %s", addrReg, RegAllocator.getParamVal(val, num)));
            return;
        }
        if (Inst.isStackParam(addr)) {
            StackAlloactor.storeAtAddr(RegAllocator.getParamVal(val, num), addr, offset);
            return;
        }
        addrReg = RegAllocator.getParamVal(addr, num);
        MipsGenerator.addInst(String.format("\tsw %s, %d(%s)", RegAllocator.getParamVal(val, num), offset, addrReg));
    }

    @Override
    public List<String> usedReg() {
        return Stream.of(val, addr).filter(p -> !Inst.isInt(p)).toList();
    }

    @Override
    public List<String> getParams() {
        return new ArrayList<>();
    }

    @Override
    public Inst generateEquivalentInst(HashMap<String, String> regMap) {
        return new StoreInst(Inst.getEquivalentReg(regMap, val), Inst.getEquivalentReg(regMap, addr), offset, arrName, isGlobalArea);
    }

    @Override
    public String getResult() {
        return null;
    }

    public boolean isArray() {
        return arrName != null;
    }
}

