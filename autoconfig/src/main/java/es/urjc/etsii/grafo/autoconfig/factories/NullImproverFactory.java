//package es.urjc.etsii.grafo.autoconfig.factories;
//
//import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
//import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
//import es.urjc.etsii.grafo.create.Constructive;
//import es.urjc.etsii.grafo.improve.Improver;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class NullImproverFactory extends AlgorithmComponentFactory {
//
//    @Override
//    public Object buildComponent(Map<String, Object> params) {
//        return Improver.nul();
//    }
//
//    @Override
//    public List<ComponentParameter> getRequiredParameters() {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public Class<?> produces() {
//        return Improver.NullImprover.class;
//    }
//}
