package ident.idents;

import ident.AbstractIdent;

public class Var extends AbstractIdent {
    private boolean isConst;
    private int dim;

    public Var(String name, boolean isConst, int dim) {
        super(name);
        this.isConst = isConst;
        this.dim = dim;
    }

    public int getDim() {
        return dim;
    }

    public boolean isConst() {
        return isConst;
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
