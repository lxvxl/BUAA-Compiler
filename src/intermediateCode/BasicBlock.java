package intermediateCode;

import intermediateCode.instructions.Label;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    private String label;
    private List<String> nextBlock;
    private List<Inst> insts;

    public BasicBlock(String label) {
        this.label = label;
        this.nextBlock = new ArrayList<>();
        this.insts = new ArrayList<>();
    }

    public void setNextBlock(String label1, String label2) {
        nextBlock.add(label1);
        nextBlock.add(label2);
    }

    public void setNextBlock(String label) {
        nextBlock.add(label);
    }

    public void addInst(Inst inst) {
        insts.add(inst);
    }

    public void output() {
        System.out.println("==========基本块==========");
        for (Inst inst : insts) {
            System.out.println((inst instanceof Label ? "" : "\t") + inst);
        }
        System.out.println("出口：" + String.join(",", nextBlock));
    }

    public List<Inst> getInsts() {
        return insts;
    }
}
