package es.urjc.etsii.grafo.autoconfig.factories;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderUtil;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;

import java.util.List;
import java.util.Map;

public abstract class GraspConstructiveFactory extends AlgorithmComponentFactory {

    // We use raw types because the actual implementation types are user provided
    @SuppressWarnings({"rawtypes", "unchecked"})
    public GraspBuilder initBuilder(Map<String, Object> params) {
        var graspBuilder = new GraspBuilder();
        var fmode = AlgorithmBuilderUtil.prepareParameterValue(params.get("fmode"), FMode.class);
        graspBuilder.withMode((FMode) fmode);
        if(params.containsKey("alphaMin") && params.containsKey("alphaMax")){
            graspBuilder.withAlphaInRange((double) params.get("alphaMin"), (double) params.get("alphaMax"));
        } else if(params.containsKey("alpha")){
            Object v = params.get("alpha");
            if(v instanceof Integer vi){
                graspBuilder.withAlphaValue(vi.doubleValue());
            } else if (v instanceof Double vd){
                graspBuilder.withAlphaValue(vd);
            }
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
                new ComponentParameter("fmode", FMode.class, ParameterType.PROVIDED, new Object[0]),
                new ComponentParameter("candidateListManager", GRASPListManager.class, ParameterType.NOT_ANNOTATED, new Object[]{})
        );
    }
}
