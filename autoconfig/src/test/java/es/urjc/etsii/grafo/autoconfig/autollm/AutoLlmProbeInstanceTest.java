package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpecJsonCodec;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.irace.*;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceImporter;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.ExecutionJournalUtil;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AutoLlmProbeInstanceTest {
    @TempDir Path temp;

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void probesUseTheSharedReferenceInsteadOfTheFirstTrainingInstance(boolean explicit) throws Exception {
        Path slow = Files.writeString(temp.resolve("A-slow.txt"), "slow");
        Path fast = Files.writeString(temp.resolve("Z-fast.txt"), "fast");
        Path index = Files.writeString(temp.resolve("training.index"), slow.getFileName() + "\n" + fast.getFileName());
        var paths = new LinkedHashMap<>(Map.of("irace", index.toString(), "autollm-comparison", index.toString()));
        if (explicit) paths.put("reference", fast.toString());
        var inputs = new InstanceConfiguration();
        inputs.setPath(paths);
        inputs.setPreload(false);
        var importer = (InstanceImporter<TestInstance>) mock(InstanceImporter.class);
        when(importer.importInstance(slow.toString())).thenReturn(new TestInstance("slow"));
        when(importer.importInstance(fast.toString())).thenReturn(new TestInstance("fast"));
        var instances = new InstanceManager<>(inputs, importer);
        if (!explicit) instances.getInstance(fast.toString()).setProperty(Instance.LOAD_TIME_NANOS, 0L);

        var solver = new SolverConfig();
        var generator = mock(AlgorithmCandidateGenerator.class);
        when(generator.buildTree(solver.getTreeDepth(), solver.getMaxDerivationRepetition()))
                .thenReturn(List.of(new TreeNode("ROOT", ProbeAlgorithm.class)));
        when(generator.componentParams()).thenReturn(Map.of(ProbeAlgorithm.class, List.of()));
        when(generator.toIraceParameterSpace(anyList())).thenReturn(new IraceParameterSpace(List.of("ROOT"), List.of()));
        var space = new AutoconfigSearchSpace(solver, generator);
        Path seeds = Files.writeString(temp.resolve("seeds.json"),
                "[" + new ComponentSpecJsonCodec().toJson(new ComponentSpec("ProbeAlgorithm")) + "]");
        var irace = new IraceConfig();
        irace.setInitialConfigurationsFile(seeds.toString());
        Path baseline = temp.resolve("baseline.json");
        AutoLlmFileUtil.write(baseline, List.of());
        Path config = temp.resolve("config.json");
        AutoLlmFileUtil.write(config, AutoLlmConfig.defaults());
        var environment = new MockEnvironment()
                .withProperty("autollm.child.config-file", config.toString())
                .withProperty("autollm.child.baseline-components-file", baseline.toString())
                .withProperty("advanced.scan-pkgs", "no.probe.components");
        var evaluator = (IraceTargetEvaluator<TestSolution, TestInstance>) mock(IraceTargetEvaluator.class);
        when(evaluator.preflightBatch(anyList())).thenAnswer(invocation -> {
            List<IraceExecuteConfig> requests = invocation.getArgument(0);
            assertEquals(fast.toString(), requests.getFirst().getInstance());
            return List.of(new ExecuteResponse(1.0, 0.01));
        });
        var orchestrator = new AutoLlmCheckOrchestrator<>(environment, space, mock(AutomaticAlgorithmBuilder.class),
                instances, evaluator, mock(AutoconfigRunState.class), solver, new BlockConfig(), irace,
                Optional.of(mock(TimeLimitCalculator.class)), Optional.of(mock(SolutionValidator.class)), mock(ExecutionLifecycleCoordinator.class));

        String previous = System.getProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY);
        System.setProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY, temp.resolve("execution").toString());
        try {
            Context.Configurator.setObjectives(Objective.of("Test", FMode.MINIMIZE,
                    TestSolution::getScore, TestMove::getScoreChange));
            orchestrator.run("--autollm-probe");
            verify(evaluator).preflightBatch(anyList());
            assertEquals(Map.of("instance", fast.toString()),
                    AutoLlmFileUtil.read(temp.resolve("execution/probe-instance.json"), Map.class));
            if (explicit) verifyNoInteractions(importer);
            else {
                verify(importer).importInstance(slow.toString());
                verify(importer).importInstance(fast.toString());
                verifyNoMoreInteractions(importer);
            }
        } finally {
            Context.reset();
            if (previous == null) System.clearProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY);
            else System.setProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY, previous);
        }
    }

    private static final class ProbeAlgorithm {}
}
