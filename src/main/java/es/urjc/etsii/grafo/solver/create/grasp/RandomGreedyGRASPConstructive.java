package es.urjc.etsii.grafo.solver.create.grasp;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.improve.DefaultMoveComparator;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.RandomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
public class RandomGreedyGRASPConstructive<M extends Move<S, I>, S extends Solution<I>, I extends Instance> extends Constructive<S, I> {
    private static final Logger log = Logger.getLogger(RandomGreedyGRASPConstructive.class.getName());
    protected final String randomType;
    protected final GRASPListManager<M, S, I> candidateListManager;
    private final AlphaProvider alphaProvider;
    private final MoveComparator<M, S, I> comparator;

    public RandomGreedyGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double alpha, boolean maximizing) {
        this.candidateListManager = candidateListManager;
        this.comparator = new DefaultMoveComparator<>(maximizing);
        assert isGreaterOrEqualsThan(alpha, 0) && isLessOrEquals(alpha, 1);

        randomType = String.format("FIXED{a=%.2f}", alpha);
        alphaProvider = () -> alpha;
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction
     * Alpha Takes values between [0,1] being 1 → totally random, 0 → full greedy.
     *
     * @param minAlpha   minimum value for the random alpha
     * @param maxAlpha   maximum value for the random alpha
     * @param comparator
     */
    public RandomGreedyGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double minAlpha, double maxAlpha, MoveComparator<M, S, I> comparator) {
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
     * Alpha Takes values between [0,1] being 1 → totally random, 0 → full greedy.
     *
     * @param minAlpha   minimum value for the random alpha
     * @param maxAlpha   maximum value for the random alpha
     * @param maximizing True if maximizing, false if minimizing
     */
    public RandomGreedyGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double minAlpha, double maxAlpha, boolean maximizing) {
        this(candidateListManager, minAlpha, maxAlpha, new DefaultMoveComparator<>(maximizing));
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction, between 0 and 1 (inclusive).
     */
    public RandomGreedyGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, boolean maximizing) {
        this(candidateListManager, 0, 1, maximizing);
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
        while (!cl.isEmpty()) {
            int index = randomGreedy(alpha, cl);
            M chosen = cl.get(index);
            chosen.execute();
            cl = candidateListManager.updateCandidateList(sol, chosen, cl, index);
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
        // Declare another random so we only use a value of the original random per iteration
        Random r = new Random(RandomManager.getRandom().nextLong());
        for (var element : cl) {
            if (r.nextDouble() >= alpha) {
                rcl.add(element);
            }
        }
        if (rcl.isEmpty()) {
            // Return a random element and call it a day
            return r.nextInt(cl.size());
        }

        // get best
        M best = null;
        for (M m : rcl) {
            if (best == null) best = m;
            else best = comparator.getBest(best, m);
        }

        // Choose a random from all the items with equal best score
        log.fine(String.format("Best score found: %s", best.getValue()));
        var besties = new ArrayList<M>(cl.size());
        for (M m : rcl) {
            if (DoubleComparator.equals(m.getValue(), best.getValue())) {
                besties.add(m);
            }
        }
        int chosen = RandomManager.nextInt(0, besties.size());
        log.fine(String.format("Number of movements with same score: %s, chosen: %s", besties.size(), besties.get(chosen)));
        return cl.indexOf(besties.get(chosen));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "randomType='" + randomType + '\'' +
                ", list=" + candidateListManager +
                '}';
    }
}
