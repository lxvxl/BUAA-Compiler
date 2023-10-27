package parser.nodes;

import error.ErrorHandler;
import error.ParsingFailedException;
import ident.RepeatDefException;
import ident.SymbolTable;
import ident.idents.Func;
import ident.idents.Var;
import intermediateCode.CodeGenerator;
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

public class FuncDef implements TreeNode {
    private final List<TreeNode> children;

    private FuncDef(List<TreeNode> children) {
        this.children = children;
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public static FuncDef parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 FuncDef");


        try {
            children.add(FuncType.parse(lm));
            children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
            children.add(lm.getSymbolWithCategory(CategoryCode.LPARENT));
            try {
                children.add(FuncFParams.parse(lm));
            } catch (ParsingFailedException ignored) {}
            SyntaxChecker.addRparentWithCheck(children, lm);
            children.add(Block.parse(lm));

            lm.revokeMark();
            Logger.write("e解析 FuncDef 成功");
            return new FuncDef(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 FuncDef 失败");
            throw e;
        }
    }

    @Override
    public void compile() {
        //添加函数定义
        String returnType = ((Symbol)((FuncType)children.get(0)).getChildren().get(0)).symbol();
        String name = ((Symbol)children.get(1)).symbol();
        List<Var> params = new ArrayList<>();

        try {
            SymbolTable.addIdent(new Func(name, returnType, params));
        } catch (RepeatDefException e) {
            ErrorHandler.putError(((Symbol)children.get(1)).lineNum(), 'b');
        }

        SyntaxChecker.funcIn(returnType);
        CodeGenerator.FuncIn(name);
        if (children.get(3) instanceof FuncFParams funcFParams) {
            funcFParams.compile();
            params.addAll(FuncFParams.getParams());
        }

        Block block = (Block)children.get(children.size() - 1);
        block.compile();
        SyntaxChecker.funcOut();

        //检查返回值相关错误
        //TODO 函数调用不能将const arr当作参数类型
        if (returnType.equals("int") && !block.containReturn()) {
            ErrorHandler.putError(((Symbol)block.getChildren().get(block.getChildren().size() - 1)).lineNum(), 'g');
        }
    }
}
