package ed.inf.adbs.minibase.base;

/**
 * Represents a "SUM(x)" term.
 */
public class SumAggregateTerm extends AggregateTerm {

    public SumAggregateTerm(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "SUM(" + name + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SumAggregateTerm)) return false;
        return (this.name).equals(((SumAggregateTerm) obj).getVariable());
    }

}
