package intermediateCode.optimize;

import Writer.MipsGenerator;
import intermediateCode.Inst;
import intermediateCode.instructions.CallInst;
import intermediateCode.instructions.GetIntInst;
import intermediateCode.instructions.LoadInst;

import java.util.*;
import java.util.stream.Stream;

public class RegAllocator {
    private static final Set<String> freeTempReg = new HashSet<>();//记录空余的寄存器
    private static final Map<String, String> paramsInReg = new HashMap<>();//记录变量与临时寄存器的对应关系
    private static final Map<String, String> globalRegsAlloc = new HashMap<>();//记录栈变量对应的寄存器

    private static final Map<String, Set<String>> paramConflicts = new HashMap<>();//记录局部变量的冲突关系
    private static final Map<String, Integer> paramRefCount = new HashMap<>(); //记录局部变量引用次数
    private static final Map<String, TreeSet<Integer>> paramUsage = new HashMap<>(); //记录某个变量被哪些指令使用了


    /**
     * 将param的值提取到某个寄存器中，并返回这个寄存器
     * 对于整数，返回的寄存器内装有这个整数
     * 对于栈变量，返回的寄存器内装有这个栈变量的地址
     * 对于临时变量，返回的寄存器装有这个临时变量的值
     * 对于全局变量，返回的是装有这个全局变量地址的寄存器
     */
    public static String getParamVal(String param, int lineNum) {
        //先单独判断一下是不是整数
        if (Inst.isInt(param)) {
            if (paramsInReg.containsKey(param)) {
                paramUsage.put(param, new TreeSet<>(){{add(lineNum);}});
                return paramsInReg.get(param);
            }
            paramUsage.put(param, new TreeSet<>(){{add(lineNum);}});//为了在getFreeReg中及时将其释放掉
            String result = getFreeReg(lineNum, param);
            MipsGenerator.addInst(String.format("\tli %s, %s", result, param));
            return result;
        } else if (Inst.isGlobalParam(param)) {
            if (paramsInReg.containsKey(param)) {
                paramUsage.put(param, new TreeSet<>(){{add(lineNum);}});
                return paramsInReg.get(param);
            }
            paramUsage.put(param, new TreeSet<>(){{add(lineNum);}});//为了在getFreeReg中及时将其释放掉
            String result = getFreeReg(lineNum, param);
            MipsGenerator.addInst(String.format("\tla %s, g_%s", result, param.substring(1)));
            return result;
        } else if (Inst.isTempParam(param)) {
            if (paramsInReg.containsKey(param)) {
                return paramsInReg.get(param);
            }
            String result = getFreeReg(lineNum, param);
            StackAlloactor.loadAndRemoveVal(param, result);
            return result;
        } else { //栈变量
            if (paramsInReg.containsKey(param)) {
                //paramUsage.put(param, new TreeSet<>(){{add(lineNum);}});
                return paramsInReg.get(param);
            }
            //paramUsage.put(param, new TreeSet<>(){{add(lineNum);}});//为了在getFreeReg中及时将其释放掉
            String result = getFreeReg(lineNum, param);
            StackAlloactor.getAddrVal(param, result);
            return result;
        }
    }

    /**
     * 如果一个栈变量被保存在寄存器中，返回这个寄存器。否则，返回null
     */
    public static String getStackParamReg(String addr) {
        return globalRegsAlloc.get(addr);
    }

    /**
     * 为变量申请一个空闲的寄存器
     */
    public static String getFreeReg(int lineNum, String param) {
        if (freeTempReg.isEmpty()) {
            clearInvalidRegs(lineNum);
        }
        if (freeTempReg.isEmpty()) {
            String removeParam = paramsInReg.keySet().stream()
                    .filter(Inst::isTempParam)
                    .max((o1, o2) -> {
                        Integer nextUsed1 = paramUsage.get(o1).higher(lineNum - 1);
                        Integer nextUsed2 = paramUsage.get(o2).higher(lineNum - 1);
                        if (nextUsed1 == null) {
                            return 1;
                        } else if (nextUsed2 == null) {
                            return  -1;
                        } else {
                            return nextUsed1 - nextUsed2;
                        }
                    }).get();
            String reg = paramsInReg.get(removeParam);
            paramsInReg.remove(removeParam);
            StackAlloactor.saveVal(removeParam, reg);
            paramsInReg.put(param, reg);
            return reg;
        }
        String reg = freeTempReg.iterator().next();
        freeTempReg.remove(reg);
        paramsInReg.put(param, reg);
        return reg;
    }

