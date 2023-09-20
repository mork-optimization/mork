package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Profile("instance-selector")
public class InstanceSelector<I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(InstanceSelector.class);
    public static final String DEFAULT_OUTPUT_PATH = "instance_properties.csv";
    private static final Set<String> ignoredProperties = Set.of(Instance.LOAD_TIME_NANOS);
    private final InstanceManager<I> instanceManager;
    private final InstanceConfiguration instanceConfiguration;

    public InstanceSelector(InstanceManager<I> instanceManager, InstanceConfiguration instanceConfiguration) {
        this.instanceManager = instanceManager;
        this.instanceConfiguration = instanceConfiguration;
    }

    @Override
    public void run(String... args) {
        long start = System.nanoTime();
        try {
            String path = instanceConfiguration.getForSelection();
            if (path != null && !path.isBlank()) {
                instanceConfiguration.getPaths().put("default", path);
            }
            var instanceIDs = this.instanceManager.getInstanceSolveOrder("default", false);
            EventPublisher.getInstance().publishEvent(new ExecutionStartedEvent(Mork.getFMode(), List.of("Instance selection")));
            var properties = instancesToPropertyMatrix(instanceIDs);
            log.info("Writting CSV");
            writeCSV(Path.of(DEFAULT_OUTPUT_PATH), properties);
            log.info("Launching selector");
            selectInstances();
            log.info("Completed");
        } finally {
            long end = System.nanoTime();
            EventPublisher.getInstance().publishEvent(new ExecutionEndedEvent(end - start));
        }
    }

    protected void selectInstances() {
        String path = instanceConfiguration.getForSelection();
        if (path == null || path.isBlank()) {
            path = instanceConfiguration.getPath("default");
        }
        var referenceClass = instanceManager.getUserImporterImplementation().getClass();
        var isJAR = IOUtil.isJAR(referenceClass);
        var pb = new ProcessBuilder()
                .inheritIO();
        String size = String.valueOf(instanceConfiguration.getPreliminarPercentage());
        String preOut = instanceConfiguration.getPreliminarOutputPath();
        try {
            IOUtil.extractResource("instance-selector/instance-selector.py", "instance_selector.py", isJAR, true);
            IOUtil.extractResource("instance-selector/requirements.txt", "requirements.txt", isJAR, true);
            pb.command("python3", "-m", "pip", "install", "-r", "requirements.txt").start().waitFor();
            pb.command("python3", "instance_selector.py", "-i", path, "-o", preOut, "-p", DEFAULT_OUTPUT_PATH, "-s", size).start().waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    protected void writeCSV(Path p, Object[][] properties) {
        StringBuilder sb = new StringBuilder();
        for (var row : properties) {
            for (var cell : row) {
                sb.append(cell);
                sb.append(',');
            }
            sb.setCharAt(sb.length() - 1, '\n');
        }
        try {
            Files.writeString(p, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file with instance properties", e);
        }
    }

    protected Object[][] instancesToPropertyMatrix(List<String> instanceIDs) {
        Object[][] instanceProperties = null;
        int nProperties = -1;

        try (var pb = Executor.getPBarBuilder("Instance analysis").setInitialMax(instanceIDs.size()).build()) {
            for (int row = 0; row < instanceIDs.size(); row++) {
                var id = instanceIDs.get(row);
                pb.setExtraMessage(id);
                var instance = this.instanceManager.getInstance(id);
                var properties = filterProperties(instance);
                if (instanceProperties == null) {
                    nProperties = properties.size();
                    instanceProperties = new Object[instanceIDs.size() + 1][nProperties + 1]; // +1 for title row and instance name
                    instanceProperties[0][0] = "id";
                    int idx = 1;
                    for (var k : properties.keySet()) {
                        instanceProperties[0][idx++] = k;
                    }
                } else {
                    if (nProperties != properties.size()) {
                        throw new IllegalArgumentException("Instance %s contains %s properties instead of the expected %s".formatted(instance.getId(), properties.size(), nProperties));
                    }
                }
                instanceProperties[row + 1][0] = instance.getId();
                for (int i = 1; i < instanceProperties[0].length; i++) {
                    instanceProperties[row + 1][i] = properties.get((String) instanceProperties[0][i]);
                }
                pb.step();
            }
        }
        return instanceProperties;
    }


    protected Map<String, Double> filterProperties(I instance) {
        var properties = Instance.getUniquePropertiesKeys();
        var copy = new LinkedHashMap<String, Double>();
        properties.stream()
                .filter(e -> !ignoredProperties.contains(e))
                .filter(e -> instance.getProperty(e) instanceof Number)
                .sorted(String::compareTo)
                .forEach(e -> copy.put(e, ((Number) instance.getProperty(e)).doubleValue()));
        return copy;
    }
}
