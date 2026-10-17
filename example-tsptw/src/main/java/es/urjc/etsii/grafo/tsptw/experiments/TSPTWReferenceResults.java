package es.urjc.etsii.grafo.tsptw.experiments;

import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.tsptw.Main;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Published travel-cost references, including the optimality status of each instance. */
public class TSPTWReferenceResults extends ReferenceResultProvider {

    private final Map<String, ReferenceResult> results = new HashMap<>();

    public TSPTWReferenceResults() throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("reference-results/travel-cost.csv").getInputStream(), StandardCharsets.UTF_8))) {
            if (!"instance,cost,optimal,source".equals(reader.readLine())) {
                throw new IOException("Invalid TSPTW reference-results header");
            }
            String line;
            while ((line = reader.readLine()) != null) {
                var parts = line.split(",", 4);
                if (parts.length != 4 || !(parts[2].equals("true") || parts[2].equals("false"))) {
                    throw new IOException("Invalid TSPTW reference result: " + line);
                }
                var reference = new ReferenceResult().addScore(Main.OBJECTIVE.getName(), parts[1]);
                reference.setOptimalValue(Boolean.parseBoolean(parts[2]));
                if (results.put(parts[0], reference) != null) {
                    throw new IOException("Duplicate TSPTW reference result: " + parts[0]);
                }
            }
        }
    }

    @Override
    public ReferenceResult getValueFor(String instanceName) {
        return results.getOrDefault(instanceName, EMPTY_REFERENCE_RESULT);
    }

    @Override
    public String getProviderName() {
        return "Lopez-Ibanez TSPTW travel cost";
    }
}
