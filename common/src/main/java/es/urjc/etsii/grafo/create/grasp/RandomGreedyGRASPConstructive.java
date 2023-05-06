package es.urjc.etsii.grafo.create.grasp;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * GRASP Constructive method
 *
 * @param <M> Move type
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class RandomGreedyGRASPConstructive<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends GRASPConstructive<M, S, I> {

    private static final Logger log = LoggerFactory.getLogger(RandomGreedyGRASPConstructive.class);

    protected RandomGreedyGRASPConstructive(FMode fmode, GRASPListManager<M, S, I> candidateListManager, ToDoubleFunction<M> greedyFunction, AlphaProvider provider, String alphaType) {
        super(fmode, candidateListManager, greedyFunction, provider, alphaType);
    }

    /**
     * Get candidate using random greedy strategy
     *
     * @param alpha alpha value
     * @param cl    candidate list
     * @return candidate index
     */
    @Override
    protected int getCandidateIndex(double alpha, List<M> cl) {
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

        M best = CollectionUtil.getBest(rcl, greedyFunction, fmode::isBetter);
        double bestValue = greedyFunction.applyAsDouble(best);
        assert best != null : "null best with RCL:" + rcl;

        // Choose a random from all the items with equal best score
        log.debug("Best score found: {}", bestValue);
        var besties = new ArrayList<M>(cl.size());
        for (M m : rcl) {
            if (DoubleComparator.equals(greedyFunction.applyAsDouble(m), bestValue)) {
                besties.add(m);
            }
        }
        int chosen = RandomManager.getRandom().nextInt(0, besties.size());
        log.debug("Number of movements with same score: {}, chosen: {}", besties.size(), besties.get(chosen));
        return cl.indexOf(besties.get(chosen));
    }

    @Override
    public String toString() {
        return "RGGRASP{" +
                "a='" + alphaType + '\'' +
                ", l=" + candidateListManager +
                ", mode=" + this.fmode +
                ", g=" + greedyFunction +
                '}';
    }
}
