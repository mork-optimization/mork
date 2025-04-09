package es.urjc.etsii.grafo.improve;


import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class BaseLSTest {
    @BeforeAll
    public static void init(){
        Metrics.disableMetrics();
    }

    @SuppressWarnings("unchecked")
    protected Neighborhood<TestMove, TestSolution, TestInstance> getNeighborhoodMock(FMode fmode, double[] values, TestSolution solution){
        List<TestMove> moves = new ArrayList<>();
        for(double d: values){
            moves.add(new TestMove(solution, d, fmode));
        }
        var neighborhood = Mockito.mock(Neighborhood.class);
        when(neighborhood.explore(solution)).thenReturn(ExploreResult.fromList(moves));
        return neighborhood;
    }
}
