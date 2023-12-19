package parser;

import error.ErrorHandler;
import error.ParsingFailedException;
import intermediateCode.CodeGenerator;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;

import java.util.ArrayList;
import java.util.List;

public class SyntaxChecker {
    private static final List<String> loopHeads = new ArrayList<>();
    private static final List<String> loopTails = new ArrayList<>();
    private static String funcState = "out"; //out, void, int
    private static String expReturnReg = "0"; //上一个类exp节点计算后结果的存储寄存器

    public static void addSemicnWithCheck(List<TreeNode> children, LexicalManager lm){
        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.SEMICN));
        } catch (ParsingFailedException e) {
            children.add(new Symbol(";", CategoryCode.SEMICN, lm.checkSymbol(-1).lineNum()));
            ErrorHandler.putError(lm.checkSymbol(-1).lineNum(), 'i');
        }
    }

    public static void addRparentWithCheck(List<TreeNode> children, LexicalManager lm){
        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.RPARENT));
        } catch (ParsingFailedException e) {
            children.add(new Symbol(")", CategoryCode.RPARENT, lm.checkSymbol(-1).lineNum()));
            ErrorHandler.putError(lm.checkSymbol(-1).lineNum(), 'j');
        }
    }

    public static void addRbrackWithCheck(List<TreeNode> children, LexicalManager lm){
        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.RBRACK));
        } catch (ParsingFailedException e) {
            ErrorHandler.putError(lm.checkSymbol(-1).lineNum(), 'k');
            children.add(new Symbol("]", CategoryCode.RBRACK, lm.checkSymbol(-1).lineNum()));

        }
    }

    public static void loopIn(String headLabel, String tailLabel) {
        loopHeads.add(headLabel);
        loopTails.add(tailLabel);
    }

    public static void loopOut() {
        loopHeads.remove(loopHeads.size() - 1);
        loopTails.remove(loopTails.size() - 1);
    }

    public static String getHeadLabel() {
        return loopHeads.get(loopHeads.size() - 1);
    }

    public static String getTailLabel() {
        return loopTails.get(loopTails.size() - 1);
    }

    public static boolean isInLoop() {
        return loopHeads.size() > 0;
    }

    public static void funcIn(String funcType) {
        funcState = funcType;
    }

    public static void funcOut() {
        funcState = "out";
    }

    public static boolean isReturnValid(boolean hasExp) {
        return funcState.equals("void") && hasExp;
    }

    public static String getExpReturnReg() {
        return expReturnReg;
    }

    public static void setExpReturnReg(String reg) {
        expReturnReg = reg;
    }

}
