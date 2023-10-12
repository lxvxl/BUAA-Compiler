package lexical;

import error.ParsingFailedException;

import java.util.ArrayList;
import java.util.List;

public class LexicalManager {
    private final List<Symbol> symbols;
    private int p = 0;
    private List<Integer> stack = new ArrayList<>();

    public LexicalManager(String filePath) {
        Reader reader = new Reader("testfile.txt");
        LexicalAnalyser lexicalAnalyser = new LexicalAnalyser(reader);
        symbols = lexicalAnalyser.analysis();
    }

    /**
     * 获取下一个symbol，并将指针指向下一个
     * @return 返回当前symbol。若没有更多symbol，则返回End
     */
    public Symbol getSymbol() {
        if (p < symbols.size()) {
            Symbol symbol = symbols.get(p);
            p++;
            return symbol;
        } else {
            return new Symbol("", CategoryCode.END, symbols.get(symbols.size() - 1).lineNum());
        }
    }

    public Symbol checkSymbol(int offset) {
        if (p + offset < symbols.size()) {
            return symbols.get(p + offset);
        } else {
            return new Symbol("", CategoryCode.END, symbols.get(symbols.size() - 1).lineNum());
        }
    }

    public Symbol getSymbolWithCategory(CategoryCode code) throws ParsingFailedException {
        if (checkSymbol().type() != code) {
            throw new ParsingFailedException(String.format("尝试获得类型为%s的token，但是当前token为%s",
                    code.name(), checkSymbol().toString()));
        }
        return getSymbol();
    }

    public Symbol checkSymbol() {
        if (p < symbols.size()) {
            Symbol symbol = symbols.get(p);
            return symbol;
        } else {
            return new Symbol("", CategoryCode.END, symbols.get(symbols.size() - 1).lineNum());
        }
    }

    /**
     * 标记当前的位置
     */
    public void mark() {
        stack.add(p);
    }

    /**
     * 回溯到之前的位置
     */
    public void traceBack() {
        int last = stack.size() - 1;
        p = stack.get(last);
        stack.remove(last);
    }

    /**
     * 去除之前的标记
     */
    public void revokeMark() {
        stack.remove(stack.size() - 1);
    }
}
