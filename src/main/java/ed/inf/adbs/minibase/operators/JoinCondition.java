package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.dbhelper.Tuple;

import java.util.List;

/**
 * This class contains the implementation for converting a ComparisonAtom into a condition instance
 * and has methods for checking whether two input tuples satisfy a join condition.
 * The two operands of input ComparisonAtom to this class can be both Variable instances.
 * Note: If a comparison atom only contain one variable, then instead of JoinOperator it must be a select condition that should be
 * processed in SelectOperator.
 */
public class JoinCondition {
    private String op; //operator
    private boolean reverseOrder = false;
    /* Suppose ComparisonAtom represents a predicate: term1 op term2
       reverseOrder is false if (term1 in leftTuple) && (term2 in rightTuple) i.e the tuple order matches operand order
       reverseOrder=true if (term1 in rightTuple) && (term2 in leftTuple) i.e the tuple order reverses operand order
    */
    private int index1; // index of operand1 in corresponding tuple (either left or right tuple depends on reverseOrder)
    private int index2; // index of operand2 in corresponding tuple

    /**
     * Constructor to initialise JoinCondition.
     * This extracts the indices of two variable operands from the corresponding tuple.
     * reverseOrder flag will be set if Order between operands and between the tuples they appeared are opposite.
     * @param compAtom the comparison atom
     * @param leftVarList
     * @param rightVarList
     */
    public JoinCondition(ComparisonAtom compAtom, List<String> leftVarList, List<String> rightVarList) {
        this.op = compAtom.getOp().toString();
        if (leftVarList.contains(((Variable) compAtom.getTerm1()).getName()) ) {
            // if the left relation contains the first operand, the order remain the same
            this.index1 = leftVarList.indexOf(((Variable) compAtom.getTerm1()).getName());
            this.index2 = rightVarList.indexOf(((Variable) compAtom.getTerm2()).getName());
        } else {
            // otherwise, the orders are reversed
            this.reverseOrder = true;
            this.index1 = rightVarList.indexOf(((Variable) compAtom.getTerm1()).getName());
            this.index2 = leftVarList.indexOf(((Variable) compAtom.getTerm2()).getName());
        }
    }

    /**
     * Method to check whether two input tuples satisfy the join condition.
     * Operands from the input tuples are extracted and then
     * depending on the state of reverseOrder flag, the order of these two operand may be reversed.
     * Then join conditions are checked on these extracted operands.
     * @param leftTuple tuple from the left child operator of Join Operator.
     * @param rightTuple tuple from the right child operator of Join Operator.
     * @return {@code true} if join condition is satisfied on these two tuples; {@code false} otherwise
     */
    public boolean check(Tuple leftTuple, Tuple rightTuple) {
        Term operand1;
        Term operand2;
        // Extract operand from input tuples, order of operands depends on the reverseOrder flag
        if (!reverseOrder) {
            operand1 = leftTuple.getTerms().get(this.index1);
            operand2 = rightTuple.getTerms().get(this.index2);
        } else {
            operand1 = rightTuple.getTerms().get(this.index1);
            operand2 = leftTuple.getTerms().get(this.index2);
        }

        // Check the join condition on extracted operands

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
            System.out.println("None of the if branches were evoked in the Selection Operator!");
            return false;
        }
    }
}
