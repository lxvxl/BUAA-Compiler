package parser.nodes;

import com.sun.source.tree.Tree;
import error.ErrorHandler;
import error.ParsingFailedException;
import ident.SymbolTable;
import ident.idents.Func;
import ident.idents.Var;
import intermediateCode.CodeGenerator;
import intermediateCode.instructions.CallInst;
import intermediateCode.instructions.CmpInst;
import intermediateCode.instructions.SubInst;
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
    public void compile() {
        //if UnaryExp → Ident '(' [FuncRParams] ')'
        if (children.get(0) instanceof Symbol ident) {
            //查看函数是否已经定义
            Func func = (Func) SymbolTable.searchIdent(ident.symbol());
            if (func == null) {
                ErrorHandler.putError(ident.lineNum(), 'c');
                return;
            }
            //获得实参类型
            List<Integer> rParams= new ArrayList<>();
            if (children.get(2) instanceof FuncRParams) {
                ((FuncRParams)children.get(2)).getDims(rParams);
            }
            List<Var> definedParams = func.getParams();
            //查看实参形参数量是否匹配
            if (definedParams.size() != rParams.size()) {
                ErrorHandler.putError(ident.lineNum(), 'd');
                return;
            }
            //查看实参形参类型是否匹配
            for (int i = 0; i < definedParams.size(); i++) {
                if (definedParams.get(i).getDim() != rParams.get(i) && rParams.get(i) != -2) {
                    ErrorHandler.putError(ident.lineNum(), 'e');
                    return;
                }
            }
            List<String> params;
            if (children.get(2) instanceof FuncRParams funcRParams) {
                funcRParams.compile();
                params = funcRParams.getParams();
            } else {
                params = new ArrayList<>();
            }
            SyntaxChecker.setExpReturnReg(CodeGenerator.generateCall(func.getName(), params));
        } else if (children.get(0) instanceof PrimaryExp primaryExp) {
            primaryExp.compile();
        } else if (children.get(0) instanceof UnaryOp unaryOp) {
            children.get(1).compile();
            switch (unaryOp.getOp()) {
                case MINU -> {
                    String preResult = SyntaxChecker.getExpReturnReg();
                    String afterResult = CodeGenerator.generateSub("0", preResult);
                    SyntaxChecker.setExpReturnReg(afterResult);
                }
                case NOT -> {
                    String preResult = SyntaxChecker.getExpReturnReg();
                    if (preResult.charAt(0) != '%') {
                        SyntaxChecker.setExpReturnReg(Integer.toString(Integer.parseInt(preResult) == 0 ? 1 : 0));
                    } else {
                        String afterResult = CodeGenerator.generateCmp("0", preResult, "==");
                        SyntaxChecker.setExpReturnReg(afterResult);
                    }
                }
                default -> {}
            }
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
