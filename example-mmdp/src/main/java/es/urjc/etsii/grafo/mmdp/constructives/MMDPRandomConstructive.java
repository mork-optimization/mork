package es.urjc.etsii.grafo.mmdp.constructives;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.mmdp.model.MMDPInstance;
import es.urjc.etsii.grafo.mmdp.model.MMDPSolution;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a random MMDP solution: shuffles all instance nodes and takes the first {@code m} of them
 * as the selected set. Same logic as the {@code jmh} constructive, now extending Mork's
 * reusable {@link Constructive}.
 */
public class MMDPRandomConstructive extends Constructive<MMDPSolution, MMDPInstance> {

    @Override
    public MMDPSolution construct(MMDPSolution solution) {
        MMDPInstance instance = solution.getInstance();

        List<Integer> shuffled = new ArrayList<>(instance.getNumNodes());
        for (int i = 0; i < instance.getNumNodes(); i++) {
            shuffled.add(i);
        }
        CollectionUtil.shuffle(shuffled);

        List<Integer> nodes = new ArrayList<>(shuffled.subList(0, instance.getNumSolutionNodes()));

        solution.setNodes(nodes);
        solution.setScore(solution.recalculateScore());
        solution.notifyUpdate();
        return solution;
    }
}
