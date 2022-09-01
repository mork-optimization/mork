package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(VNS.class);

    protected final String algorithmName;

    protected Improver<S, I> improver;


    /**
     * Constructive procedure
     */
    protected Constructive<S, I> constructive;

    /**
     * Shake procedure
     */
    protected Shake<S, I> shake;

    /**
     * Calculates K value for each VNS step. {@see KMapper}
     */
    protected KMapper<S, I> kMapper;

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
     * @param improver     List of improvers/local searches
     */
    @AutoconfigConstructor
    public VNS(String algorithmName, @IntegerParam(min = 1) int maxK, Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I> improver) {
        this(algorithmName, getDefaultKMapper(maxK), constructive, shake, improver);
    }

    /**
     * Execute VNS until finished
     *
     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
     * @param kMapper       k value provider, @see VNS.KMapper
     * @param shake        Perturbation method
     * @param constructive  Constructive method
     * @param improver     List of improvers/local searches
     */
    public VNS(String algorithmName, KMapper<S, I> kMapper, Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I> improver) {
        this.algorithmName = algorithmName;
        this.kMapper = kMapper;

        // Ensure Ks are sorted, maxK is the last element
        this.shake = shake;
        this.constructive = constructive;
        this.improver = improver;
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
        solution = improver.improve(solution);

        int internalK = 0;
        // While stop not request OR k in range. k check is done and breaks inside loop
        while (!TimeControl.isTimeUp()) {
            int userK = kMapper.mapK(solution, internalK);
            if (userK == KMapper.STOPNOW) {
                printStatus(internalK, KMapper.STOPNOW, solution);
                break;
            }
            printStatus(internalK, userK, solution);

            S copy = solution.cloneSolution();
            copy = shake.shake(copy, userK);
            copy = improver.improve(copy);
            if (copy.isBetterThan(solution)) {
                solution = copy;
                internalK = 0;
            } else {
                internalK++;
            }
        }
        return solution;
    }

    /**
     * Print the current status of the VNS procedure, i.e., the current iteration the best solution.
     *
     * @param internalK my value of K, starts at 0 and increments by 1
     * @param mappedK external K value used for custom shake methods
     * @param solution     solution
     */
    private void printStatus(int internalK, int mappedK, S solution) {
        log.debug("{}:{} -> \t{}", internalK, mappedK, solution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "VNS{" +
                "improvers=" + improver +
                ", constructive=" + constructive +
                ", shakes=" + shake +
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

    private static <S extends Solution<S,I>, I extends Instance> KMapper<S,I> getDefaultKMapper(int maxK){
        return (solution, originalK) -> originalK >= maxK ? KMapper.STOPNOW : originalK;
    }
}
