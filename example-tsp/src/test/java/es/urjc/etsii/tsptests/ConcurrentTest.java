package es.urjc.etsii.tsptests;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.orchestrator.UserExperimentOrchestrator;
import es.urjc.etsii.grafo.solver.Mork;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
@ActiveProfiles(profiles = {"test","testconcurrent", "user-experiment"})
@DirtiesContext
class ConcurrentTest {

    @BeforeAll
    public static void before(){
        Mork.setSolvingMode(FMode.MINIMIZE);
    }
    private static Logger log = LoggerFactory.getLogger(ConcurrentTest.class);

    @Autowired
    private UserExperimentOrchestrator<TSPSolution, TSPInstance> userExperimentOrchestrator;

    @Test
    void testExecutor() {
        // Launch basic experiment
        userExperimentOrchestrator.run();
    }

    @AfterAll
    public static void deleteResults() throws IOException {
        log.info("Deleting resultstest");
        try {
            FileUtils.forceDeleteOnExit(new File("resultstest"));
        } catch (FileNotFoundException e){
            log.warn("File resultstest does not exist");
        }
    }

}
