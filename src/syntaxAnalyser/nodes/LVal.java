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
                children.add(lm.getSymbolWithCategory(CategoryCode.RBRACK));
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
        for (TreeNode node: children) {
            node.compile(writer);
        }
                try {
            writer.write(String.format("<%s>\n", this.getClass().getName().split("\\.")[2]));
        } catch (IOException ignored) {}
    }
}
