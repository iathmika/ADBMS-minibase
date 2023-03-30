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
            System.out.println("Head inside Query: " + getHead());
            System.out.println("Head terms inside Query: " + getHead().getVariables());
            System.out.println("Variables inside Query: " + getHead().getSumAggregate());
            List<Variable> vars = getHead().getVariables();
            SumAggregate sumAggr = getHead().getSumAggregate();
            System.out.println("Sum Aggregate inside Q: " + sumAggr);
            for (int i = 0; i < vars.size(); i++) {
                terms.add((Term) vars.get(i));
            }
            if (sumAggr != null) {
                Term sumA = new SumAggregateTerm(sumAggr.getProductTerms().get(0).toString());
                terms.add(sumA);
            }

            this.headAtom = new RelationalAtom(getHead().getName(), terms);
            System.out.println("terms inside Query: " + terms);
            System.out.println("head atom inside Query: " + this.headAtom);

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
