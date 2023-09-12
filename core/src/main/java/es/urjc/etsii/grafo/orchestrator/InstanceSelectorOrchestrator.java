package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
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
public class InstanceSelectorOrchestrator<I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(InstanceSelectorOrchestrator.class);
    public static final String DEFAULT_OUTPUT_PATH = "instance_properties.csv";
    private static final Set<String> ignoredProperties = Set.of(Instance.LOAD_TIME_NANOS);
    private final InstanceManager<I> instanceManager;

    public InstanceSelectorOrchestrator(InstanceManager<I> instanceManager) {
        this.instanceManager = instanceManager;
    }

    @Override
    public void run(String... args) {
        var instanceIDs = this.instanceManager.getInstanceSolveOrder("default", false);
        var properties = instancesToPropertyMatrix(instanceIDs);
        log.info("Writting CSV");
        writeCSV(Path.of(DEFAULT_OUTPUT_PATH), properties);
        log.info("Completed");
    }

    protected void writeCSV(Path p, Object[][] properties) {
        StringBuilder sb = new StringBuilder();
        for(var row: properties){
            for(var cell: row){
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

        try(var pb = Executor.getPBarBuilder("Instance validation").setInitialMax(instanceIDs.size()).build()){
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
                    for(var k: properties.keySet()){
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
                .forEach(e-> copy.put(e, ((Number) instance.getProperty(e)).doubleValue()));
        return copy;
    }
}
