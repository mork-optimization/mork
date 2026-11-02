package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import org.junit.jupiter.api.extension.*;
import java.nio.file.*;

/** Each test gets a provisioned registry; production always requires explicit host provisioning. */
public final class WorkerRegistryFixture implements BeforeEachCallback, AfterEachCallback {
    private Path directory;
    public void beforeEach(ExtensionContext context) throws Exception {
        directory = Files.createTempDirectory("mork-worker-test-").toRealPath();
        System.setProperty("mork.test.workerRegistry", directory.toString());
        WorkerRegistry.system().provision();
    }
    public void afterEach(ExtensionContext context) throws Exception {
        System.clearProperty("mork.test.workerRegistry");
        try (var files = Files.walk(directory)) {
            var paths = new java.util.ArrayList<Path>();
            for (var iterator = files.iterator(); iterator.hasNext();) paths.add(iterator.next());
            paths.sort(java.util.Comparator.reverseOrder());
            for (Path file : paths) Files.delete(file);
        }
    }
}
