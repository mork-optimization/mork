package es.urjc.etsii.grafo.vrpod.model.solution;

import es.urjc.etsii.grafo.vrpod.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.solution.Move;

public abstract class BaseMove extends Move<VRPODSolution, VRPODInstance> {

    public abstract double getValue();

    protected BaseMove(VRPODSolution solution) {
        super(solution);
    }
}
