package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.dbhelper.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the implementation of the project operation.
 * It selects some or ALL columns and may re-order the columns from the child relation.
 * Note that if the query head contains SumOperator it will be applied in place of this class.
 */
public class ProjectOperator extends Operator {

    private Operator child;
    private String projectionName;

    private List<Integer> projectIndices = new ArrayList<>();
    // a map from fields in new relation to child relation, indicates where to find the projection column in child tuple
    // e.g. projectIndices[0] = 2 means the first column after projection is the third column in original relation

    private List<String> reportBuffer = new ArrayList<>();
    // a buffer of all the reported tuples, used for duplication check

    /**
     * This is the constructor to Initialise the project operator.
     * This constructor extract the target variable mask from the query head, and then
     * builds a map of : relation from indices after projection -> corresponding indices before projection.
     * @param childOperator the child operator.
     * @param queryHead the relational atom representing the query head
     */
    public ProjectOperator(Operator childOperator, RelationalAtom queryHead) {
        this.child = childOperator;
        List<String> childVariableMask = childOperator.getVarList(); // the variableMask before projection
        this.projectionName = queryHead.getName();
        // For each variable in the relational atom of query head, find the corresponding position in child relation,
        // and build a mapping relation from the target index (after projection) to original index
        for (int i = 0; i < queryHead.getTerms().size(); i++) {
            String varName = ((Variable)queryHead.getTerms().get(i)).getName();
            int idx = childVariableMask.indexOf(varName);
            this.projectIndices.add(idx);
            this.varList.add(varName);
            // this.variableMask will record the variable positions after projection
        }
    }

    /**
     * Reset the child operator, and also clean the report buffer.
     */
    @Override
    public void reset() {
        this.child.reset();
        this.reportBuffer = new ArrayList<>();
    }

    /**
     * Method to get the next output tuple from child operator,
     * This uses projectIndices to map the original tuple to the projected tuple,
     * checks for duplication using reportBuffer before returning.
     * @return the next projected tuple (without duplication).
     */
    @Override
    public Tuple getNextTuple() {
        Tuple childOutput = this.child.getNextTuple();
        while (childOutput != null) {
            // Use the map to construct projected tuple from original tuple by aligning indices
            List<Term> termList = new ArrayList<>();
            for (int pi : this.projectIndices) {
                termList.add(childOutput.getTerms().get(pi));
            }
            // Construct a new tuple and also check duplication
            Tuple newTuple = new Tuple(this.projectionName, termList);
            if (!this.reportBuffer.contains(newTuple.toString())) {
                this.reportBuffer.add(newTuple.toString());
                return newTuple;
            }
            // If this new tuple duplicates with some previous reported tuple, iterate to the next child output tuple
            childOutput = this.child.getNextTuple();
        }
        return null;
    }

    /**
     * Testing ProjectOperator, output is printed to console.
     * @param args Command line inputs, can be empty.
     */
    public static void main(String[] args) {
        DatabaseCatalog dbc = DatabaseCatalog.getInstance();
        dbc.init("data/evaluation/db");

        List<Term> queryTerms = new ArrayList<>();
        queryTerms.add( new Variable("x"));
        queryTerms.add( new Variable("y"));
        queryTerms.add( new Variable("z"));
        RelationalAtom queryBodyAtom = new RelationalAtom("R", queryTerms); // R:(x, y, z)
        System.out.println("Query relational atom: " + queryBodyAtom);

        ScanOperator scanOp = new ScanOperator(queryBodyAtom);

        List<ComparisonAtom> compAtomList = new ArrayList<>();
        ComparisonAtom compAtom1 = new ComparisonAtom(
                new Variable("x"), new IntegerConstant(5), ComparisonOperator.fromString(">=")); // x >= 5
        compAtomList.add(compAtom1);
        ComparisonAtom compAtom2 = new ComparisonAtom(
                new Variable("z"), new StringConstant("mlpr"), ComparisonOperator.fromString(">=")); // z >= "mlpr"
        compAtomList.add(compAtom2);
        System.out.println("Query comparison atom: " + compAtom2);

        SelectOperator seleOp = new SelectOperator(scanOp, compAtomList);
        seleOp.dump(null);
        seleOp.reset();
        System.out.println("------------------------------");

        List<Term> queryHeadTerms = new ArrayList<>();
        queryHeadTerms.add( new Variable("y"));
        queryHeadTerms.add( new Variable("x"));
        RelationalAtom queryHeadAtom = new RelationalAtom("Q", queryHeadTerms);
        System.out.println(queryHeadAtom);

        ProjectOperator projOp = new ProjectOperator(seleOp, queryHeadAtom);
        projOp.dump(null);

    }
}
