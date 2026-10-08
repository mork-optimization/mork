package es.urjc.etsii.grafo.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolverConfigTest {

    @Test
    void defaultWorkerCountIsAtLeastOne() {
        assertEquals(1, SolverConfig.defaultWorkerCount(1));
        assertEquals(1, SolverConfig.defaultWorkerCount(2));
        assertEquals(4, SolverConfig.defaultWorkerCount(8));
    }

    @Test
    void explicitPositiveWorkerCountIsPreserved() {
        var config = new SolverConfig();
        config.setnWorkers(8);

        assertEquals(8, config.getnWorkers());
    }
}
