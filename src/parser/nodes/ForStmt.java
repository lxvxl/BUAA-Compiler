package parser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import logger.Logger;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ForStmt implements TreeNode {
    private final List<TreeNode> children;

    private ForStmt(List<TreeNode> children) {
        this.children = children;
    }

    //ForStmt → LVal '=' Exp
    public static ForStmt parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 ForStmt");
        try {
            children.add(LVal.parse(lm));
            children.add(lm.getSymbolWithCategory(CategoryCode.ASSIGN));
            children.add(Exp.parse(lm));

            lm.revokeMark();
            Logger.write("e解析 ForStmt 成功");
            return new ForStmt(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 ForStmt 失败");
            throw e;
        }
    }

    @Override
    public void compile() {
        for (TreeNode node: children) {
            node.compile();
        }
                
    }
}
