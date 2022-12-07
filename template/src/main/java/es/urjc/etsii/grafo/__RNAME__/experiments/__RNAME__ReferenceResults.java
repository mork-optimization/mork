// TODO Optional: Uncomment and configure the reference results to use when comparing our algorithms during experimentation
//package es.urjc.etsii.grafo.__RNAME__.experiments;
//
//import es.urjc.etsii.grafo.solver.services.reference.ReferenceResult;
//import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.HashMap;
//import java.util.Map;
//
//public class __RNAME__ReferenceResults extends ReferenceResultProvider {
//
//    private final Map<String, ReferenceResult> sotaResults = new HashMap<>();
//
//    public __RNAME__ReferenceResults() throws IOException {
//        // For each line in our CSV/TSV file with the following structure, fill the hashmap
//        // instanceName,o.fValue,executionTime
//        Files.lines(Path.of("sota.csv")).forEach(l -> {
//            var parts = l.split(",");
//            var referenceResult = new ReferenceResult();
//            referenceResult.setScore(parts[1]);         // Change columns if necessary
//            referenceResult.setTimeInSeconds(parts[2]); // Use .setTimeToBestInSeconds if it is a TTB instead of total time
//            // If the value comes from an exact algorithm, you may mark it as optimal
//            // The framework will validate that no solution improves this result, as it would be a bug
//            // optimalValue defaults to false if not specified
//            // referenceResult.setOptimalValue(true);
//            sotaResults.put(parts[0], referenceResult); // Instance name is usually the first column
//        });
//    }
//
//    @Override
//    public ReferenceResult getValueFor(String instanceName) {
//        // Return reference result if exists, empty one if not.
//        // In this example, it would be valid to have instances for which the reference values are unknown.
//        // If it is not valid, you may check if the map contains the instance, and if not, throw a RuntimeException
//        return this.sotaResults.getOrDefault(instanceName, EMPTY_REFERENCE_RESULT);
//    }
//
//    @Override
//    public String getProviderName() {
//        // There can be multiple providers (i.e, multiple classes extending ReferenceResultProvider)
//        // as long as they have different provider names
//        return "__RNAME__ SOTA";
//    }
//}
