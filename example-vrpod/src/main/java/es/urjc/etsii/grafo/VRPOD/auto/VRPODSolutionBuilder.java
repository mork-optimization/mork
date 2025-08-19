package es.urjc.etsii.grafo.VRPOD.auto;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;

public class VRPODSolutionBuilder extends SolutionBuilder<VRPODSolution, VRPODInstance> {
    @Override
    public VRPODSolution initializeSolution(VRPODInstance instance) {
        return new VRPODSolution(instance);
    }
}
