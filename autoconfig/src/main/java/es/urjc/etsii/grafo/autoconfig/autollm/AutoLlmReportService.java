package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.AutoLlmExecutionService;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.*;

/** Pooled export uses cached values and reporting-only algorithm names, never generated classes. */
@Service
public class AutoLlmReportService<S extends Solution<S,I>, I extends Instance> {
    private final ExcelSerializer<S,I> excel;
    private final InstanceConfiguration instances;
    public AutoLlmReportService(ExcelSerializer<S,I> excel, InstanceConfiguration instances) { this.excel = excel; this.instances = instances; }
    public void report(AutoLlmCampaign campaign, Path root) {
        var rows = new ArrayList<WorkUnitResult<S,I>>();
        var portable = new ArrayList<Map<String, Object>>();
        var counts = new TreeMap<String, Integer>();
        for (var trial : campaign.trials) {
            counts.merge(trial.status, 1, Integer::sum);
            if (trial.comparisonDirectory == null) continue;
            for (var row : AutoLlmFileUtil.events(root.resolve(trial.comparisonDirectory).resolve("execution/events.jsonl"))) {
                if (!"evaluation-result".equals(row.get("kind")) || !Boolean.TRUE.equals(row.get("measured"))) continue;
                var enriched = new LinkedHashMap<>(row);
                String localInstance = AutoLlmExecutionService.localPath(campaign.config, root, (String) row.get("instance"));
                enriched.put("remoteInstance", row.get("instance")); enriched.put("instance", localInstance);
                enriched.put("campaignId", campaign.id); enriched.put("trial", trial.number); enriched.put("tuningSeed", trial.tuningSeed); enriched.put("trialStatus", trial.status);
                portable.add(enriched);
                if (!"SUCCEEDED".equals(row.get("state"))) continue;
                String name = "T%02d-%s".formatted(trial.number, row.get("algorithm"));
                var algorithm = new Algorithm<S,I>(name) { @Override public S algorithm(I instance) { throw new UnsupportedOperationException("Reporting name only"); } };
                var objectives = new LinkedHashMap<String, Double>();
                for (var entry : AutoLlmFileUtil.objectMap(row.get("objectives")).entrySet()) if (entry.getValue() instanceof Number value) objectives.put(entry.getKey(), value.doubleValue());
                var properties = AutoLlmFileUtil.objectMap(row.get("properties"));
                properties.put("AutoLLM trial", trial.number); properties.put("Tuning seed", trial.tuningSeed); properties.put("Comparison seed", row.get("seed")); properties.put("Algorithm role", row.get("algorithm"));
                rows.add(new WorkUnitResult<>(UUID.fromString((String) row.get("id")), true, "autollm-campaign", localInstance, (String) row.get("instanceId"),
                        algorithm, (String) row.get("iteration"), null, objectives, properties, ((Number) row.get("executionTimeNanos")).longValue(), ((Number) row.get("timeToBestNanos")).longValue(), null));
            }
        }
        AutoLlmFileUtil.write(root.resolve("reports/comparison-rows.json"), portable);
        AutoLlmFileUtil.write(root.resolve("reports/summary.json"), Map.of("campaignId", campaign.id, "status", campaign.status, "trials", counts, "measuredRows", portable.size()));
        if (!rows.isEmpty()) {
            String name = "autollm-" + campaign.id;
            var paths = new HashMap<>(instances.getPaths()); paths.put(name, campaign.config.comparisonPath()); instances.setPath(paths);
            excel.serializeResults(name, rows, root.resolve("reports/campaign.xlsx"));
        }
    }
}
