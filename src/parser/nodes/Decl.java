package parser.nodes;

import error.ParsingFailedException;
import lexical.LexicalManager;
import logger.Logger;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

public class Decl implements TreeNode {
    private final List<TreeNode> children;

    private Decl(List<TreeNode> children) {
        this.children = children;
    }

    //Decl → ConstDecl | VarDecl
    public static Decl parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 Decl");

        //ConstDecl
        try {
            children.add(ConstDecl.parse(lm));
            lm.revokeMark();
            Logger.write("e解析 Decl 成功");
            return new Decl(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            children.clear();
            lm.mark();
        }

        //VarDecl
        try {
            children.add(VarDecl.parse(lm));
            lm.revokeMark();
            Logger.write("e解析 Decl 成功");
            return new Decl(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 Decl 失败");
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
