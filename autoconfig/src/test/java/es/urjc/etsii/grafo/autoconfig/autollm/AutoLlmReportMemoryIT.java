package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmReportMemoryIT {
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
            var trial = new AutoLlmCampaign.Trial(); trial.number = 1; trial.directory = "."; AutoLlmFileUtil.write(root.resolve("control/config.json"), campaign.config); trial.targetId = "worker"; trial.status = AutoLlmCampaign.TrialStatus.FAILED; trial.complete(AutoLlmCampaign.Stage.COMPARISON, "comparison", null); campaign.trials.add(trial);
            new AutoLlmReportService(null).report(campaign, root);
        }
    }
}
