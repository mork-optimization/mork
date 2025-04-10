package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ImproverTest {

    private static final Objective<TestMove, TestSolution, TestInstance> OBJ_MAX = Objective.of("Test", FMode.MAXIMIZE, TestSolution::getScore, TestMove::getScoreChange);

    @BeforeAll
    public static void init(){
        Metrics.disableMetrics();
        Context.Configurator.setObjectives(OBJ_MAX);
    }

    @Test
    void nullImprover(){
        TestSolution solution = new TestSolution(new TestInstance("Fake instance"));
        Improver<TestSolution, TestInstance> improver = Improver.nul();
        Assertions.assertNotNull(improver);
        TestSolution improved = Assertions.assertDoesNotThrow(() -> improver.improve(solution));
        Assertions.assertEquals(solution, improved);
    }
}
