package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.benchmark.BenchmarkService;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelConfig;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelSerializer;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.util.Context;

class AutoLlmReportMemoryIT {
    @TempDir Path root;
    @Test void streamsLargeCurveJournalsUnderANinetySixMiBHeap() throws Exception {
        Path input = root.resolve("instance.txt"); Files.writeString(input, "input");
        Path journal = root.resolve("comparison/execution/events.jsonl"); Files.createDirectories(journal.getParent());
        var curve = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < 1000; i++) curve.add(Map.of("instant", i, "value", 1000 - i));
        var event = new LinkedHashMap<>(Map.of("kind", "evaluation-result", "measured", true,
                "state", "FAILED", "instance", input.toString(), "instanceId", "instance", "algorithm", "IraceProposal", "curves", Map.of("Cost", curve)));
        event.put("id", UUID.randomUUID().toString()); event.put("iteration", "0"); event.put("seed", 42);
        event.put("executionTimeNanos", -1); event.put("timeToBestNanos", -1);
        event.put("objectives", Map.of()); event.put("properties", Map.of());
        String row = AutoLlmFileUtil.JSON.writeValueAsString(event) + "\n";
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
        public static void main(String[] args) throws Exception {
            Path root = Path.of(args[0]); String input = root.resolve("instance.txt").toString();
            var campaign = new AutoLlmCampaign(); campaign.id = "bounded-heap"; campaign.config = AutoLlmConfig.defaults().withPaths(input, input);
            var target = new AutoLlmCampaign.TargetState(); target.directory = "targets/worker"; target.resolvedConfig = campaign.config; campaign.targets.put("worker", target);
            var trial = new AutoLlmCampaign.Trial(); trial.number = 1; trial.directory = "."; AutoLlmFileUtil.write(root.resolve("control/config.json"), campaign.config); trial.targetId = "worker"; trial.status = AutoLlmCampaign.TrialStatus.FAILED; trial.complete(AutoLlmCampaign.Stage.COMPARISON, "comparison", null); campaign.trials.add(trial);
            System.setProperty("java.awt.headless", "true");
            Context.Configurator.setObjectives(Objective.of("Cost", FMode.MINIMIZE, TestSolution::getScore, TestMove::getScoreChange));
            var excelConfig = new ExcelConfig(); excelConfig.setInstanceSheetEnabled(false);
            var excel = new ExcelSerializer(excelConfig, List.of(), Optional.empty(), null, new BenchmarkService());
            new AutoLlmReportService(excel).report(campaign, root);
            Path workbookPath = root.resolve("reports/targets/worker/campaign.xlsx");
            try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(Files.newInputStream(workbookPath))) {
                if (workbook.getSheet(ExcelSerializer.FAILURES_SHEET).getLastRowNum() != 4000) {
                    throw new AssertionError("Workbook must preserve all failed executions");
                }
            }
            var snapshot = AutoLlmPortableReport.build(campaign, root,
                    new AutoLlmPortableReport.Selection("Cost", es.urjc.etsii.grafo.algorithms.FMode.MINIMIZE), java.time.Instant.now());
            String html = AutoLlmReportHtml.render(snapshot);
            if (!html.contains("4000")) throw new AssertionError("Partial report must preserve all recorded failures");
            Files.writeString(root.resolve("reports/report.html"), html);
        }
    }
}
