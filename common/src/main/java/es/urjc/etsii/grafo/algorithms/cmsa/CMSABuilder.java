package es.urjc.etsii.grafo.algorithms.cmsa;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.StringUtil;

/**
 * CMSA algorithm builder based on the Java Builder Pattern.
 *
 * @param <S> type of the solution of the problem
 * @param <I> type of the instance of the problem
 */
public class CMSABuilder<S extends Solution<S, I>, I extends Instance> {

    private Objective<?, S, I> objective;
    private CMSAConstructive<S, I> constructive;
    private CMSASolver<S, I> solver;
    private int solutionsPerIteration = 30;
    private int ageMax = 5;
    private long solverTimeLimitInMillis = 1_000;
    private int maxIterations = 0;

    /**
     * Configures the probabilistic constructive procedure used to sample solution components.
     * @param constructive probabilistic constructive procedure
     * @return this builder instance for method chaining
     */
    public CMSABuilder<S, I> withConstructive(CMSAConstructive<S, I> constructive) {
        this.constructive = constructive;
        return this;
    }

    /**
     * Configures the exact (or near-exact) method used to solve the restricted sub-instance.
     * @param solver exact method implementation
     * @return this builder instance for method chaining
     */
    public CMSABuilder<S, I> withSolver(CMSASolver<S, I> solver) {
        this.solver = solver;
        return this;
    }

    /**
     * Configures how many solutions are probabilistically constructed at each iteration before
     * merging their components into the sub-instance and calling the solver. Defaults to 30.
     * @param solutionsPerIteration number of solutions constructed at each iteration, usually denoted {@code na}
     * @return this builder instance for method chaining
     */
    public CMSABuilder<S, I> withSolutionsPerIteration(int solutionsPerIteration) {
        this.solutionsPerIteration = solutionsPerIteration;
        return this;
    }

    /**
     * Configures the maximum age a solution component can reach, while not being used by the solver,
     * before being removed from the sub-instance. Defaults to 5.
     * @param ageMax maximum component age
     * @return this builder instance for method chaining
     */
    public CMSABuilder<S, I> withAgeMax(int ageMax) {
        this.ageMax = ageMax;
        return this;
    }

    /**
     * Configures the time budget, in milliseconds, given to the solver at each iteration to solve
     * the restricted sub-instance. Defaults to 1000 (1 second).
     * @param solverTimeLimitInMillis time budget in milliseconds
     * @return this builder instance for method chaining
     */
    public CMSABuilder<S, I> withSolverTimeLimitInMillis(long solverTimeLimitInMillis) {
        this.solverTimeLimitInMillis = solverTimeLimitInMillis;
        return this;
    }

    /**
     * Configures the maximum number of iterations. Defaults to 0, disabling this limit and relying
     * exclusively on the global time limit.
     * @param maxIterations maximum number of iterations, use a value smaller or equal to zero to disable
     * @return this builder instance for method chaining
     */
    public CMSABuilder<S, I> withMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }

    /**
     * Optimize default objective function, which is the main objective declared in the current Context.
     * Note that calling this method is optional, if the objective is not set it will default to the main objective.
     * @return this builder instance for method chaining
     */
    public CMSABuilder<S, I> withDefaultObjective() {
        this.objective = Context.getMainObjective();
        return this;
    }

    /**
     * Configures the objective function to optimize.
     * @param objective objective function to optimize
     * @return this builder instance for method chaining
     */
    public CMSABuilder<S, I> withObjective(Objective<?, S, I> objective) {
        this.objective = objective;
        return this;
    }

    /**
     * Builds the CMSA algorithm with the configured parameters.
     * Uses a random name for the algorithm.
     * @return a new instance of CMSA
     */
    public CMSA<S, I> build() {
        return this.build(StringUtil.randomAlgorithmName());
    }

    /**
     * Builds the CMSA algorithm with the configured parameters.
     * @param name algorithm name
     * @return a new instance of CMSA
     */
    public CMSA<S, I> build(String name) {
        Objective<?, S, I> resolvedObjective = this.objective == null ? Context.getMainObjective() : this.objective;

        if (this.constructive == null) {
            throw new IllegalArgumentException("CMSABuilder requires a CMSAConstructive implementation. Use .withConstructive() to configure it.");
        }

        if (this.solver == null) {
            throw new IllegalArgumentException("CMSABuilder requires a CMSASolver implementation. Use .withSolver() to configure it.");
        }

        return new CMSA<>(name, resolvedObjective, constructive, solver, solutionsPerIteration, ageMax, solverTimeLimitInMillis, maxIterations);
    }
}
