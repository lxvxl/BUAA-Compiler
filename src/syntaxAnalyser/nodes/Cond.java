package syntaxAnalyser.nodes;

import error.ParsingFailedException;
import lexical.LexicalManager;
import syntaxAnalyser.TreeNode;

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
    public void compile(BufferedWriter writer) {
        for (TreeNode node: children) {
            node.compile(writer);
        }
                try {
            writer.write(String.format("<%s>\n", this.getClass().getName().split("\\.")[2]));
        } catch (IOException ignored) {}
    }
}
