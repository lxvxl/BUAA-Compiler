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

public class AddExp implements TreeNode {
    private final List<TreeNode> children;

    private AddExp(List<TreeNode> children) {
        this.children = children;
    }

    private AddExp(MulExp mulExp) {
        this.children = new ArrayList<>();
        children.add(mulExp);
    }

    private AddExp(AddExp addExp, Symbol op, MulExp mulExp){
        this.children = new ArrayList<>();
        children.add(addExp);
        children.add(op);
        children.add(mulExp);
    }

    // AddExp → MulExp | AddExp ('+' | '−') MulExp
    public static AddExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始尝试解析addexp");

        try {
            children.add(MulExp.parse(lm));
            while (checkOp(lm)) {
                children.add(lm.getSymbol());
                children.add(MulExp.parse(lm));
            }
            if (children.size() == 1) {
                lm.revokeMark();
                Logger.write("e解析addexp成功");
                return new AddExp(children);
            }

            AddExp addExp = new AddExp((MulExp) children.get(0));
            children.remove(0);
            while (children.size() > 0) {
                addExp = new AddExp(addExp, (Symbol) children.get(0), (MulExp) children.get(1));
                children.remove(0);
                children.remove(0);
            }
            lm.revokeMark();
            Logger.write("e解析addexp成功");
            return addExp;
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析addexp失败");
            throw e;
        }
    }

    private static boolean checkOp(LexicalManager lm) {
        return lm.checkSymbol().type() == CategoryCode.PLUS
                || lm.checkSymbol().type() == CategoryCode.MINU;
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
