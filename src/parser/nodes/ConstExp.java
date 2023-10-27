package parser.nodes;

import error.ParsingFailedException;
import lexical.LexicalManager;
import logger.Logger;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConstExp implements TreeNode {
    private final List<TreeNode> children;

    private ConstExp(List<TreeNode> children) {
        this.children = children;
    }

    public static ConstExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 ConstExp");
        try {
            children.add(AddExp.parse(lm));
            lm.revokeMark();
            Logger.write("e解析 ConstExp 成功");
            return new ConstExp(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 ConstExp 失败");
            throw e;
        }
    }

    @Override
    public void compile() {
        children.get(0).compile();
    }
}
