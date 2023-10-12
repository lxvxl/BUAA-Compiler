package parser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;
import logger.Logger;
import parser.TreeNode;

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
        Logger.write("s开始解析blockitem, lineNum=" + lm.checkSymbol().lineNum());

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

    /**
     * @return 如果这个block是一个return exp 则返回true
     */
    public boolean containReturn() {
        TreeNode firstChild = children.get(0);
        if (firstChild instanceof Stmt) {
            TreeNode stmtFirstChild = ((Stmt) firstChild).getChildren().get(0);
            if (stmtFirstChild instanceof Symbol && ((Symbol) stmtFirstChild).type() == CategoryCode.RETURNTK
                &&  ((Stmt) firstChild).getChildren().size() == 3) {
                return true;
            }
        }
        return false;
    }
}
