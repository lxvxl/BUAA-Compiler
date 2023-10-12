package parser.nodes;

import error.ParsingFailedException;
import lexical.CategoryCode;
import lexical.LexicalManager;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuncType implements TreeNode {
    private final List<TreeNode> children;

    private FuncType(List<TreeNode> children) {
        this.children = children;
    }

    //FuncType → 'void' | 'int'
    public static FuncType parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();

        if (lm.checkSymbol().type() == CategoryCode.VOIDTK) {
            children.add(lm.getSymbolWithCategory(CategoryCode.VOIDTK));

            return new FuncType(children);
        }
        if (lm.checkSymbol().type() == CategoryCode.INTTK) {
            children.add(lm.getSymbolWithCategory(CategoryCode.INTTK));

            return new FuncType(children);
        }
        throw new ParsingFailedException("在FuncType中尝试获取void或int，但都失败了。当前符号是" + lm.checkSymbol());
    }

    @Override
    public void compile(BufferedWriter writer) {
        for (TreeNode node: children) {
            node.compile(writer);
        }
        
    }

    public List<TreeNode> getChildren() {
        return children;
    }
}
