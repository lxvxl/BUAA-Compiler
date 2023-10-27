package parser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;
import parser.SyntaxChecker;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Number implements TreeNode {
    private final List<TreeNode> children;

    private Number(List<TreeNode> children) {
        this.children = children;
    }

    public static Number parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        children.add(lm.getSymbolWithCategory(CategoryCode.INTCON));
        return new Number(children);
    }

    @Override
    public void compile() {
        SyntaxChecker.setExpReturnReg(((Symbol)children.get(0)).symbol());
    }
}
