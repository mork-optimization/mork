package es.urjc.etsii.grafo.create;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConstructiveTest {
    @Test
    void nullConstructive(){
        TestSolution solution = new TestSolution(new TestInstance("Fake instance"));
        Constructive<TestSolution, TestInstance> constructive = new NullConstructive<>();
        Assertions.assertNotNull(constructive);
        TestSolution improved = Assertions.assertDoesNotThrow(() -> constructive.construct(solution));
        Assertions.assertEquals(solution, improved);
    }
}
