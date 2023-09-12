package es.urjc.etsii.grafo.metrics;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static es.urjc.etsii.grafo.metrics.Metrics.areMetricsEnabled;
import static org.junit.jupiter.api.Assertions.*;

class MetricsStorageManagerTest {

    @BeforeAll
    public static void init(){
        Metrics.disableMetrics();
    }

    @Test
    void metricsManager(){
        assertFalse(areMetricsEnabled());
        assertThrows(IllegalStateException.class, Metrics::getCurrentThreadMetrics);
        assertThrows(IllegalStateException.class, Metrics::resetMetrics);

        Metrics.enableMetrics();
        assertTrue(areMetricsEnabled());

        assertThrows(IllegalStateException.class, Metrics::getCurrentThreadMetrics);
        Metrics.resetMetrics();
        var metrics = Metrics.getCurrentThreadMetrics();
        assertNotNull(metrics);

        Metrics.register(TestMetric.class, TestMetric::new);
        TestMetric testMetric = Metrics.get(TestMetric.class);
        testMetric.add(10);
        testMetric.add(System.nanoTime(), 30);
        var testMetric2 = Metrics.get(TestMetric.class);
        assertEquals(testMetric2, Metrics.get(TestMetric.class.getSimpleName()));
        assertEquals(testMetric2, Metrics.get(testMetric.getName()));

        assertEquals(2, testMetric2.values.size());
        assertEquals(10, testMetric2.values.iterator().next().value());
        Metrics.resetMetrics(System.nanoTime());
        assertTrue(Metrics.get(TestMetric.class).values.isEmpty());


        Metrics.disableMetrics();
        assertFalse(areMetricsEnabled());
        assertThrows(IllegalStateException.class, () -> Metrics.get(TestMetric.class.getSimpleName()));
        assertThrows(IllegalStateException.class, () -> Metrics.get(testMetric.getName()));
        assertThrows(IllegalStateException.class, () -> Metrics.getCurrentThreadMetrics().metrics.get(TestMetric.class.getSimpleName()));
        assertThrows(IllegalStateException.class, Metrics::getCurrentThreadMetrics);
        assertThrows(IllegalStateException.class, Metrics::resetMetrics);
    }

}