package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class CampaignManagementV8Test {
    @TempDir Path temporary;
    private AutoLlmConfig config(String json) { return AutoLlmFileUtil.JSON.readValue(json, AutoLlmConfig.class); }

    @Test void heapDefaultsApplyToExecutionAndLocalPreparationWithoutReplacingOverrides() {
        var defaults = config("{}");
        assertEquals("8g", defaults.execution().heap());
        assertEquals("8g", defaults.localPreparation().execution().heap());
        var explicit = config("""
                {"preparation":{"localHeap":"4g"},"execution":{"heap":"16g"}}
                """);
        assertEquals("16g", explicit.execution().heap());
        assertEquals("4g", explicit.localPreparation().execution().heap());
    }

    @Test void workspaceLocksAreAutomaticAndIndependent() throws Exception {
        Path a = temporary.resolve("a"), b = temporary.resolve("b");
        assertNotEquals(WorkspaceState.identity(a), WorkspaceState.identity(b));
        try (var first = WorkspaceState.acquire(a, () -> false, () -> {});
             var second = WorkspaceState.acquire(b, () -> false, () -> {});
             var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            var cancelled = new java.util.concurrent.atomic.AtomicBoolean();
            var waiting = new CountDownLatch(1);
            var contender = pool.submit(() -> { try (var lock = WorkspaceState.acquire(a, cancelled::get, waiting::countDown)) { return true; } });
            assertTrue(waiting.await(5, TimeUnit.SECONDS)); assertFalse(contender.isDone()); cancelled.set(true);
            assertThrows(ExecutionException.class, () -> contender.get(5, TimeUnit.SECONDS));
        }
        try (var next = WorkspaceState.acquire(a, () -> false, () -> {})) { assertTrue(next.lock().isValid()); }
    }
    @Test void canonicalDefaultsReconnectHeapPathsAndInheritedWorkersAreEquivalent() throws Exception {
        String repo = temporary.toRealPath().toString().replace('\\', '/');
        var omitted = config("""
                {"project":{"repository":"%s"},"execution":{"workers":4,"target":"SSH","heap":"2g","ssh":{"servers":[{"id":"a","host":"alias","workspace":"/remote/../opaque"}]}}}
                """.formatted(repo));
        var explicit = config("""
                {"project":{"repository":"%s/./"},"codex":{"model":" "},"execution":{"workers":4,"target":"SSH","heap":"2048M","ssh":{"reconnect":{"initialDelayMillis":2000,"maximumDelayMillis":60000,"budgetMillis":1800000},"servers":[{"id":"a","host":"alias","workspace":"/remote/../opaque","workers":4,"heap":"2097152k"}]}}}
                """.formatted(repo));
        var a = AutoLlmNormalization.normalize(omitted); var b = AutoLlmNormalization.normalize(explicit);
        assertEquals(a, b); assertEquals(AutoLlmFileUtil.canonicalHash(a), AutoLlmFileUtil.canonicalHash(b));
        assertEquals("/remote/../opaque", a.execution().ssh().onlyServer().workspace());
        assertEquals("/remote/../opaque/campaigns/" + temporary.getFileName(), WorkloadPathUtil.remoteRoot(a, temporary));
        assertEquals(a, AutoLlmNormalization.normalize(a));
        var diff = AutoLlmNormalization.diff(Map.of("seeds", List.of(1, 2), "budget", 10), Map.of("seeds", List.of(2, 1), "budget", 11));
        var paths = new ArrayList<String>(); for (var difference : diff) paths.add(difference.path());
        assertEquals(List.of("budget", "seeds[0]", "seeds[1]"), paths);
    }




    @Test void workspaceLedgersCannotClaimAnotherWorkspacesResources() throws Exception {
        Path a = temporary.toRealPath().resolve("a"), b = temporary.toRealPath().resolve("b");
        var first = new OwnershipManifest(a); var second = new OwnershipManifest(b);
        Path operation = a.resolve("campaign"); var owned = first.directory(operation, "campaign", operation.toString());
        assertThrows(IllegalStateException.class, () -> second.directory(operation, "campaign", b.toString()));
        Path transcript = temporary.resolve("session.jsonl");
        Files.writeString(transcript, AutoLlmFileUtil.JSON.writeValueAsString(Map.of("type", "session_meta", "payload", Map.of("id", "session", "cwd", operation.toString()))) + "\n");
        first.transcript(transcript, "session", operation, operation.toString());
        assertThrows(IllegalStateException.class, () -> second.transcript(transcript, "session", operation, b.toString()));
        assertTrue(second.resources().isEmpty()); assertEquals(2, first.resources().size()); OwnershipManifest.verify(owned);
    }
    @Test void externalScreeningReferencesSurviveOwnerWorkspaceCleanupUntilTheConsumerIsDeleted() throws Exception {
        Path a = temporary.toRealPath().resolve("consumer"), b = temporary.toRealPath().resolve("provider");
        var consumer = new OwnershipManifest(a); var provider = new OwnershipManifest(b);
        consumer.directory(a, "workspace", a.toString()); provider.directory(b, "workspace", b.toString());
        String campaignId = UUID.randomUUID().toString(); Path campaign = a.resolve(campaignId);
        consumer.directory(campaign, "campaign", campaign.toString());
        Path screening = b.resolve("screenings/" + UUID.randomUUID()), execution = screening.resolve("evidence");
        provider.directory(screening, "screening", screening.toString()); provider.directory(execution, "execution", execution.toString());
        Path disposable = b.resolve(UUID.randomUUID().toString()); provider.directory(disposable, "campaign", disposable.toString());
        consumer.screeningDependency(execution, campaign);
        var ownerCleanup = new AutoLlmCleanup(new AutoLlmCampaignStore(config("{\"workspace\":\"" + b.toString().replace('\\', '/') + "\"}")));
        var ownerPlan = ownerCleanup.plan(new AutoLlmCleanup.Selection("workspace", null, null, false, true));
        assertTrue(ownerPlan.blockers().isEmpty(), ownerPlan.blockers().toString());
        assertTrue(ownerPlan.retain().containsKey(screening.toString())); assertTrue(ownerPlan.retain().containsKey(execution.toString()));
        assertEquals("SUCCEEDED", ownerCleanup.apply(ownerPlan.digest(), true).status());
        assertFalse(Files.exists(disposable)); assertTrue(Files.exists(execution)); assertFalse(provider.resources().isEmpty());
        var consumerCleanup = new AutoLlmCleanup(new AutoLlmCampaignStore(config("{\"workspace\":\"" + a.toString().replace('\\', '/') + "\"}")));
        var consumerPlan = consumerCleanup.plan(new AutoLlmCleanup.Selection("campaign", campaignId, null, false, true));
        assertEquals(1, consumerPlan.dependencies().size()); assertTrue(consumerPlan.retain().containsKey(screening.toString()));
        assertEquals("SUCCEEDED", consumerCleanup.apply(consumerPlan.digest(), true).status()); assertTrue(Files.exists(execution));
        var finalPlan = ownerCleanup.plan(new AutoLlmCleanup.Selection("workspace", null, null, false, true));
        assertTrue(finalPlan.blockers().isEmpty(), finalPlan.blockers().toString());
        var receipt = ownerCleanup.apply(finalPlan.digest(), true);
        assertEquals("SUCCEEDED", receipt.status(), receipt.toString()); assertFalse(Files.exists(b));
    }
    @Test void cleanupDeletesCompleteOwnedDirectoryAndNativeTranscriptWithoutExport() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var store = new AutoLlmCampaignStore(AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class));
        String id = UUID.randomUUID().toString(); Path campaign = store.directory(id);
        new OwnershipManifest(workspace).directory(workspace, "workspace", workspace.toString());
        new OwnershipManifest(workspace).directory(campaign, "campaign", campaign.toString());
        Files.writeString(campaign.resolve("generated.txt"), "generated after inventory");
        Path dataset = temporary.resolve("dataset"); Files.writeString(dataset, "original");
        Files.createSymbolicLink(campaign.resolve("dataset-link"), dataset);
        Path transcript = temporary.toRealPath().resolve("session.jsonl"); String session = UUID.randomUUID().toString();
        Files.writeString(transcript, AutoLlmFileUtil.JSON.writeValueAsString(Map.of("type", "session_meta", "payload", Map.of("id", session, "cwd", campaign.toString()))) + "\n");
        new OwnershipManifest(workspace).transcript(transcript, session, campaign, campaign.toString());
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, false));
        Files.writeString(campaign.resolve("later-file"), "also owned");
        var result = cleanup.apply(plan.digest(), true);
        assertEquals("SUCCEEDED", result.status(), result.toString()); assertFalse(Files.exists(campaign)); assertFalse(Files.exists(transcript)); assertEquals("original", Files.readString(dataset));
        assertEquals("UNKNOWN", cleanup.status(plan.digest()).status());
    }
    @Test void cleanupRetainsSharedDependenciesAndRejectsPathSubstitution() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var store = new AutoLlmCampaignStore(AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class));
        String id = UUID.randomUUID().toString(); Path campaign = store.directory(id); Path screening = workspace.resolve("screenings/" + UUID.randomUUID());
        new OwnershipManifest(workspace).directory(campaign, "campaign", campaign.toString());
        new OwnershipManifest(workspace).directory(screening, "screening", screening.toString());
        new OwnershipManifest(workspace).reference(screening, campaign.toString()); new OwnershipManifest(workspace).reference(screening, "/other-workspace/campaign");
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, true));
        assertTrue(plan.retain().containsKey(screening.toString()));
        Files.move(campaign, campaign.resolveSibling("original")); Files.createDirectories(campaign); Files.writeString(campaign.resolve("unrelated"), "do not delete");
        assertThrows(RuntimeException.class, () -> cleanup.requestCancellation(plan.digest()));
        assertFalse(Files.exists(campaign.resolve("cleanup-requested.json")));
        assertEquals("PENDING", cleanup.apply(plan.digest(), true).status());
        assertTrue(Files.exists(campaign.resolve("unrelated"))); assertTrue(Files.exists(screening));
    }
    @Test void interruptedDeletionResumesAndChangedReferencesRequireNewReview() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace"); var store = new AutoLlmCampaignStore(AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class));
        String id = UUID.randomUUID().toString(); Path campaign = store.directory(id);
        new OwnershipManifest(workspace).directory(campaign, "campaign", campaign.toString());
        store.reserve(id);
        AutoLlmFileUtil.write(workspace.resolve("campaign-session.json"), Map.of("campaignId", id));
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, true));
        assertEquals("PENDING", cleanup.apply(plan.digest(), false).status());
        assertEquals("SUCCEEDED", cleanup.apply(plan.digest(), true).status());
        assertFalse(Files.exists(workspace.resolve("operation-reservation.json")));
        assertFalse(Files.exists(workspace.resolve("campaign-session.json")));
        assertEquals("SUCCEEDED", cleanup.apply(plan.digest(), true).status());
        String next = UUID.randomUUID().toString(); Path other = store.directory(next);
        new OwnershipManifest(workspace).directory(other, "campaign", other.toString());
        var reviewed = cleanup.plan(new AutoLlmCleanup.Selection("campaign", next, null, false, true));
        new OwnershipManifest(workspace).reference(other, "/other-owner");
        assertEquals("PENDING", cleanup.apply(reviewed.digest(), true).status()); assertTrue(Files.exists(other));
    }
    @Test void controllerCleanupDoesNotCancelOrWaitForAnUnrelatedCampaign() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var store = new AutoLlmCampaignStore(AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class));
        String selected = UUID.randomUUID().toString(), active = UUID.randomUUID().toString();
        new OwnershipManifest(workspace).directory(store.directory(selected), "campaign", store.directory(selected).toString());
        new OwnershipManifest(workspace).directory(store.directory(active), "campaign", store.directory(active).toString());
        store.reserve(active);
        var service = org.mockito.Mockito.mock(AutoLlmCampaignService.class);
        org.mockito.Mockito.when(service.active()).thenReturn(active);
        var mode = org.mockito.Mockito.mock(es.urjc.etsii.grafo.monitoring.MonitorModeState.class);
        org.mockito.Mockito.when(mode.mode()).thenReturn(es.urjc.etsii.grafo.monitoring.MonitorModeState.Mode.AUTOLLM);
        var controller = new AutoLlmController(service, store, mode, new org.springframework.mock.env.MockEnvironment());
        var plan = (AutoLlmCleanup.Plan) controller.cleanupPlan(new AutoLlmCleanup.Selection("campaign", selected, null, false, true));
        var receipt = (AutoLlmCleanup.Receipt) controller.cleanupApply(Map.of("digest", plan.digest()));
        assertEquals("SUCCEEDED", receipt.status(), receipt.toString());
        assertFalse(Files.exists(store.directory(selected))); assertTrue(Files.exists(store.directory(active)));
        assertEquals(active, AutoLlmFileUtil.JSON.readTree(Files.readString(workspace.resolve("operation-reservation.json"))).path("id").asString());
        org.mockito.Mockito.verify(service, org.mockito.Mockito.never()).cancel(org.mockito.ArgumentMatchers.anyString());
    }
    @Test void workspaceCleanupIncludesStandaloneOperationsAndResumesLostDeletionReceipt() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var store = new AutoLlmCampaignStore(AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class));
        new OwnershipManifest(workspace).directory(workspace, "workspace", workspace.toString());
        Path screening = workspace.resolve("screenings/" + UUID.randomUUID()), probe = workspace.resolve("target-probes/" + UUID.randomUUID());
        var first = new OwnershipManifest(workspace).directory(screening, "screening", screening.toString());
        new OwnershipManifest(workspace).directory(probe, "probe", probe.toString());
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("workspace", null, null, false, true));
        assertTrue(plan.blockers().isEmpty(), plan.blockers().toString());
        new OwnershipManifest(workspace).delete(first, plan.references()); // Host committed deletion; controller receipt was lost.
        var receipt = cleanup.apply(plan.digest(), true);
        assertEquals("SUCCEEDED", receipt.status(), receipt.toString()); assertFalse(Files.exists(workspace));
        assertTrue(new OwnershipManifest(workspace).resources().isEmpty());
    }
    @Test void assignmentCreatedAfterReviewRequiresAnotherCleanupPlan() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var store = new AutoLlmCampaignStore(AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class));
        String id = UUID.randomUUID().toString(); Path campaign = store.directory(id);
        new OwnershipManifest(workspace).directory(campaign, "campaign", campaign.toString());
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, true));
        Path assignment = campaign.resolve("trials/001/assignments/new");
        new OwnershipManifest(workspace).directory(assignment, "execution", assignment.toString());
        assertEquals("PENDING", cleanup.apply(plan.digest(), true).status()); assertTrue(Files.exists(assignment));
        assertThrows(IllegalStateException.class, () -> cleanup.requestCancellation(plan.digest()));
    }
    @Test void terminationProofRequiresEvidenceMatchingTheRecordedPid() throws Exception {
        Path output = temporary.resolve("process");
        AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", Long.MAX_VALUE, "start", "gone"));
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(output));
        AutoLlmFileUtil.write(output.resolve("process-terminated.json"), Map.of("pid", Long.MAX_VALUE));
        assertDoesNotThrow(() -> AutoLlmProcessRunner.requireTerminated(output));
        AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", ProcessHandle.current().pid(), "start", "wrong"));
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(output));
    }
    @Test void busyHealthyWorkerAndStaleAvailabilityRemainSeparate() {
        var target = new AutoLlmCampaign.TargetState(); target.baselineReady = true;
        target.remote = Map.of("status", "WAITING_FOR_WORKSPACE", "lastSynchronizedAt", Instant.now().toString());
        assertEquals("HEALTHY", target.getHealth()); assertEquals("QUALIFIED", target.getQualification()); assertEquals("BUSY", target.getAvailability());
        target.remote = Map.of("status", "WAITING_FOR_WORKSPACE", "lastSynchronizedAt", Instant.now().minusSeconds(60).toString());
        assertEquals("UNKNOWN", target.getAvailability()); assertEquals("HEALTHY", target.getHealth());
    }
    @Test void progressIgnoresPollingAndDiagnosticsDoNotInventAttribution() throws Exception {
        Path snapshot = temporary.resolve("progress.json");
        var first = EvaluationProgress.observe(snapshot, AutoLlmFileUtil.JSON.readTree("{\"elapsedMillis\":1,\"evaluations\":{\"completed\":2}}"));
        var polled = EvaluationProgress.observe(snapshot, AutoLlmFileUtil.JSON.readTree("{\"elapsedMillis\":999,\"evaluations\":{\"completed\":2}}"));
        assertEquals(first, polled);
        var changed = EvaluationProgress.observe(snapshot, AutoLlmFileUtil.JSON.readTree("{\"evaluations\":{\"completed\":3}}")); assertEquals(2, changed.sequence());
        for (var fixture : Map.of("COMPILER", "[ERROR] /src/A.java:[8,4] cannot find symbol", "TEST", "baselineTest <<< FAILURE!", "DEPENDENCY", "Could not transfer artifact dependency", "UNKNOWN", "unrecognized crash").entrySet()) {
            var diagnostic = BuildDiagnostics.classify(fixture.getValue(), "/host/report"); assertEquals(fixture.getKey(), diagnostic.kind()); assertEquals("UNKNOWN", diagnostic.attribution());
        }
        assertTrue(BuildDiagnostics.classify("x".repeat(10000), "log").excerpt().getBytes().length <= 4096);
    }
}
