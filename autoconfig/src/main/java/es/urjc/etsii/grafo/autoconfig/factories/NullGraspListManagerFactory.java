//package es.urjc.etsii.grafo.autoconfig.factories;
//
//import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
//import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
//import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class NullGraspListManagerFactory extends AlgorithmComponentFactory {
//
//    @Override
//    public Object buildComponent(Map<String, Object> params) {
//        return GRASPListManager.nul();
//    }
//
//    @Override
//    public List<ComponentParameter> getRequiredParameters() {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public Class<?> produces() {
//        return GRASPListManager.NullGraspListManager.class;
//    }
//}
