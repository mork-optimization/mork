package es.urjc.etsii.grafo.CAP;

import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import org.apache.commons.collections4.map.HashedMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CAPReferenceResults extends ReferenceResultProvider {

    private final static String filename = "sota.tsv";
    private final static String separator = "\t";

    Map<String, ReferenceResult> sotaResults = new HashedMap<>();

    public CAPReferenceResults() throws IOException {
        Files.lines(Path.of(filename)).forEach(l -> {
            var parts = l.split(separator);
            var referenceResult = new ReferenceResult();
            referenceResult.addScore(Main.OBJ.getName(), parts[1]);
            referenceResult.setTimeInSeconds(parts[2]);
            sotaResults.put(parts[0], referenceResult);
        });
    }

    @Override
    public ReferenceResult getValueFor(String instanceName) {
        var ref = this.sotaResults.get(instanceName);
        if(ref != null){
            return ref;
        }
        // try removing suffix
        var alternativeName = instanceName.replaceAll("_\\d\\.txt", ".txt");
        ref = this.sotaResults.get(alternativeName);
        var result = new ReferenceResult();
        if(ref != null){
            result.setTimeInSeconds(ref.getTimeInSeconds()); // ignoring scores, only use the time
        }
        return result;
    }

    @Override
    public String getProviderName() {
        return "PaperResults";
    }
}

