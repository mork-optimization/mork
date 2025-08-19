package es.urjc.etsii.grafo.BMSSC.model.sol;

import es.urjc.etsii.grafo.BMSSC.model.BMSSCInstance;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;

public class BMSSCSolutionBuilder extends SolutionBuilder<BMSSCSolution, BMSSCInstance> {
    @Override
    public BMSSCSolution initializeSolution(BMSSCInstance instance) {
        return new BMSSCSolution(instance);
    }
}
