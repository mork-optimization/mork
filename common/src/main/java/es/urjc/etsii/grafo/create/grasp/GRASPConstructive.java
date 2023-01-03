package es.urjc.etsii.grafo.create.grasp;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.ValidationUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.ToDoubleFunction;

public abstract class GRASPConstructive<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends Reconstructive<S, I> {

    protected final String alphaType;
    protected final GRASPListManager<M, S, I> candidateListManager;
    protected final AlphaProvider alphaProvider;
    protected final ToDoubleFunction<M> greedyFunction;
    // A move is better than another one when its greedy function is greater if maximizing or less than if minimizing
    protected final BiPredicate<Double, Double> isBetter;
    protected final BiPredicate<Double, Double> isBetterOrEquals;

    /**
     * Should we maximize or minimize
     */
    protected final FMode fmode;

    protected GRASPConstructive(FMode fmode, GRASPListManager<M, S, I> candidateListManager, ToDoubleFunction<M> greedyFunction, AlphaProvider provider, String alphaType) {
        this.candidateListManager = candidateListManager;
        this.greedyFunction = greedyFunction;
        this.alphaProvider = provider;
        this.alphaType = alphaType;
        this.fmode = fmode;
        this.isBetter = DoubleComparator.isBetterFunction(this.fmode);
        this.isBetterOrEquals = DoubleComparator.isBetterOrEqualsFunction(this.fmode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public S construct(S solution) {
        candidateListManager.beforeGRASP(solution);
        var constructedSolution = assignMissing(solution);
        candidateListManager.afterGRASP(constructedSolution);
        return constructedSolution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public S reconstruct(S solution) {
        return assignMissing(solution);
    }

    /**
     * Assign missing elements to solution.
     * This method ends when the candidate list is empty.
     * The difference between this method and construct is that this method does not call beforeGRASP().
     * This method can be used in algorithms such as iterated greedy during the reconstruction phase.
     *
     * @param solution Solution to complete
     * @return Completed solution.
     */
    public S assignMissing(S solution) {
        double alpha = alphaProvider.getAlpha();
        var cl = candidateListManager.buildInitialCandidateList(solution);
        assert ValidationUtil.assertFastAccess(cl);
        while (!cl.isEmpty()) {
            int index = getCandidateIndex(alpha, cl);
            M chosen = cl.get(index);
            chosen.execute(solution);
            cl = candidateListManager.updateCandidateList(solution, chosen, cl, index);
            assert ValidationUtil.assertFastAccess(cl);
            ValidationUtil.assertValidScore(solution);
        }
        return solution;
    }

    /**
     * Get candidate using any strategy
     *
     * @param alpha alpha value
     * @param cl    candidate list
     * @return candidate index
     */
    protected abstract int getCandidateIndex(double alpha, List<M> cl);
}
