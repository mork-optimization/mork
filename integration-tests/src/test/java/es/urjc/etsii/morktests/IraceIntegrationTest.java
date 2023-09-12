package es.urjc.etsii.morktests;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solver.Mork;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

//@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS, classes = {Mork.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) //
//@ActiveProfiles(profiles = {"autoconfig"})
//@DirtiesContext
public class IraceIntegrationTest {

    @BeforeAll
    static void setup() throws IOException {
        FileUtils.copyDirectory(new File("../template/src/main/resources/irace"), new File("../integration-tests/src/main/resources/irace"));
        //FileUtils.copyDirectory(new File("../example-tsp/instances"), new File("../integration-tests/instances"));
    }

    @AfterAll
    static void deleteIraceFiles(){
        FileUtils.deleteQuietly(new File("../integration-tests/src/main/resources/irace"));
        //FileUtils.deleteQuietly(new File("../integration-tests/instances"));
        //FileUtils.deleteQuietly(new File("integration-tests/src/main/resources/irace"));
    }

    @Test
    void launchAutoconfig(){
        Mork.start(new String[]{"--autoconfig"}, FMode.MAXIMIZE);
    }
}
