package es.urjc.etsii.grafo.autoconfig.factories;

import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.create.grasp.GRASPConstructive;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GraspConstructiveFactory extends AlgorithmComponentFactory {

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
        String strategy = (String) params.get("strategy");
        if(strategy == null){
            throw new AlgorithmParsingException("Missing required strategy parameter");
        } else if(strategy.equals("greedyRandom")){
            graspBuilder.withStrategyGreedyRandom();
        } else if (strategy.equals("randomGreedy")){
            graspBuilder.withStrategyRandomGreedy();
        } else {
            throw new AlgorithmParsingException("Unknown grasp strategy type: " + strategy);
        }
        return graspBuilder.build();
    }

    @Override
    public List<ComponentParameter> getRequiredParameters() {
        return Arrays.asList(
                new ComponentParameter("alpha", ParameterType.REAL, 0, 1),
                new ComponentParameter("strategy", ParameterType.CATEGORICAL, new Object[]{"greedyRandom", "randomGreedy"})
        );
    }

    @Override
    public Class<?> produces() {
        return GRASPConstructive.class;
    }
}
