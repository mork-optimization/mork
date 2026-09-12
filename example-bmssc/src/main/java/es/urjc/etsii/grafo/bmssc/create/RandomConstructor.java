package es.urjc.etsii.grafo.bmssc.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.AssignMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.Context;

public class RandomConstructor extends Constructive<BMSSCSolution, BMSSCInstance> {
    @AutoconfigConstructor
    public RandomConstructor() {}

    @Override
    public BMSSCSolution construct(BMSSCSolution solution) {
        var instance = solution.getInstance();
        if (solution.getNotAssignedPoints().size() != instance.n) {
            throw new IllegalArgumentException("Construction requires an empty solution");
        }
        BMSSCUtil.withPartialSolution(() -> {
            int[] points = new int[instance.n];
            for (int p = 0; p < points.length; p++) points[p] = p;
            ArrayUtil.shuffle(points);
            int next = 0;
            for (int c = 0; c < instance.k; c++) {
                for (int i = 0; i < instance.getClusterSize(c); i++) {
                    new AssignMove(solution, points[next++], c).execute(solution);
                }
            }
            return solution;
        });
        assert Context.validate(solution);
        Metrics.addCurrentObjectives(solution);
        return solution;
    }
}
