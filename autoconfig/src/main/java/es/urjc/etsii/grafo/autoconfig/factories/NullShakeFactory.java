//package es.urjc.etsii.grafo.autoconfig.factories;
//
//import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
//import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
//import es.urjc.etsii.grafo.improve.Improver;
//import es.urjc.etsii.grafo.shake.Shake;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class NullShakeFactory extends AlgorithmComponentFactory {
//
//    @Override
//    public Object buildComponent(Map<String, Object> params) {
//        return Shake.nul();
//    }
//
//    @Override
//    public List<ComponentParameter> getRequiredParameters() {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public Class<?> produces() {
//        return Shake.NullShake.class;
//    }
//}
