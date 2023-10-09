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

public class FuncDef implements TreeNode {
    private final List<TreeNode> children;

    private FuncDef(List<TreeNode> children) {
        this.children = children;
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public static FuncDef parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 FuncDef");


        try {
            children.add(FuncType.parse(lm));
            children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
            children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
            try {
                children.add(FuncFParams.parse(lm));
            } catch (ParsingFailedException ignored) {}
            children.add(lm.getSymbolWithCategory(CategoryCode.RPARENT));
            children.add(Block.parse(lm));

            lm.revokeMark();
            Logger.write("e解析 FuncDef 成功");
            return new FuncDef(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 FuncDef 失败");
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
