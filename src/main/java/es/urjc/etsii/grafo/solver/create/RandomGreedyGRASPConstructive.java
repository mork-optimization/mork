package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.RandomManager;

import java.util.ArrayList;
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
public abstract class RandomGreedyGRASPConstructive<M extends Move<S, I>, S extends Solution<I>, I extends Instance> extends Constructive<S, I> {
    private static final Logger log = Logger.getLogger(RandomGreedyGRASPConstructive.class.getName());

    private final AlphaProvider alphaProvider;
    protected final String randomType;
    private final MoveComparator<M, S, I> comparator;


    public RandomGreedyGRASPConstructive(double alpha, MoveComparator<M, S, I> comparator) {
        this.comparator = comparator;
        assert isGreaterOrEqualsThan(alpha, 0) && isLessOrEquals(alpha, 1);

        randomType = String.format("FIXED{a=%.2f}", alpha);
        alphaProvider = () -> alpha;
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction
     * Alpha Takes values between [0,1] being 1 --> totally random, 0 --> full greedy.
     *
     * @param minAlpha   minimum value for the random alpha
     * @param maxAlpha   maximum value for the random alpha
     * @param comparator
     */
    public RandomGreedyGRASPConstructive(double minAlpha, double maxAlpha, MoveComparator<M, S, I> comparator) {
        this.comparator = comparator;
        assert isGreaterOrEqualsThan(minAlpha, 0) && isLessOrEquals(minAlpha, 1);
        assert isGreaterOrEqualsThan(maxAlpha, 0) && isLessOrEquals(maxAlpha, 1);
        assert isGreaterThan(maxAlpha, minAlpha);

        alphaProvider = () -> RandomManager.getRandom().nextDouble() * (maxAlpha - minAlpha) + minAlpha;
        randomType = String.format("RANGE{min=%.2f, max=%.2f}", minAlpha, maxAlpha);
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction, between 0 and 1 (inclusive).
     *
     * @param comparator
     */
    public RandomGreedyGRASPConstructive(MoveComparator<M, S, I> comparator) {
        this(0, 1, comparator);
    }

    /**
     * Initialize solution before GRASP algorithm is run
     * F.e: In the case of clustering algorithms, usually each cluster needs to have at least one point,
     * different solutions types may require different initialization
     *
     * @param s Solution to initialize before running the GRASP constructive method
     */
    public void beforeGRASP(S s) {
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
        while (!cl.isEmpty()) {
            // Choose an index from the candidate list following different strategies, GreedyRandom, RandomGreedy...
            int index = randomGreedy(alpha, cl);
            M chosen = cl.get(index);
            chosen.execute();
            cl = updateCandidateList(sol, chosen, cl, index);
            assert cl instanceof RandomAccess : "Candidate List should have O(1) access time";

            // Catch bugs while building a solution
            assert DoubleComparator.equals(sol.getScore(), sol.recalculateScore()) :
                    String.format("Score mismatch, incremental %s, absolute %s. Review your incremental score calculation.", sol.getScore(), sol.recalculateScore());
        }
        return sol;
    }

    private int randomGreedy(double alpha, List<M> cl) {
        // TODO can we avoid creating another list?
        var rcl = new ArrayList<M>(cl.size());
        for (var element : cl) {
            if (RandomManager.getRandom().nextDouble() >= alpha) {
                rcl.add(element);
            }
        }
        if (rcl.isEmpty()) {
            // Return a random element and call it a day
            return RandomManager.nextInt(0, cl.size());
        }

        // get best
        M best = null;
        for (M m : rcl) {
            if (best == null) best = m;
            else best = comparator.getBest(best, m);
        }
        return cl.indexOf(best);
    }


    /**
     * Generate initial candidate list. The list will be sorted by the constructor.
     *
     * @param sol Current es.urjc.etsii.grafo.solution
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    public abstract List<M> buildInitialCandidateList(S sol);

    /**
     * Update candidate list after each movement. The list will be sorted by the constructor.
     *
     * @param s     Current es.urjc.etsii.grafo.solution, move has been already applied
     * @param t     Chosen move
     * @param index index of the chosen move in the candidate list
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    public abstract List<M> updateCandidateList(S s, M t, List<M> candidateList, int index);

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
