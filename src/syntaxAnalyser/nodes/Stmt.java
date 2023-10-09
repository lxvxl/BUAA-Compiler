package syntaxAnalyser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import syntaxAnalyser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Stmt implements TreeNode {
    private final List<TreeNode> children;

    private Stmt(List<TreeNode> children) {
        this.children = children;
    }

    //Stmt → LVal '=' Exp ';'
    //| [Exp] ';'
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    //| 'break' ';'
    //| 'continue' ';'
    //| 'return' [Exp] ';'
    //| LVal '=' 'getint''('')'';'
    //| 'printf''('FormatString{','Exp}')'';'
    public static Stmt parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();

        try {
            switch (lm.checkSymbol().type()) {
                //Block
                case LBRACE:
                    children.add(Block.parse(lm));
                    lm.revokeMark();
                    return new Stmt(children);
                //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                case IFTK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.IFTK));
                    children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                    children.add(Cond.parse(lm));
                    children.add(lm.getSymbolWithCategory(CategoryCode.RPARENT));
                    children.add(Stmt.parse(lm));
                    if (lm.checkSymbol().type() == CategoryCode.ELSETK) {
                        children.add(lm.getSymbolWithCategory(CategoryCode.ELSETK));
                        children.add(Stmt.parse(lm));
                    }
                    lm.revokeMark();
                    return new Stmt(children);
                //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                case FORTK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.FORTK));
                    children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                    try {
                        children.add(ForStmt.parse(lm));
                    } catch (ParsingFailedException ignored) {}
                    children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
                    try {
                        children.add(Cond.parse(lm));
                    } catch (ParsingFailedException ignored) {}
                    children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
                    try {
                        children.add(ForStmt.parse(lm));
                    } catch (ParsingFailedException ignored) {}
                    children.add(lm.getSymbolWithCategory(CategoryCode.RPARENT));
                    children.add(Stmt.parse(lm));

                    lm.revokeMark();
                    return new Stmt(children);
                //'break' ';'
                case BREAKTK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.BREAKTK));
                    children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
                    lm.revokeMark();
                    return new Stmt(children);
                //'continue' ';'
                case CONTINUETK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.CONTINUETK));
                    children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
                    lm.revokeMark();
                    return new Stmt(children);
                //'return' [Exp] ';'
                case RETURNTK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.RETURNTK));
                    try {
                        children.add(Exp.parse(lm));
                    } catch (ParsingFailedException ignored) {}
                    children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
                    lm.revokeMark();
                    return new Stmt(children);
                //'printf''('FormatString{','Exp}')'';'
                case PRINTFTK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.PRINTFTK));
                    children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                    children.add(lm.getSymbolWithCategory(CategoryCode.STRCON));
                    while (lm.checkSymbol().type() == CategoryCode.COMMA) {
                        children.add(lm.getSymbolWithCategory(CategoryCode.COMMA));
                        children.add(Exp.parse(lm));
                    }
                    children.add(lm.getSymbolWithCategory(CategoryCode.RPARENT));
                    children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));

                    lm.revokeMark();
                    return new Stmt(children);
                // ';'
                case SEMICN:
                    children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
                    lm.revokeMark();
                    return new Stmt(children);
                //LVal '=' Exp ';'
                //Exp ';'
                //LVal '=' 'getint''('')'';'
                default:
                    //先尝试通过Exp的方式进行解析
                    lm.mark();
                    Exp exp = Exp.parse(lm);
                    if (lm.checkSymbol().type() == CategoryCode.SEMICN) {
                        children.add(exp);
                        children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
                        lm.revokeMark();
                        lm.revokeMark();
                        return new Stmt(children);
                    }
                    lm.traceBack();

                    children.add(LVal.parse(lm));
                    children.add(lm.getSymbolWithCategory(CategoryCode.ASSIGN));
                    if (lm.checkSymbol().type() == CategoryCode.GETINTTK) {
                        children.add(lm.getSymbolWithCategory(CategoryCode.GETINTTK));
                        children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                        children.add(lm.getSymbolWithCategory(CategoryCode.RPARENT));
                        children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
                    } else {
                        children.add(Exp.parse(lm));
                        children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
                    }
                    lm.revokeMark();
                    return new Stmt(children);
            }
        } catch (ParsingFailedException e) {
            lm.traceBack();
            throw e;
        }
    }

    @Override
    public void compile(BufferedWriter writer) {
        for (TreeNode node: children) {
            node.compile(writer);
        }
                try {
            writer.write(String.format("<%s>\n", this.getClass().getName().split("\\.")[2]));
        } catch (IOException ignored) {}
    }
}