    private static void clearInvalidRegs(int lineNum) {
        Iterator<Map.Entry<String, String>> iterator = paramsInReg.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (paramUsage.get(entry.getKey()).higher(lineNum - 1) == null) {
                iterator.remove();
                freeTempReg.add(entry.getValue());
            }
        }
    }

    public static void initFuncParam(List<String> params) {
        for (int i = 0; i < params.size(); i++) {
            String addr = params.get(i);
            if (i < 4) {
                if (globalRegsAlloc.containsKey(addr)) {
                    MipsGenerator.addInst(String.format("\tmove %s, $a%d", globalRegsAlloc.get(addr), i));
                } else {
                    MipsGenerator.addInst(String.format("\tsw $a%d, %d($sp)", i, i * 4));
                    StackAlloactor.alloca(addr, 4, i * 4);
                }
            } else {
                if (globalRegsAlloc.containsKey(addr)) {
                    MipsGenerator.addInst(String.format("\tlw %s, %d($sp)", globalRegsAlloc.get(addr), i * 4));
                } else {
                    StackAlloactor.alloca(addr, 4, i * 4);
                }
            }
        }
    }

    public static void clearTempReg() {
        freeTempReg.addAll(paramsInReg.values());
        paramsInReg.clear();
    }

    public static boolean isDisposableParam(String param) {
        return paramUsage.get(param).size() == 1;
    }

    /**
     * 增加栈变量的引用次数
     */
    public static void addParamRef(String param) {
        Integer count = paramRefCount.get(param);
        paramRefCount.put(param, count == null ? 1 : count + 1);
    }

    /**
     * 记录栈变量的相互冲突关系
     */
    public static void recordConflicts(Set<String> activeParam, String another) {
        Set<String> conflictSet = paramConflicts.computeIfAbsent(another, k -> new HashSet<>());
        activeParam.stream()
                .filter(Inst::isStackParam)
                .forEach(acParam -> {
                    conflictSet.add(acParam);
                    paramConflicts.computeIfAbsent(acParam, k -> new HashSet<>()).add(another);
                });
    }

    public static void dealFuncParamConflict(List<String> params) {
        for (int i = 0; i < params.size(); i++) {
            String param1 = params.get(i);
            Set<String> conflictSet = paramConflicts.computeIfAbsent(param1, k -> new HashSet<>());
            for (int j = i + 1; j < params.size(); j++) {
                String param2 = params.get(j);
                conflictSet.add(param2);
                paramConflicts.computeIfAbsent(param2, k -> new HashSet<>()).add(param1);
            }
        }
    }

    public static void recordParamUsage(Inst inst) {
        inst.usedReg().forEach(p -> paramUsage.computeIfAbsent(p, k -> new TreeSet<>()).add(inst.num()));
        if (inst instanceof GetIntInst getIntInst) {
            paramUsage.computeIfAbsent(getIntInst.result(), k -> new TreeSet<>()).add(inst.num());
        } else if (inst instanceof CallInst callInst) {
            paramUsage.computeIfAbsent(callInst.result(), k -> new TreeSet<>()).add(inst.num());
        }
    }

    public static void allocateGlobalReg() {
        //去除冲突变量中的没有被引用计数记录的变量。这些变量可能是混进来的全局变量
        System.out.println("开始分配寄存器");
        paramConflicts.entrySet().removeIf(entry -> !paramRefCount.containsKey(entry.getKey()));
        final List<String> globalRegs = Stream.of("$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7").toList();
        List<String> paramList = new ArrayList<>();
        Set<String> paramSet = new HashSet<>();
        Set<String> stackSet = new HashSet<>();
        //逐步将变量从图中删去
        while (paramSet.size() + stackSet.size() != paramConflicts.size()) {
            boolean isDelete = false;
            for (String param : paramConflicts.keySet()) {
                if (paramSet.contains(param) || stackSet.contains(param)) {
                    continue;
                }
                Set<String> conflicts = paramConflicts.get(param);
                int degrees = (int) conflicts.stream()
                        .filter(p -> !paramSet.contains(p) && !stackSet.contains(p))
                        .count();
                if (degrees < globalRegs.size()) {
                    paramList.add(param);
                    paramSet.add(param);
                    isDelete = true;
                    System.out.println("将寄存器" + param + "分配为全局寄存器");
                }
            }
            if (!isDelete) {
                String minRefParam = paramConflicts.keySet().stream()
                        .filter(p -> !paramSet.contains(p) && !stackSet.contains(p))
                        .min(Comparator.comparingInt(paramRefCount::get))
                        .get();
                stackSet.add(minRefParam);
                System.out.println("将寄存器" + minRefParam + "分配在栈上");
            }
        }
        System.out.println("中间代码寄存器分配完毕");
        //进行寄存器分配
        Set<String> unusedGlobalRegs = new HashSet<>(globalRegs);
        for (int i = paramList.size() - 1; i >= 0; i--) {
            String param = paramList.get(i);
            Optional<String> reg = globalRegs.stream()
                    .filter(r -> paramConflicts.get(param).stream()
                            .noneMatch(p -> r.equals(globalRegsAlloc.get(p))))
                    .findFirst();
            globalRegsAlloc.put(param, reg.get());
            unusedGlobalRegs.remove(reg.get());
        }
        System.out.println("全局寄存器分配完毕！");
        for (String param : globalRegsAlloc.keySet()) {
            System.out.println(param + ": " + globalRegsAlloc.get(param));
        }
        freeTempReg.addAll(unusedGlobalRegs);
    }

    public static List<String> getUsedRegs(CallInst callInst) {
        clearInvalidRegs(callInst.num());
        return Stream.of("$v1",
                "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9",
                "$s0", "$s1", "$s2", "$s3","$s4", "$s5","$s6", "$s7",
                "$k0", "$k1", "$fp", "$ra").filter(reg -> !freeTempReg.contains(reg)).toList();
    }

    public static void resetAll() {
        freeTempReg.clear();
        freeTempReg.addAll(List.of("$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9", "$k0", "$k1", "$v1"));
        paramsInReg.clear();
        paramConflicts.clear();
        paramRefCount.clear();
        globalRegsAlloc.clear();
        paramUsage.clear();
    }
}
