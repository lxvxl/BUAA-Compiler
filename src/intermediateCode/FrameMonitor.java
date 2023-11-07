package intermediateCode;

import Writer.MipsGenerator;

import java.util.*;

public class FrameMonitor {
    private static final Map<String, Integer> params = new HashMap<>();
    private static final Map<String, Integer> alloca = new HashMap<>();
    private static int bottom;

    public static void funcIn() {
        bottom = 1;
        params.clear();
        alloca.clear();
    }

    /**
     * 对于存储在寄存器reg的参数param，将其保存到函数的栈中
     */
    public static void initParam(String param, String reg) {
        params.put(param, bottom);
        MipsGenerator.addInst(String.format("\tsw %s, %d($sp)", reg, -bottom * 4));
        bottom++;
    }

    /**
     * 将参数param的值取出到reg中
     */
    public static void getParamVal(String param, String reg) {
        if (params.containsKey(param)) {
            MipsGenerator.addInst(String.format("\tlw %s, %d($sp)", reg, -params.get(param) * 4));
        } else if (alloca.containsKey(param)) {
            MipsGenerator.addInst(String.format("\tadd %s, $sp, %d", reg, -alloca.get(param) * 4));
        } else if (param.charAt(0) == '@') {
            MipsGenerator.addInst(String.format("\tla %s, %s", reg, "g_" + param.substring(1)));
        } else {
            MipsGenerator.addInst(String.format("\tli %s, %d", reg, Integer.parseInt(param)));
        }
    }

    /**
     * 在栈上申请一块大小为size的空间，并用addr来标识它
     */
    public static void allocaParam(int size, String addr) {
        bottom += size / 4;
        alloca.put(addr, bottom - 1);
    }

    /**
     * 使用addr标识位于栈上loc位置的空间
     */
    public static void mapParam(String addr, int loc) {
        alloca.put(addr, loc);
    }

    public static int storeEnv(List<String> regs) {
        int temp = bottom;
        for (String reg : regs) {
            MipsGenerator.addInst(String.format("\tsw %s, -%d($sp)", reg, temp * 4));
            temp++;
        }
        return temp;
    }

    public static void restoreEnv(List<String> regs) {
        int temp = bottom;
        for (String reg : regs) {
            MipsGenerator.addInst(String.format("\tlw %s, -%d($sp)", reg, temp * 4));
            temp++;
        }
    }
}
