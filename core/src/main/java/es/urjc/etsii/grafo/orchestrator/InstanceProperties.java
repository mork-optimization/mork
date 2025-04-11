package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InstanceProperties<I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(InstanceProperties.class);
    public static final String DEFAULT_OUTPUT_PATH = "instance_properties.csv";
    public static final Set<String> ignoredProperties = Set.of(Instance.LOAD_TIME_NANOS);
    protected final InstanceManager<I> instanceManager;
    protected final InstanceConfiguration instanceConfiguration;

    public InstanceProperties(
            InstanceManager<I> instanceManager,
            InstanceConfiguration instanceConfiguration
    ) {
        this.instanceManager = instanceManager;
        this.instanceConfiguration = instanceConfiguration;
    }

    @Override
    public void run(String... args) {
        long start = System.nanoTime();
        try {
            EventPublisher.getInstance().publishEvent(new ExecutionStartedEvent(Context.getObjectivesW(), List.of("Instance analysis")));
            analyzeInstances();
        } finally {
            long end = System.nanoTime();
            EventPublisher.getInstance().publishEvent(new ExecutionEndedEvent(end - start));
        }
    }

    public void analyzeInstances(){
        String path = instanceConfiguration.getForSelection();
        if (path != null && !path.isBlank()) {
            instanceConfiguration.getPaths().put("default", path);
        }
        var instanceIDs = this.instanceManager.getInstanceSolveOrder("default", false);
        log.info("Analyzing instances...");
        var properties = instancesToPropertyMatrix(instanceIDs);
        log.info("Writting CSV...");
        writeCSV(Path.of(DEFAULT_OUTPUT_PATH), properties);
        log.info("Finished writing CSV");
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
                pb.setExtraMessage(FilenameUtils.getName(id));
                var instance = this.instanceManager.getInstance(id);
                var properties = filterProperties(instance);
                if (instanceProperties == null) {
                    nProperties = properties.size();
                    instanceProperties = new Object[instanceIDs.size() + 1][nProperties + 2]; // +1 for title row, +2 for instance id and path
                    instanceProperties[0][0] = "id";
                    instanceProperties[0][1] = "path";
                    int idx = 2;
                    for (var k : properties.keySet()) {
                        instanceProperties[0][idx++] = k;
                    }
                } else {
                    if (nProperties != properties.size()) {
                        throw new IllegalArgumentException("Instance %s contains %s properties instead of the expected %s".formatted(instance.getId(), properties.size(), nProperties));
                    }
                }
                instanceProperties[row + 1][0] = instance.getId();
                instanceProperties[row + 1][1] = instance.getPath();
                for (int i = 2; i < instanceProperties[0].length; i++) {
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
