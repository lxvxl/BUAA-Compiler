package syntaxAnalyser.nodes;

import error.ParsingFailedException;
import lexical.LexicalManager;
import logger.Logger;
import syntaxAnalyser.TreeNode;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

public class BlockItem implements TreeNode {
    private final List<TreeNode> children;

    private BlockItem(List<TreeNode> children) {
        this.children = children;
    }

    //BlockItem → Decl | Stmt
    public static BlockItem parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析blockitem");

        try {
            children.add(Decl.parse(lm));
            lm.revokeMark();
            Logger.write("e解析blockitem成功");
            return new BlockItem(children);
        } catch (ParsingFailedException ignored) {}

        try {
            children.add(Stmt.parse(lm));
            lm.revokeMark();
            Logger.write("e解析blockitem成功");
            return new BlockItem(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析blockitem失败");
            throw e;
        }
    }

    @Override
    public void compile(BufferedWriter writer) {
        for (TreeNode node: children) {
            node.compile(writer);
        }
    }
}
