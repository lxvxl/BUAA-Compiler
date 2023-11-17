package parser.nodes;

import error.ParsingFailedException;
import intermediateCode.CodeGenerator;
import intermediateCode.instructions.BrInst;
import intermediateCode.instructions.Label;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;
import logger.Logger;
import parser.SyntaxChecker;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LAndExp implements TreeNode {
    private final List<TreeNode> children;
    private List<EqExp> eqExps;
    private static String ifTrueLabel;
    private static String ifFalseLabel;

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
            List<EqExp> eqExps = children
                    .stream()
                    .filter(obj -> obj instanceof EqExp)
                    .map(obj -> (EqExp) obj)
                    .toList();

            if (children.size() == 1) {
                lm.revokeMark();
                Logger.write("e解析 LAndExp 成功");
                return new LAndExp(children).setEqExps(eqExps);
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
            return lAndExp.setEqExps(eqExps);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 LAndExp 失败");
            throw e;
        }
    }

    private static boolean checkOp(LexicalManager lm) {
        return lm.checkSymbol().type() == CategoryCode.AND;
    }

    //如果为真，前往ifTrueLabel, 否则，前往ifFalseLabel
    @Override
    public void compile() {
        for (int i = 0; i < eqExps.size() - 1; i++) {
            eqExps.get(i).compile();
            String nextlabel = CodeGenerator.generateLabel();
            CodeGenerator.generateBr(SyntaxChecker.getExpReturnReg(), nextlabel, ifFalseLabel);
            CodeGenerator.addInst(new Label(nextlabel));
        }
        eqExps.get(eqExps.size() - 1).compile();
        CodeGenerator.generateBr(SyntaxChecker.getExpReturnReg(), ifTrueLabel, ifFalseLabel);
    }

    public LAndExp setEqExps(List<EqExp> eqExps) {
        this.eqExps = eqExps;
        return this;
    }

    public static void setLabel(String trueLabel, String falseLabel) {
        ifTrueLabel = trueLabel;
        ifFalseLabel = falseLabel;
    }
}
