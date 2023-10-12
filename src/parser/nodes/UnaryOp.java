package parser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnaryOp implements TreeNode {
    private final List<TreeNode> children;

    private UnaryOp(List<TreeNode> children) {
        this.children = children;
    }

    //UnaryOp → '+' | '−' | '!'
    public static UnaryOp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        if (lm.checkSymbol().type() == CategoryCode.PLUS
                || lm.checkSymbol().type() == CategoryCode.MINU
                || lm.checkSymbol().type() == CategoryCode.NOT) {
            children.add(lm.getSymbol());
            return new UnaryOp(children);
        } else {
            throw new ParsingFailedException();
        }
    }

    @Override
    public void compile(BufferedWriter writer) {
        for (TreeNode node: children) {
            node.compile(writer);
        }
                
    }
}
