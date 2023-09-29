package es.urjc.etsii.grafo.autoconfig.fakecomponents;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;

import java.util.Optional;

public class TestAutoconfigNeighborhood extends RandomizableNeighborhood<TestMove, TestSolution, TestInstance> {

    @AutoconfigConstructor
    public TestAutoconfigNeighborhood() {
    }

    @Override
    public ExploreResult<TestMove, TestSolution, TestInstance> explore(TestSolution solution) {
        throw new UnsupportedOperationException("Only for testing");
    }

    @Override
    public Optional<TestMove> getRandomMove(TestSolution solution) {
        throw new UnsupportedOperationException("Only for testing");
    }
}
