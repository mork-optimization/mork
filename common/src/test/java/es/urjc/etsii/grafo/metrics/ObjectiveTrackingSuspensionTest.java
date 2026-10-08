package es.urjc.etsii.grafo.metrics;

import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObjectiveTrackingSuspensionTest {

    private static final String OBJECTIVE_NAME = "score";

    private final TestInstance instance = new TestInstance("test");
    private final AtomicInteger evaluations = new AtomicInteger();

    @BeforeEach
    void setUp() {
        Context.reset();
        Metrics.disableMetrics();
        Metrics.enableMetrics();

        Objective<TestMove, TestSolution, TestInstance> objective = Objective.ofMinimizing(
                OBJECTIVE_NAME,
                solution -> {
                    evaluations.incrementAndGet();
                    return solution.getScore();
                },
                TestMove::getScoreChange
        );
        Context.Configurator.setObjectives(objective);
        Metrics.register(OBJECTIVE_NAME, reference -> new DeclaredObjective(
                OBJECTIVE_NAME,
                objective.getFMode(),
                reference
        ));
        Metrics.register(TestMetric.class, TestMetric::new);
        Metrics.resetMetrics();
    }

    @AfterEach
    void tearDown() {
        Metrics.disableMetrics();
        Context.reset();
    }

    @Test
    void suspendedObjectivesAreNeitherEvaluatedNorRecorded() {
        Metrics.addCurrentObjectives(new TestSolution(instance, 100));
        try (var ignored = Context.suspendObjectiveTracking()) {
            Metrics.addCurrentObjectives(new TestSolution(instance, 1));
        }
        Metrics.addCurrentObjectives(new TestSolution(instance, 90));

        DeclaredObjective metric = Metrics.get(OBJECTIVE_NAME);
        var values = metric.getValues().toArray(TimeValue[]::new);
        assertEquals(2, values.length);
        assertEquals(100, values[0].value());
        assertEquals(90, values[1].value());
        assertEquals(2, evaluations.get());
    }

    @Test
    void suspensionSurvivesMetricsReset() {
        try (var ignored = Context.suspendObjectiveTracking()) {
            Metrics.resetMetrics();
            Metrics.addCurrentObjectives(new TestSolution(instance, 1));
            assertTrue(Metrics.<DeclaredObjective>get(OBJECTIVE_NAME).getValues().isEmpty());
        }

        Metrics.addCurrentObjectives(new TestSolution(instance, 90));
        assertEquals(1, Metrics.<DeclaredObjective>get(OBJECTIVE_NAME).getValues().size());
    }

    @Test
    void customMetricsRemainEnabledDuringObjectiveSuspension() {
        try (var ignored = Context.suspendObjectiveTracking()) {
            Metrics.addCurrentObjectives(new TestSolution(instance, 1));
            Metrics.add(TestMetric.class, 42);
        }

        assertTrue(Metrics.<DeclaredObjective>get(OBJECTIVE_NAME).getValues().isEmpty());
        var customValues = Metrics.<TestMetric>get(TestMetric.class).getValues();
        assertEquals(1, customValues.size());
        assertEquals(42, customValues.first().value());
    }

    @Test
    void scopeDoesNotRequireMetricsToBeEnabled() {
        Metrics.disableMetrics();

        assertDoesNotThrow(() -> {
            try (var ignored = Context.suspendObjectiveTracking()) {
                assertTrue(Context.isObjectiveTrackingSuspended());
                Metrics.addCurrentObjectives(new TestSolution(instance, 1));
            }
        });
        assertFalse(Context.isObjectiveTrackingSuspended());
    }
}
