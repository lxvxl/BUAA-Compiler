package intermediateCode.instructions;

import Writer.MipsGenerator;
import intermediateCode.CodeGenerator;
import intermediateCode.FrameMonitor;
import intermediateCode.Inst;
import intermediateCode.optimize.RegAllocator;
import intermediateCode.optimize.StackAlloactor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record LoadInst(String result, String addr, int offset, String arrName, boolean isGlobalArea) implements Inst {

    @Override
    public String toString() {
        return String.format("%s = load %s %s      //arrName=%s", result, addr, offset, arrName);
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
        int num = num();
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
        return new LoadInst(result,
                Inst.getEquivalentReg(regMap, addr),
                offset,
                Inst.getEquivalentReg(regMap, arrName),
                isGlobalArea);
    }

    @Override
    public String getResult() {
        return result;
    }


    public boolean isArray() {
        return arrName != null;
    }

    @Override
    public int num() {
        return CodeGenerator.getInstNum(this);
    }

    @Override
    public Inst replace(int n, String funcName) {
        throw new RuntimeException();
    }

    public Inst replace(int n, String funcName, Map<String, String> arrMap) {
        String newAddr = Inst.transformParam(addr, n, funcName);
        String newArrName = Inst.transformArrName(arrName, n, funcName, arrMap);
        return new LoadInst(Inst.transformParam(result, n, funcName),
                newAddr,
                offset,
                newArrName,
                Inst.isGlobalParam(newArrName) || Inst.isGlobalParam(newAddr));
    }

    @Override
    public Inst replaceFor(int n) {
        return new LoadInst(Inst.transformFor(result, n),
                Inst.transformFor(addr, n),
                offset,
                arrName,
                isGlobalArea);
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}

