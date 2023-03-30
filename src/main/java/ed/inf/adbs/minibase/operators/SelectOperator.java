package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.dbhelper.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the implementation logic for filtering the tuples according to a series of
 * conditions provided by {@link ComparisonAtom}.
 * SelectOperator filters the output of its child operator so that only the tuples which satisfy all
 * the select conditions can passed.
 *
 * The check of conditions are implemented in a separate class: {@link SelectCondition}, which provides a
 * {@link SelectCondition#check(Tuple)} method to check whether a tuple satisfies a given condition.
 * The input {@code ComparisonAtom} list will be converted into a {@code SelectCondition} list,
 * then the select conditions are checked by travelling through this list and calling the {@code check()} method.
 */
public class SelectOperator extends Operator {

    private Operator child;
    private List<SelectCondition> conditions = new ArrayList<>();

    /**
     * Constructor to initialise SelectOperator.
     * It directly retrieves the variable list from child operator, since it will not be changed
     * in the select operation.
     * @param child The child operator.
     * @param compAtomList a list of SELECT conditions, as a list of {@link ComparisonAtom} instances.
     */
    public SelectOperator(Operator child, List<ComparisonAtom> compAtomList) {
        this.child = child;
        this.varList = this.child.getVarList();

        for (ComparisonAtom comparisonAtom : compAtomList) {
            this.conditions.add(new SelectCondition(comparisonAtom, this.varList));
        }
    }

    /**
     * Reset the operator status.
     * No state in this operator needs to be reset, but its child operator needs to be reset.
     */
    @Override
    public void reset() {
        this.child.reset();
    }

    /**
     * This method returns the next tuple that satisfies the SELECT conditions.
     * It iteratively fetches the next tuple from its child operator until a fetched tuple satisfies
     * ALL the SELECT conditions.
     * This function makes use of the check of SELECT conditions contained in the class {@link SelectCondition}.
     * @return the next Tuple or `null` if the child operator reaches the end
     */
    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple = this.child.getNextTuple();
        while (nextTuple != null) {
            boolean pass = true;
            for (SelectCondition condition : this.conditions) {
                if (!condition.check(nextTuple)) {
                    pass = false;
                    break;
                }
            }
            if (pass)
                return nextTuple;
            else
                nextTuple = this.child.getNextTuple();
        }
        return null;
    }

    /**
     * Unit testing SelectOperator
     * @param args These are command line arguments.
     */
    public static void main(String[] args) {
        DatabaseCatalog dbc = DatabaseCatalog.getInstance();
        dbc.init("data/evaluation/db");

        List<Term> queryAtomTerms = new ArrayList<>();
        queryAtomTerms.add( new IntegerConstant(9));
        queryAtomTerms.add( new Variable("x"));
        queryAtomTerms.add( new Variable("y"));
        RelationalAtom queryAtom = new RelationalAtom("R", queryAtomTerms); // R:(9, x, y)
        //System.out.println("Query relational atom: " + queryAtom);

        ScanOperator scanOp = new ScanOperator(queryAtom);

        List<ComparisonAtom> compAtomList = new ArrayList<>();
        ComparisonAtom compAtom1 = new ComparisonAtom(
                new Variable("x"), new IntegerConstant(5), ComparisonOperator.fromString("<")); // x < 5
        compAtomList.add(compAtom1);
        ComparisonAtom compAtom2 = new ComparisonAtom(
                new Variable("y"), new StringConstant("mlpr"), ComparisonOperator.fromString(">=")); // y > "mlpr"
        compAtomList.add(compAtom2);
        SelectOperator seleOp = new SelectOperator(scanOp, compAtomList);
        seleOp.dump(null);
    }

}
