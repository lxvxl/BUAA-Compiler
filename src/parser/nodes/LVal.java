package parser.nodes;

import error.ErrorHandler;
import error.ParsingFailedException;
import ident.SymbolTable;
import ident.idents.Var;
import intermediateCode.CodeGenerator;
import intermediateCode.instructions.AddInst;
import intermediateCode.instructions.LoadInst;
import intermediateCode.instructions.MulInst;
import intermediateCode.instructions.StoreInst;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;
import logger.Logger;
import parser.SyntaxChecker;
import parser.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class LVal implements TreeNode {
    private final List<TreeNode> children;

    private LVal(List<TreeNode> children) {
        this.children = children;
    }

    //LVal → Ident {'[' Exp ']'}
    public static LVal parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 LVal");

        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
            while (lm.checkSymbol().type() == CategoryCode.LBRACK) {
                children.add(lm.getSymbolWithCategory(CategoryCode.LBRACK));
                children.add(Exp.parse(lm));
                SyntaxChecker.addRbrackWithCheck(children, lm);
            }

            lm.revokeMark();
            Logger.write("e解析 LVal 成功");
            return new LVal(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 LVal 失败");
            throw e;
        }
    }

    @Override
    public void compile() {
        Symbol ident = (Symbol)children.get(0);
        Var var = (Var) SymbolTable.searchIdent(ident.symbol());
        if (var == null) {
            ErrorHandler.putError(ident.lineNum(), 'c');
            return;
        }
        String addr = var.getAddrReg();
        String areaName = addr;
        boolean isGlobalArea = var.isPtr() || addr.charAt(0) == '@';
        if (var.isPtr()) {
            addr = CodeGenerator.generateLoad(addr, "0", null, false);
        }
        switch (children.size()) {
            case 1 -> {
                //若是零维，返回值，否则，返回地址
                if (var.getDim() > 0) {
                    SyntaxChecker.setExpReturnReg(addr);
                } else {
                    if (var.isConst()) {
                        SyntaxChecker.setExpReturnReg(var.getInitVal());
                    } else {
                        String result = CodeGenerator.generateLoad(addr, "0", null, isGlobalArea);
                        SyntaxChecker.setExpReturnReg(result);
                    }
                }
            }
            case 4 -> {
                children.get(2).compile();
                String loc = SyntaxChecker.getExpReturnReg();
                //若是一维，返回值，否则，返回地址
                if (var.getDim() == 1) {
                    try {
                        if (var.isConst()) {
                            SyntaxChecker.setExpReturnReg(var.getInitVal(Integer.parseInt(loc)));
                        } else {
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        String result = CodeGenerator.generateLoad(addr, loc, areaName, isGlobalArea);
                        SyntaxChecker.setExpReturnReg(result);
                    }
                } else {
                    String off = CodeGenerator.generateMul(loc, var.getElementSize());
                    String finalAddr = CodeGenerator.generateAdd(off, addr);
                    SyntaxChecker.setExpReturnReg(finalAddr);
                }
            }
            case 7 -> {
                children.get(2).compile();
                String loc1 = SyntaxChecker.getExpReturnReg();
                children.get(5).compile();
                String loc2 = SyntaxChecker.getExpReturnReg();
                try {
                    if (var.isConst()) {
                        SyntaxChecker.setExpReturnReg(var.getInitVal(Integer.parseInt(loc1), Integer.parseInt(loc2)));
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    String off1 = CodeGenerator.generateMul(loc1, var.getElementSize());
                    String middleAddr = CodeGenerator.generateAdd(addr, off1);
                    String result = CodeGenerator.generateLoad(middleAddr, loc2, areaName, isGlobalArea);
                    SyntaxChecker.setExpReturnReg(result);
                }
            }
        }
    }

    public void storeVal(String val) {

        Symbol ident = (Symbol)children.get(0);
        Var var = (Var) SymbolTable.searchIdent(ident.symbol());
        String addr = var.getAddrReg();
        String areaName = addr;
        boolean isGlobalArea = var.isPtr() || addr.charAt(0) == '@';
        switch (children.size()) {
            case 1 -> {
                CodeGenerator.generateStore(val, addr, "0", null, isGlobalArea);
            }
            case 4 -> {
                if (var.isPtr()) {
                    addr = CodeGenerator.generateLoad(addr, "0", null, false);
                }
                children.get(2).compile();
                String loc = SyntaxChecker.getExpReturnReg();
                CodeGenerator.generateStore(val, addr, loc, areaName, isGlobalArea);
            }
            case 7 -> {
                if (var.isPtr()) {
                    addr = CodeGenerator.generateLoad(addr, "0", null, false);
                }
                children.get(2).compile();
                String loc1 = SyntaxChecker.getExpReturnReg();
                children.get(5).compile();
                String loc2 = SyntaxChecker.getExpReturnReg();

                String off1 = CodeGenerator.generateMul(loc1, var.getElementSize());
                String middleAddr = CodeGenerator.generateAdd(addr, off1);
                CodeGenerator.generateStore(val, middleAddr, loc2, areaName, isGlobalArea);
            }
        }
    }

    public int checkDim() {
        Var ident = (Var) SymbolTable.searchIdent(((Symbol)children.get(0)).symbol());
        if (ident == null) {
            return -2;
        }
        int realDim = ident.getDim() - children.size() / 3;
        return Math.max(realDim, -1);
    }

    /**
     * @return 若不可变且符号已定义，返回lineNum，否则返回0
     */
    public int changeable() {
        Var ident = (Var) SymbolTable.searchIdent(((Symbol)children.get(0)).symbol());
        if (ident == null) {
            return 0;
        }
        return ident.isConst() ? ((Symbol)children.get(0)).lineNum() : 0;
    }
}
