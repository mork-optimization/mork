package es.urjc.etsii.grafo.autoconfig.factories;

import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;
import es.urjc.etsii.grafo.create.grasp.RandomGreedyGRASPConstructive;

import java.util.List;
import java.util.Map;

public class RGGraspConstructiveFactory extends AlgorithmComponentFactory {

    @Override
    public Object buildComponent(Map<String, Object> params) {
        var graspBuilder = new GraspBuilder();
        graspBuilder.withMaximizing((boolean) params.get("maximizing"));
        if(params.containsKey("alphaMin") && params.containsKey("alphaMax")){
            graspBuilder.withAlphaInRange((double) params.get("alphaMin"), (double) params.get("alphaMax"));
        } else if(params.containsKey("alpha")){
            graspBuilder.withAlphaValue((double) params.get("alpha"));
        } else {
            graspBuilder.withAlphaRandom();
        }
        // TODO review graspBuilder.withGreedyFunction();
        graspBuilder.withListManager((GRASPListManager) params.get("candidateListManager"));
        graspBuilder.withStrategyRandomGreedy();
        return graspBuilder.build();
    }

    @Override
    public List<ComponentParameter> getRequiredParameters() {
        return List.of(
                new ComponentParameter("alpha", Double.TYPE, ParameterType.REAL, 0, 1)
        );
    }

    @Override
    public Class<?> produces() {
        return RandomGreedyGRASPConstructive.class;
    }
}
