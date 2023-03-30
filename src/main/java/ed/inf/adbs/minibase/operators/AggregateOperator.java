package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.dbhelper.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The main logic of aggregation is implemented in this class.
 * It contains the implementation for a project operation with a `SUM` term at the end of the list of terms of query head.
 * `SumOperator` which is a subclass of this class will need to override the getNextTuple().
 */
public abstract class AggregateOperator extends Operator {

    protected String projectionName;
    protected int aggregateIndex;
    protected Operator child;
    protected String aggregateVariable;

    // This is for indicating where to find the projection column in the child tuple.
    protected List<Integer> projectIndices = new ArrayList<>();

    protected List<AggregateBuffer> outputBuffer = new ArrayList<>();
    /* A list of AggregateBuffers to store the AggregateBuffer instances,
       Each of these aggregate buffers represent an output tuple of this operator.
       In these AggregateBuffers, the accumulation of aggregation term will be processed. */

    protected HashMap<String, Integer> tuple2BufferIndex = new HashMap<>();
    // This is a mapping from a tuple in string format to its index in outputBuffer.

    /*
      This is used to check whether a tuple without aggregation term has been encountered,
      If yes then aggregation term will be accumulated to existing record in outputBuffer
      Else a new record in outputBuffer will be inserted into outputBuffer. */


    /**
     * Constructor to initialise the Aggregate operator.
     * The last aggregation term and other normal terms are separately processed.
     * @param childOperator
     * @param queryHead the relational atom representing the head of the query
     */
    public AggregateOperator(Operator childOperator, RelationalAtom queryHead) {
        this.child = childOperator;
        List<String> childvarList = childOperator.getVarList(); // the varList before projection
        this.projectionName = queryHead.getName();
        for (int i = 0; i < queryHead.getTerms().size() - 1; i++) {
            String varName = ((Variable) queryHead.getTerms().get(i)).getName();
            int idx = childvarList.indexOf(varName);
            this.projectIndices.add(idx);
            this.varList.add(varName); // this.varList will record the variable positions after projection
        }
        // process the last aggregation term:
        this.aggregateIndex = queryHead.getTerms().size()-1;
        AggregateTerm avgTerm = ((AggregateTerm) queryHead.getTerms().get(this.aggregateIndex));
        this.aggregateVariable = avgTerm.getVariable();
        String aggVar = avgTerm.getVariable();
        int idx = childvarList.indexOf(aggVar);
        this.projectIndices.add(idx);
        this.varList.add(avgTerm.toString());
        // this.varList will record the variable positions after projection.
        if(projectIndices.size() == 1 && projectIndices.get(0) == -1) {
            projectIndices.set(0,0);
        }

    }

    /**
     * Reset child operator, buffer states and the map to buffer.
     */
    @Override
    public void reset() {
        this.child.reset();
        this.tuple2BufferIndex = new HashMap<>();
        this.outputBuffer = new ArrayList<>();
    }

    /**
     * The child class needs to override this method and do following things:
     * First call {@link #aggregate()} to iterate over all child operator tuples and do aggregation.
     * After the blocking operation travel through all the child output tuples,
     * each call of this method will remove and return the first output tuple from the buffer.
     * Notice: the aggregation operation and output tuple construction is implemented in {@link AggregateBuffer}.
     * @return a tuple after projection and aggregation.
     */
    @Override
    abstract public Tuple getNextTuple();

    /**
     * Iterate over all the output tuples from child operator, do aggregation operation over them.
     * For each child operator tuple:
     * 1. extract the term list and remove the aggregation term.
     * 2. The rest of term list will be converted to a string as a key in {@code tuple2BufferIndex}.
     * 3. Check key duplication to see if it needs a GROUP operation:
     *  If a tuple without aggregation term has already been recorded, a GROUP operation is required,
     *  and the new tuple will be merged into the existing record, i.e. the new aggregation term will be
     *  accumulated on the existing record.
     *  Otherwise, a new buffer record will be created for the new tuple.
     */
    protected void aggregate() {
        Tuple childOutput = this.child.getNextTuple();
        while (childOutput != null) {
            // extract the term list and remove the aggregation term
            List<Term> termList = new ArrayList<>();
            System.out.println("projectIndices: "+projectIndices);
            for (int pi : this.projectIndices) {
                termList.add(childOutput.getTerms().get(pi));
            }
            Tuple newTuple = new Tuple(this.projectionName, termList);
            IntegerConstant aggTerm = (IntegerConstant) newTuple.getTerms().remove(this.aggregateIndex);

            // convert the term list (without aggregation term) into string, acting as a key for hashmap
            String bufferKey = newTuple.getTerms().toString();
            if (this.tuple2BufferIndex.containsKey(bufferKey)) {
                // GROUP operation, accumulate the aggregation term
                int bufferIndex = this.tuple2BufferIndex.get(bufferKey);
                this.outputBuffer.get(bufferIndex).addSum(aggTerm.getValue());
            } else {
                // new tuple, create a new buffer record for it
                AggregateBuffer aggrBfr = new AggregateBuffer(newTuple.getTerms(), this.aggregateIndex, this.aggregateVariable);
                aggrBfr.addSum(aggTerm.getValue());
                this.outputBuffer.add(aggrBfr);
                this.tuple2BufferIndex.put(bufferKey, this.outputBuffer.size()-1);
            }
            childOutput = this.child.getNextTuple();
        }
    }
}
