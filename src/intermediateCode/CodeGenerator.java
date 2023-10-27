package intermediateCode;

import ident.SymbolTable;
import ident.idents.Func;
import ident.idents.Var;
import intermediateCode.instructions.*;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

public class CodeGenerator {
    private static final LinkedList<FuncCode> funcs = new LinkedList<>();
    private static int registerNum = 0;
    private static int labelNum = 0;
    private static final List<Inst> globalInsts = new ArrayList<>();

    public record FuncCode(String name, List<Inst> insts) {
        public void output() {
            Func func = (Func) SymbolTable.searchIdent(name);
            StringBuffer buffer = new StringBuffer();
            for (Var var : func.getParams()) {
                if (!buffer.isEmpty()) {
                    buffer.append(' ');
                }
                buffer.append(var.getAddrReg());
            }
            System.out.printf("function %s %s (%s)\n", func.getReturnType(), func.getName(), buffer.toString());
            for (Inst inst : insts) {
                System.out.println("\t" + inst);
            }
        }
    }

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

    public static String generateReg() {
        registerNum += 1;
        return '%' + Integer.toString(registerNum);
    }

    public static String generateLabel() {
        labelNum++;
        return "label " + labelNum;
    }

    public static void addInst(Inst inst) {
        if (funcs.size() >= 1) {
            funcs.getLast().insts.add(inst);
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
}
