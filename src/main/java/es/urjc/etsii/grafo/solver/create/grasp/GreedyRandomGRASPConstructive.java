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
    private final boolean maximizing;

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
        this.maximizing = maximizing;
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
        this.candidateListManager = candidateListManager;
        this.comparator = new DefaultMoveComparator<>(maximizing);
        assert isGreaterOrEqualsThan(minAlpha, 0) && isLessOrEquals(minAlpha, 1);
        assert isGreaterOrEqualsThan(maxAlpha, 0) && isLessOrEquals(maxAlpha, 1);
        assert isGreaterThan(maxAlpha, minAlpha);

        alphaProvider = () -> RandomManager.getRandom().nextDouble() * (maxAlpha - minAlpha) + minAlpha;
        randomType = String.format("RANGE{min=%.2f, max=%.2f}", minAlpha, maxAlpha);
        this.maximizing = maximizing;
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction, between 0 and 1 (inclusive).
     */
    public GreedyRandomGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, boolean maximizing) {
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

        var minMax = getMinMax(cl);
        double min = minMax[0];
        double max = minMax[1];
        int[] validIndexes = new int[cl.size()];
        int next = 0;

        if(maximizing){
            double limit = max + alpha * (min - max);
            // Filter which positions contain valid elements
            for (int i = 0, clSize = cl.size(); i < clSize; i++) {
                if (isGreaterOrEqualsThan(cl.get(i).getValue(), limit)) {
                    validIndexes[next++] = i;
                }
            }
        } else {
            double limit = min + alpha * (max - min);

            // Filter which positions contain valid elements
            for (int i = 0, clSize = cl.size(); i < clSize; i++) {
                if (isLessOrEquals(cl.get(i).getValue(), limit)) {
                    validIndexes[next++] = i;
                }
            }
        }
        int index = RandomManager.nextInt(0, next);
        return validIndexes[index];
    }

    private double[] getMinMax(List<M> cl){
        assert !cl.isEmpty();
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (M m : cl) {
            if (m.getValue() < min){
                min = m.getValue();
            }
            if (m.getValue() > max){
                max = m.getValue();
            }
        }
        assert min != Double.MAX_VALUE;
        assert max != -Double.MAX_VALUE;

        return new double[]{min, max};
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "randomType='" + randomType + '\'' +
                ", comparator=" + comparator +
                '}';
    }


}
