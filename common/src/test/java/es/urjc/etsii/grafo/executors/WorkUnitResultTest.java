package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.metrics.MetricsStorage;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestAlgorithm;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.StringUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static es.urjc.etsii.grafo.executors.WorkUnitResult.computeSolutionProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkUnitResultTest {

    private final TestInstance instance = new TestInstance("dummy");

    @Test
    void customPropertiesTest() {
        TestSolution emptyProperties = new TestSolution(instance, 0, new HashMap<>());
        assertTrue(computeSolutionProperties(emptyProperties).isEmpty());

        TestSolution nullProperties = new TestSolution(instance, 0, null);
        assertTrue(computeSolutionProperties(nullProperties).isEmpty());

        var rndString1 = StringUtil.randomAlgorithmName();
        TestSolution singleProperty = new TestSolution(instance, 0, new HashMap<>() {{
            put("key", s -> rndString1);
        }});
        var props = computeSolutionProperties(singleProperty);
        assertEquals(1, props.size());
        assertEquals(rndString1, props.get("key"));

        var rndString2 = StringUtil.randomAlgorithmName();
        var rndNumber = new Random(0).nextDouble();
        TestSolution twoProperties = new TestSolution(instance, 0, new HashMap<>() {{
            put("key1", s -> rndString2);
            put("randomNumber", s -> rndNumber);
        }});
        var twoProps = computeSolutionProperties(twoProperties);
        assertEquals(2, twoProps.size());
        assertEquals(rndString2, twoProps.get("key1"));
        assertEquals(rndNumber, twoProps.get("randomNumber"));
        assertThrows(UnsupportedOperationException.class, () -> twoProps.put("extra", 1));
    }

    @Test
    void cachesObjectivesOnceInDeclarationOrder() {
        var firstEvaluations = new AtomicInteger();
        var secondEvaluations = new AtomicInteger();
        Objective<?, TestSolution, TestInstance> first = Objective.ofMinimizing("first", (TestSolution solution) -> {
            firstEvaluations.incrementAndGet();
            return solution.getScore();
        }, null);
        Objective<?, TestSolution, TestInstance> second = Objective.ofMinimizing("second", (TestSolution solution) -> {
            secondEvaluations.incrementAndGet();
            return solution.getScore() * 2;
        }, null);
        setObjectives(first, second);

        var solution = new TestSolution(instance, 7);
        var result = successfulResult(solution);

        assertNotNull(result.resultId());
        assertEquals(List.of("first", "second"), result.objectives().keySet().stream().toList());
        assertEquals(7, result.mainObjectiveValue());
        assertEquals(1, firstEvaluations.get());
        assertEquals(1, secondEvaluations.get());

        solution.setScore(99);
        assertEquals(Map.of("first", 7.0, "second", 14.0), result.objectives());
        assertEquals(7, result.mainObjectiveValue());
        assertEquals(1, firstEvaluations.get());
        assertEquals(1, secondEvaluations.get());
        assertThrows(UnsupportedOperationException.class, () -> result.objectives().put("third", 3.0));
    }

    @Test
    void solutionlessCopyRetainsCachedMetadata() {
        Context.Configurator.setObjectives(Objective.ofMinimizing("score", (TestSolution s) -> s.getScore(), null));
        var solution = new TestSolution(instance, 5, Map.of("property", ignored -> "value"));
        var result = successfulResult(solution);

        var solutionless = result.withoutSolution();

        assertNull(solutionless.solution());
        assertEquals(result.objectives(), solutionless.objectives());
        assertEquals(result.solutionProperties(), solutionless.solutionProperties());
        assertSame(solutionless, solutionless.withoutSolution());
    }

    @Test
    void rejectsNullCustomPropertyValueWithActionableMessage() {
        var solution = new TestSolution(instance, 0, Map.of("badProperty", ignored -> null));

        var error = assertThrows(IllegalArgumentException.class, () -> computeSolutionProperties(solution));

        assertTrue(error.getMessage().contains("badProperty"));
        assertTrue(error.getMessage().contains("non-null"));
    }

    @Test
    void canonicalConstructorDefensivelyCopiesOrderedMaps() {
        var objectives = new LinkedHashMap<String, Double>();
        objectives.put("first", 1.0);
        objectives.put("second", 2.0);
        var properties = new LinkedHashMap<String, Object>();
        properties.put("firstProperty", 1);
        var result = directResult(objectives, properties);

        objectives.clear();
        properties.clear();

        assertEquals(List.of("first", "second"), result.objectives().keySet().stream().toList());
        assertEquals(Map.of("firstProperty", 1), result.solutionProperties());

        var invalidProperties = new HashMap<String, Object>();
        invalidProperties.put("badProperty", null);
        var error = assertThrows(IllegalArgumentException.class, () -> directResult(Map.of(), invalidProperties));
        assertTrue(error.getMessage().contains("badProperty"));
    }

    private WorkUnitResult<TestSolution, TestInstance> successfulResult(TestSolution solution) {
        var workUnit = new WorkUnit<TestSolution, TestInstance>("experiment", "path", new TestAlgorithm(), 1);
        return WorkUnitResult.ok(workUnit, instance.getId(), solution, 10, 5, new MetricsStorage(), new ArrayList<>());
    }

    private WorkUnitResult<TestSolution, TestInstance> directResult(
            Map<String, Double> objectives,
            Map<String, Object> properties
    ) {
        return new WorkUnitResult<>(
                UUID.randomUUID(),
                true,
                "experiment",
                "path",
                instance.getId(),
                new TestAlgorithm(),
                "1",
                new TestSolution(instance, 1),
                objectives,
                properties,
                10,
                5,
                new MetricsStorage(),
                new ArrayList<>()
        );
    }

    @SafeVarargs
    private final void setObjectives(Objective<?, TestSolution, TestInstance>... objectives) {
        Context.Configurator.setObjectives(false, objectives);
    }
}
