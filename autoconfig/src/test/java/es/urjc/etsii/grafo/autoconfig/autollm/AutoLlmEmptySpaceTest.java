package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.irace.*;
import es.urjc.etsii.grafo.autoconfig.service.*;
import es.urjc.etsii.grafo.config.*;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.ExecutionJournalUtil;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AutoLlmEmptySpaceTest {
    @TempDir Path temp;

    @SuppressWarnings("unchecked")
    @ParameterizedTest @ValueSource(booleans = {false, true})
    void rejectsEmptyAutomaticSpaceAtTheCheckBoundary(boolean candidate) {
        Path config = temp.resolve("config.json");
        AutoLlmFileUtil.write(config, AutoLlmConfig.defaults());
        var environment = new MockEnvironment().withProperty("autollm.child.config-file", config.toString());
        if (candidate) {
            Path baseline = temp.resolve("components.json");
            AutoLlmFileUtil.write(baseline, List.of());
            environment.withProperty("autollm.child.baseline-components-file", baseline.toString());
        }
        var orchestrator = new AutoLlmCheckOrchestrator<TestSolution, TestInstance>(environment, mock(AutoconfigSearchSpace.class),
                mock(AutomaticAlgorithmBuilder.class), mock(InstanceManager.class), mock(IraceTargetEvaluator.class),
                mock(AutoconfigRunState.class), new SolverConfig(), new BlockConfig(), new IraceConfig(), Optional.empty(),
                Optional.of(mock(SolutionValidator.class)), mock(ExecutionLifecycleCoordinator.class));
        String previous = System.getProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY);
        System.setProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY, temp.toString());
        try {
            var error = assertThrows(AutoLlmFailureUtil.StageException.class, () -> orchestrator.run("--autollm-check"));
            assertEquals("EMPTY_SEARCH_SPACE", error.failure().code());
            assertEquals(candidate ? AutoLlmFailureUtil.Category.COMPONENT : AutoLlmFailureUtil.Category.UNKNOWN, error.failure().category());
            assertEquals(error.failure(), AutoLlmFileUtil.read(temp.resolve("failure.json"), AutoLlmFailureUtil.Failure.class));
        } finally {
            if (previous == null) System.clearProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY);
            else System.setProperty(ExecutionJournalUtil.DIRECTORY_PROPERTY, previous);
        }
    }
}
