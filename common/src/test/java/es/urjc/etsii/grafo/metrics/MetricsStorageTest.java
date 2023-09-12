package es.urjc.etsii.grafo.metrics;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsStorageTest {

    @BeforeAll
    public static void init(){
        Metrics.enableMetrics();
        Metrics.register(TestMetric.class, TestMetric::new);
        Metrics.register(TestMetric2.class, TestMetric2::new);
    }

    static final long ERROR_MARGIN = 50_000_000; // 50ms
    static final TimeValue[] TYPEREF = new TimeValue[0];
    @Test
    void basicTest() {
        Metrics.resetMetrics();
        var metricStorage = Metrics.getCurrentThreadMetrics();
        long currentTime = System.nanoTime();
        long diff = metricStorage.referenceNanoTime - currentTime;
        assertTrue(diff < ERROR_MARGIN); // should use nanoTime, with a margin for error of 50ms

        currentTime = System.nanoTime();
        Metrics.add(TestMetric.class, 76);

        assertThrows(IllegalArgumentException.class, () -> Metrics.get(UnregisteredTestMetric.class));
        assertThrows(IllegalArgumentException.class, () -> Metrics.get("asdfg"));
        var testMetric = Metrics.get(TestMetric.class);
        assertEquals(1, testMetric.values.size());

        var timeValue = testMetric.values.iterator().next();
        diff = timeValue.instant() - currentTime;
        assertTrue(diff < ERROR_MARGIN);
        assertEquals(76, timeValue.value());
    }

    @Test
    void mergeAndContains() {
        Metrics.resetMetrics();
        var storageA = new MetricsStorage(10);
        var seriesA1 = new TestMetric(10);
        storageA.metrics.put(seriesA1.getName(), seriesA1);
        seriesA1.add(31, 1);
        seriesA1.add(35, 5);

        var storageB = new MetricsStorage(20);
        var seriesB1 = new TestMetric(20);
        var seriesB2 = new TestMetric2(20);
        storageB.metrics.put(seriesB1.getName(), seriesB1);
        storageB.metrics.put(seriesB2.getName(), seriesB2);
        seriesB1.add(34, 4);
        seriesB2.add(38, 8);

        var storageC = new MetricsStorage(30);
        var seriesC2 = new TestMetric2(30);
        storageC.metrics.put(seriesC2.getName(), seriesC2);
        seriesC2.add(36, 6);


        var merged = Metrics.merge(storageA, storageB, storageC);
        assertEquals(MetricsStorage.NO_REF, merged.referenceNanoTime);

        assertEquals(2, merged.metrics.size());
        assertEquals(3, merged.metrics.get("TestMetric").values.size());
        assertEquals(2, merged.metrics.get("TestMetric2").values.size());

        var orderedA = merged.metrics.get("TestMetric").values.toArray(TYPEREF);
        assertEquals(4, orderedA[0].value());
        assertEquals(1, orderedA[1].value());
        assertEquals(5, orderedA[2].value());
        var orderedB = merged.metrics.get("TestMetric2").values.toArray(TYPEREF);
        assertEquals(6, orderedB[0].value());
        assertEquals(8, orderedB[1].value());

    }

    @Test
    void areaUnderCurve(){
        Metrics.resetMetrics(10);
        // calculate area between 15 and 35, or in other words, ignore first 5 nanoseconds, duration 30
        assertThrows(IllegalArgumentException.class, () -> MetricUtil.areaUnderCurve(TestMetric.class, 5, 30));
        assertDoesNotThrow(() -> Metrics.add(TestMetric.class, 11, 5));
        assertDoesNotThrow(() -> Metrics.add(TestMetric.class, 21, 2));
        assertDoesNotThrow(() -> Metrics.add(TestMetric.class, 31, 1));
        assertDoesNotThrow(() -> Metrics.add(TestMetric.class, 41, 0.5));
        assertThrows(ArithmeticException.class, () -> MetricUtil.areaUnderCurve(TestMetric.class, 5, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> MetricUtil.areaUnderCurve(TestMetric.class, 0, 30));
        double area1 = MetricUtil.areaUnderCurve(TestMetric.class, 1, 35);
        double area2 = MetricUtil.areaUnderCurve(TestMetric.class, 6, 40);
        double expected1 = 50 + 20 + 10 + 2.5, expected2 = 25 + 20 + 10 + 7.5;
        assertEquals(expected1, area1);
        assertEquals(expected2, area2);
    }

}