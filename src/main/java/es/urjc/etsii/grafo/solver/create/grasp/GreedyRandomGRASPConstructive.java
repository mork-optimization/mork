package es.urjc.etsii.grafo.solver.create.grasp;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.improve.DefaultMoveComparator;
import es.urjc.etsii.grafo.util.RandomManager;

import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.DoubleComparator.*;

/**
 * GRASP Constructive method
 *
 * @param <M> Move type
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class GreedyRandomGRASPConstructive<M extends Move<S, I>, S extends Solution<I>, I extends Instance> extends Constructive<S, I> {
    private static final Logger log = Logger.getLogger(GreedyRandomGRASPConstructive.class.getName());
    protected final String randomType;
    protected final Comparator<M> comparator;
    protected final GRASPListManager<M, S, I> candidateListManager;
    private final AlphaProvider alphaProvider;

    /**
     * GRASP Constructor, mantains a fixed alpha value
     * Stops when the neighborhood does not provide any movement
     *
     * @param alpha      Randomness, adjusts the candidate list size.
     *                   Takes values between [0,1] being 1 --> totally random, 0 --> full greedy.
     * @param maximizing true if we are maximizing the score, false if minimizing
     */
    public GreedyRandomGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double alpha, boolean maximizing) {
        this.candidateListManager = candidateListManager;
        assert isGreaterOrEqualsThan(alpha, 0) && isLessOrEquals(alpha, 1);
        this.comparator = new DefaultMoveComparator<>(maximizing);
        randomType = String.format("FIXED{a=%.2f}", alpha);
        alphaProvider = () -> alpha;
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction
     * Alpha Takes values between [0,1] being 1 --> totally random, 0 --> full greedy.
     *
     * @param minAlpha minimum value for the random alpha
     * @param maxAlpha maximum value for the random alpha
     */
    public GreedyRandomGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double minAlpha, double maxAlpha, Comparator<M> comparator) {
        this.candidateListManager = candidateListManager;
        this.comparator = comparator;
        assert isGreaterOrEqualsThan(minAlpha, 0) && isLessOrEquals(minAlpha, 1);
        assert isGreaterOrEqualsThan(maxAlpha, 0) && isLessOrEquals(maxAlpha, 1);
        assert isGreaterThan(maxAlpha, minAlpha);

        alphaProvider = () -> RandomManager.getRandom().nextDouble() * (maxAlpha - minAlpha) + minAlpha;
        randomType = String.format("RANGE{min=%.2f, max=%.2f}", minAlpha, maxAlpha);
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction
     * Alpha Takes values between [0,1] being 1 --> totally random, 0 --> full greedy.
     *
     * @param minAlpha   minimum value for the random alpha
     * @param maxAlpha   maximum value for the random alpha
     * @param maximizing true if maximizing, false if minimizing
     */
    public GreedyRandomGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double minAlpha, double maxAlpha, boolean maximizing) {
        this(candidateListManager, minAlpha, maxAlpha, new DefaultMoveComparator<>(maximizing));
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction, between 0 and 1 (inclusive).
     */
    public GreedyRandomGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, boolean maximizing) {
        this(candidateListManager, 0, 1, maximizing);
    }

    private int binarySearchFindLimit(List<M> cl, double v, boolean asc) {
        // Adapted from the Java Collections Implementation
        int low = 0;
        int high = cl.size() - 1;

        while (low <= high) {
            int mid = low + high >>> 1;
            var midVal = cl.get(mid);
            int cmp = Double.compare(midVal.getValue(), v);
            if (cmp < 0) {
                if (asc) low = mid + 1;
                else high = mid - 1;
            } else {
                if (cmp == 0) {
                    // Found exact match --> Return current mid element
                    return mid;
                }
                if (asc) high = mid - 1;
                else low = mid + 1;
            }
        }

        return low; // Not found, but return the insertion point as a positive number
    }

    @Override
    public S construct(S sol) {
        candidateListManager.beforeGRASP(sol);
        return assignMissing(sol);
    }

    public S assignMissing(S sol) {
        double alpha = alphaProvider.getAlpha();
        var cl = candidateListManager.buildInitialCandidateList(sol);
        assert cl instanceof RandomAccess : "Candidate List should have O(1) access time";
        cl.sort(comparator);
        while (!cl.isEmpty()) {
            // Choose an index from the candidate list following different strategies, GreedyRandom, RandomGreedy...
            int index = greedyRandom(alpha, cl);
            M chosen = cl.get(index);
            chosen.execute();
            cl = candidateListManager.updateCandidateList(sol, chosen, cl, index);
            assert cl instanceof RandomAccess : "Candidate List should have O(1) access time";
            //assert validateComparator(comparator, cl);
            cl.sort(comparator);

            // Catch bugs while building the es.urjc.etsii.grafo.solution
            // no-op if running in performance mode, triggers score recalculation if debugging
            assert isPositiveOrZero(sol.getScore());
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "randomType='" + randomType + '\'' +
                ", comparator=" + comparator +
                '}';
    }

    @FunctionalInterface
    interface AlphaProvider {
        double getAlpha();
    }
}
