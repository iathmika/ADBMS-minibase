package ed.inf.adbs.minibase.base;
/**
 * This class is created to represent the terms with aggregation operations in query head.
 */
public class AggregateTerm extends Term {

    protected String name;

    public AggregateTerm(String name) {
        this.name = name;
    }

    public String getVariable() {
        return name;
    }

}