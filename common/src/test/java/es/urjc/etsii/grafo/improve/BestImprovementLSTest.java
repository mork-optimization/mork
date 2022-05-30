package es.urjc.etsii.grafo.improve;


import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BestImprovementLSTest extends BaseLSTest {

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void testOrderMaximizing(boolean maximizing){
        double[] values = {
                0, 0.5, 195, -95438, 196341, -99614, 12, 861523, Math.PI, Math.E
        };
        var solution = new TestSolution(new TestInstance("Fake Instance"));
        var mockNeighborhod = getNeighborhoodMock(maximizing, values, solution);
        var firstImprovementLS = new LocalSearchBestImprovement<>(maximizing, mockNeighborhod);
        var chosenMove = firstImprovementLS.getMove(solution);
        Assertions.assertTrue(chosenMove.isPresent());
        double value = chosenMove.get().getValue();
        if(maximizing){
            Assertions.assertEquals(value, getMax(values));
        } else {
            Assertions.assertEquals(value, getMin(values));
        }
    }

}
