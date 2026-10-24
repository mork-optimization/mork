package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmReportServiceTest {
    @TempDir Path root;
    @Test @SuppressWarnings({"rawtypes", "unchecked"})
    void groupsWorkbooksByTargetAndRetainsFailedRowsWithTheirProvenance() throws Exception {
        Path input = root.resolve("instance.txt"); Files.writeString(input, "input");
        var config = AutoLlmConfig.defaults().withPaths(input.toString(), input.toString());
        var campaign = new AutoLlmCampaign(); campaign.id = UUID.randomUUID().toString(); campaign.config = config;
        var captured = new LinkedHashMap<Path,List<WorkUnitResult>>(); var excel = mock(ExcelSerializer.class);
        doAnswer(call -> { captured.put(call.getArgument(3), call.getArgument(1)); return null; }).when(excel).serializeCachedResults(anyString(), anyList(), anyList(), any(Path.class));
        for (String id : List.of("a", "b")) {
            var target = new AutoLlmCampaign.TargetState(); target.directory = "targets/" + id; target.resolvedConfig = config;
            target.host = new WorkloadProtocol.Host(3, Map.of("hostname", id), 1, 1, 1, 1, 1, List.of()); campaign.targets.put(id, target);
            var trial = new AutoLlmCampaign.Trial(); trial.number = campaign.trials.size() + 1; trial.targetId = id; trial.targetRevision = trial.number;
            trial.status = id.equals("a") ? "SUCCEEDED" : "FAILED"; trial.comparisonDirectory = target.directory + "/comparison"; campaign.trials.add(trial);
            for (String algorithm : List.of("SOTA", "IraceProposal")) {
                var row = new LinkedHashMap<String,Object>(); row.put("kind", "evaluation-result"); row.put("measured", true); row.put("state", id.equals("b") && algorithm.equals("IraceProposal") ? "FAILED" : "SUCCEEDED");
                row.put("id", UUID.randomUUID().toString()); row.put("instance", input.toString()); row.put("instanceId", "instance"); row.put("algorithm", algorithm); row.put("iteration", "0"); row.put("seed", 42);
                row.put("objectives", Map.of("cost", 3)); row.put("properties", Map.of()); row.put("executionTimeNanos", 100); row.put("timeToBestNanos", 50);
                AutoLlmFileUtil.append(root.resolve(trial.comparisonDirectory).resolve("execution/events.jsonl"), row);
            }
        }
        new AutoLlmReportService(excel).report(campaign, root);
        assertEquals(Set.of(root.resolve("reports/targets/a/campaign.xlsx"), root.resolve("reports/targets/b/campaign.xlsx")), captured.keySet());
        assertEquals(2, captured.get(root.resolve("reports/targets/a/campaign.xlsx")).size());
        assertEquals(1, captured.get(root.resolve("reports/targets/b/campaign.xlsx")).size());
        var portable = AutoLlmFileUtil.JSON.readTree(Files.readString(root.resolve("reports/comparison-rows.json")));
        assertEquals(4, portable.size()); assertEquals("FAILED", portable.get(3).path("state").asString());
        assertEquals("b", portable.get(3).path("targetId").asString()); assertEquals("b", portable.get(3).path("executionHost").path("identity").path("hostname").asString());
        assertEquals(2, portable.get(3).path("poolRevision").asLong());
        assertEquals("a", captured.get(root.resolve("reports/targets/a/campaign.xlsx")).getFirst().solutionProperties().get("Execution target"));
    }
}
