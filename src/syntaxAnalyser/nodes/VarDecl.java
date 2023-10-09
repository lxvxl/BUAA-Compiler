package syntaxAnalyser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import logger.Logger;
import syntaxAnalyser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VarDecl implements TreeNode {
    private final List<TreeNode> children;

    private VarDecl(List<TreeNode> children) {
        this.children = children;
    }

    // VarDecl → BType VarDef { ',' VarDef } ';'
    public static VarDecl parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        TreeNode node;
        lm.mark();
        Logger.write("s开始解析 VarDecl");

        try {
            children.add(BType.parse(lm));
            children.add(VarDef.parse(lm));
            while (lm.checkSymbol().type() == CategoryCode.COMMA) {
                children.add(lm.getSymbolWithCategory(CategoryCode.COMMA));
                children.add(VarDef.parse(lm));
            }
            children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));

            lm.revokeMark();
            Logger.write("e解析 VarDecl 成功");
            return new VarDecl(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 VarDecl 失败");
            throw e;
        }
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
