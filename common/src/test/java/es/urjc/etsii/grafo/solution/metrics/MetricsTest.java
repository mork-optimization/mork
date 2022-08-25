package es.urjc.etsii.grafo.solution.metrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MetricsTest {

    static final long ERROR_MARGIN = 50_000_000; // 50ms
    static final TimeValue[] TYPEREF = new TimeValue[0];
    @Test
    void basicTest() {
        long currentTime = System.nanoTime();
        var metric = new Metrics();
        long diff = metric.referencePoint - currentTime;
        Assertions.assertTrue(diff < ERROR_MARGIN); // should use nanoTime, with a margin for error of 50ms

        currentTime = System.nanoTime();
        metric.addDatapoint("test", 76);
        Assertions.assertThrows(IllegalArgumentException.class, () -> metric.addDatapoint("any", 10, 10));
        Assertions.assertThrows(IllegalArgumentException.class, () -> metric.byName("any"));
        var set = metric.byName("test");
        Assertions.assertEquals(1, set.size());

        var timeValue = set.iterator().next();
        diff = timeValue.timeElapsed() - currentTime;
        Assertions.assertTrue(diff < ERROR_MARGIN);
        Assertions.assertEquals(76, timeValue.value());
    }

    @Test
    void mergeAndContains() {
        var metricA = new Metrics(10);
        metricA.addDatapoint("testA", 1, 31);
        metricA.addDatapoint("testA", 5, 35);
        var metricB = new Metrics(20);
        metricB.addDatapoint("testA", 4, 34);
        metricB.addDatapoint("testB", 8, 38);

        var metricC = new Metrics(30);
        metricC.addDatapoint("testB", 6, 36);


        var merged = Metrics.merge(metricA, metricB, metricC);
        Assertions.assertEquals(10, merged.referencePoint); // Lowest reference point of all metrics
        Assertions.assertEquals(2, merged.metricValues.size());
        Assertions.assertEquals(3, merged.metricValues.get("testA").size());
        Assertions.assertEquals(2, merged.metricValues.get("testB").size());

        var orderedA = merged.metricValues.get("testA").toArray(TYPEREF);
        Assertions.assertEquals(1, orderedA[0].value());
        Assertions.assertEquals(4, orderedA[1].value());
        Assertions.assertEquals(5, orderedA[2].value());
        var orderedB = merged.metricValues.get("testB").toArray(TYPEREF);
        Assertions.assertEquals(6, orderedB[0].value());
        Assertions.assertEquals(8, orderedB[1].value());

    }

    @Test
    void hypervolume(){
        var metricA = new Metrics(10);
        // calculate area between 15 and 35, or in other words, ignore first 5 nanoseconds, duration 30
        Assertions.assertThrows(IllegalArgumentException.class, () -> metricA.hypervolume("test", 5, 30));
        Assertions.assertDoesNotThrow(() -> metricA.addDatapoint("test", 5, 11));
        Assertions.assertDoesNotThrow(() -> metricA.addDatapoint("test", 2, 21));
        Assertions.assertDoesNotThrow(() -> metricA.addDatapoint("test", 1, 31));
        Assertions.assertDoesNotThrow(() -> metricA.addDatapoint("test", 0.5, 41));
        Assertions.assertThrows(ArithmeticException.class, () -> metricA.hypervolume("test", 5, Long.MAX_VALUE));
        Assertions.assertThrows(IllegalArgumentException.class, () -> metricA.hypervolume("test", 0, 30));
        double area1 = metricA.hypervolume("test", 1, 35);
        double area2 = metricA.hypervolume("test", 6, 40);
        double expected1 = 50 + 20 + 10 + 2.5, expected2 = 25 + 20 + 10 + 7.5;
        Assertions.assertEquals(expected1, area1);
        Assertions.assertEquals(expected2, area2);
    }

}