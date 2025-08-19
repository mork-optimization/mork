package es.urjc.etsii.grafo.BMSSC.experiment;

import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ReimplementationResults extends ReferenceResultProvider {

    Map<String, ReferenceResult> results;

    public ReimplementationResults() throws IOException {
        this.results = new HashMap<>();
        var lines = Files.readAllLines(Path.of("sotaTimes.tsv"));
        for(var l: lines){
            var parts = l.split("\t");
            var result = new ReferenceResult();
            result.setTimeInSeconds(parts[1]);
            results.put(parts[0], result);
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
