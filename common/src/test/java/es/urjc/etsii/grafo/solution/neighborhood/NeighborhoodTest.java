package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

public class NeighborhoodTest {

    TestSolution solution;

    @BeforeEach
    public void initSol(){
        var instance = new TestInstance("NeighborhoodTestInstance");
        this.solution = new TestSolution(instance);
    }

    @Test
    public void emptyNeighboorhood(){
        Neighborhood<?, TestSolution, TestInstance> neighborhhod = Neighborhood.empty();
        Assertions.assertTrue(neighborhhod.stream(this.solution).findAny().isEmpty(), "Neighborhood.empty() should be empty");
    }

    @Test
    public void concatTwoNeighborhood(){
        var neighA = NeighTestHelper.neighborhood(solution, 1, 2, 3);
        var neighB = NeighTestHelper.neighborhood(solution, 4, 5, 6);

        var neighborhood = Neighborhood.concat(neighA, neighB);
        verifyMoveOrder(neighborhood.stream(solution), 1,2,3,4,5,6);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void concatNNeighborhood(){
        var neighborhoods = new Neighborhood[]{
                NeighTestHelper.neighborhood(solution, 1, 2, 3),
                NeighTestHelper.neighborhood(solution, 4, 5, 6),
                NeighTestHelper.neighborhood(solution, 7, 8),
                NeighTestHelper.neighborhood(solution, 9, 10, 11, 12),
                NeighTestHelper.neighborhood(solution, 13),
        };

        var neighborhood = Neighborhood.concat(neighborhoods);
        verifyMoveOrder(neighborhood.stream(solution), 1,2,3,4,5,6,7,8,9,10,11,12,13);
    }

    @Test
    public void interleaveTwoNeighborhood(){
        var neighA = NeighTestHelper.neighborhood(solution, 1, 2, 3);
        var neighB = NeighTestHelper.neighborhood(solution, 4, 5, 6);

        var neighborhood = Neighborhood.interleave(neighA, neighB);
        verifyMoveOrder(neighborhood.stream(solution), 1,4,2,5,3,6);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void interleaveNNeighborhood(){
        var neighborhoods = new Neighborhood[]{
                NeighTestHelper.neighborhood(solution, 1, 2, 3),
                NeighTestHelper.neighborhood(solution, 4, 5, 6),
                NeighTestHelper.neighborhood(solution, 7, 8),
                NeighTestHelper.neighborhood(solution, 9, 10, 11, 12),
                NeighTestHelper.neighborhood(solution, 13),
        };

        var neighborhood = Neighborhood.interleave(neighborhoods);
        verifyMoveOrder(neighborhood.stream(solution), 1,4,7,9,13,2,5,8,10,3,6,11,12);
    }

    private void verifyMoveOrder(Stream<TestMove> moves, double... expectedValues){
        double[] values = moves.mapToDouble(TestMove::getValue).toArray();
        Assertions.assertArrayEquals(expectedValues, values);
    }
}
