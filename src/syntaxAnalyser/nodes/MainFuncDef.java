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

public class MainFuncDef implements TreeNode {
    private final List<TreeNode> children;

    private MainFuncDef(List<TreeNode> children) {
        this.children = children;
    }

    //MainFuncDef → 'int' 'main' '(' ')' Block
    public static MainFuncDef parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        TreeNode node;
        lm.mark();
        Logger.write("s开始解析 MainFuncDef");

        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.INTTK));
            children.add(lm.getSymbolWithCategory(CategoryCode.MAINTK));
            children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
            children.add(lm.getSymbolWithCategory(CategoryCode.RPARENT));
            children.add(Block.parse(lm));

            lm.revokeMark();
            Logger.write("e解析 MainFuncDef 成功");
            return new MainFuncDef(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 MainFuncDef 失败");
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
