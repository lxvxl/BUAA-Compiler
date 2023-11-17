package intermediateCode;

import Writer.MipsGenerator;
import ident.SymbolTable;
import ident.idents.Func;
import ident.idents.Var;
import intermediateCode.instructions.*;
import intermediateCode.optimize.BasicBlock;
import parser.nodes.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record FuncCode(String name, List<Inst> insts) {
    private static final boolean peepholeOp = true;

    public void optimize() {
        HashMap<String, BasicBlock> labelToBlock = new HashMap<>();
        List<BasicBlock> basicBlocks = new ArrayList<>();
        generateBasicBlocks(basicBlocks, labelToBlock);
        insts.clear();
        for (BasicBlock block : basicBlocks) {
            block.dagOptimize();
        }

        boolean isFinished = false;
        while (!isFinished) {
            isFinished = true;
            for (int i = basicBlocks.size() - 1; i >= 0; i--) {
                BasicBlock curBlock = basicBlocks.get(i);
                for (String label : curBlock.getNextBlock()) {
                    if (curBlock.addUsefulVar(labelToBlock.get(label).getInUsefulVar())) {
                        isFinished = false;
                    }
                }
                if (curBlock.generateInUsefulVar()) {
                    isFinished = false;
                }
            }
        }
        basicBlocks.forEach(BasicBlock::removeUnusedVar);

        basicBlocks.forEach(b -> insts.addAll(b.getInsts()));
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
        for (Inst inst : insts) {
            System.out.println((inst instanceof Label ? "" : "\t") + inst);
        }
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

    /**
     * 生成基本快
     */
    public void generateBasicBlocks(List<BasicBlock> basicBlocks, HashMap<String, BasicBlock> labelToBlock) {
        Map<String, String> regMap = new HashMap<>();
        Func func = (Func) SymbolTable.searchIdent(name);
        func.getParams().forEach(var -> regMap.put(var.getAddrReg(), var.getAddrReg()));
        BasicBlock curBlock = new BasicBlock(name, regMap);
        labelToBlock.put(name, curBlock);
        for (Inst inst : insts) {
            if (inst instanceof Label label) {
                if (curBlock != null) {
                    curBlock.setNextBlock(label.label());
                    basicBlocks.add(curBlock);
                }
                curBlock = new BasicBlock(label.label(), regMap);
                labelToBlock.put(label.label(), curBlock);
                curBlock.addInst(inst);
            } else if (inst instanceof JumpInst jumpInst) {
                if (curBlock == null) {
                    continue;
                }
                curBlock.setNextBlock(jumpInst.label());
                curBlock.addInst(inst);
                basicBlocks.add(curBlock);
                curBlock = null;
            } else if (inst instanceof BrInst brInst) {
                if (curBlock == null) {
                    continue;
                }
                curBlock.setNextBlock(brInst.trueLabel(), brInst.falseLabel());
                curBlock.addInst(inst);
                basicBlocks.add(curBlock);
                curBlock = null;
            } else {
                if (curBlock == null) {
                    continue;
                }
                curBlock.addInst(inst);
            }
        }
        if (curBlock != null && !curBlock.getInsts().isEmpty()) {
            basicBlocks.add(curBlock);
        }
    }


}
