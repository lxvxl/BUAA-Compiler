package parser.nodes;

import error.ParsingFailedException;
import ident.SymbolTable;
import ident.idents.Var;
import lexical.CategoryCode;
import lexical.LexicalManager;
import logger.Logger;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuncFParams implements TreeNode {
    private final List<TreeNode> children;
    private static final List<Var> params = new ArrayList<>();

    private FuncFParams(List<TreeNode> children) {
        this.children = children;
    }

    // FuncFParams → FuncFParam { ',' FuncFParam }
    public static FuncFParams parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        TreeNode node;
        lm.mark();
        Logger.write("s开始解析 FuncFParams");

        try {
            children.add(FuncFParam.parse(lm));
            while (lm.checkSymbol().type() == CategoryCode.COMMA) {
                children.add(lm.getSymbolWithCategory(CategoryCode.COMMA));
                children.add(FuncFParam.parse(lm));
            }

            lm.revokeMark();
            Logger.write("e解析 FuncFParams 成功");
            return new FuncFParams(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 FuncFParams 失败");
            throw e;
        }
    }

    @Override
    public void compile() {
        SymbolTable.advanceBlockIn();
        params.clear();
        for (TreeNode node: children) {
            node.compile();
        }
    }

    public static void addParam(Var var) {
        params.add(var);
    }

    public static List<Var> getParams() {
        return new ArrayList<>(params);
    }

    public List<TreeNode> getChildren() {
        return children;
    }
}
