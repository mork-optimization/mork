package es.urjc.etsii.grafo.autoconfig.testutil;

import es.urjc.etsii.grafo.autoconfig.factories.GRGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.factories.RGGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;

import java.util.Arrays;
import java.util.List;

public class TestUtil {
    public static List<AlgorithmComponentFactory> getTestFactories(){
        return Arrays.asList(
                new RGGraspConstructiveFactory(),
                new GRGraspConstructiveFactory()
        );
    }
}
