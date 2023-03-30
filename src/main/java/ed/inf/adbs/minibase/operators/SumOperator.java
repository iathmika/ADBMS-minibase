package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.dbhelper.Tuple;

/**
 * This class contains the main logic for implementation of `SUM` operator.
 * i.e it implements a project operation with a SUM term at the end of the term list of the query head .
 * Main logic of aggregation and projection is implemented in {@link ed.inf.adbs.minibase.operators.AggregateOperator}.
 */
public class SumOperator extends AggregateOperator {

    /**
     * Call super class constructor to initialise the operator.
     * @param childOperator the child operator.
     * @param queryHead the relational atom in query head.
     */
    public SumOperator(Operator childOperator, RelationalAtom queryHead) {
        super(childOperator, queryHead);
    }

    /**
     * This method first calls {@link #aggregate()} to iterate over all child operator tuples and do aggregation.
     * After the blocking operation travels through all the child output tuples,
     * each call of this method will remove and return the first output tuple from the buffer.
     * Note: the aggregation operation and output tuple construction is implemented in {@link AggregateBuffer}.
     * @return a tuple after projection and aggregation.
     */
    @Override
    public Tuple getNextTuple() {
        // do aggregation, after the first call of this function, this will do no updates
        this.aggregate();
        // after all the output tuples from child operator are processed,
        // return the top tuple in buffer for each call of this method.
        if (this.outputBuffer.size() > 0) {
            // add the aggregation term into term list, return the generated Tuple
            return this.outputBuffer.remove(0).getSumTuple();
        } else {
            return null;
        }
    }
}
