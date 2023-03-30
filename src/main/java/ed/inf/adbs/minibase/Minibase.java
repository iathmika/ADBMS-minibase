package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.fileWriter.OutputFileWriter;
import ed.inf.adbs.minibase.operators.*;
import ed.inf.adbs.minibase.parser.QueryParser;
import ed.inf.adbs.minibase.dbhelper.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory database system
 *
 */
public class Minibase {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: Minibase database_dir input_file output_file");
            return;
        }

        String databaseDir = args[0] + "/";
        String inputFile = args[1];
        String outputFile = args[2];

        evaluateCQ(databaseDir, inputFile, outputFile);
    }

    public static void evaluateCQ(String databaseDir, String inputFile, String outputFile) {
        try {
            DatabaseCatalog dbcat = DatabaseCatalog.getInstance();
            dbcat.init(databaseDir);

            Query query = QueryParser.parse(Paths.get(inputFile));

            // Build the query plan tree for the input query,
            // then execute the {@link Operator#dump(String)} method on root to get the query result
            Operator queryPlan = buildQueryPlan(query);
            if (queryPlan != null) {
                OutputFileWriter.initialiseOutputWriter(outputFile);
                queryPlan.dump(outputFile);
            } else {
                System.out.println("-- Empty query --");
            }

        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

    /**
     * Build a query plan (as a left-deep join tree of {@link Operator} instances) for the input query.
     * The {@code RelationalAtom} in the query body will be processed from left to right,
     * building a tree in a Post-Order Traversal.
     * For each {@code RelationalAtom}:
     *      (1) Generate a {@link ScanOperator} for its target relation;
     *      (2) Generate a {@link SelectOperator} above it, depending on the {@code ComparisonAtom} related to it;
     *      (3) Join the roots of current subtree and the previous subtree on the right, using a {@link JoinOperator}.
     * After all the body atoms are processed, one of {@link ProjectOperator}/{@link SumOperator}
     * will be built above the root of previous tree.
     * @param query a {@link Query} instance, represents a input query.
     * @return the root of the query plan tree (whose nodes are {@link Operator} instances) of input query.
     */
    private static Operator buildQueryPlan(Query query) {
        // Use two list to store RelationAtoms and ComparisonAtoms separately
        // Later the script will build a new tree branch (starting from ScanOperator) for each RelationalAtom,
        // and find the relative ComparisonAtoms for each ComparisonAtom, using these conditions in SelectOperator and JoinOperator.
        List<RelationalAtom> relationalAtoms = new ArrayList<>();
        List<ComparisonAtom> selectConditions = new ArrayList<>();

        // Get the list of appeared variable names
        List<String> usedVariables = new ArrayList<>();
        for (Atom atom : query.getBody()) {
            // the variables in ComparisonAtom are permitted to appear in some RelationalAtom
            // so no need to check the variables in ComparisonAtom
            if (atom instanceof RelationalAtom) {
                for (Term term : ((RelationalAtom) atom).getTerms()) {
                    if (term instanceof Variable && !usedVariables.contains(((Variable) term).getName()))
                        usedVariables.add(((Variable) term).getName());
                }
            }
        }

        // Split the body atoms into two groups: RelationalAtom and ComparisonAtom
        // Replace the constants in RelationalAtom with new variable, and add a corresponding ComparisonAtom
        for (Atom atom : query.getBody()) {
            if (atom instanceof RelationalAtom) { // this branch handle relationalAtom
                List<Term> termList = ((RelationalAtom) atom).getTerms();
                String relationName = ((RelationalAtom) atom).getName();
                for (int i = 0; i < termList.size(); i++) {
                    Term originalTerm = termList.get(i);
                    // if the RelationalAtom contains Constant, replace them by new Variable and corresponding ComparisonAtom
                    if (originalTerm instanceof Constant) {
                        String newVarName = generateNewVariableName(usedVariables);
                        termList.set(i, new Variable(newVarName));
                        selectConditions.add(new ComparisonAtom(
                                new Variable(newVarName),
                                originalTerm,
                                ComparisonOperator.fromString("=")
                        ));
                    }
                }
                relationalAtoms.add(new RelationalAtom(relationName, termList));
            } else { // this branch handles ComparisonAtom
                selectConditions.add((ComparisonAtom)atom);
            }
        }

        // Generate the query plan tree
        Operator root = null;
        List<String> previousVariables = new ArrayList<>();
        for (RelationalAtom rAtom : relationalAtoms) {
            // subtreeVariables: Stores the appeared variable names in the previous built subtree,
            // it will be updated after each RelationalAtom is processed (i.e. the variables in it will be added into this list).
            // We may check whether some variables in ComparisonAtom are recorded in the list
            // to determine whether a join condition is applicable on previous subtree and a new branch for RelationalAtom.
            List<String> subtreeVariables = new ArrayList<>();
            for (Term term : rAtom.getTerms()) {
                if (term instanceof Variable) subtreeVariables.add(((Variable) term).getName());
            }

            // Scan operation
            Operator subtree = new ScanOperator(rAtom);

            // Select operation
            List<ComparisonAtom> selectCompAtomList = new ArrayList<>();
            for (ComparisonAtom cAtom : selectConditions)
                if (variableAllAppeared(cAtom, subtreeVariables))
                    selectCompAtomList.add(cAtom);
            subtree = new SelectOperator(subtree, selectCompAtomList);

            // Join operation
            List<String> mergedVariables = new ArrayList<>();
            mergedVariables.addAll(previousVariables);
            mergedVariables.addAll(subtreeVariables);
            if (root == null) {
                // if this is the first branch of query plan tree, record it as root
                root = subtree;
            } else {
                // if before this branch starting from the current RelationalAtom,
                // there already exists a subtree at left side,
                // apply JoinOperator on their roots.
                List<ComparisonAtom> joinCompAtomList = new ArrayList<>();
                for (ComparisonAtom cAtom : selectConditions) {
                    if (!variableAllAppeared(cAtom, previousVariables) &&
                            !variableAllAppeared(cAtom, subtreeVariables) &&
                            variableAllAppeared(cAtom, mergedVariables))
                        joinCompAtomList.add(cAtom);
                }
                root = new JoinOperator(root, subtree, joinCompAtomList);
            }

            // update variable list after two subtrees are joined
            previousVariables = mergedVariables;
        }

        // Project operation & Aggregation operations
        Head head = query.getHead();
        if(head.getSumAggregate() != null)
        {
            root = new SumOperator(root, query.getHeadAtom());
        }
        else {

            root = new ProjectOperator(root, query.getHeadAtom());
        }
        return root;
    }

    /**
     * Generate a new variable name that has not been used in RelationalAtoms.
     * The new variable will be used to replace the Constant in some RelationalAtom.
     * The logic is name the new variable in the form of "var" + id (e.g. "var1", "var2", "var3");
     * If a new variable name already exists, the id will be incremented until the variable name become novel.
     * @param usedNames a list of variable names occupied by current RelationalAtoms
     * @return a new variable name.
     */
    private static String generateNewVariableName(List<String> usedNames) {
        int count = 0;
        String newVar = "var" + String.valueOf(count);
        while (usedNames.contains(newVar)) {
            count++;
            newVar = "var" + String.valueOf(count);
        }
        usedNames.add(newVar);
        return newVar;
    }

    /**
     * Check whether the variables in a ComparisonAtom all appeared in the variable list of a subtree.
     * @param comparisonAtom a ComparisonAtom to be checked.
     * @param currentVariables a list variables that appears on a subtree of query plan tree.
     * @return {@code true} if the variables in input ComparisonAtom all appeared in input variable list, {@code false} otherwise.
     */
    private static boolean variableAllAppeared(ComparisonAtom comparisonAtom, List<String> currentVariables) {
        if (comparisonAtom.getTerm1() instanceof Variable)
            if (!currentVariables.contains(((Variable) comparisonAtom.getTerm1()).getName()))
                return false;
        if (comparisonAtom.getTerm2() instanceof Variable)
            if (!currentVariables.contains(((Variable) comparisonAtom.getTerm2()).getName()))
                return false;
        return true;
    }



    /**
     * Example method for getting started with the parser.
     * Reads CQ from a file and prints it to screen, then extracts Head and Body
     * from the query and prints them to screen.
     */

    public static void parsingExample(String filename) {
        try {
            Query query = QueryParser.parse(Paths.get(filename));
//            Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w), z < w");
//            Query query = QueryParser.parse("Q(x, w) :- R(x, 'z'), S(4, z, w), 4 < 'test string' ");

            System.out.println("Entire query: " + query);
            RelationalAtom head = query.getHeadAtom();
            System.out.println("Head: " + head);
            List<Atom> body = query.getBody();
            System.out.println("Body: " + body);
        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

}
