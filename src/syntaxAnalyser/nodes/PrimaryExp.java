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

public class PrimaryExp implements TreeNode {
    private final List<TreeNode> children;

    private PrimaryExp(List<TreeNode> children) {
        this.children = children;
    }


    // PrimaryExp → '(' Exp ')' | LVal | Number
    public static PrimaryExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 PrimaryExp");

        try {
            switch (lm.checkSymbol().type()) {
                case LPARENT:
                    children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                    children.add(Exp.parse(lm));
                    children.add(lm.getSymbolWithCategory(CategoryCode.RPARENT));
                    lm.revokeMark();
                    Logger.write("e解析 PrimaryExp 成功");
                    return new PrimaryExp(children);
                case INTCON:
                    children.add(Number.parse(lm));
                    lm.revokeMark();
                    Logger.write("e解析 PrimaryExp 成功");
                    return new PrimaryExp(children);
                default:
                    children.add(LVal.parse(lm));
                    lm.revokeMark();
                    Logger.write("e解析 PrimaryExp 成功");
                    return new PrimaryExp(children);
            }
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 PrimaryExp 失败");
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
