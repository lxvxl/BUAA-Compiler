package parser.nodes;

import error.ParsingFailedException;
import ident.SymbolTable;
import lexical.CategoryCode;
import lexical.LexicalManager;
import logger.Logger;
import parser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Block implements TreeNode {
    private final List<TreeNode> children;

    private Block(List<TreeNode> children) {
        this.children = children;
    }

    // Block → '{' { BlockItem } '}'
    public static Block parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析block");

        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.LBRACE));
            while (true) {
                try {
                    children.add(BlockItem.parse(lm));
                } catch (ParsingFailedException ignored) {
                    break;
                }
            }
            children.add(lm.getSymbolWithCategory(CategoryCode.RBRACE));

            lm.revokeMark();
            Logger.write("e解析block成功");
            return new Block(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析block失败");
            throw e;
        }
    }

    @Override
    public void compile(BufferedWriter writer) {
        Logger.write("s进入block");
        SymbolTable.blockIn();
        for (TreeNode node: children) {
            node.compile(writer);
        }
        SymbolTable.blockOut();
        Logger.write("e离开block");
        
    }

    /**
     * @return 如果Block以return exp为结尾，则返回true
     */
    public boolean containReturn() {
        if (children.size() == 2) {
            return false;
        }
        return ((BlockItem)children.get(children.size() - 2)).containReturn();
    }

    public List<TreeNode> getChildren() {
        return children;
    }
}
