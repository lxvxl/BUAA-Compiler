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

public class ConstInitVal implements TreeNode {
    private final List<TreeNode> children;

    private ConstInitVal(List<TreeNode> children) {
        this.children = children;
    }

    //ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}
    public static ConstInitVal parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 ConstInitVal");

        //'{' [ ConstInitVal { ',' ConstInitVal } ] '}
        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.LBRACE));
            //[ ConstInitVal { ',' ConstInitVal } ]
            try {
                children.add(ConstInitVal.parse(lm));
                while (lm.checkSymbol().type() == CategoryCode.COMMA) {
                    children.add(lm.getSymbolWithCategory(CategoryCode.COMMA));
                    children.add(ConstInitVal.parse(lm));
                }
            } catch (ParsingFailedException ignored) {}
            children.add(lm.getSymbolWithCategory(CategoryCode.RBRACE));

            lm.revokeMark();
            Logger.write("e解析 ConstInitVal 成功");
            return new ConstInitVal(children);
        } catch (ParsingFailedException ignored) {
            lm.traceBack();
            children.clear();
            lm.mark();
        }

        try {
            children.add(ConstExp.parse(lm));

            lm.revokeMark();
            Logger.write("e解析 ConstInitVal 成功");
            return new ConstInitVal(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 ConstInitVal失败");
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
