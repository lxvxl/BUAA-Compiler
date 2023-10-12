package parser.nodes;

import error.ErrorHandler;
import error.ParsingFailedException;
import ident.RepeatDefException;
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
import java.util.ArrayList;
import java.util.List;

public class VarDef implements TreeNode {
    private final List<TreeNode> children;

    private VarDef(List<TreeNode> children) {
        this.children = children;
    }

    //VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    public static VarDef parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 VarDef");

        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
            while (lm.checkSymbol().type() == CategoryCode.LBRACK) {
                children.add(lm.getSymbolWithCategory(CategoryCode.LBRACK));
                children.add(ConstExp.parse(lm));
                SyntaxChecker.addRbrackWithCheck(children, lm);
            }
            if (lm.checkSymbol().type() == CategoryCode.ASSIGN) {
                children.add(lm.getSymbolWithCategory(CategoryCode.ASSIGN));
                children.add(InitVal.parse(lm));
            }

            lm.revokeMark();
            Logger.write("e解析 VarDef 成功");
            return new VarDef(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 VarDef 失败");
            throw e;
        }
    }

    @Override
    public void compile(BufferedWriter writer) {
        for (TreeNode node: children) {
            node.compile(writer);
        }
        try {
            SymbolTable.addIdent(new Var(((Symbol)children.get(0)).symbol(), false, (children.size() - 1) / 3));
        } catch (RepeatDefException e) {
            ErrorHandler.putError(((Symbol)children.get(0)).lineNum(), 'b');
        }
        
    }
}
