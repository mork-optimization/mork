//package es.urjc.etsii.grafo.autoconfig.factories;
//
//import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
//import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
//import es.urjc.etsii.grafo.create.Constructive;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class NullConstructiveFactory extends AlgorithmComponentFactory {
//
//    @Override
//    public Object buildComponent(Map<String, Object> params) {
//        return Constructive.nul();
//    }
//
//    @Override
//    public List<ComponentParameter> getRequiredParameters() {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public Class<?> produces() {
//        return Constructive.NullConstructive.class;
//    }
//}
