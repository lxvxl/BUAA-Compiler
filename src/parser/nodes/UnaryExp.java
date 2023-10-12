package parser.nodes;

import com.sun.source.tree.Tree;
import error.ErrorHandler;
import error.ParsingFailedException;
import ident.SymbolTable;
import ident.idents.Func;
import ident.idents.Var;
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

public class UnaryExp implements TreeNode {
    private final List<TreeNode> children;

    private UnaryExp(List<TreeNode> children) {
        this.children = children;
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    public static UnaryExp parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 UnaryExp");

        try {
            Logger.write(lm.checkSymbol().toString() + " " + lm.checkSymbol(1).toString());
            if (lm.checkSymbol().type() == CategoryCode.IDENFR
                    && lm.checkSymbol(1).type() == CategoryCode.LPARENT) {
                //Ident '(' [FuncRParams] ')'
                children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
                children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                try {
                    children.add(FuncRParams.parse(lm));
                    //TODO 这里修改过一次，但不确定是否正确
                } catch (ParsingFailedException ignored) {}
                SyntaxChecker.addRparentWithCheck(children, lm);
            } else if (lm.checkSymbol().type() == CategoryCode.PLUS
                    || lm.checkSymbol().type() == CategoryCode.MINU
                    || lm.checkSymbol().type() == CategoryCode.NOT) {
                //UnaryOp UnaryExp
                children.add(UnaryOp.parse(lm));
                children.add(UnaryExp.parse(lm));
            } else {
                //PrimaryExp
                children.add(PrimaryExp.parse(lm));
            }
            lm.revokeMark();
            Logger.write("e解析 UnaryExp 成功");
            return new UnaryExp(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 UnaryExp 失败");
            throw e;
        }
    }

    @Override
    public void compile(BufferedWriter writer) {
        //if UnaryExp → Ident '(' [FuncRParams] ')'
        if (children.get(0) instanceof Symbol) {
            //查看函数是否已经定义
            Symbol ident = (Symbol)children.get(0);
            Func func = (Func) SymbolTable.searchIdent(ident.symbol());
            if (SymbolTable.searchIdent(ident.symbol()) == null) {
                ErrorHandler.putError(ident.lineNum(), 'c');
                return;
            } else {
                //获得实参类型
                List<Integer> rDims= new ArrayList<>();
                if (children.get(2) instanceof FuncRParams) {
                    ((FuncRParams)children.get(2)).getDims(rDims);
                }
                List<Var> definedParams = func.getParams();
                if (definedParams.size() != rDims.size()) {
                    ErrorHandler.putError(ident.lineNum(), 'd');
                    return;
                } else {
                    for (int i = 0; i < definedParams.size(); i++) {
                        if (definedParams.get(i).getDim() != rDims.get(i) && rDims.get(i) != -2) {
                            ErrorHandler.putError(ident.lineNum(), 'e');
                            break;
                        }
                    }
                }
            }
        }
        for (TreeNode node: children) {
            node.compile(writer);
        }
    }

    public int checkDim() {
        TreeNode firstChild = children.get(0);
        if (firstChild instanceof PrimaryExp) {
            return ((PrimaryExp)firstChild).checkDim();
        } else if (firstChild instanceof Symbol) {
            Func func = (Func) SymbolTable.searchIdent(((Symbol) firstChild).symbol());
            if (func == null) {
                return -2;
            } if (func.getReturnType().equals("void")) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
