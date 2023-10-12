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

public class ConstDef implements TreeNode {
    private final List<TreeNode> children;

    private ConstDef(List<TreeNode> children) {
        this.children = children;
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    public static ConstDef parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        TreeNode node;
        lm.mark();
        Logger.write("s开始解析ConstDef");

        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
            while (lm.checkSymbol().type() == CategoryCode.LBRACK) {
                children.add(lm.getSymbolWithCategory(CategoryCode.LBRACK));
                children.add(ConstExp.parse(lm));
                SyntaxChecker.addRbrackWithCheck(children, lm);
            }
            children.add(lm.getSymbolWithCategory(CategoryCode.ASSIGN));
            children.add(ConstInitVal.parse(lm));
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析ConstDef失败");
            throw e;
        }

        lm.revokeMark();
        Logger.write("e解析ConstDef成功");
        return new ConstDef(children);
    }

    @Override
    public void compile(BufferedWriter writer) {
        for (TreeNode node: children) {
            node.compile(writer);
        }
        try {
            SymbolTable.addIdent(new Var(((Symbol)children.get(0)).symbol(), true, children.size() / 3 - 1));
        } catch (RepeatDefException e) {
            ErrorHandler.putError(((Symbol)children.get(0)).lineNum(), 'b');
        }
        
    }
}
