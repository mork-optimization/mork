package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.Move;

public abstract class BMSSCMove extends Move<BMSSCSolution, BMSSCInstance> {
    protected BMSSCMove(BMSSCSolution solution) { super(solution); }
    public abstract double getCostDelta();
}
