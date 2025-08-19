package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.solution.Move;

public abstract class BaseMove extends Move<VRPODSolution, VRPODInstance> {

    public abstract double getValue();

    protected BaseMove(VRPODSolution solution) {
        super(solution);
    }
}
