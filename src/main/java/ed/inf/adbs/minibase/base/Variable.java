package ed.inf.adbs.minibase.base;

public class Variable extends Term {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Variable)) return false;
        return (this.name).equals(((Variable) obj).getName());
    }
}
