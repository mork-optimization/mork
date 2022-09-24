package es.urjc.etsii.grafo.autoconfig.factories;

import es.urjc.etsii.grafo.create.grasp.RandomGreedyGRASPConstructive;

import java.util.Map;

public class RGGraspConstructiveFactory extends GraspConstructiveFactory {

    @Override
    public Object buildComponent(Map<String, Object> params) {
        var graspBuilder = super.initBuilder(params);
        graspBuilder.withStrategyRandomGreedy();
        return graspBuilder.build();
    }

    @Override
    public Class<?> produces() {
        return RandomGreedyGRASPConstructive.class;
    }
}
