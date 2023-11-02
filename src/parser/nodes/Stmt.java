package parser.nodes;

import error.ErrorHandler;
import error.ParsingFailedException;
import ident.SymbolTable;
import intermediateCode.CodeGenerator;
import intermediateCode.instructions.*;
import lexical.CategoryCode;
import lexical.LexicalManager;
import lexical.Symbol;
import logger.Logger;
import parser.SyntaxChecker;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Stmt implements TreeNode {
    private final List<TreeNode> children;

    private Stmt(List<TreeNode> children) {
        this.children = children;
    }

    //Stmt → LVal '=' Exp ';'
    //| [Exp] ';'
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    //| 'break' ';'
    //| 'continue' ';'
    //| 'return' [Exp] ';'
    //| LVal '=' 'getint''('')'';'
    //| 'printf' '(' FormatString {','Exp} ')' ';'
    public static Stmt parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();

        try {
            Logger.write("进入Stmt，当前symbol为" + lm.checkSymbol().toString());
            switch (lm.checkSymbol().type()) {
                //Block
                case LBRACE:
                    children.add(Block.parse(lm));
                    lm.revokeMark();
                    return new Stmt(children);
                //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                case IFTK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.IFTK));
                    children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                    children.add(Cond.parse(lm));
                    SyntaxChecker.addRparentWithCheck(children, lm);
                    children.add(Stmt.parse(lm));
                    if (lm.checkSymbol().type() == CategoryCode.ELSETK) {
                        children.add(lm.getSymbolWithCategory(CategoryCode.ELSETK));
                        children.add(Stmt.parse(lm));
                    }
                    lm.revokeMark();
                    return new Stmt(children);
                //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                case FORTK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.FORTK));
                    children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                    try {
                        children.add(ForStmt.parse(lm));
                    } catch (ParsingFailedException ignored) {}
                    SyntaxChecker.addSemicnWithCheck(children, lm);
                    try {
                        children.add(Cond.parse(lm));
                    } catch (ParsingFailedException ignored) {}
                    SyntaxChecker.addSemicnWithCheck(children, lm);
                    try {
                        children.add(ForStmt.parse(lm));
                    } catch (ParsingFailedException ignored) {}
                    SyntaxChecker.addRparentWithCheck(children, lm);
                    children.add(Stmt.parse(lm));

                    lm.revokeMark();
                    return new Stmt(children);
                //'break' ';'
                case BREAKTK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.BREAKTK));
                    SyntaxChecker.addSemicnWithCheck(children, lm);
                    lm.revokeMark();
                    return new Stmt(children);
                //'continue' ';'
                case CONTINUETK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.CONTINUETK));
                    SyntaxChecker.addSemicnWithCheck(children, lm);
                    lm.revokeMark();
                    return new Stmt(children);
                //'return' [Exp] ';'
                case RETURNTK:
                    //Logger.write("进入分支return");
                    children.add(lm.getSymbolWithCategory(CategoryCode.RETURNTK));
                    try {
                        children.add(Exp.parse(lm));
                    } catch (ParsingFailedException ignored) {}
                    SyntaxChecker.addSemicnWithCheck(children, lm);
                    lm.revokeMark();
                    return new Stmt(children);
                //'printf''('FormatString{','Exp}')'';'
                case PRINTFTK:
                    children.add(lm.getSymbolWithCategory(CategoryCode.PRINTFTK));
                    children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                    children.add(lm.getSymbolWithCategory(CategoryCode.STRCON));
                    while (lm.checkSymbol().type() == CategoryCode.COMMA) {
                        children.add(lm.getSymbolWithCategory(CategoryCode.COMMA));
                        children.add(Exp.parse(lm));
                    }
                    SyntaxChecker.addRparentWithCheck(children, lm);
                    SyntaxChecker.addSemicnWithCheck(children, lm);

                    lm.revokeMark();
                    return new Stmt(children);
                // ';'
                case SEMICN:
                    SyntaxChecker.addSemicnWithCheck(children, lm);
                    lm.revokeMark();
                    return new Stmt(children);
                //LVal '=' Exp ';'
                //Exp ';'
                //LVal '=' 'getint''('')'';'
                default:
                    //先尝试通过Exp的方式进行解析
                    lm.mark();
                    Exp exp = Exp.parse(lm);
                    if (lm.checkSymbol().type() != CategoryCode.ASSIGN) {
                        children.add(exp);
                        SyntaxChecker.addSemicnWithCheck(children, lm);
                        lm.revokeMark();
                        lm.revokeMark();
                        return new Stmt(children);
                    }
                    lm.traceBack();

                    //LVal '=' Exp ';'
                    //LVal '=' 'getint''('')'';'
                    children.add(LVal.parse(lm));
                    children.add(lm.getSymbolWithCategory(CategoryCode.ASSIGN));
                    if (lm.checkSymbol().type() == CategoryCode.GETINTTK) {
                        children.add(lm.getSymbolWithCategory(CategoryCode.GETINTTK));
                        children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
                        SyntaxChecker.addRparentWithCheck(children, lm);
                        SyntaxChecker.addSemicnWithCheck(children, lm);
                    } else {
                        children.add(Exp.parse(lm));
                        SyntaxChecker.addSemicnWithCheck(children, lm);
                    }
                    lm.revokeMark();
                    return new Stmt(children);
            }
        } catch (ParsingFailedException e) {
            lm.traceBack();
            throw e;
        }
    }

    @Override
    public void compile() {
        TreeNode firstChild = children.get(0);

        if (firstChild instanceof LVal lVal) {
            //左值赋值语法错误
            int line = lVal.changeable();
            if (line > 0) {
                ErrorHandler.putError(line, 'h');
                SyntaxChecker.loopOut();
                return;
            }
            if (children.get(2) instanceof Exp exp) {
                //LVal '=' Exp ';'
                exp.compile();
                lVal.storeVal(SyntaxChecker.getExpReturnReg());
            } else {
                //LVal '=' 'getint''('')'';'
                lVal.storeVal(CodeGenerator.generateGetInt());
            }
        } else if (firstChild instanceof Exp exp) {
            exp.compile();
        } else if (firstChild instanceof Block block) {
            block.compile();
        } else if (firstChild instanceof Symbol firstSymbol) {
            switch (firstSymbol.type()) {
                case PRINTFTK -> {
                    //TODO 需要修改的更高效
                    String formatString = ((Symbol) children.get(2)).symbol();
                    int count = 0;
                    for (char c : formatString.toCharArray()) {
                        if (c == '%') {
                            count++;
                        }
                    }
                    List<String> params = children.stream()
                            .filter(obj -> obj instanceof Exp)
                            .map(obj -> {
                                obj.compile();
                                return SyntaxChecker.getExpReturnReg();
                            }).toList();
                    if (count != params.size()) {
                        ErrorHandler.putError(firstSymbol.lineNum(), 'l');
                    }
                    int p = 0;
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i < formatString.length() - 1; i++) {
                        char c = formatString.charAt(i);
                        if (c == '%') {
                            CodeGenerator.generatePutStr(builder.toString());
                            builder = new StringBuilder();
                            CodeGenerator.addInst(new PutIntInst(params.get(p)));
                            p++;
                            i++;
                        } else {
                            builder.append(c);
                        }
                    }
                    CodeGenerator.generatePutStr(builder.toString());
                }
                case FORTK -> {
                    //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                    //如果是for，需要提前进入block
                    SymbolTable.advanceBlockIn();

                    String headLabel = CodeGenerator.generateLabel();
                    String bodyLabel = CodeGenerator.generateLabel();
                    String tailLabel = CodeGenerator.generateLabel();
                    String endLabel = CodeGenerator.generateLabel();
                    SyntaxChecker.loopIn(tailLabel, endLabel);

                    List<TreeNode> candidates = new ArrayList<>(children.stream()
                            .filter(c -> !(c instanceof Symbol))
                            .toList());
                    if (candidates.get(0) instanceof ForStmt forStmt1) {//执行初始化
                        forStmt1.compile();
                        candidates.remove(0);
                    }
                    CodeGenerator.addInst(new JumpInst(headLabel));
                    CodeGenerator.addInst(new Label(headLabel));    //头标签
                    LOrExp.setLabel(bodyLabel, endLabel);
                    if (candidates.get(0) instanceof Cond cond) {   //条件判断
                        cond.compile();
                        candidates.remove(0);
                    }
                    CodeGenerator.addInst(new Label(bodyLabel));    //循环体标签
                    candidates.get(candidates.size() - 1).compile();//循环体执行
                    CodeGenerator.addInst(new JumpInst(tailLabel));
                    CodeGenerator.addInst(new Label(tailLabel));
                    if (candidates.get(0) instanceof ForStmt forStmt2) {//尾处理
                        forStmt2.compile();
                    }
                    CodeGenerator.addInst(new JumpInst(headLabel)); //前往头标签
                    CodeGenerator.addInst(new Label(endLabel));    //尾标签
                    SyntaxChecker.loopOut();
                }
                case BREAKTK -> {
                    if (!SyntaxChecker.isInLoop()) {
                        ErrorHandler.putError(firstSymbol.lineNum(), 'm');
                    }
                    CodeGenerator.addInst(new JumpInst(SyntaxChecker.getTailLabel()));
                }
                case CONTINUETK -> {
                    if (!SyntaxChecker.isInLoop()) {
                        ErrorHandler.putError(firstSymbol.lineNum(), 'm');
                    }
                    CodeGenerator.addInst(new JumpInst(SyntaxChecker.getHeadLabel()));
                }
                case RETURNTK -> {
                    if (SyntaxChecker.isReturnValid(children.size() == 3)) {
                        ErrorHandler.putError(firstSymbol.lineNum(), 'f');
                    }
                    if (children.size() == 3) {
                        children.get(1).compile();
                        CodeGenerator.addInst(new RetInst(SyntaxChecker.getExpReturnReg()));
                    } else {
                        CodeGenerator.addInst(new RetInst(null));
                    }
                }
                case IFTK -> {
                    //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                    String trueLabel = CodeGenerator.generateLabel();
                    String falseLabel = CodeGenerator.generateLabel();

                    LOrExp.setLabel(trueLabel, falseLabel);
                    children.get(2).compile();
                    CodeGenerator.addInst(new Label(trueLabel));
                    children.get(4).compile();
                    if (children.size() > 5) {
                        String endLabel = CodeGenerator.generateLabel();
                        CodeGenerator.addInst(new JumpInst(endLabel));
                        CodeGenerator.addInst(new Label(falseLabel));
                        children.get(6).compile();
                        CodeGenerator.addInst(new Label(endLabel));
                    } else {
                        CodeGenerator.addInst(new Label(falseLabel));
                    }
                }
            }
        }
    }

    public List<TreeNode> getChildren() {
        return children;
    }
}
