package lexical;

import error.ErrorHandler;
import lexical.Reader;


import java.util.ArrayList;
import java.util.List;

public class LexicalAnalyser {
    private final Reader reader;
    private int lineNum = 1;

    public LexicalAnalyser(Reader reader) {
        this.reader = reader;
    }

    public List<Symbol> analysis() {
        List<Symbol> symbols = new ArrayList<>();
        Symbol symbol = nextSymbol();
        while (symbol != null) {
            //System.out.println(symbol);
            symbols.add(symbol);
            symbol = nextSymbol();
        }
        return symbols;
    }

    private Symbol nextSymbol() {
        char c = preprocess();
        StringBuffer token = new StringBuffer();

        if (c == '\0') {
            return null;
        }
        if (c == '_' || Character.isLetter(c)) {
            while (c == '_' || Character.isLetter(c) || Character.isDigit(c)) {
                token.append(c);
                c = reader.getChar();
            }
            reader.unGetChar();
            String word = token.toString();
            switch (word) {
                case "main":
                    return new Symbol(word, CategoryCode.MAINTK, lineNum);
                case "const":
                    return new Symbol(word, CategoryCode.CONSTTK, lineNum);
                case "int":
                    return new Symbol(word, CategoryCode.INTTK, lineNum);
                case "break":
                    return new Symbol(word, CategoryCode.BREAKTK, lineNum);
                case "continue":
                    return new Symbol(word, CategoryCode.CONTINUETK, lineNum);
                case "if":
                    return new Symbol(word, CategoryCode.IFTK, lineNum);
                case "else":
                    return new Symbol(word, CategoryCode.ELSETK, lineNum);
                case "for":
                    return new Symbol(word, CategoryCode.FORTK, lineNum);
                case "getint":
                    return new Symbol(word, CategoryCode.GETINTTK, lineNum);
                case "printf":
                    return new Symbol(word, CategoryCode.PRINTFTK, lineNum);
                case "return":
                    return new Symbol(word, CategoryCode.RETURNTK, lineNum);
                case "void":
                    return new Symbol(word, CategoryCode.VOIDTK, lineNum);
                default:
                    return new Symbol(word, CategoryCode.IDENFR, lineNum);
            }
        }
        if (Character.isDigit(c)) {
            while (Character.isDigit(c)) {
                token.append(c);
                c = reader.getChar();
            }
            reader.unGetChar();
            return new Symbol(token.toString(), CategoryCode.INTCON, lineNum);
        }
        if (c == '"') {
            token.append(c);
            while (true) {
                c = reader.getChar();
                token.append(c);
                if (c == '"') {
                    break;
                } else if (c == '\0' || c == '\n') {
                    ErrorHandler.putError(lineNum, "未匹配的\"");
                    return null;
                } else if (c == '%') {
                    char next = reader.getChar();
                    reader.unGetChar();
                    if (next != 'd') {
                        ErrorHandler.putError(lineNum, 'a');
                    }
                } else if (c == '\\') {
                    char next = reader.getChar();
                    reader.unGetChar();
                    if (next != 'n') {
                        ErrorHandler.putError(lineNum, 'a');
                    }
                } else if (!(c == 32 || c == 33 || c >= 40 && c <= 126)) {
                    ErrorHandler.putError(lineNum, 'a');
                }
            }
            return new Symbol(token.toString(), CategoryCode.STRCON, lineNum);
        }
        switch (c) {
            case '!':
                if (reader.getChar() == '=') {
                    return new Symbol("!=", CategoryCode.NEQ, lineNum);
                } else {
                    reader.unGetChar();
                    return new Symbol("!", CategoryCode.NOT, lineNum);
                }
            case '&':
                if (reader.getChar() == '&') {
                    return new Symbol("&&", CategoryCode.AND, lineNum);
                } else {
                    ErrorHandler.putError(lineNum, "单个的&");
                    return null;
                }
            case '|':
                if (reader.getChar() == '|') {
                    return new Symbol("||", CategoryCode.OR, lineNum);
                } else {
                    ErrorHandler.putError(lineNum, "单个的|");
                    return null;
                }
            case '+':
                return new Symbol("+", CategoryCode.PLUS, lineNum);
            case '-':
                return new Symbol("-", CategoryCode.MINU, lineNum);
            case '*':
                return new Symbol("*", CategoryCode.MULT, lineNum);
            case '/':
                return new Symbol("/", CategoryCode.DIV, lineNum);
            case '%':
                return new Symbol("%", CategoryCode.MOD, lineNum);
            case '<':
                if (reader.getChar() == '=') {
                    return new Symbol("<=", CategoryCode.LEQ, lineNum);
                } else {
                    reader.unGetChar();
                    return new Symbol("<", CategoryCode.LSS, lineNum);
                }
            case '>':
                if (reader.getChar() == '=') {
                    return new Symbol(">=", CategoryCode.GEQ, lineNum);
                } else {
                    reader.unGetChar();
                    return new Symbol(">", CategoryCode.GRE, lineNum);
                }
            case '=':
                if (reader.getChar() == '=') {
                    return new Symbol("==", CategoryCode.EQL, lineNum);
                } else {
                    reader.unGetChar();
                    return new Symbol("=", CategoryCode.ASSIGN, lineNum);
                }
            case ';':
                return new Symbol(";", CategoryCode.SEMICN, lineNum);
            case ',':
                return new Symbol(",", CategoryCode.COMMA, lineNum);
            case '(':
                return new Symbol("(", CategoryCode.LPARENT, lineNum);
            case ')':
                return new Symbol(")", CategoryCode.RPARENT, lineNum);
            case '[':
                return new Symbol("[", CategoryCode.LBRACK, lineNum);
            case ']':
                return new Symbol("]", CategoryCode.RBRACK, lineNum);
            case '{':
                return new Symbol("{", CategoryCode.LBRACE, lineNum);
            case '}':
                return new Symbol("}", CategoryCode.RBRACE, lineNum);
            default:
                ErrorHandler.putError(lineNum, "未知符号" + c);
                return null;
        }
    }

    /**
     * 预处理，过滤掉注释和空白字符
     */
    private char preprocess() {
        char c;
        while (true) {
            //先找到下一个非空字符
            do {
                c = reader.getChar();
                if (c == '\n') {
                    lineNum++;
                }
            } while (Character.isWhitespace(c));

            //判断是不是'/'
            if (c == '/') {
                if (reader.getChar() == '/') {
                    do {
                        c = reader.getChar();
                    } while (c != '\n' && c != '\0');
                    if (c == '\0') {
                        return c;
                    }
                    lineNum++;
                    continue;
                } else {
                    reader.unGetChar();
                }
                if (reader.getChar() == '*') {
                    while(true) {
                        c = reader.getChar();
                        if (c == '\n') {
                            lineNum++;
                        } else if (c == '*'){
                            if (reader.getChar() == '/') {
                                break;
                            } else {
                                reader.unGetChar();
                            }
                        } else if(c == '\0') {
                            return c;
                        }
                    }
                    continue;
                } else {
                    reader.unGetChar();
                }
            }
            break;
        }
        return c;
    }
}
