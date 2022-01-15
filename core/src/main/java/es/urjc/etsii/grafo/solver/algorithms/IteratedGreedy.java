package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.Reconstructive;
import es.urjc.etsii.grafo.solver.destructor.DestroyRebuild;
import es.urjc.etsii.grafo.solver.destructor.Destructive;
import es.urjc.etsii.grafo.solver.destructor.Shake;
import es.urjc.etsii.grafo.solver.improve.Improver;

import java.util.Arrays;
import java.util.logging.Logger;

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
 * Iterated greedy algorithms  natural extension is to improve the generated solutions by
 * the application of a improvement algorithm, such as local search procedures.
 * <p>
 * Algorithmic outline of an IG with an additional local search step
 * <p>
 * For further information about Iterated Greedy Algorithms see:
 * Stützle T., Ruiz R. (2018) Iterated Greedy.
 * In: Martí R., Pardalos P., Resende M. (eds) Handbook of Heuristics.
 * Springer, Cham. https://doi.org/10.1007/978-3-319-07124-4_10
 *
 * @param <S> the type of the problem solution
 * @param <I> the type of problem instances
 */
public class IteratedGreedy<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger logger = Logger.getLogger(IteratedGreedy.class.getName());

    /**
     * Constructive procedure
     */
    private Constructive<S, I> constructive;

    /**
     * Destructive an reconstructive procedure
     */
    //private Shake<S, I> destructionReconstruction;
    private Shake<S, I> destructionReconstruction;

    /**
     * Improving procedures
     */
    private Improver<S, I>[] improvers;

    /**
     * Maximum number of iterations the algorithm could be executed.
     */
    private int maxIterations;

    /**
     * Maximum number of iterations without improving the algorithm could be executed.
     */
    private int stopIfNotImprovedIn;


    /**
     * Protected constructor, for serialization, use public one when creating a new IteratedGreedy
     */
    protected IteratedGreedy() {}

    /**
     *  Iterated Greedy Algorithm constructor
     *
     * @param maxIterations  maximum number of iterations the algorithm could be executed.
     * @param stopIfNotImprovedIn maximum number of iterations without improving the algorithm could be executed.
     * @param constructive constructive procedure to generate the initial solution of the algorithm
     * @param destructionReconstruction destruction and reconstruction procedures
     * @param improvers improving procedures. Could be 0 or more.
     */
    @SafeVarargs
    public IteratedGreedy(int maxIterations, int stopIfNotImprovedIn, Constructive<S, I> constructive, Shake<S, I> destructionReconstruction, Improver<S, I>... improvers) {
        if (stopIfNotImprovedIn < 1) {
            throw new IllegalArgumentException("stopIfNotImprovedIn must be greater than 0");
        }
        this.maxIterations = maxIterations;
        this.stopIfNotImprovedIn = stopIfNotImprovedIn;
        this.constructive = constructive;
        this.destructionReconstruction = destructionReconstruction;
        this.improvers = improvers;
    }

    /**
     *  Iterated Greedy Algorithm constructor: uses same constructive
     *  method when building the initial solution and after the destructive.
     *
     * @param maxIterations  maximum number of iterations the algorithm could be executed.
     * @param stopIfNotImprovedIn maximum number of iterations without improving the algorithm could be executed.
     * @param constructive constructive procedure to generate the initial solution of the algorithm, and rebuild the solution after the destructive method
     * @param destructive destructive method called before the reconstructive
     * @param improvers improving procedures. Could be 0 or more.
     */
    @SafeVarargs
    public IteratedGreedy(int maxIterations, int stopIfNotImprovedIn, Reconstructive<S, I> constructive, Destructive<S, I> destructive, Improver<S, I>... improvers) {
        this(maxIterations, stopIfNotImprovedIn, constructive, new DestroyRebuild<>(constructive, destructive), improvers);
    }

    /**
     *  Iterated Greedy Algorithm constructor: uses one constructive
     *  method when building the initial solution and another one when reconstructing
     *
     * @param maxIterations  maximum number of iterations the algorithm could be executed.
     * @param stopIfNotImprovedIn maximum number of iterations without improving the algorithm could be executed.
     * @param constructive constructive procedure to generate the initial solution of the algorithm, and rebuild the solution after the destructive method
     * @param destructive destructive method called before the reconstructive
     * @param improvers improving procedures. Could be 0 or more.
     */
    @SafeVarargs
    public IteratedGreedy(int maxIterations, int stopIfNotImprovedIn, Constructive<S, I> constructive, Destructive<S, I> destructive, Reconstructive<S, I> reconstructive, Improver<S, I>... improvers) {
        this(maxIterations, stopIfNotImprovedIn, constructive, new DestroyRebuild<>(reconstructive, destructive), improvers);
    }

    // POR AQUI, CAMBIAR SHAKE POR DESTROY REBUILD
    // ADD CONSTRUCTOR TO PASS ONLY DESTRUCTOR AND REBUILD USING THE SAME CONSTRUCTIVE METHOID


    /**
     * {@inheritDoc}
     *
     * Iterated greedy algorithm procedure
     */
    @Override
    public S algorithm(I instance) {
        S solution = this.newSolution(instance);
        solution = this.constructive.construct(solution);
        solution = ls(solution);
        logger.fine(String.format("Initial solution: %s - %s", solution.getScore(), solution));
        int iterationsWithoutImprovement = 0;
        for (int i = 0; i < maxIterations; i++) {
            S copy = solution.cloneSolution();
            copy = this.destructionReconstruction.shake(copy, 1);
            copy = ls(copy);
            if (copy.isBetterThan(solution)) {
                solution = copy;
                logger.fine(String.format("Improved at iteration %s: %s - %s", i, solution.getScore(), solution));
                iterationsWithoutImprovement = 0;
            } else {
                iterationsWithoutImprovement++;
                if (iterationsWithoutImprovement >= this.stopIfNotImprovedIn) {
                    logger.fine(String.format("Not improved after %s iterations, stopping in iteration %s. Current score %s - %s", stopIfNotImprovedIn, i, solution.getScore(), solution));
                    break;
                }
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
        if (improvers == null) return solution;

        for (Improver<S, I> improver : improvers) {
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
                ", improvers=" + Arrays.toString(improvers) +
                '}';
    }
}
