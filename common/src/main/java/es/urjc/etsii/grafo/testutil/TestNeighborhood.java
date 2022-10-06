package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class TestNeighborhood extends RandomizableNeighborhood<TestMove, TestSolution, TestInstance> {

    private List<TestMove> fakeMoves;

    public TestNeighborhood(List<TestMove> fakeMoves) {
        this.fakeMoves = fakeMoves;
    }

    public TestNeighborhood(TestSolution fakeSolution, double... values){
        this.fakeMoves = new ArrayList<>(values.length);
        for (double i : values) {
            this.fakeMoves.add(new TestMove(fakeSolution, i));
        }
    }

    @Override
    public ExploreResult<TestMove, TestSolution, TestInstance> explore(TestSolution solution) {
        return ExploreResult.fromList(this.fakeMoves);
    }

    @Override
    public Optional<TestMove> getRandomMove(TestSolution solution) {
        return Optional.of(new TestMove(solution, fakeMoves.get(0).getValue()));
    }
}
