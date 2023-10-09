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

public class FuncRParams implements TreeNode {
    private final List<TreeNode> children;

    private FuncRParams(List<TreeNode> children) {
        this.children = children;
    }

    //FuncRParams → Exp { ',' Exp }
    public static FuncRParams parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 FuncRParams");
        try {
            children.add(Exp.parse(lm));
            while (lm.checkSymbol().type() == CategoryCode.COMMA) {
                children.add(lm.getSymbolWithCategory(CategoryCode.COMMA));
                children.add(Exp.parse(lm));
            }

            lm.revokeMark();
            Logger.write("e解析 FuncRParams 成功");
            return new FuncRParams(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 FuncRParams 失败");
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
