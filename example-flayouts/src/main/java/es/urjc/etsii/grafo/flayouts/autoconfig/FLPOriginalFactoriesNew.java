package es.urjc.etsii.grafo.flayouts.autoconfig;

import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.flayouts.constructives.FLPRandomConstructive;
import es.urjc.etsii.grafo.flayouts.constructives.grasp.FLPAddListManager;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.flayouts.shake.RandomRemoveDestructive;
import java.util.List;
import java.util.Map;

/** Expose original implementations without modifying their constructors or behavior. */
public final class FLPOriginalFactoriesNew {
    private FLPOriginalFactoriesNew() {}

    public static class RandomConstructiveFactory extends AlgorithmComponentFactory {
        @Override
        public Object buildComponent(Map<String, Object> params) { return new FLPRandomConstructive(); }
        @Override
        public List<ComponentParameter> getRequiredParameters() { return List.of(); }
        @Override
        public Class<?> produces() { return FLPRandomConstructive.class; }
    }

    public static class AddListManagerFactory extends AlgorithmComponentFactory {
        @Override
        public Object buildComponent(Map<String, Object> params) { return new FLPAddListManager(); }
        @Override
        public List<ComponentParameter> getRequiredParameters() { return List.of(); }
        @Override
        public Class<?> produces() { return FLPAddListManager.class; }
    }

    public static class SwapFactory extends AlgorithmComponentFactory {
        @Override
        public Object buildComponent(Map<String, Object> params) { return new FLPSwapNeigh(); }
        @Override
        public List<ComponentParameter> getRequiredParameters() { return List.of(); }
        @Override
        public Class<?> produces() { return FLPSwapNeigh.class; }
    }

    public static class RemoveFactory extends AlgorithmComponentFactory {
        @Override
        public Object buildComponent(Map<String, Object> params) { return new FLPRemoveNeigh(); }
        @Override
        public List<ComponentParameter> getRequiredParameters() { return List.of(); }
        @Override
        public Class<?> produces() { return FLPRemoveNeigh.class; }
    }

    public static class OptFactory extends AlgorithmComponentFactory {
        @Override
        public Object buildComponent(Map<String, Object> params) { return new FLPOptNeigh(); }
        @Override
        public List<ComponentParameter> getRequiredParameters() { return List.of(); }
        @Override
        public Class<?> produces() { return FLPOptNeigh.class; }
    }

    public static class RelocateFactory extends AlgorithmComponentFactory {
        @Override
        public Object buildComponent(Map<String, Object> params) { return new FLPRelocateNeigh(true); }
        @Override
        public List<ComponentParameter> getRequiredParameters() {
            return List.of();
        }
        @Override
        public Class<?> produces() { return FLPRelocateNeigh.class; }
    }

    public static class DestructiveFactory extends AlgorithmComponentFactory {
        @Override
        public Object buildComponent(Map<String, Object> params) { return new RandomRemoveDestructive(((Number) params.get("ratio")).doubleValue()); }
        @Override
        public List<ComponentParameter> getRequiredParameters() {
            return List.of(new ComponentParameter("ratio", double.class, ParameterType.REAL, 0.02, 0.4));
        }
        @Override
        public Class<?> produces() { return RandomRemoveDestructive.class; }
    }
}
