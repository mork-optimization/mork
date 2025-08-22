package es.urjc.etsii.grafo.TSPTW.constructives;

import es.urjc.etsii.grafo.TSPTW.model.TSPTWInstance;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.util.ArrayUtil;

public class TSPTWRandomConstructive extends Constructive<TSPTWSolution, TSPTWInstance> {

    @Override
    public TSPTWSolution construct(TSPTWSolution solution) {
        // IN --> Empty solution from solution(instance) constructor
        // OUT --> Feasible solution with an assigned score

        var instance = solution.getInstance();
        // node 0 is depot, not part of the tour.
        // Shuffle the other nodes
        int[] tour = new int[instance.n()-1];
        for (int i = 0; i < instance.n(); i++) {
            tour[i] = i+1;
        }
        ArrayUtil.shuffle(tour);
        solution.setTour(tour);

        return solution;
    }
}
