package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;

import java.util.Arrays;
import java.util.stream.Stream;

public class NeighTestHelper {

    public static Neighborhood<TestMove, TestSolution, TestInstance> neighborhood(TestMove... moves){
        return new Neighborhood<>() {
            @Override
            public ExploreResult<TestMove, TestSolution, TestInstance> explore(TestSolution testSolution) {
                return new ExploreResult<>(Arrays.asList(moves));
            }
            @Override
            public String toString() {
                return "TestNeighborhood";
            }
        };
    }

    public static Neighborhood<TestMove, TestSolution, TestInstance> neighborhood(TestSolution solution, double... values){
        return new Neighborhood<>() {
            @Override
            public ExploreResult<TestMove, TestSolution, TestInstance> explore(TestSolution testSolution) {
                return new ExploreResult<>(Arrays.stream(values).mapToObj(v -> new TestMove(solution, v)));
            }
            @Override
            public String toString() {
                return "TestNeighborhood";
            }
        };
    }
}
