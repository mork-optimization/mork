package es.urjc.etsii.morktests;

import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.orchestrator.InstanceSelector.DEFAULT_OUTPUT_PATH;

public class ComplexityAnalysisTest {

    private static final Path propertiesPath = Path.of(DEFAULT_OUTPUT_PATH);


    @BeforeAll
    static void setup() throws IOException {
        if (Files.exists(propertiesPath)) {
            Files.delete(propertiesPath);
        }
    }

    @Test
    void testComplexity() {
        Objective<?, ACSolution, ACInstance> objective = Objective.ofDefaultMaximize();
        var success = Mork.start(new String[]{
                "--server.port=0",
                "--instance-properties",
                "--instances.path.default=instancesautoconfig/sleepy",
                "--event.webserver.stopOnExecutionEnd=true",
        }, objective);
        Assertions.assertTrue(success);
        Assertions.assertTrue(Files.exists(propertiesPath));

        // Execute a normal experiment to extract time statistics from the algorithms we have configured
        success = Mork.start(new String[]{
                "--server.port=0",
                "--instances.path.default=instancesautoconfig/sleepy",
                "--solver.iterations=30",
                "--solver.parallelExecutor=false",
                "--solver.metrics=true",
                "--solver.benchmark=false",
                "--serializers.solution-json.enabled=true",
                "--serializers.solution-json.frequency=all",
                "--serializers.solution-json.folder=timestats",
                "--event.webserver.stopOnExecutionEnd=true"
        }, objective);
        Assertions.assertTrue(success);
        Assertions.assertTrue(Files.exists(Path.of("timestats")));
        // TODO call python script to analyze results automatically

        // Sleep for 5 seconds to allow the webserver to be stopped
        ConcurrencyUtil.sleep(5, TimeUnit.SECONDS);
    }
}
