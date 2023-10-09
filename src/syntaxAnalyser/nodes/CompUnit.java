package syntaxAnalyser.nodes;

import error.ParsingFailedException;
import lexical.LexicalManager;
import logger.Logger;
import syntaxAnalyser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompUnit implements TreeNode {
    private final List<TreeNode> children;

    private CompUnit(List<TreeNode> children) {
        this.children = children;
    }

    //CompUnit → {Decl} {FuncDef} MainFuncDef
    public static CompUnit parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析CompUnit");

        try {
            while (true) {
                try {
                    children.add(Decl.parse(lm));
                } catch (ParsingFailedException e) {
                    break;
                }
            }
            while (true) {
                try {
                    children.add(FuncDef.parse(lm));
                } catch (ParsingFailedException e) {
                    break;
                }
            }
            children.add(MainFuncDef.parse(lm));
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析CompUnit失败");
            throw e;
        }
        lm.revokeMark();
        Logger.write("e解析CompUnit成功");
        return new CompUnit(children);
    }

    @Override
    public void compile(BufferedWriter writer) {
        for (TreeNode node: children) {
            node.compile(writer);
        }
        try {
            writer.write(String.format("<%s>\n", this.getClass().getName().split("\\.")[2]));
        } catch (IOException ignored) {}
    }
}
