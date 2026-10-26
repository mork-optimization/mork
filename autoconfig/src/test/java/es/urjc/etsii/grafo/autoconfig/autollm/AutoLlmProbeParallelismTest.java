package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpecJsonCodec;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.irace.*;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.metrics.DeclaredObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.ExecutionJournalUtil;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import probefixtures.ProbeNew;
import tools.jackson.core.type.TypeReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmProbeParallelismTest {
    @TempDir Path temp;
    private SolverConfig solver;
    private AutoconfigRunState state;
    private SolutionValidator<TestSolution, TestInstance> validator;
    private AutoLlmCheckOrchestrator<TestSolution, TestInstance> orchestrator;
    private IntConsumer evaluate = score -> {};
    private String previousDirectory;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() throws Exception {
        previousDirectory = System.getProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY);
        System.setProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY, temp.toString());
        Context.Configurator.setObjectives(Objective.of("Test", FMode.MINIMIZE,
                TestSolution::getScore, TestMove::getScoreChange));
        Metrics.disableMetrics();
        Metrics.register("Test", instant -> new DeclaredObjective("Test", FMode.MINIMIZE, instant));
        solver = new SolverConfig();
        solver.setRandomType(RandomType.DEFAULT);
        solver.setParallelExecutor(true);
        solver.setnWorkers(2);
        solver.setAutorestart(false);
        solver.setIgnoreInitialMillis(0);
        solver.setIntervalDurationMillis(1000);
        solver.setLogScaleArea(false);

        var generator = mock(AlgorithmCandidateGenerator.class);
        when(generator.buildTree(solver.getTreeDepth(), solver.getMaxDerivationRepetition()))
                .thenReturn(List.of(new TreeNode("ROOT", SeedAlgorithm.class), new TreeNode("ROOT", ProbeNew.class)));
        when(generator.componentParams()).thenReturn(Map.of(
                SeedAlgorithm.class, List.of(new ComponentParameter("score", int.class, ParameterType.INTEGER, 1, 2)),
                ProbeNew.class, List.of()));
        when(generator.toIraceParameterSpace(anyList()))
                .thenReturn(new IraceParameterSpace(List.of("ROOT", "ROOT_SeedAlgorithm.score"), List.of()));
        var space = new AutoconfigSearchSpace(solver, generator);
        var builder = (AutomaticAlgorithmBuilder<TestSolution, TestInstance>) mock(AutomaticAlgorithmBuilder.class);
        when(builder.buildFromConfig(any())).thenAnswer(invocation -> {
            AlgorithmConfiguration config = invocation.getArgument(0);
            int score = config.getValueAsInt("ROOT_SeedAlgorithm.score", 3);
            return new Algorithm<TestSolution, TestInstance>("probe-" + score) {
                @Override public TestSolution algorithm(TestInstance instance) {
                    var metric = Metrics.get("Test");
                    assertTrue(metric.getValues().isEmpty(), "Each probe needs fresh metrics, including when reusing a worker");
                    evaluate.accept(score);
                    metric.add(metric.getReferenceNanoTime(), score);
                    return new TestSolution(instance, score);
                }
            };
        });
        state = new AutoconfigRunState(builder);
        var instances = (InstanceManager<TestInstance>) mock(InstanceManager.class);
        when(instances.getInstanceSolveOrder(anyString(), eq(false))).thenReturn(List.of("instance.dat"));
        when(instances.getReferenceInstancePath("irace")).thenReturn("instance.dat");
        when(instances.requireConfiguredInstancePath("irace", "instance.dat")).thenReturn("instance.dat");
        when(instances.getInstance("instance.dat")).thenReturn(new TestInstance("instance"));
        validator = (SolutionValidator<TestSolution, TestInstance>) mock(SolutionValidator.class);
        when(validator.validate(any())).thenReturn(ValidationResult.ok());
        var irace = new IraceConfig();
        irace.setRejectExecutionErrors(true);
        var evaluator = new IraceTargetEvaluator<>(solver, irace, instances,
                List.of((SolutionBuilder<TestSolution, TestInstance>) mock(SolutionBuilder.class)),
                List.of(builder), Optional.of(validator), Optional.empty(), state);

        var codec = new ComponentSpecJsonCodec();
        Path seeds = Files.writeString(temp.resolve("seeds.json"), "["
                + codec.toJson(new ComponentSpec("SeedAlgorithm", Map.of("score", 1))) + ","
                + codec.toJson(new ComponentSpec("SeedAlgorithm", Map.of("score", 2))) + "]");
        irace.setInitialConfigurationsFile(seeds.toString());
        Path baseline = temp.resolve("baseline.json");
        AutoLlmFileUtil.write(baseline, List.of());
        Path config = temp.resolve("config.json");
        AutoLlmFileUtil.write(config, AutoLlmConfig.defaults());
        var environment = new MockEnvironment().withProperty("autollm.child.config-file", config.toString())
                .withProperty("autollm.child.baseline-components-file", baseline.toString())
                .withProperty("advanced.scan-pkgs", "probefixtures");
        orchestrator = new AutoLlmCheckOrchestrator<>(environment, space, builder, instances, evaluator, state,
                solver, new BlockConfig(), irace, Optional.of(mock(TimeLimitCalculator.class)),
                Optional.of(validator), mock(ExecutionLifecycleCoordinator.class));
    }

    @AfterEach
    void cleanup() {
        Metrics.disableMetrics();
        Context.reset();
        if (previousDirectory == null) System.clearProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY);
        else System.setProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY, previousDirectory);
    }

    @Test
    void seedsAndComponentsOverlapWithinTheWorkerLimitAndKeepIsolatedState() {
        var firstTwoStarted = new CountDownLatch(2);
        var thirdStarted = new CountDownLatch(1);
        var active = new AtomicInteger();
        var peak = new AtomicInteger();
        var draws = new ConcurrentHashMap<Integer, List<Long>>();
        evaluate = score -> {
            int running = active.incrementAndGet();
            peak.accumulateAndGet(running, Math::max);
            try {
                long first = Context.getRandom().nextLong();
                if (score <= 2) {
                    firstTwoStarted.countDown();
                    await(firstTwoStarted);
                    if (score == 1) await(thirdStarted);
                } else thirdStarted.countDown();
                draws.put(score, List.of(first, Context.getRandom().nextLong()));
            } finally {
                active.decrementAndGet();
            }
        };

        orchestrator.run("--autollm-probe");

        assertEquals(2, peak.get());
        assertEquals(3, draws.size());
        assertEquals(draws.get(1), draws.get(2));
        assertEquals(draws.get(1), draws.get(3), "Worker reuse must reset the RNG for the configured seed");
        assertSuccessfulOutcomes();
        var completed = results();
        assertEquals(3, completed.size());
        assertEquals("seed-1", completed.getFirst().get("configurationId"), "Results must also be journaled before the first request finishes");
        var runningIds = new HashSet<Object>();
        for (var event : AutoLlmFileUtil.events(temp.resolve("events.jsonl"))) {
            if ("evaluation-phase".equals(event.get("kind")) && "RUNNING".equals(event.get("phase"))) {
                runningIds.add(event.get("id"));
                assertTrue(((Number) event.get("deadline")).longValue() > 0);
            }
        }
        assertEquals(3, runningIds.size(), "Each probe must keep its own watchdog identity");
        assertTrue(AutoLlmFileUtil.read(temp.resolve("active.json"), Map.class).isEmpty());
        assertEquals(0, state.status().budget().used());
    }

    @Test
    void singleWorkerTargetsExecuteTheSameCompleteBatchSequentially() {
        solver.setParallelExecutor(false);
        solver.setnWorkers(1);
        orchestrator.run("--autollm-probe");
        assertSuccessfulOutcomes();
        var order = new ArrayList<Object>();
        for (var result : results()) order.add(result.get("configurationId"));
        assertEquals(List.of("seed-0", "seed-1", "ProbeNew"), order);
        assertEquals(0, state.status().budget().used());
    }

    @ParameterizedTest
    @ValueSource(strings = {"seed", "component", "penalty"})
    void parallelResultsPreserveSeedAndComponentFailureRules(String failure) {
        evaluate = score -> {
            if (failure.equals("component") && score == 3) throw new IllegalStateException("Generated probe exception");
        };
        when(validator.validate(any())).thenAnswer(invocation -> {
            TestSolution solution = invocation.getArgument(0);
            boolean invalid = failure.equals("seed") && solution.getScore() == 1
                    || failure.equals("penalty") && solution.getScore() == 3;
            return invalid ? ValidationResult.fail("Fixture infeasibility") : ValidationResult.ok();
        });

        if (failure.equals("penalty")) assertDoesNotThrow(() -> orchestrator.run("--autollm-probe"));
        else {
            var error = assertThrows(AutoLlmFailureUtil.StageException.class, () -> orchestrator.run("--autollm-probe"));
            assertEquals(failure.equals("seed") ? "SEED_PROBE" : "COMPONENT_PROBE", error.failure().code());
            assertEquals(AutoLlmFailureUtil.Category.COMPONENT, error.failure().category());
        }
        var outcomes = outcomes();
        assertEquals(3, outcomes.size(), "Keep every completed seed and component result even when one is rejected");
        assertEquals("Inf 0", outcomes.get(failure.equals("seed") ? "seed-0" : "ProbeNew"));
        assertEquals(3, results().size());
        assertEquals(0, state.status().budget().used());
    }

    private void assertSuccessfulOutcomes() {
        var outcomes = outcomes();
        assertEquals(List.of("seed-0", "seed-1", "ProbeNew"), List.copyOf(outcomes.keySet()));
        assertTrue(outcomes.get("seed-0").startsWith("1000.0 "));
        assertTrue(outcomes.get("seed-1").startsWith("2000.0 "));
        assertTrue(outcomes.get("ProbeNew").startsWith("3000.0 "));
    }

    private Map<String, String> outcomes() {
        Map<String, Map<String, String>> report = AutoLlmFileUtil.read(temp.resolve("probes.json"), new TypeReference<>() {});
        assertTrue(report.get("skipped").isEmpty());
        return report.get("outcomes");
    }

    private List<Map<String, Object>> results() {
        var results = new ArrayList<Map<String, Object>>();
        for (var event : AutoLlmFileUtil.events(temp.resolve("events.jsonl"))) {
            if ("evaluation-result".equals(event.get("kind"))) results.add(event);
        }
        return results;
    }

    private void await(CountDownLatch latch) {
        try { assertTrue(latch.await(5, TimeUnit.SECONDS), "Probe workers did not overlap before the test deadline"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }

    private static final class SeedAlgorithm {}
}
