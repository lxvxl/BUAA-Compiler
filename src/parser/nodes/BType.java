package parser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

public class BType implements TreeNode {
    private final List<TreeNode> children;

    private BType(List<TreeNode> children) {
        this.children = children;
    }

    public static BType parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        children.add(lm.getSymbolWithCategory(CategoryCode.INTTK));
        return new BType(children);
    }

    @Override
    public void compile(BufferedWriter writer) {
        for (TreeNode node: children) {
            node.compile(writer);
        }
    }
}
