package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.BestObjective;
import es.urjc.etsii.grafo.shake.DestroyRebuild;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.metrics.MetricsManager;
import es.urjc.etsii.grafo.util.StringUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterated greedy is a search method that iterates through applications of construction
 * heuristics using the repeated execution of two main phases, the partial
 * destruction of a complete candidate solution and a subsequent reconstruction of
 * a complete candidate solution.
 * <p>
 * Algorithmic outline of the simplest version of Iterated Greedy (IG)
 * <p>
 * s = GenerateInitialSolution
 * do {
 * s' = Destruction(s)
 * s'' = Reconstruction (s'')
 * s = AcceptanceCriterion(s,s'')
 * } while (Termination criteria is not met)
 * return s
 * <p>
 * Iterated greedy algorithms natural extension is to improve the generated solutions by
 * the application of an improvement algorithm, such as local search procedures.
 * <p>
 * Algorithmic outline of an IG with an additional local search step
 * <p>
 * For further information about Iterated Greedy Algorithms see:
 * Stützle T., Ruiz R. (2018) Iterated Greedy.
 * In: Martí R., Pardalos P., Resende M. (eds) Handbook of Heuristics.
 * Springer, Cham. <a href="https://doi.org/10.1007/978-3-319-07124-4_10">...</a>
 *
 * @param <S> the type of the problem solution
 * @param <I> the type of problem instances
 */
public class IteratedGreedy<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger logger = LoggerFactory.getLogger(IteratedGreedy.class);

    /**
     * Constructive procedure
     */
    private Constructive<S, I> constructive;

    /**
     * Destructive and reconstructive procedure
     */
    private Shake<S, I> destructionReconstruction;

    /**
     * Improving procedures
     */
    private Improver<S, I> improver;

    /**
     * Maximum number of iterations the algorithm could be executed.
     */
    private int maxIterations;

    /**
     * Maximum number of iterations without improving the algorithm could be executed.
     */
    private int stopIfNotImprovedIn;

    /**
     *  Iterated Greedy Algorithm constructor
     *
     * @param name Algorithm name, uniquely identifies the current algorithm. Tip: If you dont care about the name, generate a random one using {@link StringUtil#randomAlgorithmName()}
     * @param maxIterations  maximum number of iterations the algorithm could be executed.
     * @param stopIfNotImprovedIn maximum number of iterations without improving the algorithm could be executed.
     * @param constructive constructive procedure to generate the initial solution of the algorithm
     * @param destructionReconstruction destruction and reconstruction procedures
     * @param improver improving procedures. Could be 0 or more.
     */
    @AutoconfigConstructor
    public IteratedGreedy(
            @ProvidedParam String name,
            @IntegerParam(min = 0, max = 1_000_000) int maxIterations,
            @IntegerParam(min = 1, max = 1_000_000) int stopIfNotImprovedIn,
            Constructive<S, I> constructive,
            Shake<S, I> destructionReconstruction,
            Improver<S, I> improver
    ) {
        super(name);
        if (maxIterations < 0) {
            throw new IllegalArgumentException("maxIterations must be greater or equal to 0");
        }
        if (stopIfNotImprovedIn < 1) {
            throw new IllegalArgumentException("stopIfNotImprovedIn must be greater than 0");
        }
        this.maxIterations = maxIterations;
        this.stopIfNotImprovedIn = stopIfNotImprovedIn;
        this.constructive = constructive;
        this.destructionReconstruction = destructionReconstruction;
        this.improver = improver;
    }

    /**
     *  Iterated Greedy Algorithm constructor: uses same constructive
     *  method when building the initial solution and after the destructive.
     *
     * @param name Algorithm name, uniquely identifies the current algorithm. Tip: If you dont care about the name, generate a random one using {@link StringUtil#randomAlgorithmName()}
     * @param maxIterations  maximum number of iterations the algorithm could be executed.
     * @param stopIfNotImprovedIn maximum number of iterations without improving the algorithm could be executed.
     * @param constructive constructive procedure to generate the initial solution of the algorithm, and rebuild the solution after the destructive method
     * @param destructive destructive method called before the reconstructive
     * @param improver improving procedures. Could be 0 or more.
     */
    public IteratedGreedy(String name, int maxIterations, int stopIfNotImprovedIn, Reconstructive<S, I> constructive, Destructive<S, I> destructive, Improver<S, I> improver) {
        this(name, maxIterations, stopIfNotImprovedIn, constructive, new DestroyRebuild<>(constructive, destructive), improver);
    }

    /**
     *  Iterated Greedy Algorithm constructor: uses one constructive
     *  method when building the initial solution and another one when reconstructing
     *
     * @param name Algorithm name, uniquely identifies the current algorithm. Tip: If you dont care about the name, generate a random one using {@link StringUtil#randomAlgorithmName()}
     * @param maxIterations  maximum number of iterations the algorithm could be executed.
     * @param stopIfNotImprovedIn maximum number of iterations without improving the algorithm could be executed.
     * @param constructive constructive procedure to generate the initial solution of the algorithm, solution is NOT rebuilt using this component
     * @param destructive destructive method called before the reconstructive
     * @param reconstructive reconstructive procedure to rebuild the solution. Initial solution is NOT built using this method.
     * @param improver improving procedures. Could be 0 or more.
     */
    public IteratedGreedy(String name, int maxIterations, int stopIfNotImprovedIn, Constructive<S, I> constructive, Destructive<S, I> destructive, Reconstructive<S, I> reconstructive, Improver<S, I> improver) {
        this(name, maxIterations, stopIfNotImprovedIn, constructive, new DestroyRebuild<>(reconstructive, destructive), improver);
    }

    /**
     * {@inheritDoc}
     *
     * Iterated greedy algorithm procedure
     */
    @Override
    public S algorithm(I instance) {
        S solution = this.newSolution(instance);
        solution = this.constructive.construct(solution);
        BestObjective.add(solution.getScore());
        if(TimeControl.isTimeUp()){
            return solution;
        }
        solution = ls(solution);
        logger.debug("Initial solution: {} - {}", solution.getScore(), solution);
        int iterationsWithoutImprovement = 0;
        for (int i = 0; i < maxIterations; i++) {
            if(TimeControl.isTimeUp()){
                return solution;
            }
            S copy = solution.cloneSolution();
            copy = this.destructionReconstruction.shake(copy, 1);
            if(copy != null){
                copy = ls(copy);
            }

            // Analyze result
            if(copy == null || !copy.isBetterThan(solution)){
                iterationsWithoutImprovement++;
                if (iterationsWithoutImprovement >= this.stopIfNotImprovedIn) {
                    logger.debug("Not improved after {} iterations, stopping in iteration {}. Current score {} - {}", stopIfNotImprovedIn, i, solution.getScore(), solution);
                    break;
                }
            } else {
                solution = copy;
                logger.debug("Improved at iteration {}: {} - {}", i, solution.getScore(), solution);
                BestObjective.add(solution.getScore());
                iterationsWithoutImprovement = 0;
            }
        }

        return solution;
    }

    /**
     * Improving method. Given a solution, this method execute sequentially the improvement procedures.
     * If no improvement procedure is defined, the solution returned is the same as the one given as a parameter of the method.
     *
     * @param solution initial solution  of the procedure
     * @return the improved solution
     */
    private S ls(S solution) {
        if (improver != null){
            solution = improver.improve(solution);
        }

        return solution;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "IteratedGreedy{" +
                "constructive=" + constructive +
                ", shake=" + destructionReconstruction +
                ", improver=" + improver +
                '}';
    }
}
