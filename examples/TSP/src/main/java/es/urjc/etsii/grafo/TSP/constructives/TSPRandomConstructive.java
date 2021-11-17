package es.urjc.etsii.grafo.TSP.constructives;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solver.create.Constructive;

public class TSPRandomConstructive extends Constructive<TSPSolution, TSPInstance> {

    @Override
    public TSPSolution construct(TSPSolution solution) {
        // IN --> Empty solution from solution(instance) constructor
        // OUT --> Feasible solution with an assigned score
        // TODO: Implement random constructive


        // Remember to call solution.updateLastModifiedTime() if the solution is modified without using moves!!
        solution.updateLastModifiedTime();
        //return solution;
        throw new UnsupportedOperationException("RandomConstructive not implemented yet");
    }
}
