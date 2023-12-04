package parser.nodes;

import error.ParsingFailedException;
import lexical.LexicalManager;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Cond implements TreeNode {
    private final List<TreeNode> children;

    private Cond(List<TreeNode> children) {
        this.children = children;
    }

    //Cond → LOrExp
    public static Cond parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        children.add(LOrExp.parse(lm));
        return new Cond(children);
    }

    @Override
    public void compile() {
        for (TreeNode node: children) {
            node.compile();
        }
    }
}
