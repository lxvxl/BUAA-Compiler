package parser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;
import logger.Logger;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EqExp implements TreeNode {
    private final List<TreeNode> children;

    private EqExp(List<TreeNode> children) {
        this.children = children;
    }

    private EqExp(RelExp relExp) {
        this.children = new ArrayList<>();
        children.add(relExp);
    }

    private EqExp(EqExp eqExp, Symbol op, RelExp relExp){
        this.children = new ArrayList<>();
        children.add(eqExp);
        children.add(op);
        children.add(relExp);
    }

    public static EqExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 EqExp");

        try {
            children.add(RelExp.parse(lm));
            while (checkOp(lm)) {
                children.add(lm.getSymbol());
                children.add(RelExp.parse(lm));
            }
            if (children.size() == 1) {
                lm.revokeMark();
                Logger.write("e解析 EqExp 成功");
                return new EqExp(children);
            }

            EqExp eqExp = new EqExp((RelExp) children.get(0));
            children.remove(0);
            while (children.size() > 0) {
                eqExp = new EqExp(eqExp, (Symbol) children.get(0), (RelExp) children.get(1));
                children.remove(0);
                children.remove(0);
            }
            lm.revokeMark();
            Logger.write("e解析 EqExp 成功");
            return eqExp;
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 EqExp 失败");
            throw e;
        }
    }

    private static boolean checkOp(LexicalManager lm) {
        return lm.checkSymbol().type() == CategoryCode.EQL
                || lm.checkSymbol().type() == CategoryCode.NEQ;
    }

    @Override
    public void compile() {
        for (TreeNode node: children) {
            node.compile();
        }
                
    }
}
