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
    /*
    public void dump(String outputFile) {
        try {
            PrintWriter writer = null;
            if (outputFile != null && !outputFile.equals("")) {
                writer = new PrintWriter(outputFile);
            }

            boolean isFirstLine = true;
            // use this flag to let the print of later lines to begin with a '\n' token
            // (the purpose is to remove the empty line at the end occurred when the print operations are all PrintWriter.println() )

            Tuple nextTuple = this.getNextTuple();
            while (nextTuple != null) {
                if (writer == null) {
                    System.out.println(nextTuple);
                } else {
                    if (isFirstLine) {
                        // if the current tuple is the first line of output, print it without modification
                        writer.print(nextTuple.toString());
                        isFirstLine = false;
                    } else {
                        // when the output file already has some lines, use '\n' to start a new line and then print this tuple
                        writer.print("\n" + nextTuple.toString());
                    }
                }
                nextTuple = this.getNextTuple();
            }

            if (writer!=null)
                writer.close();

        } catch (Exception e) {
            System.err.println("Exception occurred during dump operation");
            e.printStackTrace();
        }
    }
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
