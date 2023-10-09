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

public class Block implements TreeNode {
    private final List<TreeNode> children;

    private Block(List<TreeNode> children) {
        this.children = children;
    }

    // Block → '{' { BlockItem } '}'
    public static Block parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析block");

        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.LBRACE));
            while (true) {
                try {
                    children.add(BlockItem.parse(lm));
                } catch (ParsingFailedException ignored) {
                    break;
                }
            }
            children.add(lm.getSymbolWithCategory(CategoryCode.RBRACE));

            lm.revokeMark();
            Logger.write("e解析block成功");
            return new Block(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析block失败");
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
