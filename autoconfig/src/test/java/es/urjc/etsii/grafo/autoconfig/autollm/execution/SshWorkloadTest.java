package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.jar.*;
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
        var defaults = AutoLlmConfig.defaults();
        config = new AutoLlmConfig(defaults.project(), defaults.workspace(), 1, 1234L, 299, null, null, null, null, defaults.codex(), defaults.preparation(), defaults.tuning(), defaults.comparison(),
                new AutoLlmConfig.Execution(32, 100L, 30000L, 30000L, 5000L, 5000L, 30000L, 30000L, "128m", AutoLlmConfig.Target.SSH,
                        new AutoLlmConfig.Ssh("fixture", runner.toString(), List.of(new AutoLlmConfig.Server("fixture", "mork-fixture", temp.resolve("remote workspace").toString(), java, Map.of(), ssh.toString(), null, null)))));
        root = temp.resolve("local mirror").toAbsolutePath(); Files.createDirectories(root);
    }
    @AfterEach void stopSsh() { if (server != null) server.destroyForcibly(); }
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
    @Test void reconnectsWithoutDuplicateSubmissionAndPreservesLiteralArguments() throws Exception {
        var submissions = new AtomicInteger(); var disconnected = new AtomicBoolean();
        SshTransport transport = new SshTransport(config, temp.resolve("transport")) {
            @Override public byte[] call(String action, String remote, String id, Object input) {
                if (action.equals("submit")) submissions.incrementAndGet();
                if (action.equals("poll") && disconnected.compareAndSet(false, true)) throw new ConnectionException("simulated connection loss after submit");
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
    @Test void resumeUsesTheVerifiedRunnerAfterLocalBuildOutputDisappears() throws Exception {
        var transport = new SshTransport(config, root.resolve(".transport")); transport.bootstrap();
        var host = AutoLlmFileUtil.JSON.readValue(transport.call("identity", WorkloadPathUtil.remoteRoot(config, root), null, config), WorkloadProtocol.Host.class);
        AutoLlmFileUtil.write(root.resolve("execution-target.json"), host);
        Files.delete(Path.of(config.execution().ssh().runnerArtifact()));
        var service = new AutoLlmExecutionService(new AutoLlmProcessRunner());
        var reattached = service.probe(config, root);
        assertEquals(host.identity(), reattached.identity()); assertEquals(host.effectiveWorkers(), reattached.effectiveWorkers());
        assertTrue(reattached.usableDiskBytes() > 0);
        Path frozen = root.resolve(".transport/runner.jar"); Files.writeString(frozen, "changed frozen runner");
        assertThrows(IllegalStateException.class, () -> service.probe(config, root));
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
        assertEquals("REMOTE_UNREACHABLE", failure.failure().code()); assertEquals(3, downloads.get());
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
        return new AutoLlmWorkloadExecutor.Workload(command, root, output, Map.of(), List.of(), List.of(), 30000, 0, 100, true, 5000);
    }
    private void run(List<String> command) throws Exception {
        var process = new ProcessBuilder(command).redirectErrorStream(true).start(); String output = new String(process.getInputStream().readAllBytes());
        assertTrue(process.waitFor(10, TimeUnit.SECONDS)); assertEquals(0, process.exitValue(), output);
    }
    private static String read(Path file) { try { return Files.readString(file); } catch (IOException e) { return e.toString(); } }
}
