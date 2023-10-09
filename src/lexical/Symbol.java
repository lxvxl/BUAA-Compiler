package lexical;

import syntaxAnalyser.TreeNode;

import java.io.BufferedWriter;
import java.io.IOException;

public record Symbol(String symbol, CategoryCode type, int lineNum) implements TreeNode {
    @Override
    public String toString() {
        return type.name() + " " + symbol.toString();
    }

    @Override
    public void compile(BufferedWriter writer) {
        try {
            writer.write(this.toString() + '\n');
        } catch (IOException ignored) {}
    }
}
