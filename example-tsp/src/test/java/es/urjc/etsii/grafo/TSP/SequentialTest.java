package es.urjc.etsii.grafo.TSP;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.services.Orchestrator;
import es.urjc.etsii.grafo.solver.Mork;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest(classes = {Mork.class})
@ActiveProfiles(profiles = {"test", "testsequential"})
@DirtiesContext
class SequentialTest {

    private static Logger log = LoggerFactory.getLogger(SequentialTest.class);

    @Autowired
    private Orchestrator<TSPSolution, TSPInstance> orchestrator;

    @Test
    void testExecutor() {
        // Launch basic experiment
        orchestrator.run();
    }

    @AfterAll
    public static void deleteResults() throws IOException {
        log.info("Deleting resultstest");
        try {
            FileUtils.forceDelete(new File("resultstest"));
        } catch (FileNotFoundException e){
            log.warn("File resultstest does not exist");
        }    }
}
