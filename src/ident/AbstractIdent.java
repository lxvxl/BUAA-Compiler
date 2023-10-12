package ident;

public abstract class AbstractIdent {
    private String name;

    public AbstractIdent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
