package ident.idents;

import ident.AbstractIdent;

import java.util.List;

public class Func extends AbstractIdent {

    private String returnType;
    private List<Var> params;
    public Func(String name, String returnType, List<Var> params) {
        super(name);
        this.returnType = returnType;
        this.params = params;
    }

    public List<Var> getParams() {
        return params;
    }

    public String getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        String ret = returnType + " " + getName();
        for (Var v: params) {
            ret += " " + v.toString();
        }
        return ret;
    }
}
