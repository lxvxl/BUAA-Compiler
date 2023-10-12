package parser.nodes;

import error.ErrorHandler;
import error.ParsingFailedException;
import ident.SymbolTable;
import ident.idents.Var;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;
import logger.Logger;
import parser.SyntaxChecker;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Struct;
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
    public void compile(BufferedWriter writer) {
        Symbol ident = (Symbol)children.get(0);
        if (SymbolTable.searchIdent(ident.symbol()) == null) {
            ErrorHandler.putError(ident.lineNum(), 'c');
            return;
        }
        for (TreeNode node: children) {
            node.compile(writer);
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
     * @return 若不可变，返回lineNum，否则返回0
     */
    public int changeable() {
        Var ident = (Var) SymbolTable.searchIdent(((Symbol)children.get(0)).symbol());
        if (ident == null) {
            ErrorHandler.putError(((Symbol)children.get(0)).lineNum(), 'c');
            return 0;
        }
        return ident.isConst() ? ((Symbol)children.get(0)).lineNum() : 0;
    }
}
