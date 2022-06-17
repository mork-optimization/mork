package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImproverTest {

    @Test
    public void nullImprover(){
        TestSolution solution = new TestSolution(new TestInstance("Fake instance"));
        Improver<TestSolution, TestInstance> improver = Improver.nul();
        Assertions.assertNotNull(improver);
        TestSolution improved = Assertions.assertDoesNotThrow(() -> improver.improve(solution));
        Assertions.assertEquals(solution, improved);
    }
}
