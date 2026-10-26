package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AutoLlmPreparationBudgetTest {
    @TempDir Path temp;

    @Test
    void defaultsAndPartialConfigurationsKeepIndependentTenMinuteBudgets() {
        var defaults = AutoLlmConfig.defaults().preparation();
        assertEquals(600_000L, defaults.probeDurationMillis());
        assertEquals(600_000L, defaults.pilotDurationMillis());

        var config = AutoLlmFileUtil.JSON.readValue(
                "{\"preparation\":{\"pilotDurationMillis\":1234}}", AutoLlmConfig.class);
        assertEquals(600_000L, config.preparation().probeDurationMillis());
        assertEquals(1234L, config.preparation().pilotDurationMillis());
        var probeOnly = AutoLlmFileUtil.JSON.readValue(
                "{\"preparation\":{\"probeDurationMillis\":4321}}", AutoLlmConfig.class);
        assertEquals(4321L, probeOnly.preparation().probeDurationMillis());
        assertEquals(600_000L, probeOnly.preparation().pilotDurationMillis());
        var summary = AutoLlmSummaryUtil.summarize(config, null, Map.of(), 1);
        assertEquals(600_000L, summary.get("probeMillisPerTrial"));
        assertEquals(1234L, summary.get("pilotMillisPerTrial"));
    }

    @ParameterizedTest
    @CsvSource({"0,1000", "-1,1000", "1000,0", "1000,-1"})
    void rejectsNonpositiveProbeOrPilotBudgets(long probeMillis, long pilotMillis) {
        var config = new AutoLlmConfig(null, temp.toString(), null, null, null, null, null, null, null,
                null, new AutoLlmConfig.Preparation(probeMillis, pilotMillis, 0), null, null, null);
        var service = new AutoLlmCampaignService(config, new AutoLlmCampaignStore(config), mock(AutoLlmProcessRunner.class),
                mock(CodexTaskRunner.class), mock(AutoLlmReportService.class), new InstanceConfiguration());
        try {
            assertTrue(service.validate(config).contains("Budgets and repetitions must be positive"));
        } finally {
            service.close();
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1000, 2000})
    void successfulSlowProbesLeaveTheFullPilotBudget(long probeMillis) throws Exception {
        long pilotMillis = 50;
        long startupMillis = 1000;
        Path root = temp.resolve("run");
        Path baseline = root.resolve("baseline/source/target/project.jar");
        Files.createDirectories(baseline.getParent());
        try (var jar = new JarOutputStream(Files.newOutputStream(baseline))) {
            // Dependency-free fixture artifact; all process behavior is controlled below.
        }
        Path source = root.resolve("trials/001/source");
        Files.createDirectories(source.resolve("target"));
        Files.copy(baseline, source.resolve("target/project.jar"));
        AutoLlmFileUtil.write(root.resolve("trials/001/clone/clone-completed.json"), Map.of("commit", "fixed"));
        AutoLlmFileUtil.write(root.resolve("control/frozen-hashes.json"), Map.of());

        var config = new AutoLlmConfig(new AutoLlmConfig.Project(root.resolve("baseline/source").toString(), "fixed",
                List.of("fixture-build"), "target/project.jar", null), root.toString(), 1, null, null,
                root.resolve("control/sota.json").toString(), root.resolve("control/seeds.json").toString(),
                root.resolve("training.index").toString(), root.resolve("testing.index").toString(), null,
                new AutoLlmConfig.Preparation(probeMillis, pilotMillis, 0), null, null,
                new AutoLlmConfig.Execution(1, 50L, null, null, startupMillis, null, 1000L, null, null, null, null));
        var campaign = new AutoLlmCampaign();
        campaign.id = "fixture";
        campaign.config = config;
        campaign.baselineCommit = "fixed";
        var trial = new AutoLlmCampaign.Trial();
        trial.number = 1;
        trial.nextStep = "VERIFY";
        campaign.trials.add(trial);
        var observedProbeTimeout = new AtomicLong(-1);
        var observedPilotBudget = new AtomicLong(-1);
        var processes = new AutoLlmProcessRunner() {
            @Override
            public Result run(List<String> command, Path cwd, Path output, long timeout, long planned, long grace,
                              BooleanSupplier cancelled, boolean watch, long startup) {
                try {
                    Path execution = output.resolve("execution");
                    if (command.contains("--autollm-check")) {
                        AutoLlmFileUtil.write(execution.resolve("components.json"), List.of("Dummy"));
                        AutoLlmFileUtil.write(execution.resolve("search-space.json"), Map.of("parameters", 1));
                    }
                    if (command.contains("--autollm-probe")) {
                        observedProbeTimeout.set(timeout);
                        assertEquals(0, planned, "Probes must finish successfully, rather than use the pilot stop protocol");
                        // Includes execution/collection delay longer than the entire pilot window.
                        Thread.sleep(100);
                    }
                    if (command.contains("--autoconfig") && planned > 0) {
                        observedPilotBudget.set(planned);
                        assertEquals(startupMillis + pilotMillis + 1000 + grace, timeout);
                        AutoLlmFileUtil.append(execution.resolve("events.jsonl"),
                                Map.of("kind", "evaluation-result", "state", "SUCCEEDED"));
                    }
                    if (command.contains("--autollm-export")) {
                        AutoLlmFileUtil.write(execution.resolve("winner.json"), Map.of("$component", "Dummy"));
                    }
                    AutoLlmFileUtil.write(execution.resolve("terminal.json"), Map.of("outcome", "SUCCEEDED"));
                    return new Result(0, false, 0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
        };
        var listener = new AutoLlmTrialRunner.Listener() {
            public void state(AutoLlmCampaign snapshot) {}
            public void event(String kind, Object detail) {}
        };
        new AutoLlmTrialRunner(campaign, root, processes, mock(CodexTaskRunner.class), listener,
                () -> false, new Semaphore(1)).execute();

        assertEquals("SUCCEEDED", trial.status, trial.failure);
        assertEquals(probeMillis + startupMillis, observedProbeTimeout.get());
        assertEquals(pilotMillis, observedPilotBudget.get());
        assertNotNull(trial.preparedArtifact);
    }
}
