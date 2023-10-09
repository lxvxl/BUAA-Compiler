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

public class ConstDecl implements TreeNode {
    private final List<TreeNode> children;

    private ConstDecl(List<TreeNode> children) {
        this.children = children;
    }

    //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public static ConstDecl parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        TreeNode node;
        lm.mark();
        Logger.write("s开始解析ConstDecl");

        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.CONSTTK));
            children.add(BType.parse(lm));
            children.add(ConstDef.parse(lm));
            while (lm.checkSymbol().type() == CategoryCode.COMMA) {
                //?
                children.add(lm.getSymbol());
                children.add(ConstDef.parse(lm));
            }
            children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));

            lm.revokeMark();
            Logger.write("e解析ConstDecl成功");
            return new ConstDecl(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析ConstDecl失败");
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
