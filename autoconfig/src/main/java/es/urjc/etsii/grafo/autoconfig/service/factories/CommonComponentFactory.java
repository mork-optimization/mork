package es.urjc.etsii.grafo.autoconfig.service.factories;

import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;

import java.util.Map;

/**
 * Create instances of GRASP constructive method
 */
public class CommonComponentFactory {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object createGRASP(Map<String, Object> params) {
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
}
