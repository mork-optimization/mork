package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.metrics.DeclaredObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeStatsEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TimeStatsTest {

    @BeforeEach
    void setUp() {
        Metrics.enableMetrics();
        Metrics.enableTimeStats();
        Metrics.resetMetrics();
        Context.Configurator.getAndResetTimeEvents();
    }

    @Test
    void testTimedAlgorithm() {
        Context.Configurator.setObjectives(Objective.ofMinimizing("DefaultMinimize", TestSolution::getScore, TestMove::getScoreChange));
        Metrics.register("DefaultMinimize", ref -> new DeclaredObjective("DefaultMinimize", FMode.MINIMIZE, ref));
        // total time is algorithm + constructive + 2 * local search
        var alg = new TimedAlgorithm(3, 5, 1);
        var testInstance = new TestInstance("Test");
        long start = System.nanoTime();
        var solution = alg.algorithm(testInstance);
        long end = System.nanoTime();
        var timeData = Context.Configurator.getAndResetTimeEvents();
        Map<String, List<TimeStatsEvent>> organizedData = timeData.stream().collect(Collectors.groupingBy(TimeStatsEvent::method));
        Assertions.assertEquals(4, organizedData.size());
        Assertions.assertEquals(1, organizedData.get("algorithm").size());
        Assertions.assertEquals(1, organizedData.get("construct").size());
        Assertions.assertEquals(1, organizedData.get("improve").size());
        Assertions.assertEquals(1, organizedData.get("work1").size());
        Assertions.assertNull(organizedData.get("work2"));
        Metrics.disableMetrics();
    }

    @Test
    void testTimedNoMetrics() {

    }
}
