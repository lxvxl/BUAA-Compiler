package intermediateCode;

import Writer.MipsGenerator;
import ident.SymbolTable;
import ident.idents.Func;
import ident.idents.Var;
import intermediateCode.instructions.*;
import parser.nodes.Block;

import java.util.ArrayList;
import java.util.List;

public record FuncCode(String name, List<Inst> insts, List<BasicBlock> basicBlocks) {
    private static final boolean peepholeOp = true;

    public FuncCode(String name, List<Inst> insts) {
        this(name, insts, new ArrayList<>());
    }

    public void output() {
        Func func = (Func) SymbolTable.searchIdent(name);
        StringBuilder buffer = new StringBuilder();
        for (Var var : func.getParams()) {
            if (!buffer.isEmpty()) {
                buffer.append(' ');
            }
            buffer.append(var.getAddrReg());
        }
        System.out.printf("function %s %s (%s)\n", func.getReturnType(), func.getName(), buffer.toString());
        generateBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlock.output();
        }
        /*for (Inst inst : insts) {
            System.out.println((inst instanceof Label ? "" : "\t") + inst);
        }*/
    }

    public void toMips() {
        FrameMonitor.funcIn();
        MipsGenerator.addInst("func_" + name + ":");
        Func func = (Func) SymbolTable.searchIdent(name);
        for (int i = 0; i < func.getParams().size(); i++) {
            FrameMonitor.mapParam(func.getParams().get(i).getAddrReg(), -i);
        }
        for (int i = 0; i < insts().size(); i++) {
            Inst inst = insts.get(i);
            if (name.equals("main") && inst instanceof RetInst) {
                MipsGenerator.addInst("\tli $v0, 10");
                MipsGenerator.addInst("\tsyscall");
                continue;
            }

            if (peepholeOp) {
                if (inst instanceof CmpInst cmpInst
                        && i + 1 < insts().size()
                        && insts.get(i + 1) instanceof BrInst brInst) {
                    i++;
                    cmpInst.toMipsWithBr(brInst);
                    continue;
                }
            }

            inst.toMips();
        }
        MipsGenerator.addInst("\tmove $sp, $fp");
        MipsGenerator.addInst("\tjr $ra");
    }

    public void generateBasicBlocks() {
        BasicBlock curBlock = new BasicBlock(name);
        for (Inst inst : insts) {
            if (inst instanceof Label label) {
                if (curBlock != null) {
                    curBlock.setNextBlock(label.label());
                    basicBlocks.add(curBlock);
                }
                curBlock = new BasicBlock(label.label());
                curBlock.addInst(inst);
            } else if (inst instanceof JumpInst jumpInst) {
                curBlock.setNextBlock(jumpInst.label());
                curBlock.addInst(inst);
                basicBlocks.add(curBlock);
                curBlock = null;
            } else if (inst instanceof BrInst brInst) {
                curBlock.setNextBlock(brInst.trueLabel(), brInst.falseLabel());
                curBlock.addInst(inst);
                basicBlocks.add(curBlock);
                curBlock = null;
            } else {
                curBlock.addInst(inst);
            }
        }
        if (curBlock != null && !curBlock.getInsts().isEmpty()) {
            basicBlocks.add(curBlock);
        }
    }
}
