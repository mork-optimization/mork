package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ImproverTest {
    @BeforeAll
    public static void init(){
        Metrics.disableMetrics();
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
