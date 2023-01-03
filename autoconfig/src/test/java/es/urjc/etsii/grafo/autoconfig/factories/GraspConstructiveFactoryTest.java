package es.urjc.etsii.grafo.autoconfig.factories;

import es.urjc.etsii.grafo.create.grasp.GRASPConstructive;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static es.urjc.etsii.grafo.algorithms.FMode.MINIMIZE;

class GraspConstructiveFactoryTest {

    private final GRGraspConstructiveFactory grfactory = new GRGraspConstructiveFactory();
    private final RGGraspConstructiveFactory rgfactory = new RGGraspConstructiveFactory();

    private final Map<String, Object> fixedAlpha = Map.of(
            "fmode", MINIMIZE,
            "alpha", 0.2d,
            "candidateListManager", GRASPListManager.nul()
    );

    private final Map<String, Object> alphaRange = Map.of(
            "fmode", MINIMIZE,
            "alphaMin", 0.1d,
            "alphaMax", 0.3d,
            "candidateListManager", GRASPListManager.nul()
    );


    @Test
    void testBuildGRComponent() {
        Assertions.assertDoesNotThrow(() -> grfactory.buildComponent(this.fixedAlpha));
        Assertions.assertDoesNotThrow(() -> grfactory.buildComponent(this.alphaRange));
    }

    @Test
    void testBuildRGComponent() {
        Assertions.assertDoesNotThrow(() -> rgfactory.buildComponent(this.fixedAlpha));
        Assertions.assertDoesNotThrow(() -> rgfactory.buildComponent(this.alphaRange));
    }

    @Test
    void correctType() {
        GRASPConstructive.class.isAssignableFrom(grfactory.produces());
        GRASPConstructive.class.isAssignableFrom(rgfactory.produces());
    }
}
