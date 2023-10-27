package parser.nodes;

import error.ParsingFailedException;
import lexical.LexicalManager;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Exp implements TreeNode {
    private final List<TreeNode> children;

    private Exp(List<TreeNode> children) {
        this.children = children;
    }

    // Exp → AddExp
    public static Exp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        children.add(AddExp.parse(lm));
        return new Exp(children);
    }

    @Override
    public void compile() {
        for (TreeNode node: children) {
            node.compile();
        }
    }

    public int checkDim() {
        return ((AddExp)children.get(0)).checkDim();
    }
}
