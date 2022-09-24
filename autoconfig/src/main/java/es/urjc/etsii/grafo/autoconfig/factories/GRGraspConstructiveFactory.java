package es.urjc.etsii.grafo.autoconfig.factories;

import es.urjc.etsii.grafo.create.grasp.GreedyRandomGRASPConstructive;

import java.util.Map;

public class GRGraspConstructiveFactory extends GraspConstructiveFactory {

    @Override
    public Object buildComponent(Map<String, Object> params) {
        var graspBuilder = super.initBuilder(params);
        graspBuilder.withStrategyGreedyRandom();
        return graspBuilder.build();
    }

    @Override
    public Class<?> produces() {
        return GreedyRandomGRASPConstructive.class;
    }
}
