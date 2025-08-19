package es.urjc.etsii.grafo.VRPOD.experiments;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class VRPODTimeLimit extends TimeLimitCalculator<VRPODSolution, VRPODInstance> {

    private static final Logger log = LoggerFactory.getLogger(VRPODTimeLimit.class);
    private static final int maxTime = 300_000; // 300 seconds (5 min) max time for any instance

    Map<String, Double> results;

    public VRPODTimeLimit() throws IOException {
        this.results = new HashMap<>();
        var lines = Files.readAllLines(Path.of("sota.tsv"));
        for(var l: lines){
            var parts = l.split("\t");
            results.put(parts[0], Double.parseDouble(parts[1]));
        }
    }

    @Override
    public long timeLimitInMillis(VRPODInstance instance, Algorithm<VRPODSolution, VRPODInstance> algorithm) {
        var refResult = results.get(instance.getId());
        if(refResult == null){
            //log.warn("Unknown reference instance: {}", instance.getId());
            return 1_000; // 1 Segundos si se desconoce la instancia para saltarsela rapido
        }
        var desiredTime = (long)(refResult * 1000)*2; // Double the reference timelimit as the state of the art
        return Math.min(maxTime, desiredTime); // Cap time due to hardware limitations, there are too many instances to execute
    }
}
