package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static java.nio.charset.StandardCharsets.UTF_8;

class AutoLlmLifecycleTest {
    @TempDir Path temp;
    @Test void frozenFilesArchivesAndArtifactPathsCannotBeChangedOrEscaped() throws Exception {
        Path source = temp.resolve("source"); Files.createDirectories(source.resolve("src/main/java")); Path file = source.resolve("src/main/java/Original.java"); Files.writeString(file, "before");
        var hashes = AutoLlmFileUtil.sourceHashes(source); Files.writeString(file, "after");
        assertThrows(IllegalStateException.class, () -> AutoLlmFileUtil.verifySources(source, hashes));
        assertThrows(IllegalStateException.class, () -> AutoLlmFileUtil.archiveSources(source, temp.resolve("source.zip"), hashes));
        assertThrows(IllegalArgumentException.class, () -> AutoLlmFileUtil.inside(source, "../../outside"));
    }
    @Test void componentNamingAndActualParameterCapAreEnforced() {
        var baseline = Set.of("project.Original");
        assertEquals(List.of("ImproverNew"), AutoLlmComponentUtil.validate(Set.of("project.Original", "project.ImproverNew"), baseline, 299, 299));
        assertThrows(IllegalStateException.class, () -> AutoLlmComponentUtil.validate(Set.of("project.FilteredOutImprover"), baseline, 1, 299));
        assertThrows(IllegalStateException.class, () -> AutoLlmComponentUtil.validate(Set.of("project.ImproverNew"), baseline, 300, 299));
    }
    @Test void recoveryPreservesCompletedTrialsAndReconcilesOwnedChildren() throws Exception {
        var settings = new AutoLlmConfig(null, temp.toString(), null, null, null, null, null, null, null, null, null, null, null, null);
        var store = new AutoLlmCampaignStore(settings); var campaign = new AutoLlmCampaign(); campaign.id = UUID.randomUUID().toString(); campaign.createdAt = "2026-10-07"; campaign.config = settings; campaign.status = AutoLlmCampaign.CampaignStatus.RUNNING;
        var completed = new AutoLlmCampaign.Trial(); completed.number = 1; completed.status = AutoLlmCampaign.TrialStatus.SUCCEEDED; campaign.trials.add(completed);
        var interrupted = new AutoLlmCampaign.Trial(); interrupted.number = 2; interrupted.status = AutoLlmCampaign.TrialStatus.TUNING;
        var attempt = new AutoLlmCampaign.Attempt(); attempt.stage = AutoLlmCampaign.AttemptStage.TUNING; attempt.directory = "trials/002/attempts/001-tuning"; interrupted.attempts.add(attempt); campaign.trials.add(interrupted); store.save(campaign);
        var generation = new AutoLlmCampaign.Trial(); generation.number = 3; generation.status = AutoLlmCampaign.TrialStatus.GENERATION;
        var generating = new AutoLlmCampaign.Attempt(); generating.stage = AutoLlmCampaign.AttemptStage.GENERATION; generating.directory = "trials/003/attempts/001-generation"; generation.attempts.add(generating); campaign.trials.add(generation); store.save(campaign);
        String thread = UUID.randomUUID().toString();
        Path log = store.directory(campaign.id).resolve(generating.directory).resolve("stdout.log"); Files.createDirectories(log.getParent()); Files.writeString(log, "{\"type\":\"thread.started\",\"thread_id\":\"" + thread + "\"}\n");
        Path source = store.directory(campaign.id).resolve("trials/003/source");
        CodexEvidenceUtil.begin(temp.resolve("codex-home"), source, log.getParent(), null);
        AutoLlmFileUtil.append(temp.resolve("codex-home/sessions/rollout-" + thread + ".jsonl"), Map.of("type", "session_meta", "payload", Map.of("id", thread, "cwd", source.toString())));
        var lost = new AutoLlmCampaign.Trial(); lost.number = 4; lost.status = AutoLlmCampaign.TrialStatus.GENERATION;
        var missing = new AutoLlmCampaign.Attempt(); missing.stage = AutoLlmCampaign.AttemptStage.GENERATION; missing.directory = "trials/004/attempts/001-generation"; lost.attempts.add(missing); campaign.trials.add(lost); store.save(campaign);
        AutoLlmFileUtil.append(store.directory(campaign.id).resolve(missing.directory).resolve("stdout.log"), Map.of("type", "thread.started", "thread_id", UUID.randomUUID().toString()));
        var runner = new AutoLlmProcessRunner();
        var service = new AutoLlmCampaignService(settings, store, runner, codex(runner), mock(AutoLlmReportService.class), new InstanceConfiguration());
        try {
            service.recover(); var recovered = store.load(campaign.id);
            assertEquals("INTERRUPTED", recovered.status.name());
            for (var trial : recovered.trials) {
                var context = new AutoLlmExecutionContext(recovered.id, "", store.directory(campaign.id), recovered.prompt);
                context.config = recovered.config; context.trial = trial;
                var task = new AutoLlmTrialRunner(context, store.directory(campaign.id).resolve(trial.directory == null ? "trials/%03d".formatted(trial.number) : trial.directory), runner, codex(runner), new AutoLlmTrialRunner.Listener() {
                    public void state(AutoLlmExecutionContext.Snapshot snapshot) {} public void event(String kind, Object detail) {}
                }, () -> false, new java.util.concurrent.Semaphore(1));
                task.reconcile();
            }
            assertEquals("SUCCEEDED", recovered.trials.getFirst().status.name()); assertEquals("INTERRUPTED", recovered.trials.get(1).attempts.getFirst().status.name());
            assertEquals(thread, recovered.trials.get(2).threadId); assertTrue(recovered.trials.get(2).resumeTurn); assertEquals(0, recovered.trials.get(2).repairTurns);
            assertEquals("ARCHIVED", recovered.trials.get(2).attempts.getFirst().codexEvidence.status());
            assertEquals("BLOCKED", recovered.trials.get(3).status.name()); assertFalse(recovered.trials.get(3).resumeTurn);
            assertEquals("FAILED", recovered.trials.get(3).attempts.getFirst().codexEvidence.status());
            try (var lock = store.lock()) {
                assertTrue(lock.lock().isValid());
                assertThrows(IllegalStateException.class, store::lock);
            }
        } finally { service.close(); }
    }
    private CodexTaskRunner codex(AutoLlmProcessRunner runner) { return new CodexTaskRunner(runner, new MockEnvironment().withProperty("CODEX_HOME", temp.resolve("codex-home").toString())); }
}
