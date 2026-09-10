package es.urjc.etsii.grafo.cwp.constructives;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.cwp.model.CWPInstance;
import es.urjc.etsii.grafo.cwp.model.CWPSolution;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a random CWP solution: a random ordering (shuffle) of all the instance vertices. Same
 * logic as the {@code jmh} constructive, now extending Mork's reusable {@link Constructive}.
 */
public class CWPRandomConstructive extends Constructive<CWPSolution, CWPInstance> {

    @Override
    public CWPSolution construct(CWPSolution solution) {
        CWPInstance instance = solution.getInstance();

        List<Integer> order = new ArrayList<>(instance.getNumNodes());
        for (int i = 0; i < instance.getNumNodes(); i++) {
            order.add(i);
        }
        CollectionUtil.shuffle(order);

        solution.setOrder(order);
        solution.setScore(solution.recalculateScore());
        solution.notifyUpdate();
        return solution;
    }
}
