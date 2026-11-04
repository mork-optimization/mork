package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import tools.jackson.core.type.TypeReference;
import java.nio.file.*;
import java.io.File;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.jar.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static java.nio.charset.StandardCharsets.UTF_8;

class AutoLlmLifecycleIT {
    @TempDir Path temp;
    private final String java = Path.of(System.getProperty("java.home"), "bin/java").toString();
    private final String classpath = System.getProperty("java.class.path");
    private List<String> child(Path directory, String mode) {
        return List.of(java, "-Dmork.execution.directory=" + directory.resolve("execution"), "-cp", classpath, AutoLlmChildFixture.class.getName(), mode);
    }
    @Test void closesStandardInputForNoninteractiveChildren() throws Exception {
        Path directory = temp.resolve("stdin");
        var result = new AutoLlmProcessRunner().run(child(directory, "stdin"), temp, directory, 5000, 0, 50, () -> false, false, 0);
        assertEquals(0, result.exitCode());
        assertEquals("stdin-bytes=0", Files.readString(directory.resolve("stdout.log")).strip());
    }
    @Test void watchdogKillsHungEvaluationWhileOtherWorkProducesRecords() {
        var runner = new AutoLlmProcessRunner(); Path directory = temp.resolve("hang");
        var error = assertThrows(IllegalStateException.class, () -> runner.run(child(directory, "hang"), temp, directory, 10000, 0, 50, () -> false, true, 5000));
        assertTrue(error.getMessage().contains("Hung evaluation"));
        assertTrue(AutoLlmFileUtil.events(directory.resolve("execution/events.jsonl")).size() >= 2);
        var owner = AutoLlmFileUtil.read(directory.resolve("owner.json"), Map.class);
        assertFalse(ProcessHandle.of(((Number) owner.get("pid")).longValue()).map(ProcessHandle::isAlive).orElse(false));
    }
    @Test void plannedPilotDeadlineIsDistinctFromFailure() {
        var runner = new AutoLlmProcessRunner(); Path directory = temp.resolve("pilot");
        var result = runner.run(child(directory, "pilot"), temp, directory, 10000, 50, 500, () -> false, true, 5000);
        assertTrue(result.plannedDeadline()); assertEquals(0, result.exitCode()); assertTrue(Files.exists(directory.resolve("execution/terminal.json")));
    }
    @Test void cancellationStopsTheChildAndRecordsUnfinishedEvaluations() {
        Path directory = temp.resolve("cancelled");
        assertThrows(java.util.concurrent.CancellationException.class, () -> new AutoLlmProcessRunner().run(child(directory, "hang"), temp, directory, 10000, 0, 5000, () -> true, true, 5000));
        var owner = AutoLlmFileUtil.read(directory.resolve("owner.json"), Map.class);
        assertFalse(ProcessHandle.of(((Number) owner.get("pid")).longValue()).map(ProcessHandle::isAlive).orElse(false));
        for (var event : AutoLlmFileUtil.events(directory.resolve("execution/events.jsonl"))) if ("evaluation-result".equals(event.get("kind"))) assertEquals("INTERRUPTED", event.get("state"));
    }
    @ParameterizedTest @ValueSource(strings = {"component", "archive", "baseline", "framework", "transient", "persistent", "quota-generation", "quota-repair", "quota-repeat", "capacity-generation", "capacity-repair", "capacity-repeat"})
    void twoTrialsRetainIndependentDataAndContinueAfterFailure(String mode) throws Exception {
        boolean archiveFailure = mode.equals("archive");
        Path repo = temp.resolve("baseline"); Files.createDirectories(repo.resolve("src/main/java")); Files.writeString(repo.resolve("src/main/java/Original.java"), "// original\n");
        for (var entry : AutoLlmCodexFixture.BASELINE_SOURCES.entrySet()) Files.writeString(repo.resolve(entry.getKey()), entry.getValue());
        Path input = repo.resolve("instance.txt"); Files.writeString(input, "instance\n"); Files.writeString(repo.resolve("train.index"), input + "\n"); Files.writeString(repo.resolve("comparison.index"), input + "\n");
        Files.writeString(repo.resolve("sota.json"), "{\"$component\":\"Dummy\"}");
        createJar(repo.resolve("template.jar"));
        git(repo, "init"); git(repo, "add", "."); git(repo, "-c", "user.name=Test", "-c", "user.email=test@example.test", "commit", "-m", "baseline");
        Path cli = temp.resolve("fake-codex");
        Files.writeString(cli, "#!/bin/sh\nexport CODEX_HOME='" + temp.resolve("codex-home") + "'\nexec '" + java + "' -cp '" + classpath.replace("'", "'\\''") + "' " + AutoLlmCodexFixture.class.getName() + " \"$@\"\n"); cli.toFile().setExecutable(true);
        var login = new AutoLlmProcessRunner().run(List.of(cli.toString(), "login", "status"), repo, temp.resolve("login-status"), 5000, 0, 50, () -> false, false, 0);
        assertEquals(1, login.exitCode(), "The local-provider fixture must work without an OpenAI login");
        assertEquals("Not logged in", Files.readString(temp.resolve("login-status/stderr.log")).strip());
        var buildCommand = new ArrayList<>(List.of(java, "-cp", classpath, AutoLlmBuildFixture.class.getName()));
        if (mode.equals("baseline")) buildCommand.add("--fail-replay");
        if (mode.equals("framework")) buildCommand.add("--fail-once-trial");
        var settings = new AutoLlmConfig(new AutoLlmConfig.Project(repo.toString(), "HEAD", buildCommand, "target/project.jar", null), temp.resolve("campaigns").toString(), 2, 1234L, 299,
                "sota.json", null, "train.index", "comparison.index", new AutoLlmConfig.Codex(cli.toString(), null, null, (mode.startsWith("quota-") || mode.startsWith("capacity-")) ? mode + "-fixture" : archiveFailure ? "missing-archive-fixture" : mode.equals("framework") ? "fixture-model" : "repair-fixture", null, null), new AutoLlmConfig.Preparation(5000L, 5000L, 1, null, null, null, null),
                new AutoLlmConfig.Tuning(100, 2L, 20L, false), new AutoLlmConfig.Comparison(2, 20L), new AutoLlmConfig.Execution(2, 50L, 5000L, 5000L, 5000L, 5000L, 5000L, 10000L, "128m", null, null));
        var failures = new java.util.concurrent.atomic.AtomicInteger(mode.equals("transient") ? 2 : mode.equals("persistent") ? 3 : 0);
        var generations = new java.util.concurrent.atomic.AtomicInteger();
        var prerequisiteChecks = new java.util.concurrent.atomic.AtomicInteger();
        var runner = new AutoLlmProcessRunner() {
            @Override public Result run(List<String> command, Path cwd, Path output, long timeout, long planned, long grace, BooleanSupplier cancel, boolean watch, long startup) {
                if (command.contains("exec")) {
                    generations.incrementAndGet();
                    if (cwd.toString().contains("trials/001/") && failures.getAndDecrement() > 0) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.TRANSIENT_INFRASTRUCTURE, "TEMPORARY_LAUNCH", "Temporary launch failure", output);
                }
                if (!command.getFirst().equals("Rscript")) return super.run(command, cwd, output, timeout, planned, grace, cancel, watch, startup);
                prerequisiteChecks.incrementAndGet();
                try { Files.createDirectories(output); Files.writeString(output.resolve("stdout.log"), "fake-r-version"); Files.writeString(output.resolve("stderr.log"), ""); } catch (Exception e) { throw new IllegalStateException(e); }
                return new Result(0, false, 0);
            }
        };
        var store = new AutoLlmCampaignStore(settings); var instances = new InstanceConfiguration(); instances.setPath(Map.of("default", input.toString()));
        var quotaReleasedSlot = new java.util.concurrent.atomic.AtomicBoolean();
        var generator = new CodexTaskRunner(runner, new MockEnvironment().withProperty("CODEX_HOME", temp.resolve("codex-home").toString())) {
            @Override public Result run(AutoLlmConfig config, Path checkout, Path output, String prompt, String thread, BooleanSupplier cancel) {
                try { return super.run(config, checkout, output, prompt, thread, cancel); }
                catch (CodexQuotaUtil.QuotaLimitException quota) {
                    // Keep integration tests immediate; reset-time parsing is exercised separately.
                    throw new CodexQuotaUtil.QuotaLimitException(quota.getMessage(), Instant.EPOCH);
                }
            }
        };
        var service = new AutoLlmCampaignService(settings, store, runner, generator, mock(AutoLlmReportService.class), instances) {
            @Override protected AutoLlmTrialRunner runner(AutoLlmExecutionContext model, Path root, AutoLlmTrialRunner.Listener listener, BooleanSupplier stop, java.util.concurrent.Semaphore permits) {
                var trialRunner = new AutoLlmTrialRunner(model, root, runner, generator, new AutoLlmTrialRunner.Listener() {
                    public void state(AutoLlmExecutionContext.Snapshot snapshot) {
                        if ((snapshot.trial() != null && (snapshot.trial().status == AutoLlmCampaign.TrialStatus.WAITING_FOR_QUOTA || snapshot.trial().status == AutoLlmCampaign.TrialStatus.WAITING_FOR_CAPACITY))) {
                            assertEquals(0, snapshot.trial().infrastructureRetries);
                            assertEquals(0, snapshot.trial().repairTurns);
                            assertTrue(permits.availablePermits() > 0);
                            quotaReleasedSlot.set(true);
                        }
                        listener.state(snapshot);
                    }
                    public void event(String kind, Object detail) { listener.event(kind, detail); }
                }, stop, permits);
                var now = new java.util.concurrent.atomic.AtomicReference<>(Instant.now());
                java.time.Clock clock = new java.time.Clock() {
                    public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
                    public java.time.Clock withZone(java.time.ZoneId zone) { return this; }
                    public Instant instant() { return now.get(); }
                };
                trialRunner.waitTiming(clock, () -> 0, millis -> now.updateAndGet(time -> time.plusMillis(millis)));
                return trialRunner;
            }
        };
        try {
            var created = service.create(settings); service.baselineCheck(created.id); if (mode.equals("baseline")) { assertNotNull(store.load(created.id).targets.get("local").problem); return; } assertNull(store.load(created.id).targets.get("local").problem, () -> AutoLlmFileUtil.JSON.writeValueAsString(store.load(created.id))); service.approve(created.id, service.proposal(created.id).digest()); service.start(created.id, false, Set.of());
            long deadline = System.currentTimeMillis() + 120000;
            while (service.active() != null && System.currentTimeMillis() < deadline) Thread.sleep(50);
            assertNull(service.active(), () -> "Campaign did not terminate: " + AutoLlmFileUtil.JSON.writeValueAsString(store.load(created.id)));
            var campaign = store.load(created.id);
            assertEquals(1, prerequisiteChecks.get(), "Qualification must check R dependencies once, including relocation and all trials");
            assertEquals("fake-r-version", campaign.targets.get("local").versions.get("r-packages"));
            Path campaignRoot = store.directory(created.id);
            for (var completed : campaign.trials) {
                Path directory = campaignRoot.resolve(completed.executionDirectory());
                if (!completed.status.terminal() || !Files.exists(directory.resolve("frozen/executable-fingerprint.json"))) continue;
                var retention = AutoLlmFileUtil.read(directory.resolve("storage/artifact-retention.json"), Map.class);
                assertEquals("RELEASED", retention.get("status"), retention.toString());
                assertFalse(Files.exists(directory.resolve("frozen/project.jar")));
            }
            assertEquals(List.of(input.toString()), Files.readAllLines(campaignRoot.resolve("data/training.index")));
            assertFalse(Files.exists(campaignRoot.resolve("data/training")));
            Path workerIndex = campaignRoot.resolve("targets/local/data/training.index");
            if (Files.exists(workerIndex)) {
                assertEquals(List.of(input.toString()), Files.readAllLines(workerIndex));
                assertFalse(Files.exists(campaignRoot.resolve("targets/local/data/training")));
            }
            if ((mode.startsWith("quota-") || mode.startsWith("capacity-"))) {
                assertEquals("COMPLETED_WITH_FAILURES", campaign.status.name(), campaign.failure);
                var first = campaign.trials.getFirst();
                assertEquals("FAILED", first.status.name()); assertEquals("SUCCEEDED", campaign.trials.get(1).status.name());
                assertEquals(0, first.infrastructureRetries); assertEquals(mode.endsWith("-repair") ? 1 : 0, first.repairTurns);
                assertNull(first.quotaRetryAt); assertNull(first.capacityWait); assertFalse(first.repairPending); assertTrue(quotaReleasedSlot.get());
                int limited = 0;
                for (var attempt : first.attempts) if (attempt.stage.generation()) {
                    assertEquals(first.threadId, attempt.codexEvidence.threadId());
                    if (((attempt.status == AutoLlmCampaign.AttemptStatus.QUOTA_LIMIT || attempt.status == AutoLlmCampaign.AttemptStatus.CAPACITY_LIMIT))) limited++;
                }
                assertEquals(mode.endsWith("-repeat") ? 4 : 1, limited);
                String events = Files.readString(store.directory(campaign.id).resolve("campaign-events.jsonl"));
                String prefix = mode.startsWith("capacity-") ? "codex-capacity-" : "codex-quota-";
                assertTrue(events.contains(prefix + "wait")); assertTrue(events.contains(prefix + "resumed"));
                return;
            }
            if (mode.equals("framework")) {
                assertEquals("NEEDS_ATTENTION", campaign.status.name(), campaign.failure); assertEquals("BLOCKED", campaign.trials.getFirst().status.name());
                assertEquals(2, generations.get()); assertEquals(0, campaign.trials.getFirst().repairTurns);
                service.start(campaign.id, true, Set.of()); deadline = System.currentTimeMillis() + 120000;
                while (service.active() != null && System.currentTimeMillis() < deadline) Thread.sleep(50);
                assertNull(service.active()); var recovered = store.load(campaign.id);
                assertEquals("COMPLETED_WITH_FAILURES", recovered.status.name(), recovered.failure); assertEquals(2, generations.get());
                assertEquals(0, recovered.trials.getFirst().repairTurns); assertEquals("SUCCEEDED", recovered.trials.get(1).status.name());
                assertTrue(Files.readString(store.directory(campaign.id).resolve("campaign-events.jsonl")).contains("BUILD_FAILED"));
                assertTrue(AutoLlmEvidenceFiles.readString(store.directory(campaign.id).resolve(recovered.trials.getFirst().attempts.get(1).directory).resolve("build/stderr.log")).contains("Unchanged framework test failed")); return;
            }
            if (mode.equals("baseline")) {
                assertEquals("NEEDS_ATTENTION", campaign.status.name(), campaign.failure); assertEquals(0, generations.get());
                assertTrue(campaign.trials.getFirst().attempts.isEmpty()); assertEquals("SUCCEEDED", campaign.trials.get(1).status.name()); return;
            }
            if (mode.equals("persistent")) {
                assertEquals("NEEDS_ATTENTION", campaign.status.name(), campaign.failure); assertEquals("BLOCKED", campaign.trials.getFirst().status.name());
                assertEquals(2, campaign.trials.getFirst().infrastructureRetries); assertEquals(0, campaign.trials.getFirst().repairTurns);
                assertEquals(5, generations.get()); assertEquals("SUCCEEDED", campaign.trials.get(1).status.name()); return;
            }
            if (archiveFailure) {
                assertEquals("NEEDS_ATTENTION", campaign.status.name(), campaign.failure);
                assertEquals("BLOCKED", campaign.trials.getFirst().status.name());
                assertEquals(AutoLlmFailureUtil.Category.INTEGRITY, campaign.trials.getFirst().problem.category());
                assertEquals(0, campaign.trials.getFirst().repairTurns);
                assertEquals("SUCCEEDED", campaign.trials.get(1).status.name());
                assertEquals("FAILED", campaign.trials.getFirst().attempts.getFirst().codexEvidence.status());
                service.start(campaign.id, true, Set.of());
                deadline = System.currentTimeMillis() + 10000;
                while (service.active() != null && System.currentTimeMillis() < deadline) Thread.sleep(20);
                assertNull(service.active()); assertEquals("BLOCKED", store.load(campaign.id).trials.getFirst().status.name());
                assertEquals(2, generations.get());
                return;
            }
            if (mode.equals("transient")) {
                assertEquals("COMPLETED_WITH_FAILURES", campaign.status.name(), campaign.failure); assertNull(campaign.problem);
                assertEquals(2, campaign.trials.getFirst().infrastructureRetries); assertEquals(1, campaign.trials.getFirst().repairTurns);
                assertEquals("SUCCEEDED", campaign.trials.get(1).status.name(), campaign.trials.get(1).failure);
                assertEquals(80L, campaign.codexUsage.tokens().totalTokens()); assertEquals("PARTIAL", campaign.codexUsage.status()); return;
            }
            assertEquals("COMPLETED_WITH_FAILURES", campaign.status.name(), campaign.failure);
            assertEquals("FAILED", campaign.trials.getFirst().status.name(), campaign.trials.getFirst().failure);
            assertTrue(campaign.trials.getFirst().failure.contains(archiveFailure ? "could not be archived" : "Hung evaluation"));
            var second = campaign.trials.get(1); assertEquals("SUCCEEDED", second.status.name(), second.failure);
            assertNotEquals(campaign.trials.getFirst().threadId, second.threadId); assertNotEquals(campaign.trials.getFirst().tuningSeed, second.tuningSeed);
            assertEquals(campaign.trials.getFirst().comparisonSeed, second.comparisonSeed);
            String sourceInstructions;
            try (var inputStream = getClass().getResourceAsStream("/autollm/source-instructions.txt")) {
                sourceInstructions = new String(Objects.requireNonNull(inputStream).readAllBytes(), UTF_8);
            }
            for (var trial : campaign.trials) {
                assertEquals(archiveFailure ? 0 : 1, trial.repairTurns);
                assertEquals(archiveFailure && trial.number == 1 ? "UNAVAILABLE" : "COMPLETE", trial.codexUsage.status());
                if (!archiveFailure) assertEquals(40L, trial.codexUsage.tokens().totalTokens());
                for (var generation : trial.attempts) if (generation.stage.generation()) {
                    String prompt = AutoLlmEvidenceFiles.readString(store.directory(campaign.id).resolve(generation.directory).resolve("prompt.txt"));
                    assertTrue(prompt.startsWith(created.prompt));
                    assertTrue(prompt.endsWith(sourceInstructions));
                }
                for (var repair : trial.attempts) if ((repair.stage == AutoLlmCampaign.AttemptStage.REPAIR)) {
                    Map<?,?> owner = AutoLlmFileUtil.read(store.directory(campaign.id).resolve(repair.directory).resolve("owner.json"), Map.class);
                    List<?> command = assertInstanceOf(List.class, owner.get("command")); assertEquals(trial.threadId, command.get(command.indexOf("resume") + 1));
                    assertEquals(1, repair.codexEvidence.conversations().size());
                    assertEquals(20L, repair.codexEvidence.usage().tokens().totalTokens());
                    assertEquals(7L, AutoLlmEvidenceFiles.readString(store.directory(campaign.id).resolve(repair.directory).resolve(repair.codexEvidence.conversations().getFirst().path())).lines().count());
                    assertTrue(AutoLlmEvidenceFiles.readString(store.directory(campaign.id).resolve(repair.directory).resolve("prompt.txt")).contains("Command failed"));
                }
            }
            Path root = store.directory(campaign.id);
            assertTrue(AutoLlmLifecycleIT.class.desiredAssertionStatus(), "Maven tests must enable assertions");
            assertChildAssertions(root.resolve("targets/local/baseline-preflight/check"), false);
            assertChildAssertions(root.resolve("targets/local/baseline-preflight/probe"), true);
            for (var trial : campaign.trials) for (var stage : trial.attempts) {
                Path directory = root.resolve(stage.directory);
                switch (stage.stage) {
                    case PREPARATION -> {
                        if ((stage.status == AutoLlmCampaign.AttemptStatus.SUCCEEDED)) {
                            assertChildAssertions(directory.resolve("check"), false);
                            assertChildAssertions(directory.resolve("probe"), true);
                            assertChildAssertions(directory.resolve("pilot"), true);
                        }
                    }
                    case TUNING, WINNER_EXPORT, COMPARISON -> assertChildAssertions(directory, false);
                }
            }
            assertTrue(Files.exists(root.resolve(second.outputDirectory(AutoLlmCampaign.Stage.TUNING)).resolve("autoconfig-final-elites.json")));
            var rows = AutoLlmFileUtil.events(root.resolve(second.outputDirectory(AutoLlmCampaign.Stage.COMPARISON)).resolve("execution/events.jsonl"));
            int results = 0; for (var row : rows) if ("evaluation-result".equals(row.get("kind"))) results++;
            assertEquals(4, results);
            assertEquals("// original\n", Files.readString(repo.resolve("src/main/java/Original.java")));
            Map<String, String> baselineHashes = AutoLlmFileUtil.read(root.resolve("targets/local/baseline/source-hashes.json"), new TypeReference<>() {});
            Map<String, String> finalHashes = AutoLlmFileUtil.read(root.resolve(second.executionDirectory()).resolve("frozen/source-hashes.json"), new TypeReference<>() {});
            Path restored = temp.resolve("restored-trial-2");
            AutoLlmSourceService.restore(root, root.resolve(second.executionDirectory()), restored, (args, cwd, evidence) -> {
                var result = runner.run(args, cwd, evidence, 30000, 0, 1000, () -> false, false, 0);
                assertEquals(0, result.exitCode(), () -> AutoLlmFailureUtil.tail(evidence.resolve("stderr.log"), 4000));
            });
            for (var entry : AutoLlmCodexFixture.MODIFIED_SOURCES.entrySet()) {
                assertEquals(AutoLlmCodexFixture.BASELINE_SOURCES.get(entry.getKey()), Files.readString(repo.resolve(entry.getKey())));
                assertNotEquals(baselineHashes.get(entry.getKey()), finalHashes.get(entry.getKey()));
                assertEquals(entry.getValue(), Files.readString(restored.resolve(entry.getKey())));
            }
            assertTrue(Files.exists(root.resolve("reports/llm-usage.json")));
            assertEquals(archiveFailure ? 20L : 80L, campaign.codexUsage.tokens().totalTokens());
            assertEquals(archiveFailure ? "PARTIAL" : "COMPLETE", campaign.codexUsage.status());
            assertFalse(Files.exists(repo.resolve("src/main/java/FixtureUtil.java")));
            assertThrows(IllegalStateException.class, () -> service.start(campaign.id, true, Set.of()));
        } finally { service.close(); }
    }
    private void assertChildAssertions(Path directory, boolean enabled) {
        Map<String, Boolean> status = AutoLlmFileUtil.read(directory.resolve("execution/assertions.json"), new TypeReference<>() {});
        assertEquals(Map.of("application", enabled, "moves", enabled, "solutions", enabled), status, directory.toString());
    }
    private void git(Path repo, String... args) throws Exception { var command = new ArrayList<>(List.of("git")); command.addAll(List.of(args)); var process = new ProcessBuilder(command).directory(repo.toFile()).redirectErrorStream(true).start(); String output = new String(process.getInputStream().readAllBytes()); assertEquals(0, process.waitFor(), output); }
    private void createJar(Path file) throws Exception {
        var manifest = new Manifest(); manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, AutoLlmChildFixture.class.getName());
        var entries = new ArrayList<String>(); for (String entry : classpath.split(File.pathSeparator)) entries.add(Path.of(entry).toUri().toString());
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, String.join(" ", entries));
        try (var jar = new JarOutputStream(Files.newOutputStream(file), manifest)) {
            String name = AutoLlmChildFixture.class.getName().replace('.', '/') + ".class"; jar.putNextEntry(new JarEntry(name));
            try (var input = getClass().getClassLoader().getResourceAsStream(name)) { jar.write(Objects.requireNonNull(input).readAllBytes()); } jar.closeEntry();
        }
    }
}
