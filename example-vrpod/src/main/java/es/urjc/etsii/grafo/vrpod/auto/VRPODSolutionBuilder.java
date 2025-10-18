package es.urjc.etsii.grafo.vrpod.auto;

import es.urjc.etsii.grafo.vrpod.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.vrpod.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;

public class VRPODSolutionBuilder extends SolutionBuilder<VRPODSolution, VRPODInstance> {
    @Override
    public VRPODSolution initializeSolution(VRPODInstance instance) {
        return new VRPODSolution(instance);
    }
}
