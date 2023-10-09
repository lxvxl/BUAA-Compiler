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

public class FuncFParam implements TreeNode {
    private final List<TreeNode> children;

    private FuncFParam(List<TreeNode> children) {
        this.children = children;
    }

    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    public static FuncFParam parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 FuncFParam");

        try {
            children.add(BType.parse(lm));
            children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
            if (lm.checkSymbol().type() == CategoryCode.LBRACK) {
                children.add(lm.getSymbolWithCategory(CategoryCode.LBRACK));
                children.add(lm.getSymbolWithCategory(CategoryCode.RBRACK));
                while (lm.checkSymbol().type() == CategoryCode.LBRACK) {
                    children.add(lm.getSymbolWithCategory(CategoryCode.LBRACK));
                    children.add(ConstExp.parse(lm));
                    children.add(lm.getSymbolWithCategory(CategoryCode.RBRACK));
                }
            }
            Logger.write("e解析 FuncFParam 成功");
            lm.revokeMark();
            return new FuncFParam(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 FuncFParam 失败");
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
