package es.urjc.etsii.grafo.autoconfig.factories;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.create.grasp.GRASPConstructive;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static es.urjc.etsii.grafo.algorithms.FMode.MINIMIZE;

class GraspConstructiveFactoryTest {

    static final Objective<?,?,?> defaultMin = Objective.of("Test", FMode.MINIMIZE, TestSolution::getScore, TestMove::getScoreChange);
    @BeforeAll
    static void setup() {
        Context.Configurator.setObjectives(defaultMin);
    }

    private final GRGraspConstructiveFactory grfactory = new GRGraspConstructiveFactory();
    private final RGGraspConstructiveFactory rgfactory = new RGGraspConstructiveFactory();

    private final Map<String, Object> fixedAlpha = Map.of(
            "objective", defaultMin.getName(),
            "alpha", 0.2d,
            "candidateListManager", GRASPListManager.nul()
    );

    private final Map<String, Object> alphaRange = Map.of(
            "objective", defaultMin.getName(),
            "alphaMin", 0.1d,
            "alphaMax", 0.3d,
            "candidateListManager", GRASPListManager.nul()
    );


    @Test
    void testBuildGRComponent() {
        Assertions.assertDoesNotThrow(() -> grfactory.buildComponent(this.fixedAlpha));
        Assertions.assertDoesNotThrow(() -> grfactory.buildComponent(this.alphaRange));

        Assertions.assertThrows(ClassCastException.class, () -> grfactory.buildComponent(Map.of(
                "objective", MINIMIZE,
                "alpha", 0.2d,
                "candidateListManager", GRASPListManager.nul()
        )));
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
