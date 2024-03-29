package es.urjc.etsii.grafo.shake;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShakeTest {

    @Test
    void nullShake(){
        TestSolution solution = new TestSolution(new TestInstance("Fake instance"));
        Shake<TestSolution, TestInstance> shake = Shake.nul();
        Assertions.assertNotNull(shake);
        TestSolution improved = Assertions.assertDoesNotThrow(() -> shake.shake(solution, 0));
        Assertions.assertEquals(solution, improved);
    }
}
