package es.urjc.etsii.grafo.TSP.model;

import es.urjc.etsii.grafo.solution.Move;

public abstract class TSPBaseMove extends Move<TSPSolution, TSPInstance> {

    protected double distanceDelta;

    /**
     * Move constructor
     * @param solution solution
     */
    public TSPBaseMove(TSPSolution solution) {
        super(solution);
    }

    public double getDistanceDelta() {
        return distanceDelta;
    }
}
