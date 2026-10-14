package es.urjc.etsii.grafo.bmssc.experiment;

import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ReimplementationResults extends ReferenceResultProvider {

    private final Map<String, ReferenceResult> results = new HashMap<>();

    public ReimplementationResults() throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("sotaTimes.tsv").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                var parts = line.split("\t");
                var result = new ReferenceResult();
                result.setTimeInSeconds(parts[1]);
                results.put(parts[0], result);
            }
        }
    }

    @Override
    public ReferenceResult getValueFor(String instanceName) {
        return this.results.getOrDefault(instanceName, EMPTY_REFERENCE_RESULT);
    }

    @Override
    public String getProviderName() {
        return "PaperOS";
    }
}
