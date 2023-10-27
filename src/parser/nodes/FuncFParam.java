package parser.nodes;

import error.ErrorHandler;
import error.ParsingFailedException;
import ident.RepeatDefException;
import ident.SymbolTable;
import ident.idents.Var;
import intermediateCode.CodeGenerator;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;
import logger.Logger;
import parser.SyntaxChecker;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuncFParam implements TreeNode {
    private final List<TreeNode> children;

    private FuncFParam(List<TreeNode> children) {
        this.children = children;
    }

    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    public static FuncFParam parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 FuncFParam");

        try {
            children.add(BType.parse(lm));
            children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
            if (lm.checkSymbol().type() == CategoryCode.LBRACK) {
                children.add(lm.getSymbolWithCategory(CategoryCode.LBRACK));
                SyntaxChecker.addRbrackWithCheck(children, lm);
                while (lm.checkSymbol().type() == CategoryCode.LBRACK) {
                    children.add(lm.getSymbolWithCategory(CategoryCode.LBRACK));
                    children.add(ConstExp.parse(lm));
                    SyntaxChecker.addRbrackWithCheck(children, lm);
                }
            }
            Logger.write("e解析 FuncFParam 成功");
            lm.revokeMark();
            return new FuncFParam(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 FuncFParam 失败");
            throw e;
        }
    }

    @Override
    public void compile() {
        Var var = switch (children.size()) {
            case 2 -> new Var(((Symbol)children.get(1)).symbol(), false, 0);
            case 4 -> new Var(((Symbol)children.get(1)).symbol(), false, 1);
            case 7 -> {
                children.get(5).compile();
                int elementSize = Integer.parseInt(SyntaxChecker.getExpReturnReg()) * 4;
                yield new Var(((Symbol)children.get(1)).symbol(), false, 2, Integer.toString(elementSize));
            }
            default -> throw new IllegalStateException("Unexpected value: " + children.size());
        };
        try {
            SymbolTable.addIdent(var);
        } catch (RepeatDefException e) {
            ErrorHandler.putError(((Symbol)children.get(1)).lineNum(), 'b');
        }
        var.setAddrReg(CodeGenerator.generateReg() + '_' + var.getName());
        FuncFParams.addParam(var);
    }

    public List<TreeNode> getChildren() {
        return children;
    }
}
