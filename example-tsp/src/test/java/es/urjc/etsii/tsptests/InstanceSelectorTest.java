package es.urjc.etsii.tsptests;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPInstanceImporter;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.orchestrator.InstanceProperties;
import es.urjc.etsii.grafo.orchestrator.InstanceSelector;
import es.urjc.etsii.grafo.testutil.TestInstance;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstanceSelectorTest {

    private final String instancePath = "instances/TSPLIB/instances";

    @BeforeAll
    static void resetProps() throws IOException {
        TestInstance.resetProperties();
        FileUtils.copyDirectory(new File("../template/src/main/resources/instance-selector"), new File("src/main/resources/instance-selector"));
    }

    @Test
    void checkCSV() throws IOException {
        Path tmpFolder = Files.createTempDirectory("checkCSVTest");
        var tmpFolderF = tmpFolder.toFile();
        tmpFolderF.deleteOnExit();

        double prelimPercentage = 0.15;
        long nInstances = Files.list(Path.of(instancePath)).count();
        long nInstancesToPick = (long) (Math.ceil(nInstances * prelimPercentage));

        var config = new InstanceConfiguration();
        config.setPreliminarPercentage(prelimPercentage);
        config.setPath(Map.of("default", instancePath));
        config.setPreliminarOutputPath(tmpFolderF.getAbsolutePath());

        var instanceManager = new InstanceManager<>(config, new TSPInstanceImporter());
        var eventMock = Mockito.mock(ApplicationEventPublisher.class);
        new EventPublisher(eventMock);
        var selector = new InstanceSelector<>(instanceManager, config); // The Python part is not actually tested!
        selector.run();

        verifyCSV(nInstances);
        verifyPrelimPath(tmpFolderF, nInstancesToPick);
    }

    private void verifyCSV(long nInstances) throws IOException {

        var p = Path.of(InstanceSelector.DEFAULT_OUTPUT_PATH);
        var f = p.toFile();
        assertTrue(f.isFile());
        var lines = Files.readAllLines(p);
        assertEquals(nInstances, lines.size() - 1); // header in csv
        var header = lines.getFirst();
        for (var prop : TSPInstance.getUniquePropertiesKeys()) {
            if (InstanceProperties.ignoredProperties.contains(prop)) {
                continue;
            }
            assertTrue(header.contains(prop), "Property " + prop + " not found in CSV header");
        }
    }

    private void verifyPrelimPath(File tmpFolderF, long nInstancesToPick) {
        var instances = new File(tmpFolderF, "instances");
        assertTrue(instances.isDirectory());
        long nChosenInstances = instances.listFiles().length;
        assertEquals(nInstancesToPick, nChosenInstances);
        for (var f : instances.listFiles()) {
            assertTrue(f.isFile());
            assertTrue(f.getName().endsWith(".tsp"));
        }
        int nPdfs = 0;
        for (var f : tmpFolderF.listFiles()) {
            if (f.isFile()) {
                assertTrue(f.getName().endsWith(".pdf"));
                nPdfs++;
            }
        }
        assertEquals(5, nPdfs);
    }
}