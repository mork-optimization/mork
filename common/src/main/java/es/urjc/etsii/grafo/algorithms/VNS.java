package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Variable neighborhood search (VNS) is a metaheuristic for solving combinatorial
 * and global optimization problems. Its basic idea is the systematic change of
 * neighborhood both in a descent phase to find a local optimum and in a perturbation
 * phase to exit the corresponding local optimum
 * <p>
 * Algorithmic outline of the simplest version of VNS
 * </p>
 * <pre>
 * s = GenerateInitialSolution
 * while (Termination criteria is not met){
 *      k = 1
 *      while (k != kmax){
 *          s' = Shake(s,k)
 *          s'' = Improve (s')
 *          NeighborhoodChange(s,s'',k)
 *      }
 * }
 * </pre>
 *
 * More information can be found in:
 * Hansen P., Mladenović N. (2018) Variable Neighborhood Search.
 * In: Martí R., Pardalos P., Resende M. (eds) Handbook of Heuristics.
 * Springer, Cham. <a href="https://doi.org/10.1007/978-3-319-07124-4_19">...</a>
 *
 * @param <S> type of the problem solution
 * @param <I> type of the problem instance
 */
public class VNS<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = Logger.getLogger(VNS.class.getName());
    protected final String algorithmName;

    protected List<Improver<S, I>> improvers;


    /**
     * Constructive procedure
     */
    protected Constructive<S, I> constructive;

    /**
     * Shake procedure
     */
    protected List<Shake<S, I>> shakes;

    /**
     * Calculates K value for each VNS step. {@see KMapper}
     */
    protected KMapper<S, I> kMapper;

    /**
     * Execute VNS until finished
     *
     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
     * @param kMapper       k value provider, @see VNS.KMapper
     * @param shake         Perturbation method
     * @param constructive  Constructive method
     * @param improvers     List of improvers/local searches
     */
    @SafeVarargs
    public VNS(String algorithmName, KMapper<S, I> kMapper, Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I>... improvers) {
        this(algorithmName, kMapper, constructive, Collections.singletonList(shake), improvers);
    }

    /**
     * VNS with default KMapper, which starts at 0 and increments by 1 each time the solution does not improve.
     * Stops when k >= 5. Behaviour can be customized passing a custom kMapper, such as:
     * <pre>
     * {@code
     * (solution, originalK) -> originalK >= 10 ? KMapper.STOPNOW : originalK
     * }
     * </pre>
     *
     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
     * @param shake         Perturbation method
     * @param constructive  Constructive method
     * @param improvers     List of improvers/local searches
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public VNS(String algorithmName, Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I>... improvers) {
        this(algorithmName, DEFAULT_KMAPPER, constructive, Collections.singletonList(shake), improvers);
    }

    /**
     * Execute VNS until finished
     *
     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
     * @param kMapper       k value provider, @see VNS.KMapper
     * @param shakes        Perturbation method
     * @param constructive  Constructive method
     * @param improvers     List of improvers/local searches
     */
    @SafeVarargs
    public VNS(String algorithmName, KMapper<S, I> kMapper, Constructive<S, I> constructive, List<Shake<S, I>> shakes, Improver<S, I>... improvers) {
        this.algorithmName = algorithmName;
        this.kMapper = kMapper;

        // Ensure Ks are sorted, maxK is the last element
        this.shakes = shakes;
        this.constructive = constructive;
        this.improvers = Arrays.asList(improvers);
    }


    /**
     * VNS algorithm. This procedure follows this schema:
     * <pre>
     *     s = GenerateInitial solution
     *     k = 1
     *     while (k != kmax){
     *     s' = Shake(s,k)
     *     s'' = Improve (s')
     *     NeighborhoodChange(s,s'',k)
     *     }
     * </pre>
     * <p>
     * To run the VNS procedure multiples time consider use MultiStart algorithm class {@see es.urjc.etsii.grafo.solver.algorithms.multistart.MultiStartAlgorithm}
     *
     * @param instance Instance to solve
     * @return best solution found when the VNS procedure ends
     */
    public S algorithm(I instance) {
        var solution = this.newSolution(instance);
        solution = constructive.construct(solution);
        solution = localSearch(solution);

        int internalK = 0;
        // While stop not request OR k in range. k check is done and breaks inside loop
        while (!TimeControl.isTimeUp()) {
            int userK = kMapper.mapK(solution, internalK);
            if (userK == KMapper.STOPNOW) {
                printStatus(internalK + ":STOPNOW", solution);
                break;
            }
            printStatus(internalK + ":" + userK, solution);
            S bestSolution = solution;

            for (var shake : shakes) {
                S copy = bestSolution.cloneSolution();
                copy = shake.shake(copy, userK);
                copy = localSearch(copy);
                if (copy.isBetterThan(bestSolution)) {
                    bestSolution = copy;
                }
            }
            if (bestSolution == solution) {
                internalK++;
            } else {
                solution = bestSolution;
                internalK = 0;
            }
        }
        return solution;
    }

    /**
     * Improving method. Given a solution, this method execute sequentially the improvement procedures.
     *
     * @param solution initial solution of the procedure
     * @return the improved solution
     */
    private S localSearch(S solution) {
        for (Improver<S, I> ls : improvers) {
            solution = ls.improve(solution);
        }
        return solution;
    }

    /**
     * Print the current status of the VNS procedure, i.e., the current iteration the best solution.
     *
     * @param phase current state of the vns procedure
     * @param solution     solution
     */
    private void printStatus(String phase, S solution) {
        log.fine(String.format("%s: \t%s", phase, solution));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "VNS{" +
                "improvers=" + improvers +
                ", constructive=" + constructive +
                ", shakes=" + shakes +
                ", kmap=" + kMapper +
                '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortName() {
        return this.algorithmName;
    }

    /**
     * Calculates K value for each VNS step.
     */
    @FunctionalInterface
    public interface KMapper<S extends Solution<S, I>, I extends Instance> {
        int STOPNOW = -1;

        /**
         * Map internal K value to custom values during VNS execution.
         * VNS by default uses an internal K which starts in 0 and is incremented by 1 each time the solution does not improve.
         * This K can be mapped to any other number using this interface. Example: multiply by 5, stop if result would be greater than 100.
         * <pre>
         * {@code
         * (solution, originalK) -> originalK >= 20 ? KMapper.STOPNOW : originalK * 5
         * }
         * </pre>
         * would generate the following mapping
         * <pre>
         * 0 —> 0
         * 1 —> 5
         * 2 —> 10
         * 3 —> 15
         * etc.
         * </pre>
         *
         * @param solution  Current instance, provided as a parameter so K can be adapted or scaled to instance size.
         * @param originalK Current k strength. Starts in 0 and increments by 1 each time the solution does not improve.
         * @return K value to use. Return KMapper.STOPNOW to stop when the VNS should terminate
         */
        int mapK(S solution, int originalK);
    }

    /**
     * Default KMapper, maps identity, stops when k = 5
     */
    private static final KMapper DEFAULT_KMAPPER = (solution, originalK) -> originalK >= 5 ? KMapper.STOPNOW : originalK;

}
