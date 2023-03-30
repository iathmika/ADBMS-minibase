package ed.inf.adbs.minibase.dbhelper;

import ed.inf.adbs.minibase.Utils;
import ed.inf.adbs.minibase.base.Term;

import java.util.List;

/**
 * A class for storing a row/record from a relation in database.
 */
public class Tuple {
    private String relationName;
    private List<Term> terms;

    public Tuple(String relationName, List<Term> terms) {
        this.relationName = relationName;
        this.terms = terms;
    }

    public String getName() {
        return relationName;
    }

    public List<Term> getTerms() {
        return terms;
    }

    /**
     * Convert the tuple instance into print style
     * @return a String represent this tuple, columns split by ', '
     */
    @Override
    public String toString() {
        return Utils.join(terms, ", ");
    }
}
