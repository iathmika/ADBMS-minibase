# Advanced Database Systems Coursework Assignment


This repository contains the implementation of the **minimization** procedure for conjunctive queries and a lightweight database engine for evaluating queries called **Minibase**.

### TASK 1: Implementation of the minimization procedure for conjunctive queries.

This task is implemented in the  `CQMinimizer`  class and can be run using `src/main/java/ed/inf/adbs/minibase/CQMinimizer.java`, with `input_file`  and  `output_file`  relative paths as command line arguments.
Relevant **comments** have been added wherever felt necessary.

### TASK 2: Implementation of the iterator model and common RA operators.

Strategy for implementing the logic of Join operation is explained through the two classes below:

- **JoinCondition**: This class is used to check whether two input tuples satisfy a join condition. The JoinCondition constructor extracts the operator from the comparison atom and the indices of the operands in the variable list. Here, it is checked whether the order is reversed i.e if the left relation's variables (`leftVarList`)  does not contain the first operand, the order is reversed by setting a flag - reverseOrder.
  (Note: If the comparison atom only contain one variable, then instead of JoinOperator, select condition will be processed in `SelectOperator`.)

- **JoinOperator**: This contains the implementation for applying tuple nested loop JOIN operation on the output tuple sets  of two child operators. Two join relations containing same variables are processed automatically. Explicit join conditions like some comparison between two different variables in the two join relations separately are provided by `ComparisonAtom` in query body, which is an input parameter in the constructor.  All these join conditions (either implicit or explicit) are converted into `JoinCondition` instances,  which use the `check(Tuple, Tuple)` method to check whether a combination of left and right tuple satisfies the requirement.  
  The constructor converts the input `ComparisonAtom` list into `JoinCondition` list,  which implements specific methods for checking whether a tuple satisfies a condition.
  The `getNextTuple()` retrieves the next joined tuple from output of left and right child operators, implements an outer loop on left child tuples (and uses this.leftTuple) to track the left tuple. For a tuple in outer loop, it iterates over the tuples in the inner loop, checks the inner join conditions by checking for same variable names in two query atoms, checks the join conditions given by extra comparison atom stored in `conditions`and only if all join conditions are satisfied, constructs a new Tuple instance as join result.
  Otherwise, it checks the next right tuple and calls `reset()` on the right child operator, so the inner loop will be restarted from beginning and moves on to the next outer loop tuple.

Please also refer to the **comments** in the code for explanation.

### TASK 3: Optimisation of constructed query plans

Query optimisation is implemented in following ways:

1. Pushing Select Operators
   In the query plan, I push down all the select operators (if they exist) so that they are executed before the join operator to restrict the number of tuples retrieved early, to save the cost time of join. A complete cross product is only done if there is no other option.

2. Including SUM info in Head
   To implement Group-By aggregation, I extend Query.java, head.java so that information about SUM in the form of SumAggregate and SumAggregateTerm is contained in the query head. SumAggregateTerm extends AggregateTerm whereas sumAggregate extends Term.