////TODO Optional: Uncomment and configure the reference results to use when comparing our algorithms during experimentation
//package es.urjc.etsii.grafo.__RNAME__.experiments;
//
//
// import es.urjc.etsii.grafo.__RNAME__.Main;
// import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
// import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
//
// import java.io.IOException;
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
//
//            // Each objective can have a different reference value, in this example we set the score for the only objective that exists
//            referenceResult.setScores(Map.of(Main.OBJECTIVE_NAME, Double.parseDouble(parts[1]))); // Change columns if necessary
//
//            referenceResult.setTimeInSeconds(parts[2]); // Use .setTimeToBestInSeconds instead if the value is TimeToBest (TTB) instead of total execution time
//
//            // If the value comes from an exact algorithm, you may mark it as optimal
//            // The framework will validate that no solution improves this result, and report it as a bug if it happens
//            // optimalValue defaults to false if not specified
//            // referenceResult.setOptimalValue(true);
//
//            sotaResults.put(parts[0], referenceResult); // Instance name is usually the first column, update if necessary
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
