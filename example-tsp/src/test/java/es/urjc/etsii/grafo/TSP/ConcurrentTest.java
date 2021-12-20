package es.urjc.etsii.grafo.TSP;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.solver.services.Orchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {Mork.class})
@ActiveProfiles(profiles = {"test","testconcurrent"})
@DirtiesContext
class ConcurrentTest {

    @Autowired
    private Orchestrator<TSPSolution, TSPInstance> orchestrator;

    @Test
    public void testExecutor() {
        // Launch basic experiment
        orchestrator.run();
    }

}
