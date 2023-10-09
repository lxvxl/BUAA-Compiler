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

public class InitVal implements TreeNode {
    private final List<TreeNode> children;

    private InitVal(List<TreeNode> children) {
        this.children = children;
    }

    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public static InitVal parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 InitVal");

        //'{' [ InitVal { ',' InitVal } ] '}'
        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.LBRACE));
            if (lm.checkSymbol().type() != CategoryCode.RBRACE) {
                children.add(InitVal.parse(lm));
                while (lm.checkSymbol().type() == CategoryCode.COMMA) {
                    children.add(lm.getSymbolWithCategory(CategoryCode.COMMA));
                    children.add(InitVal.parse(lm));
                }
            }
            children.add(lm.getSymbolWithCategory(CategoryCode.RBRACE));

            lm.revokeMark();
            Logger.write("e解析 InitVal 成功");
            return new InitVal(children);
        } catch (ParsingFailedException e) {
            children.clear();
            lm.traceBack();
            lm.mark();
        }

        try {
            children.add((Exp.parse(lm)));

            lm.revokeMark();
            Logger.write("e解析 InitVal 成功");
            return new InitVal(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 InitVal 失败");
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
