package es.urjc.etsii.grafo.create.grasp;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.random.RandomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * GRASP Constructive method using the greedy random strategy. Use {@link GraspBuilder} to create instances of GRASP constructives, do not use this class directly.
 *
 * @param <M> Move type
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class GreedyRandomGRASPConstructive<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends GRASPConstructive<M, S, I> {

    private static final Logger log = LoggerFactory.getLogger(GreedyRandomGRASPConstructive.class);

    protected GreedyRandomGRASPConstructive(GRASPListManager<M, S, I> candidateListManager, Function<M, Double> greedyFunction, AlphaProvider provider, String alphaType, Boolean maximize) {
        super(candidateListManager, greedyFunction, provider, alphaType, maximize);
    }

    /**
     * Get candidate using greedy random strategy
     *
     * @param alpha alpha value
     * @param cl    candidate list
     * @return candidate index
     */
    @Override
    protected int getCandidateIndex(double alpha, List<M> cl) {
        var minMax = getMinMax(cl);
        double min = minMax[0];
        double max = minMax[1];
        int[] validIndexes = new int[cl.size()];
        int next = 0;

        double limit = maximizing ?
                max + alpha * (min - max) :
                min + alpha * (max - min);

        for (int i = 0, clSize = cl.size(); i < clSize; i++) {
            M move = cl.get(i);
            double value = greedyFunction.apply(move);
            if (isBetterOrEquals.test(value, limit)) {
                validIndexes[next++] = i;
            }
        }

        int index = RandomManager.getRandom().nextInt(0, next);
        return validIndexes[index];
    }

    private double[] getMinMax(List<M> cl) {
        assert !cl.isEmpty();
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (M m : cl) {
            double value = greedyFunction.apply(m);
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }
        assert min != Double.MAX_VALUE;
        assert max != -Double.MAX_VALUE;

        return new double[]{min, max};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GRGRASP" + "{" +
                "a='" + alphaType + '\'' +
                ", l=" + candidateListManager +
                '}';
    }

}
