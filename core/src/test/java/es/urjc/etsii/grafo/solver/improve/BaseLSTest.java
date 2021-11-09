package es.urjc.etsii.grafo.solver.improve;


import es.urjc.etsii.grafo.solution.neighborhood.EagerNeighborhood;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.apache.poi.util.ArrayUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class BaseLSTest {

    protected double getMax(double[] values){
        double max = Double.NEGATIVE_INFINITY;
        for(var d: values){
            if(d>max){
                max = d;
            }
        }
        return max;
    }

    protected double getMin(double[] values){
        double min = Double.POSITIVE_INFINITY;
        for(var d: values){
            if(d < min){
                min = d;
            }
        }
        return min;
    }

    protected Neighborhood<TestMove, TestSolution, TestInstance> getNeighborhoodMock(boolean maximizing, double[] values, TestSolution solution){
        List<TestMove> moves = new ArrayList<>();
        for(double d: values){
            moves.add(new TestMove(solution, d, maximizing));
        }
        var neighborhood = Mockito.mock(EagerNeighborhood.class);
        when(neighborhood.getMovements(solution)).thenReturn(moves);
        when(neighborhood.stream(solution)).thenReturn(moves.stream());
        return neighborhood;
    }
}
