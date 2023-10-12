package parser;

import error.ErrorHandler;
import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;

import java.util.List;

public class SyntaxChecker {
    private static int loopDepth = 0;
    private static String funcState = "out"; //out, void, int

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

    public static void loopIn() {
        loopDepth++;
    }

    public static void loopOut() {
        loopDepth--;
    }

    public static boolean isInLoop() {
        return loopDepth > 0;
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
}
