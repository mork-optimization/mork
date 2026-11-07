package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmStorageEstimateTest {
    @TempDir Path root;
    private AutoLlmCampaign campaign(int trials) throws Exception {
        Path input = root.resolve("input.txt"), index = root.resolve("inputs.index");
        Files.write(input, new byte[13]); Files.writeString(index, input + "\n");
        var server = new AutoLlmConfig.Server("a", "user@a", "/worker", "java", Map.of(), null, 16, null);
        var second = new AutoLlmConfig.Server("b", "user@b", "/worker", "java", Map.of(), null, 16, null);
        var execution = new AutoLlmConfig.Execution(null, null, null, null, null, null, null, null, null, AutoLlmConfig.Target.SSH,
                new AutoLlmConfig.Ssh("a", "runner.jar", List.of(server, second), null));
        var config = new AutoLlmConfig(null, root.toString(), trials, null, null, null, null, index.toString(), index.toString(), null, null,
                new AutoLlmConfig.Tuning(100, null, null, null), new AutoLlmConfig.Comparison(2, 100L), execution);
        var campaign = new AutoLlmCampaign(); campaign.config = config; campaign.localPreparation.directory = "preparation";
        AutoLlmFileUtil.write(root.resolve("control/storage-layout.json"), Map.of("version", 2));
        for (var definition : List.of(server, second)) {
            var state = new AutoLlmCampaign.TargetState(); state.definition = definition; state.directory = "targets/" + definition.id();
            campaign.targets.put(definition.id(), state);
        }
        campaign.reviewedTargets.add("a");
        Path baseline = root.resolve("preparation/baseline/source"); Files.createDirectories(baseline.resolve(".git")); Files.createDirectories(baseline.resolve("target"));
        Files.write(baseline.resolve("source.txt"), new byte[5]); Files.write(baseline.resolve(".git/objects"), new byte[11]);
        Files.write(baseline.resolve("target/project.jar"), new byte[17]);
        return campaign;
    }
    private AutoLlmStorageEstimate.Capacity capacity(long bytes) {
        return new AutoLlmStorageEstimate.Capacity(bytes, "now", "LIVE_REMOTE_FILESTORE", null, null);
    }
    @Test void measuresBaselineAndUniqueInputContainersAndOnlyProbesSelectedWorkers() throws Exception {
        var campaign = campaign(10); var calls = new AtomicInteger();
        var estimate = AutoLlmStorageEstimate.estimate(campaign, root, campaign.config, (config, target, state) -> {
            calls.incrementAndGet(); assertEquals("a", state.definition.id()); return capacity(Long.MAX_VALUE);
        });
        assertEquals(1, calls.get()); assertEquals(Set.of("a"), estimate.workers().keySet());
        assertEquals(5, estimate.sizingBasis().get("sourceBytes")); assertEquals(11, estimate.sizingBasis().get("checkoutGitBytes"));
        assertEquals(17, estimate.sizingBasis().get("artifactBytes")); assertEquals(13, estimate.sizingBasis().get("inputContainerBytes"));
        assertEquals(1, estimate.sizingBasis().get("testingInstances")); assertEquals(10, estimate.remainingTrials());
        assertNotNull(estimate.controller().capacity().availableBytes());
        assertEquals("WITHIN_ESTIMATE", estimate.workers().get("a").assessment());
        assertTrue(estimate.controller().breakdown().containsKey("rebuildAndCompressionScratch"));
        assertTrue(estimate.approvalLine().startsWith("Storage (additional peak estimate): controller "));
        assertFalse(estimate.approvalLine().contains("\n"));
    }
    @Test void exhaustedSpaceWarnsAndMissingCapacityNeverUsesOldPreflightAsCurrent() throws Exception {
        var campaign = campaign(2); campaign.reviewedTargets.add("b");
        var unknown = new AutoLlmStorageEstimate.Capacity(null, "yesterday", "UNAVAILABLE", 12345L, "offline");
        var estimate = AutoLlmStorageEstimate.estimate(campaign, root, campaign.config, (config, target, state) -> state.definition.id().equals("a") ? capacity(1) : unknown);
        assertEquals("MAY_NOT_FIT", estimate.workers().get("a").assessment());
        assertEquals("UNKNOWN", estimate.workers().get("b").assessment());
        assertNull(estimate.workers().get("b").capacity().availableBytes());
        assertEquals(12345L, estimate.workers().get("b").capacity().lastKnownAvailableBytes());
        // The controller can also be at risk on a real low-space filesystem.
        var atRisk = estimate.controller().assessment().equals("MAY_NOT_FIT") ? "controller, a" : "a";
        assertTrue(estimate.approvalLine().contains("WARNING: may not fit with 20% headroom on " + atRisk));
        assertTrue(estimate.approvalLine().contains("WARNING: capacity unavailable on b"));
    }
    @Test void warningIncludesControllerWhenItAlsoMayNotFit() {
        var range = new AutoLlmStorageEstimate.Range(1, 2);
        var worker = new AutoLlmStorageEstimate.Location("user@a", "/worker", range, capacity(1), "MAY_NOT_FIT", Map.of());
        var workers = Map.of("a", worker);
        var controllerWithSpace = new AutoLlmStorageEstimate.Location("controller", "/controller", range,
                capacity(Long.MAX_VALUE), "WITHIN_ESTIMATE", Map.of());
        var controllerWithoutSpace = new AutoLlmStorageEstimate.Location("controller", "/controller", range,
                capacity(1), "MAY_NOT_FIT", Map.of());

        assertTrue(AutoLlmStorageEstimate.approvalLine(controllerWithSpace, workers)
                .contains("WARNING: may not fit with 20% headroom on a"));
        assertTrue(AutoLlmStorageEstimate.approvalLine(controllerWithoutSpace, workers)
                .contains("WARNING: may not fit with 20% headroom on controller, a"));
    }
    @Test void remainingTrialsAndEvaluationBudgetDriveAdditionalEvidence() throws Exception {
        var campaign = campaign(4);
        for (int i = 0; i < 4; i++) { var trial = new AutoLlmCampaign.Trial(); trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED; campaign.trials.add(trial); }
        var completed = AutoLlmStorageEstimate.estimate(campaign, root, campaign.config, (config, target, state) -> capacity(Long.MAX_VALUE));
        assertEquals(0, completed.remainingTrials()); assertEquals(0, completed.controller().additionalPeak().highBytes());
        assertEquals(0, completed.workers().get("a").additionalPeak().highBytes());
        var amended = AutoLlmStorageEstimate.estimate(campaign, root, campaign.config.withTrials(5), (config, target, state) -> capacity(Long.MAX_VALUE));
        assertEquals(1, amended.remainingTrials()); assertTrue(amended.controller().additionalPeak().highBytes() > 0);
        var c = campaign.config;
        var larger = new AutoLlmConfig(c.project(), c.workspace(), 5, c.seed(), c.maxParameters(), c.sotaFile(), c.initialConfigurationsFile(), c.trainingPath(), c.comparisonPath(), c.codex(), c.preparation(),
                new AutoLlmConfig.Tuning(1_000_000, null, null, null), c.comparison(), c.execution());
        var increased = AutoLlmStorageEstimate.estimate(campaign, root, larger, (config, target, state) -> capacity(Long.MAX_VALUE));
        assertTrue(increased.controller().breakdown().get("retainedEvidenceAndSourcePatches").highBytes() > amended.controller().breakdown().get("retainedEvidenceAndSourcePatches").highBytes());
    }
    @Test void absentRunnerDoesNotBootstrapOrClaimSavedCapacityIsLive() throws Exception {
        var campaign = campaign(1); var target = campaign.targets.get("a");
        target.host = new WorkloadProtocol.Host(WorkloadProtocol.VERSION, Map.of(), 16, 16, 1, 1, 987654L, List.of());
        var estimate = AutoLlmStorageEstimate.estimate(campaign, root, campaign.config);
        var capacity = estimate.workers().get("a").capacity();
        assertEquals("UNAVAILABLE", capacity.source()); assertNull(capacity.availableBytes()); assertEquals(987654L, capacity.lastKnownAvailableBytes());
        assertFalse(Files.exists(root.resolve("targets/a/.transport")));
    }
    @Test void samplesEvaluationVolumeAndAddsDetailedTimingAllowance() throws Exception {
        var campaign = campaign(1); Path journal = root.resolve("preparation/baseline-preflight/check/execution/events.jsonl");
        Files.createDirectories(journal.getParent()); Files.writeString(journal, "{\"kind\":\"evaluation-result\",\"payload\":\"" + "a".repeat(50000) + "\"}\n");
        var estimate = AutoLlmStorageEstimate.estimate(campaign, root, campaign.config, (config, target, state) -> capacity(Long.MAX_VALUE));
        assertEquals(1, estimate.sizingBasis().get("sampledEvaluations")); assertTrue(estimate.sizingBasis().get("observedBytesPerEvaluation") > 50000);
        var c = campaign.config; var p = c.project();
        var timing = new AutoLlmConfig(new AutoLlmConfig.Project(p.repository(), p.ref(), p.buildCommand(), p.artifact(), List.of("--solver.time-stats=true")), c.workspace(), c.trials(), c.seed(), c.maxParameters(), c.sotaFile(), c.initialConfigurationsFile(), c.trainingPath(), c.comparisonPath(), c.codex(), c.preparation(), c.tuning(), c.comparison(), c.execution());
        var detailed = AutoLlmStorageEstimate.estimate(campaign, root, timing, (config, target, state) -> capacity(Long.MAX_VALUE));
        assertTrue(detailed.controller().additionalPeak().highBytes() > estimate.controller().additionalPeak().highBytes());
    }
}
