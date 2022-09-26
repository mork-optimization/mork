package es.urjc.etsii.grafo.autoconfig.factories;

import es.urjc.etsii.grafo.annotations.ProvidedParamType;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;

import java.util.List;
import java.util.Map;

public abstract class GraspConstructiveFactory extends AlgorithmComponentFactory {

    public GraspBuilder initBuilder(Map<String, Object> params) {
        var graspBuilder = new GraspBuilder();
        graspBuilder.withMaximizing((boolean) params.get("maximize"));
        if(params.containsKey("alphaMin") && params.containsKey("alphaMax")){
            graspBuilder.withAlphaInRange((double) params.get("alphaMin"), (double) params.get("alphaMax"));
        } else if(params.containsKey("alpha")){
            graspBuilder.withAlphaValue((double) params.get("alpha"));
        } else {
            graspBuilder.withAlphaRandom();
        }
        // TODO review graspBuilder.withGreedyFunction();
        graspBuilder.withListManager((GRASPListManager) params.get("candidateListManager"));

        return graspBuilder;
    }

    @Override
    public List<ComponentParameter> getRequiredParameters() {
        return List.of(
                new ComponentParameter("alpha", Double.TYPE, ParameterType.REAL, 0, 1),
                new ComponentParameter("maximize", Boolean.TYPE, ParameterType.PROVIDED, new Object[]{ProvidedParamType.MAXIMIZE}),
                new ComponentParameter("candidateListManager", GRASPListManager.class, ParameterType.NOT_ANNOTATED, new Object[]{})
        );
    }
}