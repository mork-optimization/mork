package es.urjc.etsii.grafo.metrics;

import es.urjc.etsii.grafo.algorithms.FMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeclaredObjectiveTest {

    @BeforeAll
    public static void setup(){
        Metrics.enableMetrics();
    }

    @Test
    public void testSimple(){
        var metric = new DeclaredObjective("Test", FMode.MINIMIZE, 0);
        metric.add(1, 100);
        metric.add(1, 120); // should be discarded
        metric.add(2, 99); // should be included
        metric.add(5, 80);
        metric.add(10, 70);
        metric.add(15, 71); // should be discarded
        metric.add(20, 40); // should be discarded

        // elements to find in order
        int[] values = {100, 99, 80, 70, 40};
        int[] instants = {1,2,5,10, 20};

        var it = metric.values.iterator();
        for (int i = 0; i < values.length; i++) {
            assertTrue(it.hasNext());
            var n = it.next();
            assertEquals(values[i], n.value());
            assertEquals(instants[i], n.instant());
        }

        // include a better point in the middle, verify worse points are removed from metric
        metric.add(4, 70);

        values = new int[]{100, 99, 70, 40};
        instants = new int[]{1, 2, 4,20};
        it = metric.values.iterator();
        for (int i = 0; i < values.length; i++) {
            assertTrue(it.hasNext());
            var n = it.next();
            assertEquals(values[i], n.value());
            assertEquals(instants[i], n.instant());
        }
    }
}
