package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import java.nio.file.*;
import java.io.File;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.jar.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AutoLlmLifecycleTest {
    @TempDir Path temp;
    private final String java = Path.of(System.getProperty("java.home"), "bin/java").toString();
    private final String classpath = System.getProperty("java.class.path");
    private List<String> child(Path directory, String mode) {
        return List.of(java, "-Dmork.execution.directory=" + directory.resolve("execution"), "-cp", classpath, AutoLlmChildFixture.class.getName(), mode);
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
    @ParameterizedTest @ValueSource(booleans = {false, true})
    void twoTrialsRetainIndependentDataAndContinueAfterFailure(boolean archiveFailure) throws Exception {
        Path repo = temp.resolve("baseline"); Files.createDirectories(repo.resolve("src/main/java")); Files.writeString(repo.resolve("src/main/java/Original.java"), "// original\n");
        Path input = repo.resolve("instance.txt"); Files.writeString(input, "instance\n"); Files.writeString(repo.resolve("train.index"), input + "\n"); Files.writeString(repo.resolve("comparison.index"), input + "\n");
        Files.writeString(repo.resolve("sota.json"), "{\"$component\":\"Dummy\"}");
        createJar(repo.resolve("template.jar"));
        git(repo, "init"); git(repo, "add", "."); git(repo, "-c", "user.name=Test", "-c", "user.email=test@example.test", "commit", "-m", "baseline");
        Path cli = temp.resolve("fake-codex");
        Files.writeString(cli, "#!/bin/sh\nexport CODEX_HOME='" + temp.resolve("codex-home") + "'\nexec '" + java + "' -cp '" + classpath.replace("'", "'\\''") + "' " + AutoLlmCodexFixture.class.getName() + " \"$@\"\n"); cli.toFile().setExecutable(true);
        var settings = new AutoLlmConfig(new AutoLlmConfig.Project(repo.toString(), "HEAD", List.of(java, "-cp", classpath, AutoLlmBuildFixture.class.getName()), "target/project.jar", null), temp.resolve("campaigns").toString(), 2, 1234L, 299,
                "sota.json", null, "train.index", "comparison.index", new AutoLlmConfig.Codex(cli.toString(), null, null, archiveFailure ? "missing-archive-fixture" : "repair-fixture", null), new AutoLlmConfig.Preparation(5000L, 1),
                new AutoLlmConfig.Tuning(100, 2L, 20L, false), new AutoLlmConfig.Comparison(2, 20L), new AutoLlmConfig.Execution(2, 50L, 5000L, 5000L, 5000L, 5000L, 5000L, 10000L, "128m"));
        var runner = new AutoLlmProcessRunner() {
            @Override public Result run(List<String> command, Path cwd, Path output, long timeout, long planned, long grace, BooleanSupplier cancel, boolean watch, long startup) {
                if (!command.getFirst().equals("Rscript")) return super.run(command, cwd, output, timeout, planned, grace, cancel, watch, startup);
                try { Files.createDirectories(output); Files.writeString(output.resolve("stdout.log"), "fake-r-version"); Files.writeString(output.resolve("stderr.log"), ""); } catch (Exception e) { throw new IllegalStateException(e); }
                return new Result(0, false, 0);
            }
        };
        var store = new AutoLlmCampaignStore(settings); var instances = new InstanceConfiguration(); instances.setPath(Map.of("default", input.toString()));
        var service = new AutoLlmCampaignService(settings, store, runner, codex(runner), mock(AutoLlmReportService.class), instances);
        try {
            var created = service.create(settings); service.start(created.id, false);
            long deadline = System.currentTimeMillis() + 30000;
            while (service.active() != null && System.currentTimeMillis() < deadline) Thread.sleep(50);
            assertNull(service.active(), "Campaign must terminate within the test budget");
            var campaign = store.load(created.id);
            assertEquals("COMPLETED_WITH_FAILURES", campaign.status, campaign.failure);
            assertEquals("FAILED", campaign.trials.getFirst().status, campaign.trials.getFirst().failure);
            assertTrue(campaign.trials.getFirst().failure.contains(archiveFailure ? "could not be archived" : "Hung evaluation"));
            var second = campaign.trials.get(1); assertEquals("SUCCEEDED", second.status, second.failure);
            assertNotEquals(campaign.trials.getFirst().threadId, second.threadId); assertNotEquals(campaign.trials.getFirst().tuningSeed, second.tuningSeed);
            assertEquals(campaign.trials.getFirst().comparisonSeed, second.comparisonSeed);
            for (var trial : campaign.trials) {
                assertEquals(archiveFailure ? 0 : 1, trial.repairTurns);
                assertEquals(archiveFailure && trial.number == 1 ? "UNAVAILABLE" : "COMPLETE", trial.codexUsage.status());
                if (!archiveFailure) assertEquals(40L, trial.codexUsage.tokens().totalTokens());
                for (var repair : trial.attempts) if (repair.stage.equals("REPAIR")) {
                    Map<String,Object> owner = AutoLlmFileUtil.read(store.directory(campaign.id).resolve(repair.directory).resolve("owner.json"), Map.class);
                    var command = (List<String>) owner.get("command"); assertEquals(trial.threadId, command.get(command.indexOf("resume") + 1));
                    assertEquals(1, repair.codexEvidence.conversations().size());
                    assertEquals(20L, repair.codexEvidence.usage().tokens().totalTokens());
                    assertEquals(7, Files.readAllLines(store.directory(campaign.id).resolve(repair.directory).resolve(repair.codexEvidence.conversations().getFirst().path())).size());
                }
            }
            Path root = store.directory(campaign.id);
            assertTrue(Files.exists(root.resolve(second.mainDirectory).resolve("autoconfig-final-elites.json")));
            var rows = AutoLlmFileUtil.events(root.resolve(second.comparisonDirectory).resolve("execution/events.jsonl"));
            int results = 0; for (var row : rows) if ("evaluation-result".equals(row.get("kind"))) results++;
            assertEquals(4, results);
            assertEquals("// original\n", Files.readString(repo.resolve("src/main/java/Original.java")));
            assertTrue(Files.exists(root.resolve("reports/llm-usage.json")));
            assertEquals(archiveFailure ? 20L : 80L, campaign.codexUsage.tokens().totalTokens());
            assertEquals(archiveFailure ? "PARTIAL" : "COMPLETE", campaign.codexUsage.status());
            assertFalse(Files.exists(repo.resolve("src/main/java/FixtureUtil.java")));
            assertThrows(IllegalStateException.class, () -> service.start(campaign.id, true));
        } finally { service.close(); }
    }
    @Test void protectedFilesAndArtifactPathsCannotBeChangedOrEscaped() throws Exception {
        Path source = temp.resolve("source"); Files.createDirectories(source.resolve("src/main/java")); Path file = source.resolve("src/main/java/Original.java"); Files.writeString(file, "before");
        var hashes = AutoLlmFileUtil.sourceHashes(source); Files.writeString(file, "after");
        assertThrows(IllegalStateException.class, () -> AutoLlmFileUtil.verifySources(source, hashes));
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
        var store = new AutoLlmCampaignStore(settings); var campaign = new AutoLlmCampaign(); campaign.id = UUID.randomUUID().toString(); campaign.createdAt = "2026-10-07"; campaign.config = settings; campaign.status = "RUNNING";
        var completed = new AutoLlmCampaign.Trial(); completed.number = 1; completed.status = "SUCCEEDED"; campaign.trials.add(completed);
        var interrupted = new AutoLlmCampaign.Trial(); interrupted.number = 2; interrupted.status = "TUNING";
        var attempt = new AutoLlmCampaign.Attempt(); attempt.stage = "TUNING"; attempt.directory = "trials/002/attempts/001-tuning"; interrupted.attempts.add(attempt); campaign.trials.add(interrupted); store.save(campaign);
        var generation = new AutoLlmCampaign.Trial(); generation.number = 3; generation.status = "GENERATION";
        var generating = new AutoLlmCampaign.Attempt(); generating.stage = "GENERATION"; generating.directory = "trials/003/attempts/001-generation"; generation.attempts.add(generating); campaign.trials.add(generation); store.save(campaign);
        String thread = UUID.randomUUID().toString();
        Path log = store.directory(campaign.id).resolve(generating.directory).resolve("stdout.log"); Files.createDirectories(log.getParent()); Files.writeString(log, "{\"type\":\"thread.started\",\"thread_id\":\"" + thread + "\"}\n");
        Path source = store.directory(campaign.id).resolve("trials/003/source");
        CodexEvidenceUtil.begin(temp.resolve("codex-home"), source, log.getParent(), null);
        AutoLlmFileUtil.append(temp.resolve("codex-home/sessions/rollout-" + thread + ".jsonl"), Map.of("type", "session_meta", "payload", Map.of("id", thread, "cwd", source.toString())));
        var lost = new AutoLlmCampaign.Trial(); lost.number = 4; lost.status = "GENERATION";
        var missing = new AutoLlmCampaign.Attempt(); missing.stage = "GENERATION"; missing.directory = "trials/004/attempts/001-generation"; lost.attempts.add(missing); campaign.trials.add(lost); store.save(campaign);
        AutoLlmFileUtil.append(store.directory(campaign.id).resolve(missing.directory).resolve("stdout.log"), Map.of("type", "thread.started", "thread_id", UUID.randomUUID().toString()));
        var runner = new AutoLlmProcessRunner();
        var service = new AutoLlmCampaignService(settings, store, runner, codex(runner), mock(AutoLlmReportService.class), new InstanceConfiguration());
        try {
            service.recover(); var recovered = store.load(campaign.id);
            assertEquals("INTERRUPTED", recovered.status); assertEquals("SUCCEEDED", recovered.trials.getFirst().status); assertEquals("INTERRUPTED", recovered.trials.get(1).attempts.getFirst().status);
            assertEquals(thread, recovered.trials.get(2).threadId); assertTrue(recovered.trials.get(2).resumeTurn); assertEquals(0, recovered.trials.get(2).repairTurns);
            assertEquals("ARCHIVED", recovered.trials.get(2).attempts.getFirst().codexEvidence.status());
            assertEquals("FAILED", recovered.trials.get(3).status); assertFalse(recovered.trials.get(3).resumeTurn);
            assertEquals("FAILED", recovered.trials.get(3).attempts.getFirst().codexEvidence.status());
            try (var lock = store.lock()) { assertThrows(IllegalStateException.class, store::lock); }
        } finally { service.close(); }
    }
    private CodexTaskRunner codex(AutoLlmProcessRunner runner) { return new CodexTaskRunner(runner, new MockEnvironment().withProperty("CODEX_HOME", temp.resolve("codex-home").toString())); }
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
