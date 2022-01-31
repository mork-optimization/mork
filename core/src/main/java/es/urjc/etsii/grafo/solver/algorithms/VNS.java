package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.destructor.Shake;
import es.urjc.etsii.grafo.solver.improve.Improver;
import es.urjc.etsii.grafo.solver.services.Global;

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
 * <p>
 * s = GenerateInitialSolution
 * while (Termination criteria is not met){
 * k = 1
 * while (k != kmax){
 * s' = Shake(s,k)
 * s'' = Improve (s')
 * NeighborhoodChange(s,s'',k)
 * }
 * }
 * <p>
 * Further information can ben found in:
 * Hansen P., Mladenović N. (2018) Variable Neighborhood Search.
 * In: Martí R., Pardalos P., Resende M. (eds) Handbook of Heuristics.
 * Springer, Cham. https://doi.org/10.1007/978-3-319-07124-4_19
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
     * Calculates K value for each VNS step. {@see KProvider}
     */
    protected KProvider<I> kProvider;

    /**
     * Execute VNS until finished
     *
     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
     * @param kProvider     k value provider, @see VNS.KProvider
     * @param shake         Perturbation method
     * @param constructive  Constructive method
     * @param improvers     List of improvers/local searches
     */
    @SafeVarargs
    public VNS(String algorithmName, KProvider<I> kProvider, Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I>... improvers) {
        this(algorithmName, kProvider, constructive, Collections.singletonList(shake), improvers);
    }

    /**
     * Execute VNS until finished
     *
     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
     * @param kProvider     k value provider, @see VNS.KProvider
     * @param shakes        Perturbation method
     * @param constructive  Constructive method
     * @param improvers     List of improvers/local searches
     */
    @SafeVarargs
    public VNS(String algorithmName, KProvider<I> kProvider, Constructive<S, I> constructive, List<Shake<S, I>> shakes, Improver<S, I>... improvers) {
        this.algorithmName = algorithmName;
        this.kProvider = kProvider;

        // Ensure Ks are sorted, maxK is the last element
        this.shakes = shakes;
        this.constructive = constructive;
        this.improvers = Arrays.asList(improvers);
    }


    /**
     * VNS algorithm. This procedure follows this schema:
     * s = GenerateInitial solution
     * k = 1
     * while (k != kmax){
     * s' = Shake(s,k)
     * s'' = Improve (s')
     * NeighborhoodChange(s,s'',k)
     * }
     * To run the VNS procedure multiples time consider use MultiStart algorithm class {@see es.urjc.etsii.grafo.solver.algorithms.multistart.MultiStartAlgorithm}
     *
     * @param instance Instance to solve
     * @return best solution found when the VNS procedure ends
     */
    public S algorithm(I instance) {
        var solution = this.newSolution(instance);
        solution = constructive.construct(solution);
        solution = localSearch(solution);

        int currentKIndex = 0;
        // While stop not request OR k in range. k check is done and breaks inside loop
        while (!Global.stop()) {
            int currentK = kProvider.getK(instance, currentKIndex);
            if (currentK == KProvider.STOPNOW) {
                printStatus(currentKIndex + ":STOPNOW", solution);
                break;
            }
            printStatus(currentKIndex + ":" + currentK, solution);
            S bestSolution = solution;

            for (var shake : shakes) {
                S copy = bestSolution.cloneSolution();
                copy = shake.shake(copy, currentK);    // Shake procedure
                copy = localSearch(copy);              // Improvement procedure
                if (copy.isBetterThan(bestSolution)) {
                    bestSolution = copy;
                }
            }
            if (bestSolution == solution) {  //
                currentKIndex++;             //
            } else {                         //  Neighborhood change
                solution = bestSolution;     //  procedure
                currentKIndex = 0;           //
            }                                //
        }
        return solution;
    }

    /**
     * Improving method. Given a solution, this method execute sequentially the improvement procedures.
     *
     * @param solution initial solution  of the procedure
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
     * @param phase current state of the vns procedure
     * @param s solution
     */
    private void printStatus(String phase, S s) {
        log.fine(String.format("%s: \t%s", phase, s));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "VNS{" +
                "improvers=" + improvers +
                ", constructive=" + constructive +
                ", shakes=" + shakes +
                ", kprov=" + kProvider +
                '}';
    }

    /** {@inheritDoc} */
    @Override
    public String getShortName() {
        return this.algorithmName;
    }

    /**
     * Calculates K value for each VNS step.
     */
    public interface KProvider<I extends Instance> {
        int STOPNOW = -1;

        /**
         * Calculate K value during VNS execution.
         *
         * @param instance Current instance, provided as a parameter so K can be adapted or scaled to instance size.
         * @param kIndex   Current k strength. Starts in 0 and increments by 1 each time the solution does not improve.
         * @return K value. Return KProvider.STOPNOW to stop when calculated K is greater than max K, and the VNS should terminate
         */
        int getK(I instance, int kIndex);
    }
}
