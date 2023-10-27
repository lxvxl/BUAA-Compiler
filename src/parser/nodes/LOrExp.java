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

public class LOrExp implements TreeNode {
    private final List<TreeNode> children;

    private LOrExp(List<TreeNode> children) {
        this.children = children;
    }

    private LOrExp(LAndExp lAndExp) {
        this.children = new ArrayList<>();
        children.add(lAndExp);
    }

    private LOrExp(LOrExp lOrExp, Symbol op, LAndExp lAndExp){
        this.children = new ArrayList<>();
        children.add(lOrExp);
        children.add(op);
        children.add(lAndExp);
    }

    //LOrExp → LAndExp | LOrExp '||' LAndExp
    public static LOrExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 LOrExp");
        try {
            children.add(LAndExp.parse(lm));
            while (checkOp(lm)) {
                children.add(lm.getSymbol());
                children.add(LAndExp.parse(lm));
            }
            if (children.size() == 1) {
                lm.revokeMark();
                Logger.write("e解析 LOrExp 成功");
                return new LOrExp(children);
            }

            LOrExp lOrExp = new LOrExp((LAndExp) children.get(0));
            children.remove(0);
            while (children.size() > 0) {
                lOrExp = new LOrExp(lOrExp, (Symbol) children.get(0), (LAndExp) children.get(1));
                children.remove(0);
                children.remove(0);
            }
            lm.revokeMark();
            Logger.write("e解析 LOrExp 成功");
            return lOrExp;
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 LOrExp 失败");
            throw e;
        }
    }

    private static boolean checkOp(LexicalManager lm) {
        return lm.checkSymbol().type() == CategoryCode.OR;
    }

    @Override
    public void compile() {
        for (TreeNode node: children) {
            node.compile();
        }
                
    }
}
