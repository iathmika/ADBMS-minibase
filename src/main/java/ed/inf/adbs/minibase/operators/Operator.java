package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.dbhelper.Tuple;
import ed.inf.adbs.minibase.fileWriter.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the superclass for the implementation of all the operators in the query evaluation.
 * The query planner will build a tree of instances of classes extended from this class.
 */
public abstract class Operator {

    /**
     * records the variables
     */
    protected List<String> varList = new ArrayList<>();

    /**
     * Dump the tuples of the current query plan.
     * This method will iteratively call the {@link Operator#getNextTuple()} until reach the end.
     * The resulted tuples will be printed into specified file or console, depending on the input parameter.
     * @param outputFile the path to output file; if provided as {@code null}, this method will output to the default console.
     */
    public void dump(String outputFile) {
        FileWriter opWriter = null;
        Tuple nextTuple;

        if (OutputFileWriter.outputWriterInitialised()) {
            opWriter = OutputFileWriter.getFileWriter();
        }
        try {
            while ((nextTuple = getNextTuple()) != null) {
                if (opWriter != null) {
                    opWriter.write(nextTuple.toString() + "\n");
                } else { System.out.println(nextTuple); }
            }
            if (opWriter != null)  opWriter.flush();
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Reset the states of operator, the next {@code getNextTuple} call will return from the starting point of the output tuples.
     * This method will be overridden by all subclasses.
     */
    public abstract void reset();

    /**
     * Call this method to get the next tuple of the operator output.
     * This method will be overridden by all subclasses.
     * @return
     */
    public abstract Tuple getNextTuple();

    /**
     * Get the variable mask of current query plan node.
     * The variable mask helps the alignment of variables in new operator with the variables in output tuples of current operator.
     * @return a list of variable names, corresponding to the columns of output tuple of the current operator.
     */
    public List<String> getVarList() {
        return this.varList;
    }

}
