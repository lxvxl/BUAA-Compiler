package parser.nodes;

import error.ErrorHandler;
import error.ParsingFailedException;
import ident.RepeatDefException;
import ident.SymbolTable;
import ident.idents.Var;
import intermediateCode.CodeGenerator;
import intermediateCode.instructions.AllocaInst;
import intermediateCode.instructions.StoreInst;
import intermediateCode.instructions.WordInst;
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

import static java.lang.Integer.parseInt;

public class VarDef implements TreeNode {
    private final List<TreeNode> children;

    private VarDef(List<TreeNode> children) {
        this.children = children;
    }

    //VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    public static VarDef parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        lm.mark();
        Logger.write("s开始解析 VarDef");

        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
            while (lm.checkSymbol().type() == CategoryCode.LBRACK) {
                children.add(lm.getSymbolWithCategory(CategoryCode.LBRACK));
                children.add(ConstExp.parse(lm));
                SyntaxChecker.addRbrackWithCheck(children, lm);
            }
            if (lm.checkSymbol().type() == CategoryCode.ASSIGN) {
                children.add(lm.getSymbolWithCategory(CategoryCode.ASSIGN));
                children.add(InitVal.parse(lm));
            }

            lm.revokeMark();
            Logger.write("e解析 VarDef 成功");
            return new VarDef(children);
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析 VarDef 失败");
            throw e;
        }
    }

    @Override
    public void compile() {
        int dim = 0;
        String[] results = new String[2];
        for (TreeNode node: children) {
            if (node instanceof ConstExp constExp) {
                constExp.compile();
                results[dim] = SyntaxChecker.getExpReturnReg();
                dim++;
            }
        }
        //创建标识符并将其加入符号表。如果是二维数组，需要记录其单个元素的大小
        Var var;
        if (dim == 2) {
            var = new Var(((Symbol)children.get(0)).symbol(), false, dim,
                    Integer.toString((parseInt(results[1]) * 4)));
        } else {
            var = new Var(((Symbol)children.get(0)).symbol(), false, dim);
        }

        int size = switch (dim) {
            case 0 -> 4;
            case 1 -> parseInt(results[0]) * 4;
            case 2 -> parseInt(results[0]) * parseInt(results[1]) * 4;
            default -> throw new IllegalStateException("Unexpected value: " + dim);
        };

        if (CodeGenerator.isGlobal()) {
            //如果是全局变量
            if (children.get(children.size() - 1) instanceof InitVal initVal) {
                InitVal.clearInitVals();
                initVal.compile();
                CodeGenerator.addInst(new WordInst(
                        ((Symbol)children.get(0)).symbol(),
                        size,
                        InitVal.getInitVals()));
            } else {
                CodeGenerator.addInst(new WordInst(
                        ((Symbol)children.get(0)).symbol(),
                        size,
                        null));
            }
        } else {
            //如果不是全局变量
            String addrReg = CodeGenerator.generateReg() + '_' + var.getName();
            var.setAddrReg(addrReg);
            CodeGenerator.addInst(new AllocaInst(addrReg, size, dim != 0));
            if (children.get(children.size() - 1) instanceof InitVal initVal) {
                InitVal.clearInitVals();
                initVal.compile();
                List<String> initVals = InitVal.getInitVals();
                if (dim == 0) {
                    CodeGenerator.addInst(new StoreInst(initVals.get(0), addrReg, 0, null, false));
                } else {
                    for (int i = 0; i < initVals.size(); i++) {
                        CodeGenerator.addInst(new StoreInst(initVals.get(i), addrReg, 4 * i, addrReg, false));
                    }
                }
            }
        }

        try {
            SymbolTable.addIdent(var);
        } catch (RepeatDefException e) {
            ErrorHandler.putError(((Symbol)children.get(0)).lineNum(), 'b');
        }
    }
}
