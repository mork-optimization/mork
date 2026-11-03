package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.*;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.jar.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Isolated loopback sshd: no user's SSH config, keys, known-hosts or remote server are touched. */
class SshWorkloadTest {
    @TempDir Path temp;
    private Process server;
    private AutoLlmConfig config;
    private Path root;
    private final String java = Path.of(System.getProperty("java.home"), "bin/java").toString();
    @BeforeEach void startSsh() throws Exception {
        Path sshd = Path.of(System.getenv().getOrDefault("MORK_TEST_SSHD", "/usr/sbin/sshd"));
        assumeTrue(Files.isExecutable(sshd), "Install sshd or set MORK_TEST_SSHD to run loopback SSH integration");
        Path hostKey = temp.resolve("host-key"), key = temp.resolve("client-key");
        run(List.of("ssh-keygen", "-q", "-t", "ed25519", "-N", "", "-f", hostKey.toString()));
        run(List.of("ssh-keygen", "-q", "-t", "ed25519", "-N", "", "-f", key.toString()));
        int port; try (var socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) { port = socket.getLocalPort(); }
        Path configuration = temp.resolve("sshd_config");
        Files.writeString(configuration, "Port " + port + "\nListenAddress 127.0.0.1\nHostKey " + hostKey + "\nPidFile " + temp.resolve("sshd.pid")
                + "\nAuthorizedKeysFile " + key + ".pub\nStrictModes no\nPasswordAuthentication no\nKbdInteractiveAuthentication no\nUsePAM no\nLogLevel ERROR\n");
        server = new ProcessBuilder(sshd.toString(), "-D", "-e", "-f", configuration.toString()).redirectErrorStream(true).redirectOutput(temp.resolve("sshd.log").toFile()).start();
        Thread.sleep(200);
        assumeTrue(server.isAlive(), () -> "sshd fixture unavailable: " + read(temp.resolve("sshd.log")));
        Path known = temp.resolve("known_hosts"); Files.writeString(known, "[127.0.0.1]:" + port + " " + Files.readString(Path.of(hostKey + ".pub")));
        Path ssh = temp.resolve("ssh_config");
        Files.writeString(ssh, "Host mork-fixture\n HostName 127.0.0.1\n Port " + port + "\n User " + System.getProperty("user.name")
                + "\n IdentityFile " + key + "\n IdentitiesOnly yes\n UserKnownHostsFile " + known + "\n StrictHostKeyChecking yes\n");
        Path runner = temp.resolve("runner.jar"); var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, RemoteWorkloadMain.class.getName());
        var classpath = new ArrayList<String>(); for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) classpath.add(Path.of(entry).toAbsolutePath().toUri().toString());
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, String.join(" ", classpath));
        try (var jar = new JarOutputStream(Files.newOutputStream(runner), manifest)) { /* Manifest delegates to this fixture's compiled classes. */ }
        Path workerJava = temp.resolve("worker-java");
        Files.writeString(workerJava, "#!/bin/sh\nexec " + WorkloadPathUtil.quote(java) + " \"$@\"\n");
        assertTrue(workerJava.toFile().setExecutable(true));
        var defaults = AutoLlmConfig.defaults();
        config = new AutoLlmConfig(defaults.project(), temp.resolve("controller").toString(), 1, 1234L, 299, null, null, null, null, defaults.codex(), defaults.preparation(), defaults.tuning(), defaults.comparison(),
                new AutoLlmConfig.Execution(32, 100L, 30000L, 30000L, 5000L, 5000L, 30000L, 30000L, "128m", AutoLlmConfig.Target.SSH,
                        new AutoLlmConfig.Ssh("fixture", runner.toString(), List.of(new AutoLlmConfig.Server("fixture", "mork-fixture", temp.resolve("remote workspace").toString(), workerJava.toString(), Map.of(), ssh.toString(), null, null)), new AutoLlmConfig.Reconnect(10L, 40L, 200L))));
        root = temp.resolve("local mirror").toAbsolutePath(); Files.createDirectories(root);
    }
    @AfterEach void stopSsh() { if (server != null) server.destroyForcibly(); }
    @Test void historicalAssignmentsKeepTheirOwnersAndCleanupDeletesBothAliases() throws Exception {
        var tree = AutoLlmFileUtil.JSON.valueToTree(config);
        ((tools.jackson.databind.node.ObjectNode) tree).put("workspace", temp.toRealPath().resolve("managed-workspace").toString());
        config = AutoLlmFileUtil.JSON.treeToValue(tree, AutoLlmConfig.class);
        var store = new AutoLlmCampaignStore(config); String campaignId = UUID.randomUUID().toString(); Path campaign = store.directory(campaignId);
        new OwnershipManifest(store.root()).directory(store.root(), "workspace", store.root().toString());
        new OwnershipManifest(store.root()).directory(campaign, "campaign", campaign.toString());
        var jobs = new ArrayList<WorkloadProtocol.Job>();
        Path ssh = Path.of(config.execution().ssh().onlyServer().configFile());
        Files.writeString(ssh, Files.readString(ssh).replace("Host mork-fixture", "Host mork-fixture alias-two"));
        for (String assignment : List.of("first", "second")) {
            root = campaign.resolve("trials/001/assignments/" + assignment); Files.createDirectories(root);
            AutoLlmFileUtil.write(root.resolve("execution-context.json"), Map.of("campaignId", campaignId, "targetId", "fixture", "trial", 1, "assignment", assignment));
            if (assignment.equals("second")) {
                var changed = AutoLlmFileUtil.JSON.valueToTree(config);
                ((tools.jackson.databind.node.ObjectNode) changed.path("execution").path("ssh").path("servers").get(0)).put("host", "alias-two");
                config = AutoLlmFileUtil.JSON.treeToValue(changed, AutoLlmConfig.class);
            }
            var work = workload("success", assignment);
            assertEquals(0, new SshWorkloadExecutor(config, root, new SshTransport(config, root.resolve(".transport")), state -> {}).run(work, () -> false).exitCode());
            var job = AutoLlmFileUtil.read(work.output().resolve("remote-job.json"), WorkloadProtocol.Job.class); jobs.add(job);

        }
        assertEquals(jobs.getFirst().owner().workspaceId(), jobs.getLast().owner().workspaceId());
        assertEquals("mork-fixture", jobs.getFirst().owner().config().execution().ssh().onlyServer().host());
        assertEquals("alias-two", jobs.getLast().owner().config().execution().ssh().onlyServer().host());
        for (var job : jobs) {
            var transport = ExecutionOwner.of(job).transport(); transport.bootstrap();
            assertTrue(AutoLlmFileUtil.JSON.readTree(transport.call("inspect", job.root(), job.id(), Map.of())).path("identityMatches").asBoolean());
        }
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", campaignId, null, false, true));
        assertTrue(plan.blockers().isEmpty(), plan.blockers().toString());
        var receipt = cleanup.apply(plan.digest(), true); assertEquals("SUCCEEDED", receipt.status(), receipt.toString());
        assertFalse(Files.exists(campaign));
        for (var job : jobs) assertFalse(Files.exists(Path.of(job.root())));
    }
    @Test void interruptedOwnerRegistrationRetainsItsEndpointAndReusesTheRemoteIdentity() throws Exception {
        var owner = ExecutionOwner.prepare(config, root);
        var resource = AutoLlmFileUtil.read(root.resolve("remote-resource.json"), OwnershipManifest.Resource.class);
        // Registration committed remotely, but the controller retained only its pre-request intent.
        AutoLlmFileUtil.write(root.resolve("execution-owner.json"), new ExecutionOwner(owner.version(), owner.operation(), owner.assignment(),
                owner.localRoot(), owner.executionRoot(), owner.config(), owner.controllerWorkspaceId(), null));
        Files.delete(root.resolve("remote-assets.json"));
        assertEquals(0, new SshWorkloadExecutor(config, root, new SshTransport(config, root.resolve(".transport")), state -> {}).run(workload("success", "reconnected registration"), () -> false).exitCode());
        assertEquals(owner, ExecutionOwner.read(root));
        assertEquals(resource, AutoLlmFileUtil.read(root.resolve("remote-resource.json"), OwnershipManifest.Resource.class));
    }
    @Test void lostAcknowledgementReplyRejoinsCollectedJobWithoutDownloadingOrExecutingAgain() throws Exception {
        AutoLlmFileUtil.write(root.resolve("control/storage-layout.json"), Map.of("version", 1));
        var transport = new SshTransport(config, root.resolve(".transport")) {
            @Override public byte[] call(String action, String remote, String id, Object input) {
                byte[] response = super.call(action, remote, id, input);
                if (action.equals("acknowledge")) throw new ConnectionException("lost acknowledgement reply");
                return response;
            }
        };
        var work = workload("success", "collected once");
        assertEquals(0, new SshWorkloadExecutor(config, root, transport, _ -> {}).run(work, () -> false).exitCode());
        var job = AutoLlmFileUtil.read(work.output().resolve("remote-job.json"), WorkloadProtocol.Job.class);
        Path remoteJob = Path.of(job.root()).resolve(".jobs/" + job.id());
        assertTrue(Files.exists(work.output().resolve("acknowledgement-pending.json")));
        assertFalse(Files.exists(remoteJob.resolve("result.zip")));
        var recovery = new SshTransport(config, root.resolve(".transport")) {
            @Override public void download(String remote, String id, Path target) { fail("Collected evidence must not be downloaded again"); }
            @Override public byte[] call(String action, String remote, String id, Object input) {
                assertFalse(Set.of("submit", "poll").contains(action), "Completed jobs must not be replayed");
                return super.call(action, remote, id, input);
            }
        };
        assertEquals(0, new SshWorkloadExecutor(config, root, recovery, _ -> {}).run(work, () -> false).exitCode());
        assertTrue(Files.exists(work.output().resolve("remote-acknowledged.json")));
        RemoteStorageMaintenance.maintain(root, root, () -> false);
        assertEquals("RELEASED", AutoLlmFileUtil.read(root.resolve("storage/remote-retention.json"), Map.class).get("status"));
        assertTrue(Files.exists(remoteJob.resolve("job.json")));
        assertFalse(Files.exists(Path.of(job.root()).resolve("output with spaces/literal.txt")));
        assertEquals("collected once", Files.readString(work.output().resolve("literal.txt")));
    }
    @Test void missingLocalEvidencePreventsRetryAcknowledgementAndPreservesRemoteArchive() throws Exception {
        AutoLlmFileUtil.write(root.resolve("control/storage-layout.json"), Map.of("version", 1));
        var transport = new SshTransport(config, root.resolve(".transport")) {
            @Override public byte[] call(String action, String remote, String id, Object input) {
                if (action.equals("acknowledge")) throw new ConnectionException("offline before acknowledgement");
                return super.call(action, remote, id, input);
            }
        };
        var work = workload("success", "preserved remotely");
        assertEquals(0, new SshWorkloadExecutor(config, root, transport, _ -> {}).run(work, () -> false).exitCode());
        var job = AutoLlmFileUtil.read(work.output().resolve("remote-job.json"), WorkloadProtocol.Job.class);
        Files.delete(work.output().resolve("literal.txt"));
        assertThrows(UncheckedIOException.class, () -> new SshWorkloadExecutor(config, root, new SshTransport(config, root.resolve(".transport")), _ -> {}).run(work, () -> false));
        assertTrue(Files.exists(Path.of(job.root()).resolve(".jobs/" + job.id() + "/result.zip")));
        Files.writeString(work.output().resolve("literal.txt"), "preserved remotely");
        assertEquals(0, new SshWorkloadExecutor(config, root, new SshTransport(config, root.resolve(".transport")), _ -> {}).run(work, () -> false).exitCode());
    }
    @Test void inspectionDoesNotReconcileStaleStateOrCreateAMissingJob() throws Exception {
        var transport = new SshTransport(config, temp.resolve("transport"));
        var work = workload("success", "inspection");
        assertEquals(0, new SshWorkloadExecutor(config, root, transport, state -> {}).run(work, () -> false).exitCode());
        var job = AutoLlmFileUtil.read(work.output().resolve("remote-job.json"), WorkloadProtocol.Job.class);
        Path directory = Path.of(job.root()).resolve(".jobs/" + job.id());
        AutoLlmFileUtil.write(directory.resolve("state.json"), new WorkloadProtocol.State(job.id(), WorkloadProtocol.JobStatus.RUNNING, null, null, null));
        AutoLlmFileUtil.write(directory.resolve("started.json"), Map.of("pid", ProcessHandle.current().pid(), "start", "not-this-process"));
        String saved = Files.readString(directory.resolve("state.json"));
        var inspected = AutoLlmFileUtil.JSON.readTree(transport.call("inspect", job.root(), job.id(), Map.of()));
        assertFalse(inspected.path("supervisorAlive").asBoolean());
        assertEquals(saved, Files.readString(directory.resolve("state.json")));
        String missing = "0".repeat(64);
        assertFalse(AutoLlmFileUtil.JSON.readTree(transport.call("inspect", job.root(), missing, Map.of())).path("found").asBoolean());
        assertFalse(Files.exists(Path.of(job.root()).resolve(".jobs/" + missing)));
    }
    @Test void interruptedScreeningCancellationUsesItsPersistedSingleServer() throws Exception {
        var execution = config.execution(); var ssh = execution.ssh();
        var pool = new AutoLlmConfig(config.project(), temp.resolve("campaigns").toString(), 1, config.seed(), config.maxParameters(), null, null, null, null,
                config.codex(), config.preparation(), config.tuning(), config.comparison(),
                new AutoLlmConfig.Execution(execution.workers(), execution.watchdogGraceMillis(), execution.codexTimeoutMillis(), execution.buildTimeoutMillis(), execution.startupTimeoutMillis(), execution.searchSpaceTimeoutMillis(), execution.phaseTimeoutMillis(), execution.stageTimeoutMillis(), execution.heap(), AutoLlmConfig.Target.SSH,
                        new AutoLlmConfig.Ssh(ssh.screeningServerId(), ssh.runnerArtifact(), List.of(ssh.onlyServer(), new AutoLlmConfig.Server("other", "unreachable-fixture", "/unused", "java", Map.of(), null, null, null)), null)));
        var store = new AutoLlmCampaignStore(pool);
        var job = new es.urjc.etsii.grafo.autoconfig.screening.ScreeningJobService.Job(); job.id = UUID.randomUUID().toString(); job.status = es.urjc.etsii.grafo.autoconfig.screening.ScreeningJobService.ScreeningStatus.INTERRUPTED;
        job.request = new es.urjc.etsii.grafo.autoconfig.screening.ScreeningJobService.Request(pool, null, null, null);
        job.executionConfig = pool.forServer(ssh.onlyServer());
        root = store.root().resolve("screenings").resolve(job.id); Files.createDirectories(root);
        var work = workload("success", "screening cancellation");
        assertEquals(0, new SshWorkloadExecutor(job.executionConfig, root, new SshTransport(job.executionConfig, temp.resolve("transport")), state -> {}).run(work, () -> false).exitCode());
        Files.delete(work.output().resolve("collection-completed.json"));
        AutoLlmFileUtil.write(root.resolve("job.json"), job);
        store.reserve(job.id);
        var service = new es.urjc.etsii.grafo.autoconfig.screening.ScreeningJobService(store, new AutoLlmProcessRunner());
        try {
            assertEquals(1, service.load(job.id).executionConfig.execution().ssh().servers().size());
            service.cancel(job.id);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
            while (service.active() != null && System.nanoTime() < deadline) Thread.sleep(20);
            assertNull(service.active()); assertEquals("CANCELLED", service.load(job.id).status.name());
            assertTrue(Files.exists(work.output().resolve("collection-completed.json")));
            assertDoesNotThrow(() -> store.requireReservation(UUID.randomUUID().toString()));
        } finally { service.close(); }
    }
    @Test void supervisorRetriesCollectionWithoutLaunchingCompletedWorkAgain() throws Exception {
        var transport = new SshTransport(config, temp.resolve("transport"));
        var work = workload("success", "completed once");
        assertEquals(0, new SshWorkloadExecutor(config, root, transport, state -> {}).run(work, () -> false).exitCode());
        var job = AutoLlmFileUtil.read(work.output().resolve("remote-job.json"), WorkloadProtocol.Job.class);
        Path directory = Path.of(job.root(), ".jobs", job.id());
        var completed = AutoLlmFileUtil.read(directory.resolve("execution-completed.json"), WorkloadProtocol.State.class);
        AutoLlmFileUtil.write(directory.resolve("started.json"), Map.of("pid", Long.MAX_VALUE, "start", "gone"));
        AutoLlmFileUtil.write(directory.resolve("state.json"), new WorkloadProtocol.State(job.id(), WorkloadProtocol.JobStatus.COLLECTING, completed.result(), null, null));
        Files.delete(directory.resolve("result.zip")); Files.createDirectory(directory.resolve("result.zip"));
        assertThrows(RuntimeException.class, () -> transport.call("status", job.root(), job.id(), Map.of()));
        Files.delete(directory.resolve("result.zip"));
        var collected = AutoLlmFileUtil.JSON.readValue(transport.call("status", job.root(), job.id(), Map.of()), WorkloadProtocol.State.class);
        assertEquals("SUCCEEDED", collected.status().name()); assertNotNull(collected.archiveSha256());
        assertEquals(Long.MAX_VALUE, AutoLlmFileUtil.JSON.readTree(Files.readString(directory.resolve("started.json"))).path("pid").asLong());
        assertEquals(completed.result(), collected.result());
    }
    @Test void collectsEarlySpringFailureWithItsOriginalClassification() throws Exception {
        Path baseline = root.resolve("baseline.json");
        AutoLlmFileUtil.write(baseline, List.of());
        Path output = root.resolve("startup failure");
        String remote = WorkloadPathUtil.remoteRoot(config, root);
        var command = List.of(java, "-Dmork.execution.directory=" + remote + "/startup failure/execution",
                "-cp", System.getProperty("java.class.path"), "mork.startupfixture.StartupFailureFixture",
                "--advanced.scan-pkgs=mork.startupfixture", "--spring.main.web-application-type=none",
                "--autollm.child.baseline-components-file=" + remote + "/baseline.json");
        var work = new AutoLlmWorkloadExecutor.Workload(command, root, output, Map.of("baseline.json", baseline), List.of(), List.of(), 30000, 0, 100, true, 15000);
        var result = new SshWorkloadExecutor(config, root, new SshTransport(config, temp.resolve("transport")), state -> {}).run(work, () -> false);
        assertNotEquals(0, result.exitCode());
        assertTrue(Files.exists(output.resolve("collection-completed.json")));
        var failure = AutoLlmFileUtil.read(output.resolve("execution/failure.json"), AutoLlmFailureUtil.Failure.class);
        assertEquals(AutoLlmFailureUtil.Category.COMPONENT, failure.category());
        assertEquals("INVALID_COMPONENT_DEFINITION", failure.code());
        assertFalse(Files.exists(output.resolve("execution/readiness.json")));
        assertEquals("FAILED", AutoLlmFileUtil.JSON.readTree(Files.readString(output.resolve("execution/terminal.json"))).path("outcome").asString());
    }
    @ParameterizedTest
    @ValueSource(strings = {"simulated connection loss after submit", "agent refused operation\nPermission denied (publickey).", "agent communication failed\nPermission denied (publickey)."})
    void reconnectsWithoutDuplicateSubmissionAndPreservesLiteralArguments(String diagnostic) throws Exception {
        var submissions = new AtomicInteger(); var disconnected = new AtomicBoolean();
        SshTransport transport = new SshTransport(config, temp.resolve("transport")) {
            @Override public byte[] call(String action, String remote, String id, Object input) {
                if (action.equals("submit")) submissions.incrementAndGet();
                if (action.equals("poll") && disconnected.compareAndSet(false, true)) throw connectionFailure(diagnostic);
                return super.call(action, remote, id, input);
            }
        };
        String literal = "spaces ' quotes ; $(touch NEVER) `uname` $HOME";
        var statuses = new ArrayList<String>();
        var result = new SshWorkloadExecutor(config, root, transport, state -> statuses.add((String) state.get("status"))).run(workload("success", literal), () -> false);
        assertEquals(0, result.exitCode()); assertEquals(1, submissions.get()); assertTrue(statuses.contains("WAITING_FOR_REMOTE"));
        assertEquals(literal, Files.readString(root.resolve("output with spaces/literal.txt"))); assertFalse(Files.exists(root.resolve("NEVER")));
        assertEquals("SSH", AutoLlmFileUtil.JSON.readTree(Files.readString(root.resolve("output with spaces/owner.json"))).path("executionTarget").asString());
        // A second controller invocation reuses the same completed job and byte-identical evidence.
        assertEquals(0, new SshWorkloadExecutor(config, root, transport, state -> {}).run(workload("success", literal), () -> false).exitCode());
        assertEquals(2, submissions.get()); // the second controller validates the same payload, without relaunching it
    }
    @Test void transfersSolutionsAsOneArchiveAndReusesItOnRecollection() throws Exception {
        var work = workload("solutions", "archived solutions");
        var executor = new SshWorkloadExecutor(config, root, new SshTransport(config, temp.resolve("transport")), state -> {});
        assertEquals(0, executor.run(work, () -> false).exitCode());
        Path archive = work.output().resolve("solutions.zip");
        assertEquals(2, WorkloadArchiveUtil.verify(archive, work.output().resolve("solutions")).size());
        assertFalse(Files.exists(work.output().resolve("solutions")));
        Path remote = Path.of(WorkloadPathUtil.remoteRoot(config, root), "output with spaces");
        assertTrue(Files.exists(remote.resolve("solutions.zip")));
        assertFalse(Files.exists(remote.resolve("solutions")));
        assertTrue(Files.exists(work.output().resolve("execution/terminal.json")));
        String checksum = AutoLlmFileUtil.hash(archive);
        assertEquals(0, executor.run(work, () -> false).exitCode());
        assertEquals(checksum, AutoLlmFileUtil.hash(archive));
    }
    @Test void referencesOriginalInputsLocallyAndTransfersArchiveContainersWithoutExtraction() throws Exception {
        Path original = temp.resolve("original dataset.zip"), loose = temp.resolve("loose input.txt");
        try (var zip = new ZipOutputStream(Files.newOutputStream(original))) {
            zip.putNextEntry(new ZipEntry("nested/input.txt")); zip.write("archive input".getBytes()); zip.closeEntry();
        }
        Files.writeString(loose, "loose input");
        var tokens = List.of(original + "!nested/input.txt", loose.toString());
        String index = AutoLlmInputUtil.snapshot(root, "training", tokens);
        var service = new AutoLlmExecutionService(new AutoLlmProcessRunner());
        config = service.stageInputs(config.withPaths(index, index), root);
        Path output = root.resolve("input operation");
        AutoLlmFileUtil.write(root.resolve("control/comparison-time-limits.json"), Map.of(tokens.getFirst(), 123L, tokens.getLast(), 456L));
        AutoLlmFileUtil.write(root.resolve("control/config.json"), config);
        var command = List.of(java, "-cp", System.getProperty("java.class.path"), RemoteWorkloadFixture.class.getName(), "inputs", output.toString(), config.trainingPath(), tokens.getFirst(), root.resolve("control/config.json").toString());
        assertEquals(0, service.run(config, root, command, root, output, null, List.of(), 30000, 0, true, 5000, () -> false, state -> {}).exitCode());
        var check = AutoLlmExecutionService.check(config, root, output.resolve("check.json"));
        assertEquals(tokens, check.training());
        String chosen = AutoLlmFileUtil.JSON.readTree(Files.readString(output.resolve("reference.json"))).path("instance").asString();
        assertEquals(tokens.getFirst(), AutoLlmExecutionService.localPaths(config, root).apply(chosen));
        Map<String,String> hashes = AutoLlmFileUtil.read(output.resolve("input-hashes.json"), new tools.jackson.core.type.TypeReference<>() {});
        Path remote = Path.of(WorkloadPathUtil.remoteRoot(config, root));
        assertEquals(2, hashes.size());
        var localPaths = AutoLlmExecutionService.localPaths(config, root);
        for (var entry : hashes.entrySet()) {
            assertTrue(entry.getKey().startsWith(remote + "/data/inputs/"));
            String local = localPaths.apply(entry.getKey());
            assertEquals(AutoLlmFileUtil.inputHash(local), entry.getValue());
        }
        assertTrue(hashes.keySet().iterator().next().contains("original dataset.zip!nested/input.txt"));
        assertFalse(Files.exists(root.resolve("data/inputs")));
        try (var files = Files.walk(remote.resolve("data/inputs"))) { assertEquals(2, files.filter(Files::isRegularFile).count()); }
        Map<String,Long> limits = AutoLlmFileUtil.read(remote.resolve("control/comparison-time-limits.json"), new tools.jackson.core.type.TypeReference<>() {});
        assertEquals(123L, limits.get(hashes.keySet().iterator().next()));
        assertEquals(0, service.run(config, root, command, root, output, null, List.of(), 30000, 0, true, 5000, () -> false, state -> {}).exitCode());
        Files.writeString(loose, "changed");
        assertThrows(IllegalStateException.class, () -> service.run(config, root, command, root, root.resolve("changed input operation"), null, List.of(), 30000, 0, true, 5000, () -> false, state -> {}));
    }
    @Test void serverWatchdogStopsAHungEvaluation() {
        var executor = new SshWorkloadExecutor(config, root, new SshTransport(config, temp.resolve("transport")), state -> {});
        var failure = assertThrows(IllegalStateException.class, () -> executor.run(workload("hang", "hang"), () -> false));
        assertTrue(failure.getMessage().contains("Hung evaluation"), failure.getMessage());
        var owner = AutoLlmFileUtil.read(root.resolve("output with spaces/owner.json"), Map.class);
        assertFalse(ProcessHandle.of(((Number) owner.get("pid")).longValue()).map(ProcessHandle::isAlive).orElse(false));
    }
    @Test void cancellationIsConfirmedByTheRemoteSupervisor() throws Exception {
        var cancelled = new AtomicBoolean();
        var executor = new SshWorkloadExecutor(config, root, new SshTransport(config, temp.resolve("transport")), state -> {});
        try (var threads = Executors.newSingleThreadExecutor()) {
            var future = threads.submit(() -> executor.run(workload("wait", "wait"), cancelled::get));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
            while (!Files.exists(root.resolve("output with spaces/execution/readiness.json")) && System.nanoTime() < deadline) Thread.sleep(100);
            cancelled.set(true);
            var error = assertThrows(ExecutionException.class, () -> future.get(20, TimeUnit.SECONDS));
            assertInstanceOf(CancellationException.class, error.getCause(), error.getCause().toString());
            assertEquals("CANCELLED", AutoLlmFileUtil.JSON.readTree(Files.readString(root.resolve("output with spaces/remote-status.json"))).path("status").asString());
        }
    }
    @ParameterizedTest @ValueSource(booleans = {false, true})
    void separateControllersContendOnlyWhenTheyUseTheSameWorkspace(boolean separateWorkspace) throws Exception {
        var firstConfig = config; Path firstRoot = root; var firstWork = workload("hold", "first controller");
        var firstExecutor = new SshWorkloadExecutor(firstConfig, firstRoot, new SshTransport(firstConfig, firstRoot.resolve(".transport")), state -> {});
        var cancelFirst = new AtomicBoolean(); var cancelSecond = new AtomicBoolean();
        try (var threads = Executors.newFixedThreadPool(2)) {
            var first = threads.submit(() -> firstExecutor.run(firstWork, cancelFirst::get));
            try {
                awaitReady();
                root = temp.resolve("second controller"); Files.createDirectories(root);
                var changed = AutoLlmFileUtil.JSON.valueToTree(config);
                ((tools.jackson.databind.node.ObjectNode) changed).put("workspace", root.toString());
                if (separateWorkspace) {
                    ((tools.jackson.databind.node.ObjectNode) changed.path("execution").path("ssh").path("servers").get(0))
                            .put("workspace", temp.resolve("independent worker workspace").toString());
                }
                config = AutoLlmFileUtil.JSON.treeToValue(changed, AutoLlmConfig.class);
                var secondWork = workload("success", "second controller"); var waiting = new CountDownLatch(1);
                var secondExecutor = new SshWorkloadExecutor(config, root, new SshTransport(config, root.resolve(".transport")),
                        state -> { if ("WAITING_FOR_WORKSPACE".equals(state.get("status"))) waiting.countDown(); });
                var second = threads.submit(() -> secondExecutor.run(secondWork, cancelSecond::get));
                if (separateWorkspace) {
                    // Allow remote setup and evidence collection in addition to the fixture's runtime.
                    assertEquals(0, second.get(60, TimeUnit.SECONDS).exitCode()); assertFalse(first.isDone());
                } else {
                    assertTrue(waiting.await(15, TimeUnit.SECONDS)); assertFalse(second.isDone());
                    assertFalse(Files.exists(secondWork.output().resolve("literal.txt")));
                    cancelSecond.set(true);
                    assertInstanceOf(CancellationException.class, assertThrows(ExecutionException.class, () -> second.get(15, TimeUnit.SECONDS)).getCause());
                    assertFalse(first.isDone());
                }
            } finally { cancelFirst.set(true); cancelSecond.set(true); }
            assertInstanceOf(CancellationException.class, assertThrows(ExecutionException.class, () -> first.get(15, TimeUnit.SECONDS)).getCause());
        }
    }
    @Test void aNewControllerReattachesToTheLiveJobRatherThanLaunchingAnother() throws Exception {
        var transport = new SshTransport(config, temp.resolve("transport"));
        var executor = new SshWorkloadExecutor(config, root, transport, state -> {});
        var pool = Executors.newSingleThreadExecutor();
        var future = pool.submit(() -> executor.run(workload("wait", "wait"), () -> false));
        awaitReady(); future.cancel(true); pool.shutdown(); assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        Path remote = Path.of(WorkloadPathUtil.remoteRoot(config, root));
        var owner = AutoLlmFileUtil.read(remote.resolve("output with spaces/owner.json"), Map.class);
        long pid = ((Number) owner.get("pid")).longValue(); assertTrue(ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false));
        String id = AutoLlmFileUtil.JSON.readTree(Files.readString(root.resolve("output with spaces/remote-job.json"))).path("id").asString();
        var replacement = new SshWorkloadExecutor(config, root, new SshTransport(config, temp.resolve("transport")), state -> {});
        assertThrows(CancellationException.class, () -> replacement.run(workload("wait", "wait"), () -> true));
        assertEquals(id, AutoLlmFileUtil.JSON.readTree(Files.readString(root.resolve("output with spaces/remote-job.json"))).path("id").asString());
        assertFalse(ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false));
    }
    @Test void queuedPreparationCanBeCollectedWithoutCancellingTheWorkspaceOwner() throws Exception {
        var firstRoot = root; var firstConfig = config; var firstWork = workload("hold", "tuning owner");
        var cancelFirst = new AtomicBoolean(); var detached = new CountDownLatch(1);
        try (var threads = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = threads.submit(() -> new SshWorkloadExecutor(firstConfig, firstRoot, new SshTransport(firstConfig, firstRoot.resolve(".transport")), _ -> {}).run(firstWork, cancelFirst::get));
            try {
                awaitReady();
                root = temp.resolve("queued preparation"); Files.createDirectories(root);
                var original = workload("success", "preparation");
                // Cancellation can precede installation of the build's source tree.
                var secondWork = new AutoLlmWorkloadExecutor.Workload(original.command(), root.resolve("source"), original.output(), original.inputs(),
                        original.collect(), original.reset(), original.timeoutMillis(), original.plannedMillis(), original.graceMillis(), original.watchEvaluations(), original.startupMillis());
                var waiting = new CountDownLatch(1);
                var second = threads.submit(() -> {
                    try { return new SshWorkloadExecutor(config, root, new SshTransport(config, root.resolve(".transport")), state -> {
                        if ("WAITING_FOR_WORKSPACE".equals(state.get("status"))) waiting.countDown();
                    }).run(secondWork, () -> false); }
                    finally { detached.countDown(); }
                });
                assertTrue(waiting.await(30, TimeUnit.SECONDS)); second.cancel(true); assertTrue(detached.await(10, TimeUnit.SECONDS));
                String descriptor = Files.readString(secondWork.output().resolve("remote-job.json"));
                new AutoLlmExecutionService(new AutoLlmProcessRunner()).reconcilePreparation(root, () -> false, _ -> {});
                assertFalse(first.isDone());
                assertTrue(Files.isRegularFile(secondWork.output().resolve("collection-completed.json")));
                assertEquals(descriptor, Files.readString(secondWork.output().resolve("remote-job.json")));
                assertEquals("CANCELLED", AutoLlmFileUtil.JSON.readTree(Files.readString(secondWork.output().resolve("remote-status.json"))).path("status").asString());
                var job = AutoLlmFileUtil.read(secondWork.output().resolve("remote-job.json"), WorkloadProtocol.Job.class);
                AutoLlmFileUtil.write(Path.of(job.root()).resolve(".jobs/" + job.id() + "/state.json"),
                        new WorkloadProtocol.State(job.id(), WorkloadProtocol.JobStatus.COLLECTING, null, "Retained collection from a legacy supervisor", null));
                var receipt = AutoLlmFileUtil.JSON.valueToTree(RemoteCollectionRecovery.repair(secondWork.output().resolve("remote-job.json"), SshTransport.runnerArtifact(config)));
                assertEquals("CANCELLED", receipt.path("state").path("status").asString());
                assertFalse(first.isDone()); assertEquals(descriptor, Files.readString(secondWork.output().resolve("remote-job.json")));
            } finally { cancelFirst.set(true); }
            assertInstanceOf(CancellationException.class, assertThrows(ExecutionException.class, () -> first.get(30, TimeUnit.SECONDS)).getCause());
        }
    }
    @Test void preparationReconciliationJoinsRunningWorkWithoutResubmittingIt() throws Exception {
        var work = workload("wait", "running preparation"); var detached = new CountDownLatch(1);
        try (var threads = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = threads.submit(() -> {
                try { return new SshWorkloadExecutor(config, root, new SshTransport(config, root.resolve(".transport")), _ -> {}).run(work, () -> false); }
                finally { detached.countDown(); }
            });
            awaitReady(); first.cancel(true); assertTrue(detached.await(10, TimeUnit.SECONDS));
            String descriptor = Files.readString(work.output().resolve("remote-job.json"));
            new AutoLlmExecutionService(new AutoLlmProcessRunner()).reconcilePreparation(root, () -> false, _ -> {});
            assertEquals(descriptor, Files.readString(work.output().resolve("remote-job.json")));
            assertEquals("SUCCEEDED", AutoLlmFileUtil.JSON.readTree(Files.readString(work.output().resolve("remote-status.json"))).path("status").asString());
            assertTrue(Files.isRegularFile(work.output().resolve("collection-completed.json")));
        }
    }
    @Test void aCancelledFixedStageCanRestartWithItsOldEvidencePreserved() throws Exception {
        var service = new AutoLlmExecutionService(new AutoLlmProcessRunner()); var cancelled = new AtomicBoolean();
        var work = workload("wait", "before cancellation");
        try (var pool = Executors.newSingleThreadExecutor()) {
            var future = pool.submit(() -> service.run(config, root, work.command(), work.cwd(), work.output(), null, List.of(), 30000, 0, true, 5000, cancelled::get, state -> {}));
            awaitReady(); cancelled.set(true);
            assertInstanceOf(CancellationException.class, assertThrows(ExecutionException.class, () -> future.get(20, TimeUnit.SECONDS)).getCause());
        }
        String old = AutoLlmFileUtil.JSON.readTree(Files.readString(work.output().resolve("remote-job.json"))).path("id").asString();
        var restarted = workload("success", "after cancellation");
        var result = service.run(config, root, restarted.command(), restarted.cwd(), restarted.output(), null, List.of(), 30000, 0, true, 5000, () -> false, state -> {});
        assertEquals(0, result.exitCode()); assertEquals("after cancellation", Files.readString(work.output().resolve("literal.txt")));
        assertEquals("before cancellation", Files.readString(root.resolve("remote-attempts/" + old + "/literal.txt")));
        assertNotEquals(old, AutoLlmFileUtil.JSON.readTree(Files.readString(work.output().resolve("remote-job.json"))).path("id").asString());
    }
    @Test void anUnexpectedHostIsRejectedBeforeExecutingTheWorkload() {
        AutoLlmFileUtil.write(root.resolve("execution-target.json"), new WorkloadProtocol.Host(WorkloadProtocol.VERSION, Map.of("cpu", "different server"), 32, 32, 1, 1, 1, List.of()));
        var executor = new SshWorkloadExecutor(config, root, new SshTransport(config, temp.resolve("transport")), state -> {});
        assertThrows(ExecutionTargetException.class, () -> executor.run(workload("success", "must not execute"), () -> false));
        assertFalse(Files.exists(root.resolve("output with spaces/literal.txt")));
    }
    @Test void explicitQualificationRefreshesPrerequisitesButIdentityChecksReuseThem() throws Exception {
        Path bin = temp.resolve("bin"), fail = temp.resolve("r-failed"), count = temp.resolve("r-checks"); Files.createDirectories(bin);
        Path r = bin.resolve("Rscript");
        Files.writeString(r, "#!/bin/sh\nprintf x >> '" + count + "'\nif test -f '" + fail + "'; then exit 1; fi\necho 'irace fixture'\n"); assertTrue(r.toFile().setExecutable(true));
        Path python = bin.resolve("python3"); Files.writeString(python, "#!/bin/sh\necho selector-fixture\n"); assertTrue(python.toFile().setExecutable(true));
        var server = config.execution().ssh().onlyServer();
        config = config.forServer(new AutoLlmConfig.Server(server.id(), server.host(), server.workspace(), server.javaExecutable(), Map.of("PATH", bin + ":" + System.getenv("PATH")), server.configFile(), null, null));
        var service = new AutoLlmExecutionService(new AutoLlmProcessRunner());
        var qualified = service.probe(config, root, "");
        assertEquals("x", Files.readString(count));
        assertEquals("PASSED", qualified.prerequisites().get(3).status().name());
        assertEquals("selector-python", qualified.prerequisites().get(3).name());
        Files.writeString(fail, "dependency removed");
        service.identity(config, root); assertEquals("x", Files.readString(count));
        var failure = assertThrows(AutoLlmFailureUtil.StageException.class, () -> service.probe(config, root, ""));
        assertEquals("PREREQUISITE_FAILED", failure.failure().code()); assertEquals("xx", Files.readString(count));
        var failed = AutoLlmFileUtil.read(root.resolve("target-preflight.json"), WorkloadProtocol.Host.class);
        assertEquals("r-packages", failed.prerequisites().get(2).name());
        assertEquals("FAILED", failed.prerequisites().get(2).status().name());
        Files.delete(fail); service.probe(config, root, ""); assertEquals("xxx", Files.readString(count));
    }
    @Test void resumeUsesTheVerifiedRunnerAfterLocalBuildOutputDisappears() throws Exception {
        var transport = new SshTransport(config, root.resolve(".transport")); transport.bootstrap();
        var host = AutoLlmFileUtil.JSON.readValue(transport.call("identity", WorkloadPathUtil.remoteRoot(config, root), null, config), WorkloadProtocol.Host.class);
        AutoLlmFileUtil.write(root.resolve("execution-target.json"), host);
        Files.delete(Path.of(config.execution().ssh().runnerArtifact()));
        var service = new AutoLlmExecutionService(new AutoLlmProcessRunner());
        var reattached = service.identity(config, root);
        assertEquals(host.identity(), reattached.identity()); assertEquals(host.effectiveWorkers(), reattached.effectiveWorkers());
        assertTrue(reattached.usableDiskBytes() > 0);
        Path frozen = root.resolve(".transport/runner.jar"); Files.writeString(frozen, "changed frozen runner");
        assertThrows(IllegalStateException.class, () -> service.identity(config, root));
    }
    @Test void persistentDisconnectionLeavesOneSavedJobForDirectCancellation() throws Exception {
        var submissions = new AtomicInteger();
        var transport = new SshTransport(config, temp.resolve("transport")) {
            @Override public byte[] call(String action, String remote, String id, Object input) {
                if (action.equals("submit")) submissions.incrementAndGet();
                if (action.equals("poll")) throw new ConnectionException("offline after submit");
                return super.call(action, remote, id, input);
            }
        };
        var failure = assertThrows(AutoLlmFailureUtil.StageException.class, () -> new SshWorkloadExecutor(config, root, transport, state -> {}).run(workload("wait", "saved"), () -> false));
        assertEquals("REMOTE_UNREACHABLE", failure.failure().code()); assertEquals(1, submissions.get());
        assertEquals(2, AutoLlmFileUtil.JSON.readTree(Files.readString(root.resolve("output with spaces/remote-status.json"))).path("connectionRetries").asInt());
        Path descriptor = root.resolve("output with spaces/remote-job.json"); String checksum = AutoLlmFileUtil.hash(descriptor);
        assertTrue(AutoLlmCampaignStore.hasUncollectedJobs(root));
        new AutoLlmExecutionService(new AutoLlmProcessRunner()).cancelOutstanding(config, root, state -> {});
        assertEquals(checksum, AutoLlmFileUtil.hash(descriptor)); assertFalse(AutoLlmCampaignStore.hasUncollectedJobs(root));
        assertEquals("CANCELLED", AutoLlmFileUtil.read(descriptor.resolveSibling("remote-status.json"), Map.class).get("status"));
    }
    @Test void resultDownloadRetriesAreBoundedAndResumeCollectsSameCompletedJob() {
        var downloads = new AtomicInteger();
        var transport = new SshTransport(config, temp.resolve("transport")) {
            @Override public void download(String remote, String id, Path target) { downloads.incrementAndGet(); throw new ConnectionException("result transfer unavailable"); }
        };
        var work = workload("success", "completed once");
        var failure = assertThrows(AutoLlmFailureUtil.StageException.class, () -> new SshWorkloadExecutor(config, root, transport, state -> {}).run(work, () -> false));
        assertEquals("REMOTE_UNREACHABLE", failure.failure().code()); assertTrue(downloads.get() >= 1);
        Path descriptor = work.output().resolve("remote-job.json"); String checksum = AutoLlmFileUtil.hash(descriptor);
        assertEquals(0, new SshWorkloadExecutor(config, root, new SshTransport(config, temp.resolve("transport")), state -> {}).run(work, () -> false).exitCode());
        assertEquals(checksum, AutoLlmFileUtil.hash(descriptor)); assertFalse(AutoLlmCampaignStore.hasUncollectedJobs(root));
    }
    private void awaitReady() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (!Files.exists(root.resolve("output with spaces/execution/readiness.json")) && System.nanoTime() < deadline) Thread.sleep(100);
        assertTrue(Files.exists(root.resolve("output with spaces/execution/readiness.json")));
    }
    private AutoLlmWorkloadExecutor.Workload workload(String mode, String literal) {
        Path output = root.resolve("output with spaces"); String remote = WorkloadPathUtil.remoteRoot(config, root);
        // Classpath is shared only by this loopback fixture; the production runner is a standalone attached JAR.
        var command = List.of(java, "-cp", System.getProperty("java.class.path"), RemoteWorkloadFixture.class.getName(), mode, remote + "/output with spaces", literal);
        return new AutoLlmWorkloadExecutor.Workload(command, root, output, Map.of(), List.of(), List.of(), mode.equals("hold") ? 90000 : 30000, 0, 100, true, 5000);
    }
    private void run(List<String> command) throws Exception {
        var process = new ProcessBuilder(command).redirectErrorStream(true).start(); String output = new String(process.getInputStream().readAllBytes());
        assertTrue(process.waitFor(10, TimeUnit.SECONDS)); assertEquals(0, process.exitValue(), output);
    }
    private static String read(Path file) { try { return Files.readString(file); } catch (IOException e) { return e.toString(); } }
}
