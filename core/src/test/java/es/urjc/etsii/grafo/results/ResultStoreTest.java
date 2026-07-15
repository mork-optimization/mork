package es.urjc.etsii.grafo.results;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.testutil.TestHelperFactory;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultStoreTest {

    @Test
    void storesAndFindsResultsByGeneratedUuid() {
        var store = new ResultStore<TestSolution, TestInstance>();
        var result = TestHelperFactory.solutionGenerated("instance1", "experiment", "algorithm", 1, 42, 100, 50);

        var stored = store.store(result);

        assertSame(result, stored);
        assertSame(result, store.findResult(result.resultId()).orElseThrow());
        assertSame(result.solution(), store.findSolution(result.resultId()).orElseThrow());
        assertTrue(store.findResult(UUID.randomUUID()).isEmpty());
        assertTrue(store.findSolution(UUID.randomUUID()).isEmpty());
    }

    @Test
    void returnsImmutableInsertionOrderedSnapshots() {
        var store = new ResultStore<TestSolution, TestInstance>();
        var first = TestHelperFactory.solutionGenerated("instance1", "experiment", "algorithm", 1, 42, 100, 50);
        var otherExperiment = TestHelperFactory.solutionGenerated("instance2", "other", "algorithm", 1, 43, 100, 50);
        var second = TestHelperFactory.solutionGenerated("instance3", "experiment", "algorithm", 2, 44, 100, 50);

        store.store(first);
        store.store(otherExperiment);
        var snapshot = store.getResultsForExperiment("experiment");
        store.store(second);

        assertEquals(List.of(first), snapshot);
        assertEquals(List.of(first, second), store.getResultsForExperiment("experiment"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(second));
    }

    @Test
    void releasesStrongSolutionsButRetainsCachedMetadata() {
        var store = new ResultStore<TestSolution, TestInstance>();
        var released = TestHelperFactory.solutionGenerated(
                "instance1",
                "experiment",
                "algorithm",
                1,
                42,
                100,
                50,
                java.util.Map.of("property", "value")
        );
        var retained = TestHelperFactory.solutionGenerated("instance2", "other", "algorithm", 1, 43, 100, 50);
        var originalSolution = released.solution();
        store.store(released);
        store.store(retained);

        store.releaseSolutionsForExperiment("experiment");
        store.releaseSolutionsForExperiment("experiment");

        var solutionless = store.findResult(released.resultId()).orElseThrow();
        assertNull(solutionless.solution());
        assertEquals(released.objectives(), solutionless.objectives());
        assertEquals(released.solutionProperties(), solutionless.solutionProperties());
        assertSame(originalSolution, store.findSolution(released.resultId()).orElseThrow());
        assertSame(retained.solution(), store.findResult(retained.resultId()).orElseThrow().solution());
    }

    @Test
    void rejectsDuplicateResultIds() {
        var store = new ResultStore<TestSolution, TestInstance>();
        var result = TestHelperFactory.solutionGenerated("instance1", "experiment", "algorithm", 1, 42, 100, 50);
        var duplicate = copyWithSameId(result);

        store.store(result);

        assertThrows(IllegalStateException.class, () -> store.store(duplicate));
    }

    private WorkUnitResult<TestSolution, TestInstance> copyWithSameId(WorkUnitResult<TestSolution, TestInstance> result) {
        return new WorkUnitResult<>(
                result.resultId(),
                result.success(),
                result.experimentName(),
                result.instancePath(),
                result.instanceId(),
                result.algorithm(),
                result.iteration(),
                result.solution(),
                result.objectives(),
                result.solutionProperties(),
                result.executionTime(),
                result.timeToTarget(),
                result.metrics(),
                result.timeData()
        );
    }
}
