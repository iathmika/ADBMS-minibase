package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;
import ed.inf.adbs.minibase.operators.SumOperator;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private Head head;
    private RelationalAtom headAtom;
    //private RelationalAtom head;

    private List<Atom> body;

    public Query(Head head, List<Atom> body) {
        this.head = head;
        this.body = body;
    }
   /* public Query(RelationalAtom head, List<Atom> body){
        this.headAtom = head;
        //this.head = (Head) head;
        this.body = body;
    }

    */

    public Head getHead() {
        return head;
    }
    public RelationalAtom getHeadAtom() {
        try {
            List<Term> terms = new ArrayList<>();
            List<Variable> vars = getHead().getVariables();
            SumAggregate sumAggr = getHead().getSumAggregate();

            for (int i = 0; i < vars.size(); i++) {
                terms.add((Term) vars.get(i));
            }
            if (sumAggr != null) {
                Term sumA = new SumAggregateTerm(sumAggr.getProductTerms().get(0).toString());
                terms.add(sumA);
            }

            this.headAtom = new RelationalAtom(getHead().getName(), terms);


        }
        catch (NullPointerException e) { e.printStackTrace(); }
        return this.headAtom;
    }

    public List<Atom> getBody() {
        return body;
    }

    @Override
    public String toString() {
        return head + " :- " + Utils.join(body, ", ");
    }
}
