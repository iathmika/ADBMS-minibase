package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * This class contains the implementation logic for Minimization of conjunctive queries
 *
 */
public class CQMinimizer {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];
        minimiseCQ(inputFile,outputFile);
    }

    /**
     * This method does the following -
     * It parses the input file to extract the Query, its head, body, etc and builds a list of allConstants
     * and allVariables present in the input query. It then runs a loop for the body of the query which checks the following -
     * - It checks whether the relational atom is composed of only constants and output variables. If yes then it can;t be
     * further minimised and hence adds it to minimised relations.
     * - For other cases it builds the varTuples map. This map contains the non output variables as keys with values as a set
     * of all relational atoms which contain this variable as one of the terms.
     * The `minimiseHelper` method is called in the end which contains the logic for minimisation. It returns the minimised relations.
     * These minimised relations are then written to the output file.
     * @param inputFile the input query file
     * @param outputFile the output file to write the minimised relations into
     */
    public static void minimiseCQ(String inputFile, String outputFile) {
            List <Atom> finalMinimisedRelations = new ArrayList<>();
            Query outputQuery;
        try {

            //Extracting the query from the  input file
            Query query = QueryParser.parse(Paths.get(inputFile));
            Head head = query.getHead();
            List<Atom> body = query.getBody();
            String QueryName = head.getName();
            List QV = head.getVariables();

            // To store all the relations in which a particular variable occurs because
            // when the variable will be substituted, all these occurrences will also have to be substituted.
            HashMap<String, Set<RelationalAtom>> varTuples = new HashMap();

            HashMap<String, HashMap<Term, Term>> mappings = new HashMap<String, HashMap<Term, Term>>();
            List<RelationalAtom> minimisedRelations = new ArrayList<>();
            List<RelationalAtom> remainingRelations = new ArrayList<>();
            List<Constant> allConstants = new ArrayList<>();
            List<Variable> allVariables = new ArrayList<>();

            for (int i = 0; i < body.size(); i++) {
                RelationalAtom R = (RelationalAtom) body.get(i);
                List<Term> terms = R.getTerms();

                //Store all constants
                allConstants = findConstants(allConstants, terms, QV);
                //Store all variables except query variables
                allVariables = findVariables(allVariables, terms, QV);
                if (isConstantOrQV(terms, QV)) {
                    minimisedRelations.add((RelationalAtom) body.get(i));
                }
                else if (isConstantAndVariable(terms, QV)) {
                    remainingRelations.add(R);
                    for (int j = 0; j < terms.size(); j++) {

                        if ((terms.get(j) instanceof Variable) && (!(QVcontains(terms.get(j), QV)))) {
                            if (!(varTuples.containsKey((terms.get(j)).toString()))) {
                                Set<RelationalAtom> vtuples = new HashSet<>();
                                vtuples.add(R);
                                varTuples.put((terms.get(j)).toString(), vtuples);
                            } else {
                                (varTuples.get(terms.get(j).toString())).add(R);

                            }
                        }
                    }
                }
                else {
                    if (termContainsQV(terms, QV)) {
                        remainingRelations.add(R);
                        for (int j = 0; j < terms.size(); j++) {
                            if(terms.get(j) instanceof Variable && !QV.contains((Variable) terms.get(j))) {
                                if (!(varTuples.containsKey(terms.get(j).toString()))) {
                                    Set<RelationalAtom> vtuples = new HashSet<>();
                                    vtuples.add(R);
                                    varTuples.put((terms.get(j)).toString(), vtuples);
                                } else {
                                    (varTuples.get(terms.get(j).toString())).add(R);
                                }
                            }
                        }
                    }
                }
            }
            finalMinimisedRelations = minimiseHelper(allVariables, allConstants, varTuples, minimisedRelations, remainingRelations, QV);

            outputQuery = new Query(head,finalMinimisedRelations);
            //Write to file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(outputQuery.toString());
            writer.close();

        }
        catch (IOException ioe) {
            ioe.printStackTrace();}
    }

    /**
     * This method is an initial implementation of checking for homomorphism of a variable with a term.
     * It builds a map with keys as variables (non output variables) and values are the terms (constants)
     * which could be possible mappings. The possibility of mapping is checked via checkHM method call.
     * For variables that have 0 mappings, (as indicated my nomappingVar) they are added to minimised relations.
     * and removed from remaining relations. If the remianingRelations is not empty, minimiseRest method is called
     * which recursively builds the the final minimised relations.
     * @param allVariables
     * @param allConstants
     * @param varTuples
     * @param minimisedRelations
     * @param remainingRelations
     * @param QV
     * @return minimsedRelations.
     */

    public static List<RelationalAtom> minimiseHelper(List<Variable> allVariables,
                                                List<Constant> allConstants,
                                                HashMap<String, Set<RelationalAtom>> varTuples,
                                                List<RelationalAtom> minimisedRelations,
                                                List<RelationalAtom> remainingRelations,
                                                List<Variable> QV) {
        HashMap<String, List<Term>> mappings = new HashMap<>();

        for (int i = 0; i < allVariables.size(); i++) {
            List<Term> termlist = new ArrayList<>();
            mappings.put(allVariables.get(i).toString(), termlist);
            Variable currVar = allVariables.get(i);
            for (int j = 0; j < allConstants.size(); j++) {
                if (checkHM(remainingRelations, minimisedRelations, currVar, allConstants.get(j), varTuples, QV)) {
                    mappings.get(currVar.toString()).add(allConstants.get(j));
                }
            }
        }
        //handling mapping with 0 and 1 values first
        List<Variable> nomappingsVar = new ArrayList<>();
        for (int i = 0; i < remainingRelations.size(); i++) {
            RelationalAtom ra = remainingRelations.get(i);
            List<Term> terms = ra.getTerms();
            for (int j = 0; j < terms.size(); j++) {
                if (terms.get(j) instanceof Variable && !QVcontains(terms.get(j), QV)) {
                    if (nomappingsVar.contains((Variable) terms.get(j))) {
                        continue;
                    }

                    if (mappings.get(((Variable) terms.get(j)).getName()).isEmpty()) {
                        nomappingsVar.add((Variable) terms.get(j));
                    }

                }
            }

            if (minimisedRelations.contains(ra)) {
                remainingRelations.remove(ra);
            }
            if (varsCantminimise(ra, nomappingsVar, allConstants, QV) && !minimisedRelations.contains(ra)) {
                minimisedRelations.add(ra);
                remainingRelations.remove(ra);
            }
        }
        if(!remainingRelations.isEmpty()) {
            RelationalAtom Ra = remainingRelations.remove(0);
            minimiseRest(Ra,remainingRelations, minimisedRelations, nomappingsVar, QV,allConstants,mappings);
        }
        return minimisedRelations;
    }

    /**
     * This method implements a backtracking approach to recursively check for every mapping if it's a valid mapping
     * and keeps removing relational atoms from the list of remainingRelations to finally build the list of
     * minimisedRelations.
     * @param ra the relational atom under consideration.
     * @param remainingRelations the list of relations not yet checked if it can be minimised.
     * @param minimisedRelations the list of relations that can't be further minimised
     * @param nomappingsVar the list of variables with no mappings possible
     * @param QV the list of output variables
     * @param allConstants the list of all constants in the input query
     * @param mappings the possible mappings for every non output variable.
     * @return
     */
    public static boolean minimiseRest(RelationalAtom ra, List<RelationalAtom> remainingRelations, List<RelationalAtom> minimisedRelations, List<Variable> nomappingsVar, List<Variable> QV, List<Constant> allConstants, HashMap<String, List<Term>> mappings) {
        // if remaining relations is empty and if current relational atom cannot be minimised, add it
        // to minimised relations.
        if (remainingRelations.isEmpty()) {
            if (!minimisedRelations.contains(ra) && varsCantminimise(ra, nomappingsVar, allConstants, QV)) {
                minimisedRelations.add(ra);
                return true;
            } else if (isConstantOrQV(ra.getTerms(), QV)) {
                if(minimisedRelations.contains(ra)) { return true; }
                else { return false; }
            } else {
                List<Term> terms = ra.getTerms();
                RelationalAtom ra2 = new RelationalAtom(ra.getName(),ra.getTerms());
                List<Term> ogterms = new ArrayList<>(terms);
                for (int i = 0; i < terms.size(); i++) {
                    if (terms.get(i) instanceof Variable) {
                        if (QVcontains(terms.get(i), QV) || nomappingsVar.contains(terms)) {
                            continue;
                        } else {
                            String v = terms.get(i).toString();
                            for (int j = 0; j < mappings.get(v).size(); j++) {
                                // map the variable one by one with the available terms to map
                                // for that variable as indicated by `mappings`.
                                terms.set(i, mappings.get(v).get(j));
                                // recursively call minimiseRest to check if relational atom ra2 can be minimised
                                // or if it can't then check if relational atom ra (without the substitution) can be minimised.
                                if (minimiseRest(ra2,remainingRelations,minimisedRelations,nomappingsVar,QV,allConstants,mappings)||
                                        minimiseRest(ra, remainingRelations, minimisedRelations, nomappingsVar, QV, allConstants, mappings)) {
                                    return true;
                                }
                            }
                            return false; }
                    }
                }
            }
        } else {
            List<Term> terms = ra.getTerms();
            if (varsCantminimise(ra, nomappingsVar, allConstants, QV) && !minimisedRelations.contains(ra)) {
                minimisedRelations.add(ra);
                return minimiseRest(remainingRelations.remove(0), remainingRelations, minimisedRelations, nomappingsVar, QV, allConstants, mappings);
            } else if (isConstantOrQV(ra.getTerms(), QV) && !minimisedRelations.contains(ra)) {
                return false;
            } else {
                boolean check = false;

                RelationalAtom ra2 = new RelationalAtom(ra.getName(),ra.getTerms());
                List<Term> terms2 = new ArrayList<>(ra2.getTerms());
                for(int i=0;i<terms.size();i++){
                    if(terms.get(i) instanceof Variable && !QVcontains(terms.get(i),QV)){
                        List <Term> values = mappings.get(terms.get(i).toString());
                        for(int k=0;k<values.size();k++){
                            terms.set(i,values.get(k));
                            List<RelationalAtom> remRelations = new ArrayList<>(remainingRelations);

                            // substitue all occurences of variable.
                            for(int j=0;j<remRelations.size();j++){
                                if(containsVariable(remRelations.get(j).getTerms(),terms.get(i))){
                                    for(int l=0;l<remRelations.get(j).getTerms().size();l++){
                                        remRelations.get(j).getTerms().set(l, values.get(k));
                                    }
                                }
                                if(minimisedRelations.contains(remRelations.get(j))){
                                    remRelations.remove(ra2); }
                                else {
                                    if(varsCantminimise(ra2,nomappingsVar,allConstants,QV) && !minimisedRelations.contains(ra2) && !isConstantOrQV(ra2.getTerms(),QV)){
                                        minimisedRelations.add(ra2);
                                        remRelations.remove(ra2);
                                    }
                                }
                            }
                            // recursively check if either ra2 can be minimised wit
                            return minimiseRest(ra2,remRelations,minimisedRelations,nomappingsVar,QV,allConstants,mappings) ||
                                    (minimiseRest(ra,remainingRelations,minimisedRelations,nomappingsVar,QV,allConstants,mappings ));
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * This method checks if a given relational atom contains variables that do not have any mappings.
     * This is checked using the nomappingsVar which contains the list of variables that cannot be mapped to any
     * of the terms.
     * @param ra the relational atom
     * @param nomappingsVar the list of variables that cannot be mapped to any term
     * @param allConstants the list of all constants in the query
     * @param QV the list of all output variables in the query
     * @return true if the Relational Atom contains variable that can't be mapped to anything, else false.
     */

    public static boolean varsCantminimise(RelationalAtom ra, List<Variable> nomappingsVar, List<Constant> allConstants, List<Variable> QV) {

        if (isConstantOrQV(ra.getTerms(),QV)) { return false; }
        List<Term> terms = ra.getTerms();
        boolean check = true;
        for (int i = 0; i < terms.size(); i++) {
            if (terms.get(i) instanceof Variable && !(QV.contains((Variable) terms.get(i)))) {
                if (nomappingsVar.contains((Variable) terms.get(i))) {
                    check = check && true;
                } else { check = false; }
            }}
        return check;
    }


    /**
     * This method checks if a homomorphism is possible by substituting a variable V with Term t
     * @param remainingRelations the relations that haven't been checked yet for minimisation
     * @param minimisedRelations the relations that are already minimised and can't be further minimised
     * @param v variable which is to be substituted
     * @param t the term that is the substitution
     * @param varTuples the map of variables mapped to a set of Relaitonal atoms in which they occur
     * @param QV the output variables' list
     * @return
     */

    public static boolean checkHM(List<RelationalAtom> remainingRelations, List<RelationalAtom> minimisedRelations, Variable v, Term t, HashMap<String, Set<RelationalAtom>> varTuples, List<Variable> QV) {
        Set<RelationalAtom> setTuples = varTuples.get(v.toString());
        Iterator<RelationalAtom> setIterator = setTuples.iterator();

        while (setIterator.hasNext()) {
            RelationalAtom RA = setIterator.next();
            String relationName = RA.getName();
            List<Term> terms = new ArrayList<>(RA.getTerms());
            for (int j = 0; j < terms.size(); j++) {
                if (terms.get(j) instanceof Variable) {
                    if ((((Variable) terms.get(j)).getName()).equals(v.getName())) {
                        terms.set(j, (Term) t);
                    }
                }
            }
            RelationalAtom RA2 = new RelationalAtom(relationName, terms);
            if (isConstantOrQV(terms, QV)) {
                if ((minimisedRelations.contains(RA2))) {
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether a list of terms contains only contants or output avriables
     * @param terms
     * @param QV
     * @return
     */
    public static boolean isConstantOrQV(List<Term> terms, List<Variable> QV) {
        //Method that checks if the tuple contains either only constants or either query variable or constant.
        // These will be preserved in the answer.
        for (int i = 0; i < terms.size(); i++) {
            if (i > terms.size() - 1) {
                return true;
            }
            if (terms.get(i) instanceof IntegerConstant || terms.get(i) instanceof StringConstant) {
                continue;
            } else if (QVcontains(terms.get(i), QV)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Method to check if the list of term contains only non query variables (non output variables).
     * These will be checked if they can be further reduced.
     * @param terms
     * @param QV
     * @return true if it contains non output variables else false
     */
    public static boolean isConstantAndVariable(List<Term> terms, List<Variable> QV) {
        //
        for (int i = 0; i < terms.size(); i++) {

            if (i > terms.size() - 1) {
                return true;
            }
            if (((terms.get(i) instanceof Variable) && !(QVcontains((Variable) terms.get(i), QV)))
                    || (terms.get(i) instanceof Constant)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * This method checks if a list of term contains any output variable
     * @param terms
     * @param QV
     * @return true if output variable is contained in the term list else returns false
     */

    public static boolean termContainsQV(List<Term> terms, List<Variable> QV) {
        // Method to check if at least one of the elements in the tuple contains Query variable and
        //  rest all non query variables.
        for (int i = 0; i < terms.size(); i++) {
            if (terms.get(i) instanceof Variable && QVcontains(terms.get(i), QV)) {
                return true;
            }
        }
        return false;
    }

    public static List<Constant> findConstants(List<Constant> allConstants, List<Term> terms, List<Variable> QV) {
        for (int i = 0; i < terms.size(); i++) {
            if (terms.get(i) instanceof Constant) {
                if (!(ConstantsContain(allConstants, (Constant) terms.get(i)))) {
                    allConstants.add((Constant) terms.get(i));
                }
            }

        }
        return allConstants;
    }

    /**
     * Returns the list of all non output variables.
     * @param allVariables list of variables which is updated with the non output variables
     * @param terms A list of terms to extract non output variables
     * @param QV Ouput variables lIST
     * @return the updated allVariables
     */
    public static List<Variable> findVariables(List<Variable> allVariables, List<Term> terms, List<Variable> QV) {

        for (int i = 0; i < terms.size(); i++) {
            if (terms.get(i) instanceof Variable) {

                if (!(QVcontains(terms.get(i), QV))) {
                    if (!allVariables.contains((Variable)terms.get(i))) {
                        allVariables.add((Variable) terms.get(i));
                    }
                }
            }

        }
        return allVariables;
    }

    public static boolean QVcontains(Term t, List<Variable> QV) {
        for (int i = 0; i < QV.size(); i++) {
            if (((Variable) t).getName().equals(QV.get(i).getName()))
                return true;
        }
        return false;
    }

    public static boolean ConstantsContain(List<Constant> allConstants, Constant c) {
        for (int i = 0; i < allConstants.size(); i++) {
            if ((allConstants.get(i) instanceof IntegerConstant) && (c instanceof IntegerConstant)) {
                if (((IntegerConstant) allConstants.get(i)).getValue() == ((IntegerConstant) c).getValue()) {
                    return true;
                }
            }
            if ((allConstants.get(i) instanceof StringConstant) && (c instanceof StringConstant)) {
                if (((StringConstant) allConstants.get(i)).getValue().equals(((StringConstant) c).getValue())) {
                    return true;
                }
            }

        }
        return false;
    }

    public static boolean containsVariable(List<Term> allVariables, Term v) {

        for (int i = 0; i < allVariables.size(); i++) {
            if (allVariables.get(i) instanceof Variable && v instanceof Variable) {
                if (((Variable) allVariables.get(i)).getName().equals(((Variable) v).getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
