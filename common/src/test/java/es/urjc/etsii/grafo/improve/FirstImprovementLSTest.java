package es.urjc.etsii.grafo.improve;


import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class FirstImprovementLSTest extends BaseLSTest {

    @Test
    void testOrderMinimizing(){
        testOrder(FMode.MINIMIZE);
    }

    @Test
    void testOrderMaximizing(){
        testOrder(FMode.MAXIMIZE);
    }

    public void testOrder(FMode fmode){
        double[] values = {
                0, 0.5, 195, -95438, 196341, -99614, 12, 861523, Math.PI, Math.E
        };
        var solution = new TestSolution(new TestInstance("Fake Instance"));
        var mockNeighborhod = getNeighborhoodMock(fmode, values, solution);
        var firstImprovementLS = new LocalSearchFirstImprovement<>(fmode, mockNeighborhod);
        var chosenMove = firstImprovementLS.getMove(solution);
        Assertions.assertNotNull(chosenMove);
        double value = chosenMove.getValue();
        if(fmode == FMode.MAXIMIZE){
            Assertions.assertEquals(0.5, value);
        } else {
            Assertions.assertEquals(-95438, value);
        }
    }

}
