package es.urjc.etsii.grafo.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolverConfigTest {

    @Test
    void defaultWorkerCountIsAtLeastOne() {
        assertEquals(1, es.urjc.etsii.grafo.util.ConcurrencyUtil.resolveWorkerCount(-1, 1));
        assertEquals(1, es.urjc.etsii.grafo.util.ConcurrencyUtil.resolveWorkerCount(-1, 2));
        assertEquals(4, es.urjc.etsii.grafo.util.ConcurrencyUtil.resolveWorkerCount(-1, 8));
        assertEquals(16, es.urjc.etsii.grafo.util.ConcurrencyUtil.resolveWorkerCount(-1, 32));
        assertEquals(32, es.urjc.etsii.grafo.util.ConcurrencyUtil.resolveWorkerCount(32, 32));
    }

    @Test
    void explicitPositiveWorkerCountIsPreserved() {
        var config = new SolverConfig();
        config.setnWorkers(8);

        assertEquals(8, config.getnWorkers());
    }
}
