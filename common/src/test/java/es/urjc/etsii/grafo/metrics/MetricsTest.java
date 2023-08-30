package es.urjc.etsii.grafo.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsTest {

    static final long ERROR_MARGIN = 50_000_000; // 50ms
    static final TimeValue[] TYPEREF = new TimeValue[0];
    @Test
    void basicTest() {
        long currentTime = System.nanoTime();
        var metric = new Metrics();
        long diff = metric.referenceTime - currentTime;
        assertTrue(diff < ERROR_MARGIN); // should use nanoTime, with a margin for error of 50ms

        currentTime = System.nanoTime();
        metric.addDatapoint("test", 76);
        assertThrows(IllegalArgumentException.class, () -> metric.addDatapoint("any", initializeMetric, 10, 10));
        assertThrows(IllegalArgumentException.class, () -> metric.byName("any"));
        var set = metric.byName("test");
        assertEquals(1, set.size());

        var timeValue = set.iterator().next();
        diff = timeValue.timeElapsed() - currentTime;
        assertTrue(diff < ERROR_MARGIN);
        assertEquals(76, timeValue.value());
    }

    @Test
    void mergeAndContains() {
        var metricA = new Metrics(10);
        metricA.addDatapoint("testA", initializeMetric, 1, 31);
        metricA.addDatapoint("testA", initializeMetric, 5, 35);
        var metricB = new Metrics(20);
        metricB.addDatapoint("testA", initializeMetric, 4, 34);
        metricB.addDatapoint("testB", initializeMetric, 8, 38);

        var metricC = new Metrics(30);
        metricC.addDatapoint("testB", initializeMetric, 6, 36);


        var merged = Metrics.merge(metricA, metricB, metricC);
        assertEquals(10, merged.referenceTime); // Lowest reference point of all metrics
        assertEquals(2, merged.metrics.size());
        assertEquals(3, merged.metrics.get("testA").size());
        assertEquals(2, merged.metrics.get("testB").size());

        var orderedA = merged.metrics.get("testA").toArray(TYPEREF);
        assertEquals(1, orderedA[0].value());
        assertEquals(4, orderedA[1].value());
        assertEquals(5, orderedA[2].value());
        var orderedB = merged.metrics.get("testB").toArray(TYPEREF);
        assertEquals(6, orderedB[0].value());
        assertEquals(8, orderedB[1].value());

    }

    @Test
    void areaUnderCurve(){
        var metricA = new Metrics(10);
        // calculate area between 15 and 35, or in other words, ignore first 5 nanoseconds, duration 30
        assertThrows(IllegalArgumentException.class, () -> metricA.areaUnderCurve("test", 5, 30));
        assertDoesNotThrow(() -> metricA.addDatapoint("test", initializeMetric, 5, 11));
        assertDoesNotThrow(() -> metricA.addDatapoint("test", initializeMetric, 2, 21));
        assertDoesNotThrow(() -> metricA.addDatapoint("test", initializeMetric, 1, 31));
        assertDoesNotThrow(() -> metricA.addDatapoint("test", initializeMetric, 0.5, 41));
        assertThrows(ArithmeticException.class, () -> metricA.areaUnderCurve("test", 5, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> metricA.areaUnderCurve("test", 0, 30));
        double area1 = metricA.areaUnderCurve("test", 1, 35);
        double area2 = metricA.areaUnderCurve("test", 6, 40);
        double expected1 = 50 + 20 + 10 + 2.5, expected2 = 25 + 20 + 10 + 7.5;
        assertEquals(expected1, area1);
        assertEquals(expected2, area2);
    }





}