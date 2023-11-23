package intermediateCode.optimize;

import Writer.MipsGenerator;
import intermediateCode.instructions.CallInst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class StackAlloactor {
    private static int bottom = 0;
    private static final Map<String, Integer> addrLoc = new HashMap<>();
    private static final Map<String, Integer> valLoc = new HashMap<>();
    private static final TreeSet<Integer> freeLocation = new TreeSet<>();

    public static void reset() {
        bottom = 0;
        addrLoc.clear();
        valLoc.clear();
        freeLocation.clear();
    }

    /**
     * 在栈上分配固定大小的地址
     */
    public static void alloca(String addr, int size) {
        bottom = bottom + size;
        addrLoc.put(addr, bottom);
    }

    public static void alloca(String addr, int size, int loc) {
        addrLoc.put(addr, -loc);
    }

    /**
     * 将某个临时变量暂时存在栈上
     */
    public static void saveVal(String param, String reg) {
        int location;
        if (freeLocation.isEmpty() || freeLocation.first() > bottom) {
            freeLocation.clear();
            bottom += 4;
            location = bottom;
        } else {
            location = freeLocation.first();
        }
        MipsGenerator.addInst(String.format("\tsw %s, %d($sp)", reg, -location));
        valLoc.put(param, location);
    }

    /**
     * 将某个的值变量从栈中取出
     */
    public static void loadAtAddr(String param, String reg, int offset) {
        MipsGenerator.addInst(String.format("\tlw %s, %d($sp)", reg, -addrLoc.get(param) + offset));
    }

    public static void storeAtAddr(String valReg, String addr, int offset) {
        MipsGenerator.addInst(String.format("\tsw %s, %d($sp)", valReg, -addrLoc.get(addr) + offset));
    }

    /**
     * 将某个变量的值从栈中取出，并释放栈中的空间
     */
    public static void loadAndRemoveVal(String param, String reg) {
        MipsGenerator.addInst(String.format("\tlw %s, %d($sp)", reg, -valLoc.get(param)));
        freeLocation.add(valLoc.get(param));
        addrLoc.remove(param);
    }

    public static void getAddrVal(String param, String reg) {
        MipsGenerator.addInst(String.format("\taddiu %s, %s, %d", reg, "$sp", -addrLoc.get(param)));
    }

    public static void dealCallInst(CallInst callInst) {

        List<String> params = callInst.params();
        //计算完参数之后usedRegs会更新
        //先将参数压栈
        List<String> paramRegs = params.stream().map(p -> RegAllocator.getParamVal(p, callInst.num())).toList();
        List<String> usedRegs = RegAllocator.getUsedRegs(callInst);
        for (int i = 0; i < paramRegs.size(); i++) {
            String paramReg = paramRegs.get(i);
            if (i < 4) {
                MipsGenerator.addInst(String.format("\tmove $a%d, %s", i, paramReg));
            } else {
                MipsGenerator.addInst(String.format("\tsw %s, %d($sp)", paramReg,
                        -bottom - usedRegs.size() * 4 - (paramRegs.size() - i) * 4));
            }
        }
        //将现场压栈
        for (int i = 0; i < usedRegs.size(); i++) {
            MipsGenerator.addInst(String.format("\tsw %s, %d($sp)", usedRegs.get(i), -bottom - usedRegs.size() * 4 + i * 4));
        }
        //准备跳转
        MipsGenerator.addInst("\tmove $fp, $sp");
        MipsGenerator.addInst(String.format("\taddi $sp, $sp, %d", -bottom - (usedRegs.size() + params.size()) * 4));
        MipsGenerator.addInst("\tjal func_" + callInst.funcName());
        //恢复现场
        for (int i = 0; i < usedRegs.size(); i++) {
            MipsGenerator.addInst(String.format("\tlw %s, %d($sp)", usedRegs.get(i), -bottom - usedRegs.size() * 4 + i * 4));
        }
        if (callInst.result() != null) {
            String freeReg = RegAllocator.getFreeReg(callInst.num(), callInst.result());
            MipsGenerator.addInst(String.format("\tmove %s, %s", freeReg, "$v0"));
        }
    }
}
