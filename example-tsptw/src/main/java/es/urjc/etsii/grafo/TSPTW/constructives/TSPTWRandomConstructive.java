package es.urjc.etsii.grafo.TSPTW.constructives;

import es.urjc.etsii.grafo.TSPTW.model.TSPTWInstance;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWSolution;
import es.urjc.etsii.grafo.create.Constructive;

public class TSPTWRandomConstructive extends Constructive<TSPTWSolution, TSPTWInstance> {

    @Override
    public TSPTWSolution construct(TSPTWSolution solution) {
        // IN --> Empty solution from solution(instance) constructor
        // OUT --> Feasible solution with an assigned score
        // TODO: Implement random constructive


        // Remember to call solution.notifyUpdate() if the solution is modified without using moves!!
        solution.notifyUpdate();
        //return solution;
        throw new UnsupportedOperationException("RandomConstructive not implemented yet");
    }
}
