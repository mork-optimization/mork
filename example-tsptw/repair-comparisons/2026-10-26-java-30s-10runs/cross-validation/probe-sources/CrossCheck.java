package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.tsptw.Main;
import es.urjc.etsii.grafo.util.Context;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Compile separately against each archived Java implementation. */
public class CrossCheck {
    public static void main(String[] args) throws Exception {
        TSPTWInstance instance;
        try (var reader = Files.newBufferedReader(Path.of(args[0]))) {
            instance = new TSPTWInstanceImporter().importInstance(reader, "cross-validation");
        }
        Context.reset();
        Context.Configurator.setObjectives(Main.OBJECTIVE);
        Metrics.disableMetrics();
        var mapper = new JsonMapper();
        var validator = new TSPTWSolutionValidator();
        Class<?> solutionClass = Class.forName("es.urjc.etsii.grafo.tsptw.model." + args[2]);
        for (String line : Files.readAllLines(Path.of(args[1]))) {
            var record = mapper.readValue(line, Map.class);
            var tour = (List<?>) record.get("tour");
            int n = instance.n();
            boolean valid = tour.size() == n + 1 && ((Number) tour.getFirst()).intValue() == 0
                    && ((Number) tour.getLast()).intValue() == 0;
            boolean[] seen = new boolean[n];
            int[] customers = new int[n - 1];
            if (valid) {
                for (int i = 0; i < n; i++) {
                    int node = ((Number) tour.get(i)).intValue();
                    if (node < 0 || node >= n || seen[node]) { valid = false; break; }
                    seen[node] = true;
                    if (i > 0) customers[i - 1] = node;
                }
            }
            var result = new LinkedHashMap<String, Object>();
            result.put("id", record.get("id"));
            result.put("validPermutation", valid);
            result.put("feasible", false);
            if (valid) {
                var solution = (TSPTWSolution) solutionClass.getConstructor(TSPTWInstance.class).newInstance(instance);
                solution.add(customers);
                var validation = validator.validate(solution);
                result.put("feasible", validation.isValid());
                result.put("cost", solution.cost());
                result.put("violations", solution.constraint_violations());
                result.put("infeasibility", solution.infeasibility());
                result.put("makespan", solution._makespan[n]);
                result.put("arrivals", solution._makespan);
                result.put("validatorFailures", validation.getReasonsFailed());
            }
            System.out.println(mapper.writeValueAsString(result));
        }
        Context.destroy();
    }
}
