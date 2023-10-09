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

public class LAndExp implements TreeNode {
    private final List<TreeNode> children;

    private LAndExp(List<TreeNode> children) {
        this.children = children;
    }

    private LAndExp(EqExp eqExp) {
        this.children = new ArrayList<>();
        children.add(eqExp);
    }

    private LAndExp(LAndExp lAndExp, Symbol op, EqExp eqExp){
        this.children = new ArrayList<>();
        children.add(lAndExp);
        children.add(op);
        children.add(eqExp);
    }

    public static LAndExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 LAndExp");
        try {
            children.add(EqExp.parse(lm));
            while (checkOp(lm)) {
                children.add(lm.getSymbol());
                children.add(EqExp.parse(lm));
            }
            if (children.size() == 1) {
                lm.revokeMark();
                Logger.write("e解析 LAndExp 成功");
                return new LAndExp(children);
            }

            LAndExp lAndExp = new LAndExp((EqExp) children.get(0));
            children.remove(0);
            while (children.size() > 0) {
                lAndExp = new LAndExp(lAndExp, (Symbol) children.get(0), (EqExp) children.get(1));
                children.remove(0);
                children.remove(0);
            }
            lm.revokeMark();
            Logger.write("e解析 LAndExp 成功");
            return lAndExp;
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 LAndExp 失败");
            throw e;
        }
    }

    private static boolean checkOp(LexicalManager lm) {
        return lm.checkSymbol().type() == CategoryCode.AND;
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
