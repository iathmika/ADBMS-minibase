package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.dbhelper.Tuple;

import java.util.List;

/**
 * This class is used in {@link SelectOperator},
 * providing methods for checking whether a given tuple satisfies a select condition.
 */
public class SelectCondition {
    private String op;
    private Term term1 = null;
    private int term1Idx;
    private Term term2 = null;
    private int term2Idx;

    /**
     * Initialise an instance based on an input {@link ComparisonAtom}.
     * Store the comparison operation (e.g. '=', '>') as a string,
     * Store the {@link IntegerConstant} and {@link StringConstant} operands as a copy of term,
     * The {@link Variable} operand will be stored as its index in the target tuple (represented by a variable mask).
     * @param compAtom a comparison atom that represents a select condition
     * @param variableMask the variable mask of tuples to be checked, indicates the index of variable operand
     */
    public SelectCondition(ComparisonAtom compAtom, List<String> variableMask) {
        this.op = compAtom.getOp().toString();
        // check the class of each operand, store in different formats
        if (compAtom.getTerm1() instanceof Variable) {
            this.term1Idx = variableMask.indexOf(((Variable) compAtom.getTerm1()).getName());
//            System.out.println("Term 1 is Variable, at relation index: " + this.term1Idx);
        } else {
            this.term1 = compAtom.getTerm1();
//            System.out.println("Term 1 is Constant: " + this.term1);
        }
        if (compAtom.getTerm2() instanceof Variable) {
            this.term2Idx = variableMask.indexOf(((Variable) compAtom.getTerm2()).getName());
//            System.out.println("Term 2 is Variable, at relation index: " + this.term2Idx);
        } else {
            this.term2 = compAtom.getTerm2();
//            System.out.println("Term 2 is Constant: " + this.term2);
        }
    }

    /**
     * Check whether an input tuple satisfies the select condition.
     * @param tuple tuple to be checked.
     * @return {@code true} if it satisfies the condition; {@code false} otherwise.
     */
    public boolean check(Tuple tuple) {
        // get the variable operand from input tuple, get the Constant operand from stored Term instances
        Term operand1 = this.term1 == null ? tuple.getTerms().get(term1Idx) : this.term1;
        Term operand2 = this.term2 == null ? tuple.getTerms().get(term2Idx) : this.term2;

        // interpret the string comparison operator and inspect the input tuple
        if (this.op.equals("=")) {
            return operand1.equals(operand2);
        } else if (this.op.equals("!=")) {
            return (!operand1.equals(operand2));
        } else if (this.op.equals(">")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() > ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) > 0;
        } else if (this.op.equals(">=")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() >= ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) >= 0;
        } else if (this.op.equals("<")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() < ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) < 0;
        } else if (this.op.equals("<=")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() <= ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) <= 0;
        } else {
            System.out.println("!!!! None of the if-branches is evoked in the Selection Operator !!!!");
            return false;
        }
    }
}