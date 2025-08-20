package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.Move;

public abstract class BMSSCMove extends Move<BMSSCSolution, BMSSCInstance> {

    /**
     * Move constructor
     *
     * @param solution solution
     */
    public BMSSCMove(BMSSCSolution solution) {
        super(solution);
    }

    public abstract double getValue();
}
