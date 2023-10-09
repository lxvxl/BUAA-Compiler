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

public class RelExp implements TreeNode {
    private final List<TreeNode> children;

    private RelExp(List<TreeNode> children) {
        this.children = children;
    }

    private RelExp(AddExp addExp) {
        this.children = new ArrayList<>();
        children.add(addExp);
    }

    private RelExp(RelExp relExp, Symbol op, AddExp addExp){
        this.children = new ArrayList<>();
        children.add(relExp);
        children.add(op);
        children.add(addExp);
    }

    // AddExp → MulExp | AddExp ('+' | '−') MulExp
    public static RelExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 RelExp");
        try {
            children.add(AddExp.parse(lm));
            while (checkOp(lm)) {
                children.add(lm.getSymbol());
                children.add(AddExp.parse(lm));
            }
            if (children.size() == 1) {
                lm.revokeMark();
                Logger.write("e解析 RelExp 成功");
                return new RelExp(children);
            }

            RelExp relExp = new RelExp((AddExp) children.get(0));
            children.remove(0);
            while (children.size() > 0) {
                relExp = new RelExp(relExp, (Symbol) children.get(0), (AddExp) children.get(1));
                children.remove(0);
                children.remove(0);
            }
            lm.revokeMark();
            Logger.write("e解析 RelExp 成功");
            return relExp;
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 RelExp 失败");
            throw e;
        }
    }

    private static boolean checkOp(LexicalManager lm) {
        return lm.checkSymbol().type() == CategoryCode.GRE
                || lm.checkSymbol().type() == CategoryCode.LSS
                || lm.checkSymbol().type() == CategoryCode.LEQ
                || lm.checkSymbol().type() == CategoryCode.GEQ;
    }

    @Override
    public void compile(BufferedWriter writer) {
        for (TreeNode node : children) {
            node.compile(writer);
        }
        try {
            writer.write(String.format("<%s>\n", this.getClass().getName().split("\\.")[2]));
        } catch (IOException ignored) {}
    }
}
