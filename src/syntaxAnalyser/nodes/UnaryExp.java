package syntaxAnalyser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import logger.Logger;
import syntaxAnalyser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnaryExp implements TreeNode {
    private final List<TreeNode> children;

    private UnaryExp(List<TreeNode> children) {
        this.children = children;
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    public static UnaryExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 UnaryExp");

        try {
            if (lm.checkSymbol().type() == CategoryCode.IDENFR
                    && lm.checkSymbol(1).type() == CategoryCode.LPARENT) {
                //Ident '(' [FuncRParams] ')'
                children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
                children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                if (lm.checkSymbol().type() != CategoryCode.RPARENT) {
                    children.add(FuncRParams.parse(lm));
                }
                children.add(lm.getSymbolWithCategory(CategoryCode.RPARENT));
            } else if (lm.checkSymbol().type() == CategoryCode.PLUS
                    || lm.checkSymbol().type() == CategoryCode.MINU
                    || lm.checkSymbol().type() == CategoryCode.NOT) {
                //UnaryOp UnaryExp
                children.add(UnaryOp.parse(lm));
                children.add(UnaryExp.parse(lm));
            } else {
                //PrimaryExp
                children.add(PrimaryExp.parse(lm));
            }
            lm.revokeMark();
            Logger.write("e解析 UnaryExp 成功");
            return new UnaryExp(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 UnaryExp 失败");
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
