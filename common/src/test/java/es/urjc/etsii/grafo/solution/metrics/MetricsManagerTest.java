package es.urjc.etsii.grafo.solution.metrics;

import org.junit.jupiter.api.Test;

import static es.urjc.etsii.grafo.solution.metrics.MetricsManager.areMetricsEnabled;
import static org.junit.jupiter.api.Assertions.*;

class MetricsManagerTest {

    @Test
    void metricsManager(){
        assertFalse(areMetricsEnabled());
        assertThrows(IllegalStateException.class, MetricsManager::getInstance);
        assertThrows(IllegalStateException.class, MetricsManager::resetMetrics);

        MetricsManager.enableMetrics();
        assertTrue(areMetricsEnabled());

        assertThrows(IllegalStateException.class, MetricsManager::getInstance);
        MetricsManager.resetMetrics();
        var metrics = MetricsManager.getInstance();
        assertNotNull(metrics);
        MetricsManager.addDatapoint("test", 10);
        MetricsManager.addDatapoint("test", 30, System.nanoTime());
        var metrics2 = MetricsManager.getInstance();
        var values = metrics2.byName("test");
        assertEquals(2, values.size());
        assertEquals(10, values.iterator().next().value());
        MetricsManager.resetMetrics(System.nanoTime());
        var metrics3 = MetricsManager.getInstance();
        assertThrows(IllegalArgumentException.class, () -> metrics3.byName("test"));

        assertEquals(2, values.size());
        assertEquals(10, values.iterator().next().value());

        MetricsManager.disableMetrics();
        assertFalse(areMetricsEnabled());
        assertThrows(IllegalStateException.class, MetricsManager::getInstance);
        assertThrows(IllegalStateException.class, MetricsManager::resetMetrics);
    }

}