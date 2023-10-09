package syntaxAnalyser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;
import logger.Logger;
import syntaxAnalyser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MulExp implements TreeNode {
    private final List<TreeNode> children;

    private MulExp(List<TreeNode> children) {
        this.children = children;
    }

    private MulExp(UnaryExp unaryExp) {
        this.children = new ArrayList<>();
        children.add(unaryExp);
    }

    private MulExp(MulExp mulExp, Symbol op, UnaryExp unaryExp){
        this.children = new ArrayList<>();
        children.add(mulExp);
        children.add(op);
        children.add(unaryExp);
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public static MulExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 MulExp");
        try {
            children.add(UnaryExp.parse(lm));
            while (checkOp(lm)) {
                children.add(lm.getSymbol());
                children.add(UnaryExp.parse(lm));
            }
            if (children.size() == 1) {
                lm.revokeMark();
                Logger.write("e解析 MulExp 成功");
                return new MulExp(children);
            }

            MulExp mulExp = new MulExp((UnaryExp) children.get(0));
            children.remove(0);
            while (children.size() > 0) {
                mulExp = new MulExp(mulExp, (Symbol) children.get(0), (UnaryExp) children.get(1));
                children.remove(0);
                children.remove(0);
            }
            lm.revokeMark();
            Logger.write("e解析 MulExp 成功");
            return mulExp;
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 MulExp 失败");
            throw e;
        }
    }

    private static boolean checkOp(LexicalManager lm) {
        return lm.checkSymbol().type() == CategoryCode.MULT
                || lm.checkSymbol().type() == CategoryCode.DIV
                || lm.checkSymbol().type() == CategoryCode.MOD;
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
