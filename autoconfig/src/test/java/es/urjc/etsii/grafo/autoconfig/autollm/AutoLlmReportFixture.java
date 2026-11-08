package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.FMode;
import java.nio.file.Path;
import java.util.*;

final class AutoLlmReportFixture {
    static final String START = "2026-11-01T00:00:00Z";
    static AutoLlmCampaign campaign(Path root) {
        var c = new AutoLlmCampaign(); c.id = UUID.randomUUID().toString(); c.createdAt = START;
        c.startedAt = START; c.stoppedAt = "2026-11-01T00:10:00Z"; c.status = AutoLlmCampaign.CampaignStatus.COMPLETED_WITH_FAILURES;
        c.config = AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", root.getParent().toString(), "comparison", Map.of("repetitions", 2)), AutoLlmConfig.class);
        c.baselineCommit = "frozen-commit";
        var target = new AutoLlmCampaign.TargetState(); target.directory = "targets/worker"; target.resolvedConfig = c.config;
        c.targets.put("worker", target);
        AutoLlmFileUtil.write(root.resolve("targets/worker/baseline-preflight/check/execution/reporting-metadata.json"), Map.of("primaryObjective", "cost", "objectives", Map.of("cost", "MINIMIZE", "quality", "MAXIMIZE")));
        for (int n = 1; n <= 2; n++) {
            var t = new AutoLlmCampaign.Trial(); t.number = n; t.directory = "trials/" + n; t.targetId = "worker";
            t.status = n == 1 ? AutoLlmCampaign.TrialStatus.SUCCEEDED : AutoLlmCampaign.TrialStatus.FAILED;
            c.trials.add(t); Path directory = root.resolve(t.directory);
            AutoLlmFileUtil.write(directory.resolve("control/config.json"), c.config);
            AutoLlmFileUtil.write(directory.resolve("data/comparison-manifest.json"), Map.of("/gone/a", Map.of("path", "/gone/a", "sha256", "a"), "/gone/b", Map.of("path", "/gone/b", "sha256", "b")));
            AutoLlmFileUtil.write(directory.resolve("baseline/components.json"), List.of("project.Sota"));
            AutoLlmFileUtil.write(directory.resolve("frozen/components.json"), List.of("project.Sota", "project.GreedyNew"));
            for (var stage : List.of(AutoLlmCampaign.AttemptStage.GENERATION, AutoLlmCampaign.AttemptStage.PREPARATION, AutoLlmCampaign.AttemptStage.COMPARISON)) {
                var a = new AutoLlmCampaign.Attempt(); a.number = t.attempts.size()+1; a.stage = stage;
                a.status = AutoLlmCampaign.AttemptStatus.SUCCEEDED; a.directory = t.directory + "/attempts/" + a.number;
                a.startedAt = "2026-11-01T00:0" + a.number + ":00Z"; a.finishedAt = "2026-11-01T00:0" + a.number + ":30Z";
                t.attempts.add(a); t.complete(stage.committedStage(), a.directory, null);
                if (stage.generation()) {
                    AutoLlmFileUtil.write(root.resolve(a.directory + "/manifest.json"), Map.of("inventory", List.of("One new constructive"), "components", List.of("GreedyNew uses regret"), "metaheuristics", List.of(), "tests", List.of("Feasibility verified"), "limitations", List.of("Expected benefits only"),
                            "generatedComponents", List.of(Map.of("className", "project.GreedyNew", "role", "Constructive", "description", "Regret insertion <script>alert('x')</script>"))));
                    var tokens = new GenerationEvidence.Tokens(100L, 20L, 0L, 30L, 10L, 130L);
                    var summary = new GenerationEvidence.Summary("COMPLETE", tokens, 1, 1, 0, 0);
                    AutoLlmFileUtil.write(root.resolve(a.directory + "/usage.json"), new GenerationEvidence.Usage(summary,
                            List.of(new GenerationEvidence.Entry("response-"+n,"thread-"+n,"fixture",tokens)), List.of(), List.of(), List.of()));
                }
            }
            t.winner = Map.of("$component", "SimpleAlgorithm", "constructive", Map.of("$component", "GreedyNew", "alpha", 0.5));
            for (String instance : List.of("a", "b")) for (String role : List.of("SOTA", "IraceProposal")) for (int repetition = 0; repetition < 2; repetition++) {
                boolean failed = n == 2 && role.equals("IraceProposal");
                result(root, t, instance, role, repetition, failed ? "FAILED" : "SUCCEEDED", role.equals("SOTA") || instance.equals("b") ? 100 : 80);
            }
        }
        var preparation = c.trials.getFirst().attempts.get(1);
        for (var event : List.of(Map.of("timestamp", "2026-11-01T00:02:00Z", "stage", "BUILD"), Map.of("timestamp", "2026-11-01T00:02:10Z", "stage", "METADATA")))
            AutoLlmFileUtil.append(root.resolve("campaign-events.jsonl"), Map.of("kind", "preparation-stage", "timestamp", event.get("timestamp"), "detail", Map.of("targetId", "worker", "detail", Map.of("attempt", preparation.directory, "stage", event.get("stage")))));
        AutoLlmFileUtil.write(root.resolve("campaign.json"), c);
        GenerationEvidence.report(c, root);
        return c;
    }
    /** Varied evidence and more than one dashboard page, for actual browser and print checks. */
    static AutoLlmCampaign chartCampaign(Path root) {
        var c = campaign(root); var first = c.trials.getFirst();
        var generation = first.attempts.getFirst(); var preparation = first.attempts.get(1); var comparison = first.attempts.getLast();
        var nested = chartAttempt(first, 2, AutoLlmCampaign.AttemptStage.LOCAL_PREPARATION, "00:01:10", "00:01:20");
        preparation.number = 3; preparation.status = AutoLlmCampaign.AttemptStatus.FAILED;
        preparation.problem = new AutoLlmFailureUtil.Failure(AutoLlmFailureUtil.Category.COMPONENT, "GENERATED_TEST", "Fixture: generated component failed its feasibility test", List.of(), false, "Repair the component");
        var repair = chartAttempt(first, 4, AutoLlmCampaign.AttemptStage.REPAIR, "00:03:00", "00:03:40");
        var retry = chartAttempt(first, 5, AutoLlmCampaign.AttemptStage.PREPARATION, "00:04:00", "00:05:00");
        comparison.number = 6; comparison.startedAt = "2026-11-01T00:06:00Z"; comparison.finishedAt = "2026-11-01T00:06:30Z";
        first.attempts = new ArrayList<>(List.of(generation, nested, preparation, repair, retry, comparison)); first.repairTurns = 1;
        var previous = AutoLlmFileUtil.read(root.resolve(generation.directory + "/usage.json"), GenerationEvidence.Usage.class);
        var tokens = new GenerationEvidence.Tokens(200L, 50L, 0L, 70L, 20L, 270L);
        var entries = new ArrayList<>(previous.entries()); entries.add(new GenerationEvidence.Entry("repair-response", "thread-1", "fixture", tokens));
        AutoLlmFileUtil.write(root.resolve(repair.directory + "/usage.json"), new GenerationEvidence.Usage(GenerationEvidence.aggregate(entries, 1, 0, 0), entries, List.of(), List.of(), List.of()));
        c.trials.get(1).attempts.getLast().status = AutoLlmCampaign.AttemptStatus.FAILED;
        c.trials.get(1).problem = new AutoLlmFailureUtil.Failure(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "FIXTURE_CONNECTION", "Fixture: worker disconnected", List.of(), true, "Reconnect");
        c.trials.get(1).infrastructureRetries = 2; c.trials.get(1).connectionRetries.put("comparison-job", 3);
        for (int n = 3; n <= 27; n++) {
            var t = new AutoLlmCampaign.Trial(); t.number = n; t.directory = "trials/" + n; t.targetId = n < 16 ? "worker" : "second-worker";
            t.status = n == 27 ? AutoLlmCampaign.TrialStatus.PENDING : AutoLlmCampaign.TrialStatus.INTERRUPTED; c.trials.add(t);
            if (n == 27) continue;
            var a = chartAttempt(t, 1, AutoLlmCampaign.AttemptStage.GENERATION, "00:00:30", "00:0" + (n % 6 + 1) + ":30");
            a.status = n % 3 == 0 ? AutoLlmCampaign.AttemptStatus.QUOTA_LIMIT : AutoLlmCampaign.AttemptStatus.INTERRUPTED; t.attempts.add(a);
            if (n == 26) { a.finishedAt = null; continue; }
            var counts = new GenerationEvidence.Tokens(n * 1000L, n * 200L, 0L, n * 130L, n % 2 == 0 ? null : n * 20L, n * 1130L);
            var rows = List.of(new GenerationEvidence.Entry("response-" + n, "thread-" + n, "fixture", counts));
            AutoLlmFileUtil.write(root.resolve(a.directory + "/usage.json"), new GenerationEvidence.Usage(GenerationEvidence.aggregate(rows, n % 2, n % 2 == 0 ? 1 : 0, 0), rows, List.of(), List.of(), List.of()));
        }
        GenerationEvidence.report(c, root); AutoLlmFileUtil.write(root.resolve("campaign.json"), c); return c;
    }
    private static AutoLlmCampaign.Attempt chartAttempt(AutoLlmCampaign.Trial trial, int number, AutoLlmCampaign.AttemptStage stage, String start, String end) {
        var a = new AutoLlmCampaign.Attempt(); a.number = number; a.stage = stage; a.status = AutoLlmCampaign.AttemptStatus.SUCCEEDED;
        a.directory = trial.directory + "/chart-attempts/" + number; a.startedAt = "2026-11-01T" + start + "Z"; a.finishedAt = "2026-11-01T" + end + "Z"; return a;
    }
    static void result(Path root, AutoLlmCampaign.Trial trial, String instance, String role, int repetition, String state, double cost) {
        var row = new LinkedHashMap<String,Object>();
        row.put("id", UUID.randomUUID().toString()); row.put("kind", "evaluation-result"); row.put("measured", true); row.put("state", state);
        row.put("instance", "/gone/"+instance); row.put("instanceId", instance); row.put("algorithm", role); row.put("iteration", "rep-"+repetition); row.put("seed", repetition);
        row.put("objectives", Map.of("cost", cost, "quality", cost)); row.put("executionTimeNanos", 1000000000); row.put("timeToBestNanos", 500000000); row.put("properties", Map.of());
        AutoLlmFileUtil.append(root.resolve(trial.latestDirectory(AutoLlmCampaign.Stage.COMPARISON) + "/execution/events.jsonl"), row);
    }
}
