package es.urjc.etsii.grafo.create;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstructiveTest {
    @Test
    public void nullConstructive(){
        TestSolution solution = new TestSolution(new TestInstance("Fake instance"));
        Constructive<TestSolution, TestInstance> constructive = Constructive.nul();
        Assertions.assertNotNull(constructive);
        TestSolution improved = Assertions.assertDoesNotThrow(() -> constructive.construct(solution));
        Assertions.assertEquals(solution, improved);
    }
}
