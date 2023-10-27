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

public class ConstDef implements TreeNode {
    private final List<TreeNode> children;

    private ConstDef(List<TreeNode> children) {
        this.children = children;
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    public static ConstDef parse(LexicalManager lm) throws ParsingFailedException {
        List<TreeNode> children = new ArrayList<>();
        TreeNode node;
        lm.mark();
        Logger.write("s开始解析ConstDef");

        try {
            children.add(lm.getSymbolWithCategory(CategoryCode.IDENFR));
            while (lm.checkSymbol().type() == CategoryCode.LBRACK) {
                children.add(lm.getSymbolWithCategory(CategoryCode.LBRACK));
                children.add(ConstExp.parse(lm));
                SyntaxChecker.addRbrackWithCheck(children, lm);
            }
            children.add(lm.getSymbolWithCategory(CategoryCode.ASSIGN));
            children.add(ConstInitVal.parse(lm));
        } catch (ParsingFailedException e) {
            lm.traceBack();
            Logger.write("e解析ConstDef失败");
            throw e;
        }

        lm.revokeMark();
        Logger.write("e解析ConstDef成功");
        return new ConstDef(children);
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
            var = new Var(((Symbol)children.get(0)).symbol(), true, dim,
                    Integer.toString((parseInt(results[1]) * 4)));
        } else {
            var = new Var(((Symbol)children.get(0)).symbol(), true, dim);
        }
        int size = switch (dim) {
            case 0 -> 4;
            case 1 -> parseInt(results[0]) * 4;
            case 2 -> parseInt(results[0]) * parseInt(results[1]) * 4;
            default -> throw new IllegalStateException("Unexpected value: " + dim);
        };

        ConstInitVal initVal = (ConstInitVal) children.get(children.size() - 1);
        ConstInitVal.clearInitVals();
        initVal.compile();
        List<String> initVals = ConstInitVal.getInitVals();
        var.setInitVal(initVals);
        if (CodeGenerator.isGlobal()) {
            children.get(children.size() - 1).compile();
            CodeGenerator.addInst(new WordInst(
                    ((Symbol)children.get(0)).symbol(),
                    size,
                    initVals));
        } else {
            String addrReg = CodeGenerator.generateReg() + '_' + var.getName();
            var.setAddrReg(addrReg);
            CodeGenerator.addInst(new AllocaInst(addrReg, size));

            if (dim == 0) {
                CodeGenerator.addInst(new StoreInst(initVals.get(0), addrReg, 0));
            } else {
                for (int i = 0; i < initVals.size(); i++) {
                    CodeGenerator.addInst(new StoreInst(initVals.get(i), addrReg, 4 * i));
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
