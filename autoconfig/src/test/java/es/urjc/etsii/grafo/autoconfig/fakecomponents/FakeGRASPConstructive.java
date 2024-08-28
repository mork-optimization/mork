package es.urjc.etsii.grafo.autoconfig.fakecomponents;

import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.create.grasp.AlphaProvider;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.random.RandomManager;

import static es.urjc.etsii.grafo.util.DoubleComparator.*;

public class FakeGRASPConstructive<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends Reconstructive<S,I> {

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
    private final Objective<?,?,?> objective;

    /**
     * GRASP Constructor, mantains a fixed alpha value
     * Stops when the neighborhood does not provide any movement
     *
     * @param alpha      Randomness, adjusts the candidate list size.
     *                   Takes values between [0,1] being 1 → totally random, 0 → full greedy.
     * @param objective objective function to maximize/minimize
     * @param candidateListManager list manager, implemented by the user
     */
    public FakeGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double alpha, Objective<?,?,?> objective) {
        this.candidateListManager = candidateListManager;
        this.objective = objective;
        assert isGreaterOrEquals(alpha, 0) && isLessOrEquals(alpha, 1);

        randomType = String.format("FIXED{a=%.2f}", alpha);
        alphaProvider = () -> alpha;
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction
     * Alpha Takes values between [0,1] being 1 → totally random, 0 → full greedy.
     *
     * @param minAlpha   minimum value for the random alpha
     * @param maxAlpha   maximum value for the random alpha
     * @param objective objective function to maximize/minimize
     * @param candidateListManager Candidate List Manager
     */
    public FakeGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, double minAlpha, double maxAlpha, Objective<?,?,?> objective) {
        this.candidateListManager = candidateListManager;
        this.objective = objective;
        assert isGreaterOrEquals(minAlpha, 0) && isLessOrEquals(minAlpha, 1);
        assert isGreaterOrEquals(maxAlpha, 0) && isLessOrEquals(maxAlpha, 1);
        assert isGreater(maxAlpha, minAlpha);

        alphaProvider = () -> RandomManager.getRandom().nextDouble() * (maxAlpha - minAlpha) + minAlpha;
        randomType = String.format("RANGE{min=%.2f, max=%.2f}", minAlpha, maxAlpha);
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction, between 0 and 1 (inclusive).
     *
     * @param candidateListManager candidate list manager, implemented by the user
     * @param objective objective function to maximize/minimize
     */
    public FakeGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, Objective<?,?,?> objective) {
        this(candidateListManager, 0, 1, objective);
    }

    @Override
    public S construct(S solution) {
        throw new UnsupportedOperationException();
    }

    @Override
    public S reconstruct(S solution) {
        throw new UnsupportedOperationException();
    }
}
