package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.RandomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.ToIntBiFunction;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.DoubleComparator.*;

/**
 * GRASP Constructive method
 * @param <M> Move type
 * @param <S> Solution type
 * @param <I> Instance type
 */
public abstract class GreedyRandomGRASPConstructive<M extends Move<S,I>, S extends Solution<I>, I extends Instance> extends Constructive<S,I> {
    private static final Logger log = Logger.getLogger(GreedyRandomGRASPConstructive.class.getName());

    private final AlphaProvider alphaProvider;
    protected final String randomType;

    /**
     * GRASP Constructor, mantains a fixed alpha value
     * Stops when the neighborhood does not provide any movement
     * @param alpha Randomness, adjusts the candidate list size.
     *              Takes values between [0,1] being 1 --> totally random, 0 --> full greedy.
     */
    public GreedyRandomGRASPConstructive(double alpha){
        assert isGreaterOrEqualsThan(alpha, 0) && isLessOrEquals(alpha, 1);

        randomType = String.format("FIXED{a=%.2f}", alpha);
        alphaProvider = () -> alpha;
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction
     * Alpha Takes values between [0,1] being 1 --> totally random, 0 --> full greedy.
     * @param minAlpha minimum value for the random alpha
     * @param maxAlpha maximum value for the random alpha
     */
    public GreedyRandomGRASPConstructive(double minAlpha, double maxAlpha){
        assert isGreaterOrEqualsThan(minAlpha, 0) && isLessOrEquals(minAlpha, 1);
        assert isGreaterOrEqualsThan(maxAlpha, 0) && isLessOrEquals(maxAlpha, 1);
        assert isGreaterThan(maxAlpha, minAlpha);

        alphaProvider = () -> RandomManager.getRandom().nextDouble() * (maxAlpha - minAlpha) + minAlpha;
        randomType = String.format("RANGE{min=%.2f, max=%.2f}", minAlpha, maxAlpha);
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction, between 0 and 1 (inclusive).
     */
    public GreedyRandomGRASPConstructive(){
        this(0, 1);
    }

    /**
     * Initialize solution before GRASP algorithm is run
     * F.e: In the case of clustering algorithms, usually each cluster needs to have at least one point,
     * different solutions types may require different initialization
     * @param s Solution to initialize before running the GRASP constructive method
     */
    public void beforeGRASP(S s){ }

    private int binarySearchFindLimit(List<M> cl, double v, boolean asc){
        // Adapted from the Java Collections Implementation
        int low = 0;
        int high = cl.size() - 1;

        while(low <= high) {
            int mid = low + high >>> 1;
            var midVal = cl.get(mid);
            int cmp = Double.compare(midVal.getValue(), v);
            if (cmp < 0) {
                if(asc) low = mid + 1;
                else    high = mid - 1;
            } else {
                if (cmp == 0) {
                    // Found exact match --> Return current mid element
                    return mid;
                }
                if(asc) high = mid - 1;
                else    low = mid + 1;
            }
        }

        return low; // Not found, but return the insertion point as a positive number
    }

    @Override
    public S construct(S sol) {
        this.beforeGRASP(sol);
        return assignMissing(sol);
    }

    public S assignMissing(S sol) {
        double alpha = alphaProvider.getAlpha();
        var cl = buildInitialCandidateList(sol);
        assert cl instanceof RandomAccess : "Candidate List should have O(1) access time";
        var comparator = new Move.MoveComparator<S,I>();
        cl.sort(comparator);
        while (!cl.isEmpty()) {
            // Choose an index from the candidate list following different strategies, GreedyRandom, RandomGreedy...
            int index = greedyRandom(alpha, cl);
            M chosen = cl.get(index);
            chosen.execute();
            cl = updateCandidateList(sol, chosen, cl, index);
            assert cl instanceof RandomAccess : "Candidate List should have O(1) access time";
            cl.sort(comparator);

            // Catch bugs while building the es.urjc.etsii.grafo.solution
            // no-op if running in performance mode, triggers score recalculation if debugging
            assert isPositiveOrZero(sol.getOptimalValue());
        }
        return sol;
    }

    private int greedyRandom(double alpha, List<M> cl) {
        double left = cl.get(0).getValue();
        double right = cl.get(cl.size() - 1).getValue();
        boolean asc = left < right;
        double limit = left + (alpha) * (right - left);

        int limitIndex = binarySearchFindLimit(cl, limit, asc);

        return RandomManager.nextInt(0, limitIndex + 1);
    }


    /**
     * Generate initial candidate list. The list will be sorted by the constructor.
     * @param sol Current es.urjc.etsii.grafo.solution
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    public abstract List<M> buildInitialCandidateList(S sol);

    /**
     * Update candidate list after each movement. The list will be sorted by the constructor.
     * @param s Current es.urjc.etsii.grafo.solution, move has been already applied
     * @param t Chosen move
     * @param index index of the chosen move in the candidate list
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    public abstract List<M> updateCandidateList(S s, M t, List<M> candidateList, int index);


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "alpha=" + randomType +
                '}';
    }

    @FunctionalInterface
    interface AlphaProvider {
        double getAlpha();
    }
}
