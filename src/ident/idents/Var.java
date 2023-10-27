package ident.idents;

import ident.AbstractIdent;
import parser.nodes.InitVal;

import java.util.List;

public class Var extends AbstractIdent {
    private final boolean isConst;
    private final int dim;
    private boolean isGlobal = false;
    private String addrReg;

    //记得要乘4！！一个int是四个字节！！
    private final String elementSize;
    private List<String> initVal;


    public Var(String name, boolean isConst, int dim) {
        super(name);
        this.isConst = isConst;
        this.dim = dim;
        this.elementSize = "4";
    }

    public Var(String name, boolean isConst, int dim, String elementSize) {
        super(name);
        this.isConst = isConst;
        this.dim = dim;
        this.elementSize = elementSize;
    }

    public int getDim() {
        return dim;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setGlobal() {
        isGlobal = true;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setAddrReg(String addrReg) {
        this.addrReg = addrReg;
    }

    public String getAddrReg() {
        return addrReg;
    }

    public String getElementSize() {
        return elementSize;
    }

    public void setInitVal(List<String> initVal) {
        this.initVal = initVal;
    }

    public String getInitVal() {
        return initVal.get(0);
    }

    public String getInitVal(int i) {
        return initVal.get(i);
    }

    public String getInitVal(int i, int j) {
        return initVal.get(i * Integer.parseInt(elementSize) / 4 + j);
    }

    @Override
    public String toString() {
        String ret = "";
        if (isConst) {
            ret += "const ";
        }
        ret += "int ";
        ret += getName();
        for (int i = 0; i < dim; i++) {
            ret += "[]";
        }
        return ret;
    }
}
