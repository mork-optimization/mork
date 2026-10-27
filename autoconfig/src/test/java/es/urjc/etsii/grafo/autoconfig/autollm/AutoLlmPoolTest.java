package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Exercises the real serialized coordinator with deterministic, barrier-controlled target runners. */
class AutoLlmPoolTest {
    @TempDir Path temp;
    @FunctionalInterface interface Execution { void run(AutoLlmExecutionContext model, BooleanSupplier stop) throws Exception; }
    private final Map<String, Execution> executions = new ConcurrentHashMap<>();
    private final Map<String, String> endpoints = new ConcurrentHashMap<>();
    private AutoLlmCampaignStore store;
    private AutoLlmCampaign campaign;
    private AutoLlmCampaignService service;
    private AutoLlmConfig.Server server(String id) { return new AutoLlmConfig.Server(id, id, "/work", "java", Map.of(), null, 2, null); }
    private void setup(int trials, String... ids) throws Exception {
        Path input = temp.resolve("input.txt"); Files.writeString(input, "same payload");
        Path runner = temp.resolve("runner.jar"); Files.writeString(runner, "fixture");
        var definitions = new ArrayList<AutoLlmConfig.Server>(); for (String id : ids) definitions.add(server(id));
        var config = new AutoLlmConfig(null, temp.toString(), trials, null, null, null, null, input.toString(), input.toString(), null, null, null, null,
                new AutoLlmConfig.Execution(2, null, null, null, null, null, null, null, null, AutoLlmConfig.Target.SSH, new AutoLlmConfig.Ssh(ids[0], runner.toString(), definitions)));
        store = new AutoLlmCampaignStore(config); campaign = new AutoLlmCampaign(); campaign.id = UUID.randomUUID().toString(); campaign.createdAt = "2026-10-22"; campaign.config = config; campaign.baselineCommit = "fixed";
        for (String id : ids) { var t = new AutoLlmCampaign.TargetState(); t.definition = server(id); t.directory = "targets/" + id; t.revision = ++campaign.poolRevision; t.baselineReady = true; t.resolvedConfig = config.forServer(t.definition); campaign.targets.put(id, t); }
        for (int i = 1; i <= trials; i++) { var trial = new AutoLlmCampaign.Trial(); trial.number = i; trial.tuningSeed = 100L + i; campaign.trials.add(trial); }
        AutoLlmFileUtil.write(store.directory(campaign.id).resolve("control/canonical-hashes.json"), Map.of());
        AutoLlmFileUtil.write(store.directory(campaign.id).resolve("control/instance-paths.json"), Map.of());
        AutoLlmFileUtil.write(store.directory(campaign.id).resolve("control/input-aliases.json"), Map.of());
        AutoLlmFileUtil.write(store.directory(campaign.id).resolve("control/sota.json"), Map.of());
        AutoLlmFileUtil.write(store.directory(campaign.id).resolve("control/seeds.json"), List.of()); store.save(campaign);
        service = new AutoLlmCampaignService(config, store, new AutoLlmProcessRunner(), mock(CodexTaskRunner.class), mock(AutoLlmReportService.class), new InstanceConfiguration()) {
            @Override protected AutoLlmTrialRunner runner(AutoLlmExecutionContext model, Path root, AutoLlmTrialRunner.Listener listener, BooleanSupplier stop, Semaphore sessions) {
                String id = root.getFileName().toString(); var runner = mock(AutoLlmTrialRunner.class);
                when(runner.snapshot()).thenAnswer(_ -> model.snapshot());
                when(runner.endpoint()).thenAnswer(_ -> endpoints.getOrDefault(id, id));
                doAnswer(_ -> {
                    AutoLlmFileUtil.write(root.resolve("baseline/components.json"), List.of("Original"));
                    AutoLlmFileUtil.write(root.resolve("baseline/dependencies.json"), Map.of());
                    AutoLlmFileUtil.write(root.resolve("baseline-preflight/check/execution/search-space.json"), Map.of());
                    AutoLlmFileUtil.write(root.resolve("baseline-preflight/check/execution/check.json"), Map.of("parameters", 1)); return null;
                }).when(runner).prepare(anyBoolean());
                doAnswer(_ -> {
                    model.trial.status = AutoLlmCampaign.TrialStatus.TUNING; listener.state(model.snapshot());
                    executions.get(id).run(model, stop); return null;
                }).when(runner).execute();
                return runner;
            }
        };
    }
    private void await(BooleanSupplier condition) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (!condition.getAsBoolean() && System.nanoTime() < end) Thread.sleep(10);
        assertTrue(condition.getAsBoolean(), () -> AutoLlmFileUtil.JSON.writeValueAsString(store.load(campaign.id)));
    }
    @Test void pendingCancellationCannotBeResumedWhenRecoveryReportsItUnavailable() throws Exception {
        setup(1, "a"); campaign.status = AutoLlmCampaign.CampaignStatus.CANCELLATION_PENDING; store.save(campaign);
        try {
            assertFalse(service.recovery(campaign.id).resumeCampaign().allowed());
            assertThrows(IllegalStateException.class, () -> service.start(campaign.id, true, Set.of()));
            assertNull(service.active()); assertEquals(AutoLlmCampaign.CampaignStatus.CANCELLATION_PENDING, store.load(campaign.id).status);
        } finally { service.close(); }
    }
    @Test void waitingRemoteCampaignCanResumeWhenRecoveryReportsItAvailable() throws Exception {
        setup(1, "a"); campaign.status = AutoLlmCampaign.CampaignStatus.WAITING_FOR_REMOTE; store.save(campaign);
        executions.put("a", (model, stop) -> model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED);
        try {
            assertTrue(service.recovery(campaign.id).resumeCampaign().allowed());
            service.start(campaign.id, true, Set.of()); await(() -> service.active() == null);
            assertEquals(AutoLlmCampaign.CampaignStatus.SUCCEEDED, store.load(campaign.id).status);
        } finally { service.close(); }
    }
    @Test void reservationFailureDoesNotPublishActiveOrLeakTheWorkspaceLock() throws Exception {
        setup(1, "a");
        try {
            Files.createDirectory(store.root().resolve("operation-reservation.json.tmp"));
            assertThrows(java.io.UncheckedIOException.class, () -> service.start(campaign.id, false, Set.of()));
            assertNull(service.active()); assertEquals("DRAFT", store.load(campaign.id).status.name());
            try (var lease = store.lock()) { assertTrue(lease.lock().isValid()); }
            Files.delete(store.root().resolve("operation-reservation.json.tmp"));
            executions.put("a", (model, stop) -> model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED);
            service.start(campaign.id, false, Set.of()); await(() -> service.active() == null);
            assertEquals("SUCCEEDED", store.load(campaign.id).status.name());
        } finally { service.close(); }
    }
    @Test void qualificationFailureIsSeparateFromParameterLimitsAndSubsetMustBeExplicit() throws Exception {
        setup(1, "a", "b"); endpoints.put("a", "same"); endpoints.put("b", "same");
        try {
            var result = service.baselineCheck(campaign.id);
            assertFalse(result.ready()); assertEquals(1, result.failedTargets().size());
            assertEquals(1, result.qualifiedTargets().size());
            assertThrows(IllegalStateException.class, () -> service.start(campaign.id, false, Set.of()));
            assertNull(service.active());
            String healthy = result.qualifiedTargets().getFirst();
            executions.put(healthy, (model, stop) -> model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED);
            service.start(campaign.id, false, Set.of(healthy)); await(() -> service.active() == null);
            var saved = store.load(campaign.id);
            assertEquals(healthy, saved.trials.getFirst().targetId);
            assertTrue(saved.targets.get(result.failedTargets().getFirst()).draining);
        } finally { service.close(); }
    }
    @Test void nestedTargetRootsKeepTheServerWorkspaceLeaseAndIsolateTrialPaths() throws Exception {
        var namespace = Path.of("/work/campaigns/id/targets/a");
        assertEquals(Path.of("/work"), es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadPathUtil.workspace(namespace));
        assertEquals(Path.of("/work"), es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadPathUtil.workspace(Path.of("/work/campaigns/screening")));
        var context = new AutoLlmExecutionContext("id", "targets/a", temp, "prompt");
        context.trial = new AutoLlmCampaign.Trial();
        var attempt = new AutoLlmCampaign.Attempt(); attempt.directory = "targets/a/trials/001/attempts/001-generation";
        context.trial.attempts.add(attempt);
        var snapshot = context.snapshot();
        assertEquals(attempt.directory, snapshot.trial().attempts.getFirst().directory);
        assertEquals(temp.resolve(attempt.directory), context.path(attempt.directory));
        attempt.status = AutoLlmCampaign.AttemptStatus.SUCCEEDED;
        assertEquals("RUNNING", snapshot.trial().attempts.getFirst().status.name());

    }
    @Test void runsTwoTrialsConcurrentlyAndIsolatesOneTargetsFailure() throws Exception {
        setup(3, "a", "b"); var entered = new CountDownLatch(2); var finish = new CountDownLatch(1);
        executions.put("a", (model, stop) -> { entered.countDown(); assertTrue(finish.await(10, TimeUnit.SECONDS)); throw new IllegalStateException("host lost"); });
        executions.put("b", (model, stop) -> { entered.countDown(); assertTrue(finish.await(10, TimeUnit.SECONDS)); model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED; });
        try {
            service.start(campaign.id, false, java.util.Set.of()); assertTrue(entered.await(10, TimeUnit.SECONDS), "Both targets must enter before either finishes");
            var active = store.load(campaign.id); assertEquals("a", active.trials.get(0).targetId); assertEquals("b", active.trials.get(1).targetId);
            finish.countDown(); await(() -> service.active() == null);
            var result = store.load(campaign.id); assertEquals("NEEDS_ATTENTION", result.status.name()); assertEquals("BLOCKED", result.trials.get(0).status.name());
            assertEquals("SUCCEEDED", result.trials.get(2).status.name()); assertEquals("b", result.trials.get(2).targetId); assertEquals(103, result.trials.get(2).tuningSeed);
            assertEquals("NEEDS_ATTENTION", result.targets.get("a").status.name()); assertEquals(0, result.trials.get(0).repairTurns);
        } finally { finish.countDown(); service.close(); }
    }
    @Test void enrollsWhileBusyAndDrainsWithoutMovingTheAssignedTrial() throws Exception {
        setup(3, "a"); var entered = new CountDownLatch(1); var finish = new CountDownLatch(1);
        executions.put("a", (model, stop) -> { entered.countDown(); assertTrue(finish.await(10, TimeUnit.SECONDS)); model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED; });
        executions.put("b", (model, stop) -> model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED);
        try {
            service.start(campaign.id, false, java.util.Set.of()); assertTrue(entered.await(10, TimeUnit.SECONDS)); service.drainTarget(campaign.id, "a", false);
            assertThrows(IllegalStateException.class, () -> service.drainTarget(campaign.id, "a", true));
            service.addTarget(campaign.id, "b", server("b")); service.addTarget(campaign.id, "b", server("b"));
            await(() -> store.load(campaign.id).trials.get(2).status == AutoLlmCampaign.TrialStatus.SUCCEEDED);
            var busy = store.load(campaign.id); assertEquals("a", busy.trials.get(0).targetId); assertEquals("b", busy.trials.get(1).targetId); assertTrue(busy.targets.get("a").draining);
            finish.countDown(); await(() -> service.active() == null); assertEquals("SUCCEEDED", store.load(campaign.id).status.name());
            assertEquals(2, store.load(campaign.id).targets.size()); service.drainTarget(campaign.id, "a", true);
            assertThrows(IllegalArgumentException.class, () -> service.addTarget(campaign.id, "b", server("a")));
        } finally { finish.countDown(); service.close(); }
    }
    @ParameterizedTest @ValueSource(booleans = {false, true})
    void operatorCanFailAReconciledBlockedTrialAndReleaseItsTarget(boolean drained) throws Exception {
        setup(3, "a", "b"); var finish = new CountDownLatch(1); var nextTrial = new CountDownLatch(1);
        var problem = new AutoLlmFailureUtil.Failure(AutoLlmFailureUtil.Category.UNKNOWN, "IRACE_SAMPLING_FAILED",
                "IRACE exhausted model sampling", List.of("runner.R.stderr.log"), false, "Inspect the sampler");
        executions.put("a", (model, stop) -> {
            var trial = model.trial;
            if (trial.number == 1) { trial.status = AutoLlmCampaign.TrialStatus.BLOCKED; trial.problem = problem; trial.failure = "Original R failure"; return; }
            nextTrial.countDown(); assertTrue(finish.await(10, TimeUnit.SECONDS)); trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED;
        });
        executions.put("b", (model, stop) -> { assertTrue(finish.await(10, TimeUnit.SECONDS)); model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED; });
        try {
            service.start(campaign.id, false, java.util.Set.of());
            await(() -> store.load(campaign.id).targets.get("a").status == AutoLlmCampaign.TargetStatus.NEEDS_ATTENTION);
            assertThrows(IllegalArgumentException.class, () -> service.failTrial(campaign.id, 1, " "));
            assertThrows(IllegalStateException.class, () -> service.failTrial(campaign.id, 2, "Running trial"));
            Path evidence = store.directory(campaign.id).resolve("targets/a/trials/001/error.log");
            Files.createDirectories(evidence.getParent()); Files.writeString(evidence, "Original evidence");
            Path remote = evidence.getParent().resolve("remote-job.json"); Files.writeString(remote, "{}");
            assertThrows(IllegalStateException.class, () -> service.failTrial(campaign.id, 1, "Sampling exhausted"));
            assertEquals("BLOCKED", store.load(campaign.id).trials.getFirst().status.name());
            Files.delete(remote);
            if (drained) service.drainTarget(campaign.id, "a", false);
            service.failTrial(campaign.id, 1, "Sampling exhausted");
            if (drained) {
                var waiting = store.load(campaign.id);
                assertTrue(waiting.targets.get("a").draining); assertEquals("DRAINING", waiting.targets.get("a").status.name());
                assertEquals("PENDING", waiting.trials.get(2).status.name());
                service.resumeTarget(campaign.id, "a");
            }
            assertTrue(nextTrial.await(10, TimeUnit.SECONDS));
            service.failTrial(campaign.id, 1, "Repeated request");
            var saved = store.load(campaign.id); var failed = saved.trials.getFirst();
            assertEquals("FAILED", failed.status.name()); assertEquals(problem, failed.problem); assertEquals("Original R failure", failed.failure);
            assertEquals(101L, failed.tuningSeed); assertEquals(0, failed.repairTurns);
            assertEquals("Original evidence", Files.readString(evidence));
            assertEquals("a", saved.trials.get(2).targetId); assertEquals(3, saved.targets.get("a").assignedTrial);
            String events = Files.readString(store.directory(campaign.id).resolve("campaign-events.jsonl"));
            int dispositionEvents = 0;
            for (String line : events.split("\n")) if (line.contains("trial-failed-by-operator")) dispositionEvents++;
            assertEquals(1, dispositionEvents);
            assertTrue(events.contains("Sampling exhausted"));
            finish.countDown(); await(() -> service.active() == null);
            assertEquals("COMPLETED_WITH_FAILURES", store.load(campaign.id).status.name());
            assertEquals("FAILED", service.failTrial(campaign.id, 1, "Repeated after completion").trials.getFirst().status.name());
        } finally { finish.countDown(); service.close(); }
    }
    @Test void duplicateResolvedEndpointCannotStartASecondWorkload() throws Exception {
        setup(1, "a"); var entered = new CountDownLatch(1); var finish = new CountDownLatch(1);
        campaign.targets.get("a").endpoint = "127.0.0.1:22"; store.save(campaign); endpoints.put("b", "127.0.0.1:22");
        executions.put("a", (model, stop) -> { entered.countDown(); finish.await(10, TimeUnit.SECONDS); model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED; });
        try {
            service.start(campaign.id, false, java.util.Set.of()); assertTrue(entered.await(10, TimeUnit.SECONDS)); service.addTarget(campaign.id, "b", server("b"));
            await(() -> store.load(campaign.id).targets.get("b").status == AutoLlmCampaign.TargetStatus.NEEDS_ATTENTION);
            assertTrue(store.load(campaign.id).targets.get("b").problem.message().contains("Duplicate"));
        } finally { finish.countDown(); service.close(); }
    }
    @ParameterizedTest @ValueSource(booleans = {false, true})
    void resumesOnlyTheChosenTrialOrRequalifiesItsServerWhileOtherWorkContinues(boolean requalify) throws Exception {
        setup(2, "a", "b"); var finish = new CountDownLatch(1); var resumed = new CountDownLatch(1);
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        executions.put("a", (model, stop) -> {
            var trial = model.trial;
            if (calls.getAndIncrement() == 0) { trial.status = AutoLlmCampaign.TrialStatus.BLOCKED; trial.failure = "original"; return; }
            assertEquals(1, trial.number); assertEquals(101L, trial.tuningSeed);
            resumed.countDown(); assertTrue(finish.await(10, TimeUnit.SECONDS)); trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED;
        });
        executions.put("b", (model, stop) -> { assertTrue(finish.await(10, TimeUnit.SECONDS)); model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED; });
        try {
            service.start(campaign.id, false, java.util.Set.of());
            await(() -> store.load(campaign.id).targets.get("a").status == AutoLlmCampaign.TargetStatus.NEEDS_ATTENTION);
            assertTrue(service.recovery(campaign.id).trials().get(1).resume().allowed());
            assertFalse(service.recovery(campaign.id).trials().get(2).resume().allowed());
            if (requalify) service.requalifyTarget(campaign.id, "a"); else service.resumeTrial(campaign.id, 1);
            assertTrue(resumed.await(10, TimeUnit.SECONDS));
            assertFalse(service.recovery(campaign.id).trials().get(1).resume().allowed());
            assertThrows(IllegalStateException.class, () -> service.resumeTrial(campaign.id, 1));
            assertThrows(IllegalStateException.class, () -> service.requalifyTarget(campaign.id, "b"));
            assertEquals("TUNING", store.load(campaign.id).trials.get(1).status.name());
            assertEquals(2, calls.get());
            finish.countDown(); await(() -> service.active() == null);
            assertEquals("SUCCEEDED", store.load(campaign.id).status.name());
            assertTrue(Files.readString(store.directory(campaign.id).resolve("campaign-events.jsonl")).contains(requalify ? "target-requalification-requested" : "trial-resumed-by-operator"));
        } finally { finish.countDown(); service.close(); }
    }
    @Test void failsAnInterruptedTrialBeforeResumingAStoppedCampaign() throws Exception {
        setup(2, "a");
        var trial = campaign.trials.getFirst(); trial.targetId = "a"; trial.directory = "targets/a/trials/001";
        trial.status = AutoLlmCampaign.TrialStatus.INTERRUPTED; trial.failure = "Saved R failure";
        campaign.status = AutoLlmCampaign.CampaignStatus.INTERRUPTED; campaign.targets.get("a").draining = true; store.save(campaign);
        executions.put("a", (model, stop) -> { assertEquals(2, model.trial.number); model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED; });
        try {
            try (var lock = store.lock()) {
                assertThrows(IllegalStateException.class, () -> service.failTrial(campaign.id, 1, "Sampling exhausted"));
            }
            var failed = service.failTrial(campaign.id, 1, "Sampling exhausted");
            assertEquals("INTERRUPTED", failed.status.name()); assertEquals("FAILED", failed.trials.getFirst().status.name());
            assertEquals("Saved R failure", failed.trials.getFirst().failure); assertTrue(failed.targets.get("a").draining);
            assertNull(service.active());
            service.start(campaign.id, true, java.util.Set.of()); await(() -> store.load(campaign.id).status == AutoLlmCampaign.CampaignStatus.WAITING_FOR_TARGETS);
            service.resumeTarget(campaign.id, "a"); await(() -> service.active() == null);
            var result = store.load(campaign.id);
            assertEquals("FAILED", result.trials.getFirst().status.name()); assertEquals("SUCCEEDED", result.trials.get(1).status.name());
            assertEquals("COMPLETED_WITH_FAILURES", result.status.name());
        } finally { service.close(); }
    }
    @Test void cancellationReachesAllActiveTargetsBeforeReleasingOwnership() throws Exception {
        setup(2, "a", "b"); var entered = new CountDownLatch(2); var stopped = new CountDownLatch(2);
        Execution running = (model, stop) -> {
            entered.countDown();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!stop.getAsBoolean() && System.nanoTime() < deadline) Thread.sleep(10);
            assertTrue(stop.getAsBoolean()); model.trial.status = AutoLlmCampaign.TrialStatus.INTERRUPTED; stopped.countDown();
        };
        executions.put("a", running); executions.put("b", running);
        try {
            service.start(campaign.id, false, java.util.Set.of()); assertTrue(entered.await(10, TimeUnit.SECONDS)); service.cancel(campaign.id);
            assertTrue(stopped.await(10, TimeUnit.SECONDS)); await(() -> service.active() == null);
            assertEquals("CANCELLED", store.load(campaign.id).status.name());
            assertFalse(Files.exists(temp.resolve("operation-reservation.json")));
        } finally { service.close(); }
    }
    @ParameterizedTest @ValueSource(booleans = {false, true})
    void canonicalIntegrityFailureStopsDispatchAcrossThePool(boolean sharedInput) throws Exception {
        setup(2, "a"); Path frozen = sharedInput ? temp.resolve("input.txt") : store.directory(campaign.id).resolve("control/sota.json");
        AutoLlmFileUtil.write(store.directory(campaign.id).resolve("control/canonical-hashes.json"), Map.of(sharedInput ? frozen.toString() : "control/sota.json", AutoLlmFileUtil.hash(frozen)));
        var entered = new CountDownLatch(1);
        executions.put("a", (model, stop) -> {
            entered.countDown();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!stop.getAsBoolean() && System.nanoTime() < deadline) Thread.sleep(10);
            assertTrue(stop.getAsBoolean()); model.trial.status = AutoLlmCampaign.TrialStatus.INTERRUPTED;
        });
        try {
            service.start(campaign.id, false, java.util.Set.of()); assertTrue(entered.await(10, TimeUnit.SECONDS)); Files.writeString(frozen, "changed");
            service.addTarget(campaign.id, "b", server("b")); await(() -> service.active() == null);
            var result = store.load(campaign.id); assertEquals("NEEDS_ATTENTION", result.status.name()); assertNotNull(result.problem); assertEquals(AutoLlmFailureUtil.Category.INTEGRITY, result.problem.category());
            assertNull(result.trials.get(1).targetId); assertEquals("REGISTERED", result.targets.get("b").status.name());
        } finally { service.close(); }
    }
    @Test void resumesPinnedTrialOnItsOriginalServerAndKeepsItsSeed() throws Exception {
        setup(1, "a", "b"); var trial = campaign.trials.getFirst(); trial.targetId = "a"; trial.directory = "targets/a/trials/001"; trial.status = AutoLlmCampaign.TrialStatus.INTERRUPTED;
        campaign.status = AutoLlmCampaign.CampaignStatus.INTERRUPTED; campaign.targets.get("a").draining = true; campaign.targets.get("a").assignedTrial = 1; store.save(campaign);
        executions.put("a", (model, stop) -> model.trial.status = AutoLlmCampaign.TrialStatus.SUCCEEDED);
        executions.put("b", (model, stop) -> fail("Pinned trial migrated"));
        try {
            service.start(campaign.id, true, java.util.Set.of()); await(() -> store.load(campaign.id).status == AutoLlmCampaign.CampaignStatus.WAITING_FOR_TARGETS);
            assertNotNull(service.active()); assertNull(store.load(campaign.id).targets.get("a").assignedTrial); service.resumeTarget(campaign.id, "a"); await(() -> service.active() == null);
            var result = store.load(campaign.id).trials.getFirst(); assertEquals("a", result.targetId); assertEquals(101, result.tuningSeed); assertEquals("SUCCEEDED", result.status.name());
        } finally { service.close(); }
    }
}
