package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.metrics.DeclaredObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Test;

public class TimeStatsTest {

    @Test
    void testTimedAlgorithm() {
        Metrics.enableMetrics();
        Metrics.resetMetrics();
        Context.Configurator.setObjectives(Objective.ofDefaultMinimize());
        Metrics.register("DefaultMinimize", ref -> new DeclaredObjective("DefaultMinimize", FMode.MINIMIZE, ref));
        // total time is algoritm + constructive + 2 * local search
        var alg = new TimedAlgorithm(3, 5, 1);
        var testInstance = new TestInstance("Test");
        long start = System.nanoTime();
        var solution = alg.algorithm(testInstance);
        long end = System.nanoTime();
        var timeData = Context.Configurator.getAndResetTimeEvents();
        System.out.println(timeData);
        Metrics.disableMetrics();
    }

    @Test
    void testTimedNoMetrics() {

    }
}
