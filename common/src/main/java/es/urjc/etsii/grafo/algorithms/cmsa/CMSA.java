package es.urjc.etsii.grafo.algorithms.cmsa;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.StringUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * CMSA (Construct, Merge, Solve and Adapt) is a hybrid metaheuristic that combines a probabilistic
 * constructive heuristic with an exact method. Instead of applying the exact method to the whole problem
 * instance, which is usually intractable, CMSA repeatedly restricts the search to a small, promising subset
 * of solution components (edges, assignments, items, etc.), and solves that much smaller sub-instance to
 * optimality (or as close to optimality as possible) at every iteration.
 * <p>
 * Algorithmic outline:
 * <pre>
 * Csub = {} // sub-instance: solution components with a chance of being part of a good solution
 * Sbest = null
 * repeat until stopping criterion is met:
 *     // Construct &amp; Merge
 *     for na times:
 *         S' = ProbabilisticConstruct(instance) // {@link CMSAConstructive}
 *         Csub = Csub U usedComponents(S')       // merge new components into the sub-instance, age = 0
 *     // Solve
 *     S* = Solve(instance restricted to Csub)    // {@link CMSASolver}
 *     if S* is better than Sbest: Sbest = S*
 *     // Adapt
 *     for each component c in Csub:
 *         if c in S*: age(c) = 0
 *         else: age(c) = age(c) + 1
 *               if age(c) &gt; ageMax: Csub = Csub \ {c}
 * return Sbest
 * </pre>
 * Components that keep being selected by the exact method stay in {@code Csub} indefinitely (their age is
 * reset to 0), while components that are no longer useful eventually age out and are dropped, keeping the
 * sub-instance small across iterations. This is what makes CMSA different from just repeatedly solving a
 * new random restricted sub-instance from scratch: the sub-instance progressively adapts towards the
 * components that matter.
 * <p>
 * For further information about CMSA see:
 * Blum, C., Pinacho, P., López-Ibáñez, M., &amp; Lozano, J. A. (2016). Construct, Merge, Solve and Adapt:
 * A new algorithm for combinatorial optimization. Computers &amp; Operations Research, 68, 75-88.
 * <a href="https://doi.org/10.1016/j.cor.2015.10.014">...</a>
 * <p>
 * For an in-depth treatment of CMSA, including guidance on modelling the sub-instance solved at every
 * iteration for different combinatorial optimization problems, see:
 * Blum, C. (2024). Construct, Merge, Solve &amp; Adapt: A Hybrid Metaheuristic for Combinatorial Optimization.
 * Computational Intelligence Methods and Applications. Springer.
 * <a href="https://doi.org/10.1007/978-3-031-60103-3">...</a>
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class CMSA<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = LoggerFactory.getLogger(CMSA.class);

    private final Objective<?, S, I> objective;

    /**
     * Probabilistic constructive procedure used to sample solution components at every iteration.
     */
    private final CMSAConstructive<S, I> constructive;

    /**
     * Exact (or near-exact) method used to solve the restricted sub-instance at every iteration.
     */
    private final CMSASolver<S, I> solver;

    /**
     * Number of solutions probabilistically constructed at each iteration, before merging their
     * components into the sub-instance and calling the solver. Commonly denoted as {@code na} in the literature.
     */
    private final int solutionsPerIteration;

    /**
     * Maximum age a solution component can reach, while not being used by the solver, before being
     * removed from the sub-instance.
     */
    private final int ageMax;

    /**
     * Maximum time budget, in milliseconds, given to the solver at each iteration to solve the
     * restricted sub-instance. If less time than this remains in the current execution, the remaining
     * time is used instead.
     */
    private final long solverTimeLimitInMillis;

    /**
     * Maximum number of iterations to execute. Use a value smaller or equal to zero to disable this
     * limit and rely exclusively on the global time limit.
     */
    private final int maxIterations;

    /**
     * Full CMSA constructor.
     *
     * @param name                    Algorithm name, uniquely identifies the current algorithm.
     *                                Tip: If you dont care about the name, generate a random one using {@link StringUtil#randomAlgorithmName()}
     * @param objective               objective function to optimize
     * @param constructive            probabilistic constructive procedure used to sample solution components
     * @param solver                  exact (or near-exact) method used to solve the restricted sub-instance
     * @param solutionsPerIteration   number of solutions constructed at each iteration before calling the solver, usually denoted {@code na}
     * @param ageMax                  maximum age a solution component can reach before being removed from the sub-instance
     * @param solverTimeLimitInMillis maximum time budget, in milliseconds, given to the solver at each iteration
     * @param maxIterations           maximum number of iterations, use a value smaller or equal to zero to only rely on the global time limit
     */
    public CMSA(
            String name,
            Objective<?, S, I> objective,
            CMSAConstructive<S, I> constructive,
            CMSASolver<S, I> solver,
            int solutionsPerIteration,
            int ageMax,
            long solverTimeLimitInMillis,
            int maxIterations
    ) {
        super(name);
        if (solutionsPerIteration < 1) {
            throw new IllegalArgumentException("solutionsPerIteration must be greater than 0");
        }
        if (ageMax < 0) {
            throw new IllegalArgumentException("ageMax must be greater or equal to 0");
        }
        if (solverTimeLimitInMillis < 1) {
            throw new IllegalArgumentException("solverTimeLimitInMillis must be greater than 0");
        }
        this.objective = Objects.requireNonNull(objective);
        this.constructive = Objects.requireNonNull(constructive);
        this.solver = Objects.requireNonNull(solver);
        this.solutionsPerIteration = solutionsPerIteration;
        this.ageMax = ageMax;
        this.solverTimeLimitInMillis = solverTimeLimitInMillis;
        this.maxIterations = maxIterations;
    }

    @AutoconfigConstructor
    public CMSA(
            @ProvidedParam String name,
            CMSAConstructive<S, I> constructive,
            CMSASolver<S, I> solver,
            @IntegerParam(min = 1, max = 1_000) int solutionsPerIteration,
            @IntegerParam(min = 0, max = 100) int ageMax,
            @IntegerParam(min = 1, max = 60_000) long solverTimeLimitInMillis,
            @IntegerParam(min = 0, max = 1_000_000) int maxIterations
    ) {
        this(name, Context.getMainObjective(), constructive, solver, solutionsPerIteration, ageMax, solverTimeLimitInMillis, maxIterations);
    }

    /** {@inheritDoc} */
    @Override
    public S algorithm(I instance) {
        Map<Object, Integer> age = new HashMap<>();
        Set<Object> subInstance = new HashSet<>();
        S best = null;

        int iteration = 0;
        while (!TimeControl.isTimeUp() && (maxIterations <= 0 || iteration < maxIterations)) {

            // Construct & Merge
            for (int i = 0; i < solutionsPerIteration && !TimeControl.isTimeUp(); i++) {
                S candidate = this.constructive.construct(this.newSolution(instance));
                for (var component : this.constructive.usedComponents(candidate)) {
                    if (subInstance.add(component)) {
                        age.put(component, 0);
                    }
                }
            }

            if (TimeControl.isTimeUp()) {
                break;
            }

            // Solve
            long timeBudget = solverTimeLimitInMillis;
            if (TimeControl.isEnabled()) {
                timeBudget = Math.min(timeBudget, TimeControl.remaining() / 1_000_000);
            }
            if (timeBudget <= 0) {
                break;
            }
            S solved = this.solver.solve(instance, Set.copyOf(subInstance), timeBudget);

            if (solved != null) {
                if (best == null || objective.isBetter(solved, best)) {
                    log.debug("Iteration {}: improved best solution: {} --> {}", iteration,
                            best == null ? "none" : objective.evalSol(best), objective.evalSol(solved));
                    best = solved;
                    Metrics.addCurrentObjectives(best);
                }

                // Adapt: components used by the solver stay young, the rest age and eventually drop out
                Set<Object> usedBySolver = this.constructive.usedComponents(solved);
                Iterator<Object> it = subInstance.iterator();
                while (it.hasNext()) {
                    Object component = it.next();
                    if (usedBySolver.contains(component)) {
                        age.put(component, 0);
                    } else {
                        int componentAge = age.merge(component, 1, Integer::sum);
                        if (componentAge > ageMax) {
                            it.remove();
                            age.remove(component);
                        }
                    }
                }
            } else {
                log.debug("Iteration {}: solver could not find a feasible solution to the restricted sub-instance", iteration);
            }

            iteration++;
        }

        if (best == null) {
            throw new IllegalStateException("CMSA did not find any feasible solution. Review the CMSASolver implementation, " +
                    "or increase solverTimeLimitInMillis / solutionsPerIteration so the sub-instance is easier to solve.");
        }

        return best;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "CMSA{" +
                "constructive=" + constructive +
                ", solver=" + solver +
                ", na=" + solutionsPerIteration +
                ", ageMax=" + ageMax +
                '}';
    }
}
