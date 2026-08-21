package es.urjc.etsii.grafo.algorithms.cmsa;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Set;

/**
 * Solves, exactly or as close to exactly as possible, the sub-instance of the problem induced by restricting
 * it to a given set of solution components. This is the "Solve" step of the CMSA (Construct, Merge, Solve
 * and Adapt) algorithm, see {@link CMSA} for more details about the whole procedure.
 * <p>
 * Mork does not bundle any exact solver: implementations of this class are expected to encode the restricted
 * sub-instance and delegate to whatever exact method fits the problem and is available in the classpath,
 * for example a MIP/ILP solver such as CPLEX, Gurobi, SCIP or OR-Tools, a specialized dynamic programming
 * procedure, or, when the restricted sub-instance is small enough as it is expected in CMSA, an exhaustive
 * branch and bound search.
 * <p>
 * For guidance on how to formulate the mathematical (MIP/ILP) model of the restricted sub-instance for a
 * given problem, see:
 * Blum, C. (2024). Construct, Merge, Solve &amp; Adapt: A Hybrid Metaheuristic for Combinatorial Optimization.
 * Computational Intelligence Methods and Applications. Springer.
 * <a href="https://doi.org/10.1007/978-3-031-60103-3">...</a>
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@AlgorithmComponent
public abstract class CMSASolver<S extends Solution<S, I>, I extends Instance> {

    /**
     * Solves the sub-instance induced by restricting the problem to the given set of solution components.
     * Implementations are free to return a suboptimal solution if the time limit is reached before
     * the sub-instance is solved to optimality, but the returned solution, if any, must always be feasible.
     *
     * @param instance              original problem instance
     * @param restrictedComponents  set of solution components the returned solution is restricted to use.
     *                              Values are the same objects returned by {@link CMSAConstructive#usedComponents(Solution)}
     * @param maxDurationInMillis   maximum time budget, in milliseconds, to spend solving the sub-instance
     * @return a feasible solution built only using components in {@code restrictedComponents},
     *         or {@code null} if no feasible solution could be found in the given time budget
     */
    public abstract S solve(I instance, Set<Object> restrictedComponents, long maxDurationInMillis);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
