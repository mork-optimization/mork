package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class CampaignManagementV7Test {
    @TempDir Path temporary;
    private AutoLlmConfig config(String json) { return AutoLlmFileUtil.JSON.readValue(json, AutoLlmConfig.class); }
    private WorkerRegistry.Lease intent(String operation) { return new WorkerRegistry.Lease(null, "controller", operation, "assignment", UUID.randomUUID().toString(), 0); }
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
    @Test void hostWideLeaseSurvivesDisconnectAndRejectsStaleAndUnknownSubmissions() throws Exception {
        var first = WorkerRegistry.system(); var second = new WorkerRegistry(first.directory());
        var request = intent("workspace-a/assignment"); var lease = first.acquire(request);
        assertEquals(lease, second.acquire(request));
        assertThrows(WorkerRegistry.Busy.class, () -> second.acquire(intent("workspace-b/assignment")));
        first.submit(lease, "/owned/a", "job", previous -> { assertNull(previous); return null; });
        assertThrows(IllegalStateException.class, () -> second.release(lease));
        assertThrows(WorkerRegistry.Busy.class, () -> new WorkerRegistry(first.directory()).acquire(intent("after-crash")));
        first.complete(lease, "/owned/a", "job"); first.release(lease);
        assertThrows(IllegalStateException.class, () -> first.acquire(request));
        var next = second.acquire(intent("workspace-b/assignment")); assertEquals(lease.generation() + 1, next.generation());
        assertEquals(lease.workerId(), next.workerId());
        assertThrows(IllegalStateException.class, () -> first.submit(lease, "/owned/a", "stale", previous -> null));
    }
    @Test void cleanupOfAnUnacquiredReservationNeverReleasesAnotherController() throws Exception {
        var active = WorkerRegistry.system().acquire(intent("other-controller"));
        Path workspace = temporary.toRealPath().resolve("workspace");
        var configuration = AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class);
        var store = new AutoLlmCampaignStore(configuration); String id = UUID.randomUUID().toString(); Path campaign = store.directory(id);
        OwnershipManifest.directory(campaign, "campaign", campaign.toString());
        assertThrows(WorkerRegistry.Busy.class, () -> ExecutionOwner.reserve(configuration, campaign.resolve("assignment")));
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, true));
        assertEquals("SUCCEEDED", cleanup.apply(plan.digest(), true).status());
        assertEquals(active, WorkerRegistry.system().status().lease());
    }
    @Test void simultaneousRegistryClientsCannotOverlapAndMissingRegistryFails() throws Exception {
        assertThrows(IllegalStateException.class, () -> new WorkerRegistry(temporary.resolve("unprovisioned")).status());
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            var start = new CountDownLatch(1); var successes = new java.util.concurrent.atomic.AtomicInteger();
            var futures = new ArrayList<Future<?>>();
            for (int i = 0; i < 16; i++) {
                String op = "op-" + i;
                futures.add(pool.submit(() -> { start.await(); try { WorkerRegistry.system().acquire(intent(op)); successes.incrementAndGet(); } catch (WorkerRegistry.Busy expected) {} return null; }));
            }
            start.countDown(); for (var future : futures) future.get(5, TimeUnit.SECONDS);
            assertEquals(1, successes.get());
        }
    }
    @Test void registryChecksFilesystemLocksAcrossProcesses() { WorkerRegistry.system().verifyLocks(); }
    public static class LeaseProcess {
        public static void main(String[] args) {
            var registry = new WorkerRegistry(Path.of(args[0]));
            try {
                var lease = registry.acquire(new WorkerRegistry.Lease(null, args[1], args[1], null, UUID.randomUUID().toString(), 0));
                System.out.print(lease.workerId());
            } catch (WorkerRegistry.Busy busy) { System.out.print(busy.owner.workerId()); System.exit(3); }
        }
    }
    @Test void independentControllerProcessesAndAliasesUseOneReservation() throws Exception {
        Path a = temporary.resolve("a"), b = temporary.resolve("b"); Files.createDirectories(a); Files.createDirectories(b);
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin/java").toString();
        String cp = System.getProperty("java.class.path");
        var first = new ProcessBuilder(javaExecutable, "-cp", cp, LeaseProcess.class.getName(), WorkerRegistry.system().directory().toString(), "alias-a").directory(a.toFile()).redirectErrorStream(true).start();
        assertTrue(first.waitFor(10, TimeUnit.SECONDS)); String worker = new String(first.getInputStream().readAllBytes()); assertEquals(0, first.exitValue(), worker);
        var second = new ProcessBuilder(javaExecutable, "-cp", cp, LeaseProcess.class.getName(), WorkerRegistry.system().directory().toString(), "alias-b").directory(b.toFile()).redirectErrorStream(true).start();
        assertTrue(second.waitFor(10, TimeUnit.SECONDS)); assertEquals(worker, new String(second.getInputStream().readAllBytes())); assertEquals(3, second.exitValue());
        assertNotNull(WorkerRegistry.system().status().lease(), "Controller exit must not release worker ownership");
    }
    @Test void cleanupDeletesCompleteOwnedDirectoryAndNativeTranscriptWithoutExport() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var store = new AutoLlmCampaignStore(AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class));
        String id = UUID.randomUUID().toString(); Path campaign = store.directory(id);
        OwnershipManifest.directory(workspace, "workspace", workspace.toString());
        OwnershipManifest.directory(campaign, "campaign", campaign.toString());
        Files.writeString(campaign.resolve("generated.txt"), "generated after inventory");
        Path dataset = temporary.resolve("dataset"); Files.writeString(dataset, "original");
        Files.createSymbolicLink(campaign.resolve("dataset-link"), dataset);
        Path transcript = temporary.toRealPath().resolve("session.jsonl"); String session = UUID.randomUUID().toString();
        Files.writeString(transcript, AutoLlmFileUtil.JSON.writeValueAsString(Map.of("type", "session_meta", "payload", Map.of("id", session, "cwd", campaign.toString()))) + "\n");
        OwnershipManifest.transcript(transcript, session, campaign, campaign.toString());
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
        OwnershipManifest.directory(campaign, "campaign", campaign.toString());
        OwnershipManifest.directory(screening, "screening", screening.toString());
        OwnershipManifest.reference(screening, campaign.toString()); OwnershipManifest.reference(screening, "/other-workspace/campaign");
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
        OwnershipManifest.directory(campaign, "campaign", campaign.toString());
        store.reserve(id);
        AutoLlmFileUtil.write(workspace.resolve("campaign-session.json"), Map.of("campaignId", id));
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, true));
        assertEquals("PENDING", cleanup.apply(plan.digest(), false).status());
        assertEquals("SUCCEEDED", cleanup.apply(plan.digest(), true).status());
        assertFalse(Files.exists(workspace.resolve("operation-reservation.json")));
        assertFalse(Files.exists(workspace.resolve("campaign-session.json")));
        assertEquals("SUCCEEDED", cleanup.apply(plan.digest(), true).status());
        String next = UUID.randomUUID().toString(); Path other = store.directory(next);
        OwnershipManifest.directory(other, "campaign", other.toString());
        var reviewed = cleanup.plan(new AutoLlmCleanup.Selection("campaign", next, null, false, true));
        OwnershipManifest.reference(other, "/other-owner");
        assertEquals("PENDING", cleanup.apply(reviewed.digest(), true).status()); assertTrue(Files.exists(other));
    }
    @Test void controllerCleanupDoesNotCancelOrWaitForAnUnrelatedCampaign() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var store = new AutoLlmCampaignStore(AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class));
        String selected = UUID.randomUUID().toString(), active = UUID.randomUUID().toString();
        OwnershipManifest.directory(store.directory(selected), "campaign", store.directory(selected).toString());
        OwnershipManifest.directory(store.directory(active), "campaign", store.directory(active).toString());
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
        OwnershipManifest.directory(workspace, "workspace", workspace.toString());
        Path screening = workspace.resolve("screenings/" + UUID.randomUUID()), probe = workspace.resolve("target-probes/" + UUID.randomUUID());
        var first = OwnershipManifest.directory(screening, "screening", screening.toString());
        OwnershipManifest.directory(probe, "probe", probe.toString());
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("workspace", null, null, false, true));
        assertTrue(plan.blockers().isEmpty(), plan.blockers().toString());
        OwnershipManifest.delete(first, plan.references()); // Host committed deletion; controller receipt was lost.
        assertEquals("SUCCEEDED", cleanup.apply(plan.digest(), true).status()); assertFalse(Files.exists(workspace));
        assertTrue(OwnershipManifest.resources().isEmpty());
    }
    @Test void assignmentCreatedAfterReviewRequiresAnotherCleanupPlan() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var store = new AutoLlmCampaignStore(AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class));
        String id = UUID.randomUUID().toString(); Path campaign = store.directory(id);
        OwnershipManifest.directory(campaign, "campaign", campaign.toString());
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, true));
        Path assignment = campaign.resolve("trials/001/assignments/new");
        OwnershipManifest.directory(assignment, "execution", assignment.toString());
        assertEquals("PENDING", cleanup.apply(plan.digest(), true).status()); assertTrue(Files.exists(assignment));
        assertThrows(IllegalStateException.class, () -> cleanup.requestCancellation(plan.digest()));
    }
    @Test void terminationProofRejectsUnknownChildrenAndReusedPids() throws Exception {
        Path output = temporary.resolve("process");
        AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", Long.MAX_VALUE, "start", "gone"));
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(output));
        AutoLlmFileUtil.write(output.resolve("process-terminated.json"), Map.of("pid", Long.MAX_VALUE));
        assertDoesNotThrow(() -> AutoLlmProcessRunner.requireTerminated(output));
        AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", ProcessHandle.current().pid(), "start", "wrong"));
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(output));
    }
    public static class CleanupControllerProcess {
        public static void main(String[] args) throws Exception {
            System.setProperty("mork.test.workerRegistry", args[1]);
            Path workspace = Path.of(args[0]);
            var config = AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class);
            OwnershipManifest.directory(workspace, "workspace", workspace.toString());
            try (var channel = java.nio.channels.FileChannel.open(workspace.resolve("execution.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 var lock = channel.lock()) {
                var plan = new AutoLlmCleanup(new AutoLlmCampaignStore(config)).plan(new AutoLlmCleanup.Selection("workspace", null, null, true, true));
                AutoLlmFileUtil.write(Path.of(args[2]), plan);
                Thread.sleep(60_000);
            }
        }
    }
    @Test void externalFinalizerStopsExactControllerAndDeletesAfterItsFileLocksClose() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace"), pointer = temporary.resolve("reviewed.json");
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin/java").toString();
        String cp = System.getProperty("java.class.path"); Path registry = WorkerRegistry.system().directory();
        var controller = new ProcessBuilder(javaExecutable, "-cp", cp, CleanupControllerProcess.class.getName(), workspace.toString(), registry.toString(), pointer.toString())
                .redirectErrorStream(true).redirectOutput(temporary.resolve("controller.log").toFile()).start();
        Process helper = null;
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
            while (!Files.isRegularFile(pointer) && controller.isAlive() && System.nanoTime() < deadline) Thread.sleep(20);
            assertTrue(Files.isRegularFile(pointer), Files.readString(temporary.resolve("controller.log")));
            var plan = AutoLlmFileUtil.read(pointer, AutoLlmCleanup.Plan.class); assertTrue(plan.blockers().isEmpty());
            Path receipts = workspace.getParent().resolve(".mork-cleanup/" + AutoLlmFileUtil.canonicalHash(workspace.toString()));
            Path planFile = receipts.resolve(plan.digest() + ".plan.json"), runner = registry.resolve("tools/finalizer-test.jar"); Files.createDirectories(runner.getParent());
            var manifest = new java.util.jar.Manifest(); manifest.getMainAttributes().put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(java.util.jar.Attributes.Name.MAIN_CLASS, RemoteWorkloadMain.class.getName());
            var entries = new ArrayList<String>(); for (String entry : cp.split(System.getProperty("path.separator"))) entries.add(Path.of(entry).toAbsolutePath().toUri().toString());
            manifest.getMainAttributes().put(java.util.jar.Attributes.Name.CLASS_PATH, String.join(" ", entries));
            try (var jar = new java.util.jar.JarOutputStream(Files.newOutputStream(runner), manifest)) { }
            AutoLlmFileUtil.write(receipts.resolve(plan.digest() + ".finalizer.json"), new AutoLlmCleanup.Finalizer(runner.toString(), AutoLlmFileUtil.hash(runner), null));
            helper = new ProcessBuilder(javaExecutable, "-Dmork.test.workerRegistry=" + registry, "-jar", runner.toString(), "cleanup-finish", workspace.toString(), planFile.toString())
                    .redirectErrorStream(true).redirectOutput(temporary.resolve("helper.log").toFile()).start();
            assertTrue(helper.waitFor(30, TimeUnit.SECONDS)); assertEquals(0, helper.exitValue(), Files.readString(temporary.resolve("helper.log")));
            assertTrue(controller.waitFor(5, TimeUnit.SECONDS)); assertFalse(Files.exists(workspace));
            assertEquals("SUCCEEDED", AutoLlmFileUtil.read(receipts.resolve(plan.digest() + ".receipt.json"), AutoLlmCleanup.Receipt.class).status());
            assertFalse(Files.exists(planFile));
        } finally { controller.destroyForcibly(); if (helper != null && helper.isAlive()) helper.destroyForcibly(); }
    }
    @Test void busyHealthyWorkerAndStaleAvailabilityRemainSeparate() {
        var target = new AutoLlmCampaign.TargetState(); target.baselineReady = true;
        target.remote = Map.of("availability", "BUSY", "lastSynchronizedAt", Instant.now().toString());
        assertEquals("HEALTHY", target.getHealth()); assertEquals("QUALIFIED", target.getQualification()); assertEquals("BUSY", target.getAvailability());
        target.remote = Map.of("availability", "BUSY", "lastSynchronizedAt", Instant.now().minusSeconds(60).toString());
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
