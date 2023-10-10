package es.urjc.etsii.grafo.autoconfig.testutil;

import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.autoconfig.factories.GRGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.factories.RGGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.fill.FModeParam;
import es.urjc.etsii.grafo.autoconfig.fill.ParameterProvider;

import java.util.Arrays;
import java.util.List;

public class TestUtil {
    public static List<AlgorithmComponentFactory> getTestFactories(){
        return Arrays.asList(
                new RGGraspConstructiveFactory(),
                new GRGraspConstructiveFactory()
        );
    }

    public static List<ParameterProvider> getTestProviders(){
        return Arrays.asList(
                new FModeParam(),
                new AlgorithmNameParam()
        );
    }

}
