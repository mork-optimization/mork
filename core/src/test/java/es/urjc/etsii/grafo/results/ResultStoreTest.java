package es.urjc.etsii.grafo.results;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.testutil.TestHelperFactory;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultStoreTest {

    @Test
    void storesAndFindsResultsByGeneratedId() {
        var store = new ResultStore<TestSolution, TestInstance>();
        var result = TestHelperFactory.solutionGenerated("instance1", "experiment", "algorithm", 1, 42, 100, 50);

        var stored = store.store(result);

        assertSame(result, stored);
        assertFalse(result.resultId().isBlank());
        assertDoesNotThrow(() -> UUID.fromString(result.resultId()));
        assertSame(result, store.findResult(result.resultId()).orElseThrow());
        assertTrue(store.findResult("missing").isEmpty());
    }

    @Test
    void filtersResultsByExperimentInInsertionOrder() {
        var store = new ResultStore<TestSolution, TestInstance>();
        var first = TestHelperFactory.solutionGenerated("instance1", "experiment", "algorithm", 1, 42, 100, 50);
        var otherExperiment = TestHelperFactory.solutionGenerated("instance2", "other", "algorithm", 1, 43, 100, 50);
        var second = TestHelperFactory.solutionGenerated("instance3", "experiment", "algorithm", 2, 44, 100, 50);

        store.store(first);
        store.store(otherExperiment);
        store.store(second);

        assertEquals(List.of(first, second), store.getResultsForExperiment("experiment").toList());
        assertEquals(2, store.solutionsInMemory("experiment"));
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
                result.solutionProperties(),
                result.executionTime(),
                result.timeToTarget(),
                result.metrics(),
                result.timeData()
        );
    }
}
