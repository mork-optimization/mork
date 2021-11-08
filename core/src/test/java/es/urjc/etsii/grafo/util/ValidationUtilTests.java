package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidationUtilTests {

    @Test
    public void testPositiveTTB(){
        TestInstance testInstance = new TestInstance("TestInstance");
        TestSolution solution = new TestSolution(testInstance);

        Assertions.assertThrows(AssertionError.class,() -> ValidationUtil.positiveTTB(solution));
        solution.updateLastModifiedTime();
        Assertions.assertDoesNotThrow(() -> ValidationUtil.positiveTTB(solution));
    }
}
