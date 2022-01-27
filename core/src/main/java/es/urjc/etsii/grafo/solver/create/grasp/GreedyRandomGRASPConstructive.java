package es.urjc.etsii.grafo.solver.create.grasp;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Reconstructive;
import es.urjc.etsii.grafo.util.ValidationUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

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
public class GreedyRandomGRASPConstructive<M extends Move<S, I>, S extends Solution<S,I>, I extends Instance> extends Reconstructive<S, I> {
    private static final Logger log = Logger.getLogger(GreedyRandomGRASPConstructive.class.getName());

    /**
     * String explaining how alpha provider is generating alpha values.
     * Example: FIXED{a=0.25}
     */
    protected final String randomType;

    /**
     * GRASP candidate list manager
     */
    protected final GRASPListManager<M, S, I> candidateListManager;
    private final AlphaProvider alphaProvider;
    private final boolean maximizing;

    /**
     * GRASP Constructor, mantains a fixed alpha value
     * Stops when the neighborhood does not provide any movement
     *
     * @param alpha      Randomness, adjusts the candidate list size.
     *                   Takes values between [0,1] being 1 → totally random, 0 → full greedy.
     * @param maximizing true if we are maximizing the score, false if minimizing
     * @param candidateListManager list manager, implemented by the user
     */
    public GreedyRandomGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double alpha, boolean maximizing) {
        this.candidateListManager = candidateListManager;
        assert isGreaterOrEqualsThan(alpha, 0) && isLessOrEquals(alpha, 1);
        randomType = String.format("FIXED{a=%.2f}", alpha);
        alphaProvider = () -> alpha;
        this.maximizing = maximizing;
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction
     * Alpha Takes values between [0,1] being 1 → totally random, 0 → full greedy.
     *
     * @param minAlpha   minimum value for the random alpha
     * @param maxAlpha   maximum value for the random alpha
     * @param maximizing true if maximizing, false if minimizing
     * @param candidateListManager list manager, implemented by the user
     */
    public GreedyRandomGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double minAlpha, double maxAlpha, boolean maximizing) {
        this.candidateListManager = candidateListManager;
        assert isGreaterOrEqualsThan(minAlpha, 0) && isLessOrEquals(minAlpha, 1);
        assert isGreaterOrEqualsThan(maxAlpha, 0) && isLessOrEquals(maxAlpha, 1);
        assert isGreaterThan(maxAlpha, minAlpha);

        alphaProvider = () -> RandomManager.getRandom().nextDouble() * (maxAlpha - minAlpha) + minAlpha;
        randomType = String.format("RANGE{min=%.2f, max=%.2f}", minAlpha, maxAlpha);
        this.maximizing = maximizing;
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction, between 0 and 1 (inclusive).
     *
     * @param candidateListManager candidate list manager, implemented by the user
     * @param maximizing True if maximizing, false if minimizing
     */
    public GreedyRandomGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, boolean maximizing) {
        this(candidateListManager, 0, 1, maximizing);
    }

    /** {@inheritDoc} */
    @Override
    public S construct(S sol) {
        candidateListManager.beforeGRASP(sol);
        return assignMissing(sol);
    }

    /**
     * Assign missing elements to solution.
     * This method ends when the candidate list is empty.
     * The difference between this method and construct is that this method does not call beforeGRASP().
     * This method can be used in algorithms such as iterated greedy during the reconstruction phase.
     *
     * @param sol Solution to complete
     * @return Completed solution.
     */
    public S assignMissing(S sol) {
        double alpha = alphaProvider.getAlpha();
        var cl = candidateListManager.buildInitialCandidateList(sol);
        assert cl instanceof RandomAccess : "Candidate List should have O(1) access time";
        while (!cl.isEmpty()) {
            int index = greedyRandom(alpha, cl);
            M chosen = cl.get(index);
            chosen.execute();
            cl = candidateListManager.updateCandidateList(sol, chosen, cl, index);
            ValidationUtil.assertFastAccess(cl);
            ValidationUtil.assertValidScore(sol);
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
        int index = RandomManager.getRandom().nextInt(0, next);
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


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "GRGRASP" + "{" +
                "a='" + randomType + '\'' +
                ", l=" + candidateListManager +
                '}';
    }

    @Override
    public S reconstruct(S solution) {
        return assignMissing(solution);
    }
}
