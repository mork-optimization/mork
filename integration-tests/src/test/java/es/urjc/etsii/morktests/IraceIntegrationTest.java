package es.urjc.etsii.morktests;

import es.urjc.etsii.grafo.autoconfigtests.Main;
import es.urjc.etsii.grafo.solver.Mork;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void launchAutoconfig() {
        var success = Mork.start(new String[]{
                "--autoconfig",
                "--instances.path.default=instancesautoconfig/autoconfig",
        }, Main.AC_OBJECTIVE);
        assertTrue(success);
        assertTrue(Files.exists(Path.of("plots.pdf")));
        assertTrue(Files.exists(Path.of("irace.Rdata")));
        assertTrue(Files.exists(Path.of("log-ablation.Rdata")));
        assertTrue(Files.exists(Path.of("report.html")));
    }
}
