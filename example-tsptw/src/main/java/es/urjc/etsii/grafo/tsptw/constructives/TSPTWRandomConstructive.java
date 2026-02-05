package es.urjc.etsii.grafo.tsptw.constructives;

import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.util.ArrayUtil;

public class TSPTWRandomConstructive extends Constructive<TSPTWSolution, TSPTWInstance> {

    @Override
    public TSPTWSolution construct(TSPTWSolution solution) {
        var instance = solution.getInstance();

        int[] tour = new int[instance.n() - 1];
        for (int k = 0; k < instance.n() - 1; k++) {
            tour[k] = k + 1;
        }
        ArrayUtil.shuffle(tour);
        solution.add(tour);

        solution.notifyUpdate();
        return solution;
    }
}
