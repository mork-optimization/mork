package es.urjc.etsii.grafo.autoconfig.testutil;

import es.urjc.etsii.grafo.autoconfig.factories.GraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;

import java.util.Arrays;
import java.util.List;

public class TestUtil {
    public static List<AlgorithmComponentFactory> getTestFactories(){
        return Arrays.asList(
                new GraspConstructiveFactory()
        );
    }
}
