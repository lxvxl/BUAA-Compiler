package intermediateCode;

import Writer.MipsGenerator;
import ident.SymbolTable;
import ident.idents.Func;
import intermediateCode.instructions.*;

import java.util.*;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

public class CodeGenerator {
    private static final LinkedList<FuncCode> funcs = new LinkedList<>();
    private static int registerNum = 0;
    private static int labelNum = 0;
    private static final List<Inst> globalInsts = new ArrayList<>();
    private static final Map<String, String> constStrMap = new HashMap<>();
    private static int constStrNum = 0;

    public static void output() {
        for (Inst inst : globalInsts) {
            System.out.println(inst);
        }
        for (FuncCode funcCode : funcs) {
            funcCode.output();
        }
    }

    public static void FuncIn(String name) {
        funcs.addLast(new FuncCode(name, new ArrayList<>()));
    }

    public static void toMips() {
        MipsGenerator.addInst(".data");
        for (Map.Entry<String, String> entry : constStrMap.entrySet()) {
            MipsGenerator.addInst(String.format("%s: .asciiz \"%s\"", entry.getValue(), entry.getKey()));
        }
        for (Inst inst : globalInsts) {
            inst.toMips();
        }
        MipsGenerator.addInst(".text");
        MipsGenerator.addInst("j func_main");
        for (FuncCode func : funcs) {
            func.toMips();
        }
    }

    public static String generateReg() {
        registerNum += 1;
        return '%' + Integer.toString(registerNum);
    }

    public static String generateLabel() {
        labelNum++;
        return "label_" + labelNum;
    }

    public static void addInst(Inst inst) {
        if (funcs.size() >= 1) {
            funcs.getLast().insts().add(inst);
        } else {
            globalInsts.add(inst);
        }
    }

    /**
     * 判断是否处于全局阶段
     */
    public static boolean isGlobal() {
        return funcs.isEmpty();
    }

    public static void generatePutStr(String str) {
        String label;
        if (str.isEmpty()) {
            return;
        }
        if (constStrMap.containsKey(str)) {
            label = constStrMap.get(str);
        } else {
            label = "CONSTR_" + constStrNum;
            constStrNum++;
            constStrMap.put(str, label);
        }
        addInst(new PutStrInst(label));
    }

    public static String generateAdd(String param1, String param2) {
        try {
            return valueOf(parseInt(param1) + parseInt(param2));
        } catch (Exception e) {
            String result = generateReg();
            addInst(new AddInst(result, param1, param2));
            return result;
        }
    }

    public static String generateSub(String param1, String param2) {
        try {
            return valueOf(parseInt(param1) - parseInt(param2));
        } catch (Exception e) {
            String result = generateReg();
            addInst(new SubInst(result, param1, param2));
            return result;
        }
    }

    public static String generateDiv(String param1, String param2) {
        try {
            return valueOf(parseInt(param1) / parseInt(param2));
        } catch (Exception e) {
            String result = generateReg();
            addInst(new DivInst(result, param1, param2));
            return result;
        }
    }

    public static String generateMul(String param1, String param2) {
        try {
            return valueOf(parseInt(param1) * parseInt(param2));
        } catch (Exception e) {
            String result = generateReg();
            addInst(new MulInst(result, param1, param2));
            return result;
        }
    }

    public static String generateMod(String param1, String param2) {
        try {
            return valueOf(parseInt(param1) % parseInt(param2));
        } catch (Exception e) {
            String result = generateReg();
            addInst(new ModInst(result, param1, param2));
            return result;
        }
    }

    public static String generateCmp(String param1, String param2, String op) {
        try {
            int l = parseInt(param1);
            int r = parseInt(param2);
            boolean result = switch (op) {
                case "==" -> l == r;
                case "!=" -> l != r;
                case ">" -> l > r;
                case "<" -> l < r;
                case ">=" -> l >= r;
                case "<=" -> l <= r;
                default -> throw new IllegalStateException("Unexpected value: " + op);
            };
            return result ? "1" : "0";
        } catch (Exception e) {
            String result = generateReg();
            addInst(new CmpInst(op, result, param1, param2));
            return result;
        }
    }

    public static String generateLoad(String addr, String loc) {
        String result = generateReg();
        try {
            addInst(new LoadInst(result, addr, parseInt(loc) * 4));
        } catch (Exception e) {
            String off = generateMul(loc, "4");
            String finalAddr = generateAdd(addr, off);
            addInst(new LoadInst(result, finalAddr, 0));
        }
        return result;
    }

    public static void generateStore(String val, String addr, String loc) {
        try {
            addInst(new StoreInst(val, addr, parseInt(loc) * 4));
        } catch (Exception e) {
            String off = generateMul(loc, "4");
            String finalAddr = generateAdd(addr, off);
            addInst(new StoreInst(val, finalAddr, 0));
        }
    }

    public static String generateGetInt() {
        String result = generateReg();
        addInst(new GetIntInst(result));
        return result;
    }

    public static String generateCall(String name, List<String> params) {
        String result;
        if (((Func)SymbolTable.searchIdent(name)).getReturnType().equals("int")) {
            result = generateReg();
        } else {
            result = null;
        }
        addInst(new CallInst(result, name, params));
        return result;
    }

    public static void generateBr(String param, String trueLabel, String falseLabel) {
        try {
            int n = Integer.parseInt(param);
            if (n == 0) {
                addInst(new JumpInst(falseLabel));
            } else {
                addInst(new JumpInst(trueLabel));
            }
        } catch (Exception e) {
            addInst(new BrInst(param, trueLabel, falseLabel));
        }
    }
}
