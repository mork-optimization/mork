package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;

/** Pooled export uses cached values and reporting-only algorithm names, never generated classes. */
@Service
public class AutoLlmReportService<S extends Solution<S,I>, I extends Instance> {
    private final ExcelSerializer<S,I> excel;
    public AutoLlmReportService(ExcelSerializer<S,I> excel) { this.excel = excel; }
    public void report(AutoLlmCampaign campaign, Path root) {
        var grouped = new LinkedHashMap<String, List<WorkUnitResult<S,I>>>();
        long[] measuredRows = {0};
        var counts = new TreeMap<String, Integer>();
        Path portable = root.resolve("reports/comparison-rows.json"), temporary = portable.resolveSibling("comparison-rows.json.tmp");
        try {
        Files.createDirectories(portable.getParent());
        try (var writer = AutoLlmFileUtil.JSON.writerWithDefaultPrettyPrinter().writeValuesAsArray(temporary)) {
        for (var trial : campaign.trials) {
            counts.merge(trial.status.name(), 1, Integer::sum);
            if (trial.latestDirectory(AutoLlmCampaign.Stage.COMPARISON) == null) continue;
            var localInstances = AutoLlmPoolUtil.localInstances(campaign, root, trial);
            AutoLlmFileUtil.forEachEvent(root.resolve(trial.latestDirectory(AutoLlmCampaign.Stage.COMPARISON)).resolve("execution/events.jsonl"), row -> {
                if (!"evaluation-result".equals(row.get("kind")) || !Boolean.TRUE.equals(row.get("measured"))) return;
                var enriched = new LinkedHashMap<>(row);
                String localInstance = localInstances.apply((String) row.get("instance"));
                enriched.put("remoteInstance", row.get("instance")); enriched.put("instance", localInstance);
                enriched.put("campaignId", campaign.id); enriched.put("trial", trial.number); enriched.put("tuningSeed", trial.tuningSeed); enriched.put("trialStatus", trial.status);
                enriched.put("targetId", trial.targetId); enriched.put("poolRevision", trial.targetRevision);
                var target = campaign.targets.get(trial.targetId);
                enriched.put("executionHost", target.host);
                writer.write(enriched); measuredRows[0]++;
                if (!"SUCCEEDED".equals(row.get("state"))) return;
                String name = "T%02d-%s".formatted(trial.number, row.get("algorithm"));
                var algorithm = new Algorithm<S,I>(name) { @Override public S algorithm(I instance) { throw new UnsupportedOperationException("Reporting name only"); } };
                var objectives = new LinkedHashMap<String, Double>();
                for (var entry : AutoLlmFileUtil.objectMap(row.get("objectives")).entrySet()) if (entry.getValue() instanceof Number value) objectives.put(entry.getKey(), value.doubleValue());
                var properties = AutoLlmFileUtil.objectMap(row.get("properties"));
                properties.put("Execution target", trial.targetId); properties.put("Pool revision", trial.targetRevision); properties.put("Execution host", target.host == null ? "unknown" : target.host.identity().get("hostname"));
                properties.put("AutoLLM trial", trial.number); properties.put("Tuning seed", trial.tuningSeed); properties.put("Comparison seed", row.get("seed")); properties.put("Algorithm role", row.get("algorithm"));
                grouped.computeIfAbsent(trial.targetId, _ -> new ArrayList<>()).add(new WorkUnitResult<>(UUID.fromString((String) row.get("id")), true, "autollm-campaign", localInstance, (String) row.get("instanceId"),
                        algorithm, (String) row.get("iteration"), null, objectives, properties, ((Number) row.get("executionTimeNanos")).longValue(), ((Number) row.get("timeToBestNanos")).longValue(), null));
            });
        }
        }
        try { Files.move(temporary, portable, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException e) { Files.move(temporary, portable, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        AutoLlmFileUtil.write(root.resolve("reports/summary.json"), Map.of("campaignId", campaign.id, "status", campaign.status, "trials", counts, "measuredRows", measuredRows[0], "poolRevision", campaign.poolRevision, "targets", campaign.targets));
        for (var entry : grouped.entrySet()) {
            var target = campaign.targets.get(entry.getKey());
            String name = "autollm-" + campaign.id + "-" + entry.getKey();
            var inputs = es.urjc.etsii.grafo.util.InstancePathUtil.list(target.resolvedConfig.comparisonPath());
            excel.serializeCachedResults(name, entry.getValue(), inputs, root.resolve("reports/targets/" + entry.getKey() + "/campaign.xlsx"));
        }
    }
}
