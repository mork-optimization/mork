package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.AutoLlmExecutionService;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadPathUtil;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmReportServiceTest {
    @TempDir Path root;
    @Test void streamsLargeCurveJournalsUnderANinetySixMiBHeap() throws Exception {
        Path input = root.resolve("instance.txt"); Files.writeString(input, "input");
        Path journal = root.resolve("comparison/execution/events.jsonl"); Files.createDirectories(journal.getParent());
        var curve = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < 1000; i++) curve.add(Map.of("instant", i, "value", 1000 - i));
        String row = AutoLlmFileUtil.JSON.writeValueAsString(Map.of("kind", "evaluation-result", "measured", true,
                "state", "FAILED", "instance", input.toString(), "curves", Map.of("Cost", curve))) + "\n";
        try (var writer = Files.newBufferedWriter(journal)) { for (int i = 0; i < 4000; i++) writer.write(row); }
        Path log = root.resolve("bounded-heap.log");
        var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-Xmx96m", "-XX:+ExitOnOutOfMemoryError", "-cp",
                System.getProperty("java.class.path"), LowHeapFixture.class.getName(), root.toString()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        if (!finished) process.destroyForcibly();
        assertTrue(finished, "Bounded-memory report did not finish");
        assertEquals(0, process.exitValue(), () -> {
            try { return Files.readString(log); } catch (java.io.IOException e) { return e.toString(); }
        });
        assertEquals(4000, AutoLlmFileUtil.JSON.readTree(Files.readString(root.resolve("reports/summary.json"))).path("measuredRows").asInt());
        assertTrue(Files.size(root.resolve("reports/comparison-rows.json")) > Files.size(journal));
    }
    public static class LowHeapFixture {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public static void main(String[] args) {
            Path root = Path.of(args[0]); String input = root.resolve("instance.txt").toString();
            var campaign = new AutoLlmCampaign(); campaign.id = "bounded-heap"; campaign.config = AutoLlmConfig.defaults().withPaths(input, input);
            var target = new AutoLlmCampaign.TargetState(); target.directory = "targets/worker"; target.resolvedConfig = campaign.config; campaign.targets.put("worker", target);
            var trial = new AutoLlmCampaign.Trial(); trial.number = 1; trial.targetId = "worker"; trial.status = AutoLlmCampaign.TrialStatus.FAILED; trial.complete(AutoLlmCampaign.Stage.COMPARISON, "comparison", null); campaign.trials.add(trial);
            new AutoLlmReportService(null).report(campaign, root);
        }
    }
    @Test void streamingPreservesPartialLastLineRulesAndPropagatesConsumerFailures() throws Exception {
        Path journal = root.resolve("events.jsonl"); Files.writeString(journal, "{\"value\":1}\n{\"partial\":");
        var rows = new ArrayList<Map<String,Object>>(); AutoLlmFileUtil.forEachEvent(journal, rows::add);
        assertEquals(1, rows.size());
        Files.writeString(journal, "{\"value\":1}\n");
        assertThrows(IllegalStateException.class, () -> AutoLlmFileUtil.forEachEvent(journal, row -> { throw new IllegalStateException("consumer failed"); }));
        Files.writeString(journal, "{broken}\n{\"value\":1}\n");
        assertThrows(RuntimeException.class, () -> AutoLlmFileUtil.forEachEvent(journal, row -> {}));
    }
    @ParameterizedTest @ValueSource(booleans = {false, true}) @SuppressWarnings({"rawtypes", "unchecked"})
    void groupsWorkbooksByTargetAndRetainsFailedRowsWithTheirProvenance(boolean remote) throws Exception {
        Path input = root.resolve("instance.txt"); Files.writeString(input, "input");
        var config = AutoLlmConfig.defaults().withPaths(input.toString(), input.toString());
        var campaign = new AutoLlmCampaign(); campaign.id = UUID.randomUUID().toString(); campaign.config = config;
        var captured = new LinkedHashMap<Path,List<WorkUnitResult>>(); var excel = mock(ExcelSerializer.class);
        doAnswer(call -> { captured.put(call.getArgument(3), call.getArgument(1)); return null; }).when(excel).serializeCachedResults(anyString(), anyList(), anyList(), any(Path.class));
        for (String id : List.of("a", "b")) {
            var target = new AutoLlmCampaign.TargetState(); target.directory = "targets/" + id; target.resolvedConfig = config;
            String eventInstance = input.toString();
            if (remote) {
                var execution = new AutoLlmConfig.Execution(null, null, null, null, null, null, null, null, null, AutoLlmConfig.Target.SSH,
                        new AutoLlmConfig.Ssh(id, null, List.of(new AutoLlmConfig.Server(id, id, "/remote", "java", null, null, null, null))));
                var sshConfig = new AutoLlmConfig(config.project(), config.workspace(), null, null, null, null, null,
                        input.toString(), input.toString(), null, null, null, null, execution);
                Path targetRoot = root.resolve(target.directory);
                target.resolvedConfig = new AutoLlmExecutionService(new AutoLlmProcessRunner()).stageInputs(sshConfig, targetRoot);
                var staged = AutoLlmFileUtil.JSON.readTree(Files.readString(targetRoot.resolve("data/staged-inputs.json")));
                eventInstance = WorkloadPathUtil.translate(staged.path(input.toString()).path("path").asString(), targetRoot.toString(), WorkloadPathUtil.remoteRoot(target.resolvedConfig, targetRoot));
            }
            target.host = new WorkloadProtocol.Host(3, Map.of("hostname", id), 1, 1, 1, 1, 1, List.of()); campaign.targets.put(id, target);
            var trial = new AutoLlmCampaign.Trial(); trial.number = campaign.trials.size() + 1; trial.targetId = id; trial.targetRevision = trial.number;
            trial.status = id.equals("a") ? AutoLlmCampaign.TrialStatus.SUCCEEDED : AutoLlmCampaign.TrialStatus.FAILED; trial.complete(AutoLlmCampaign.Stage.COMPARISON, target.directory + "/comparison", null); campaign.trials.add(trial);
            for (String algorithm : List.of("SOTA", "IraceProposal")) {
                var row = new LinkedHashMap<String,Object>(); row.put("kind", "evaluation-result"); row.put("measured", true); row.put("state", id.equals("b") && algorithm.equals("IraceProposal") ? "FAILED" : "SUCCEEDED");
                row.put("id", UUID.randomUUID().toString()); row.put("instance", eventInstance); row.put("instanceId", "instance"); row.put("algorithm", algorithm); row.put("iteration", "0"); row.put("seed", 42);
                row.put("objectives", Map.of("cost", 3)); row.put("properties", Map.of()); row.put("executionTimeNanos", 100); row.put("timeToBestNanos", 50);
                AutoLlmFileUtil.append(root.resolve(trial.outputDirectory(AutoLlmCampaign.Stage.COMPARISON)).resolve("execution/events.jsonl"), row);
            }
            Path comparison = root.resolve(trial.outputDirectory(AutoLlmCampaign.Stage.COMPARISON));
            Files.createDirectories(comparison.resolve("solutions"));
            Files.writeString(comparison.resolve("solutions/tour.json"), "{\"cost\":3}");
            es.urjc.etsii.grafo.autoconfig.autollm.execution.DirectoryArchiveUtil.archive(comparison.resolve("solutions"));
            assertFalse(Files.exists(comparison.resolve("solutions")));
            assertTrue(Files.exists(comparison.resolve("solutions.zip")));
        }
        new AutoLlmReportService(excel).report(campaign, root);
        assertEquals(Set.of(root.resolve("reports/targets/a/campaign.xlsx"), root.resolve("reports/targets/b/campaign.xlsx")), captured.keySet());
        assertEquals(2, captured.get(root.resolve("reports/targets/a/campaign.xlsx")).size());
        assertEquals(1, captured.get(root.resolve("reports/targets/b/campaign.xlsx")).size());
        var portable = AutoLlmFileUtil.JSON.readTree(Files.readString(root.resolve("reports/comparison-rows.json")));
        assertEquals(4, portable.size()); assertEquals("FAILED", portable.get(3).path("state").asString());
        assertEquals(input.toString(), portable.get(3).path("instance").asString());
        assertEquals("b", portable.get(3).path("targetId").asString()); assertEquals("b", portable.get(3).path("executionHost").path("identity").path("hostname").asString());
        assertEquals(2, portable.get(3).path("poolRevision").asLong());
        assertEquals("a", captured.get(root.resolve("reports/targets/a/campaign.xlsx")).getFirst().solutionProperties().get("Execution target"));
    }
}
