package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.controller.IraceUtil;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;
import es.urjc.etsii.grafo.autoconfig.controller.dto.MultiExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.controller.dto.SingleExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializerListener;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator.DEFAULT_IRACE_EXPERIMENTS;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class IraceOrchestratorTest {
    @Test
    @SuppressWarnings("unchecked")
    void followerModeDoesNotPublishTerminalEventsOrRequestShutdown() {
        var eventPublisher = mock(MorkEventPublisher.class);
        var lifecycle = mock(ExecutionLifecycleCoordinator.class);
        var resultsSerializer = (ResultsSerializerListener<TestSolution, TestInstance>) mock(ResultsSerializerListener.class);
        var orchestrator = new IraceOrchestrator<>(
                new SolverConfig(),
                new BlockConfig(),
                mock(IraceConfig.class),
                new ServerProperties(),
                new InstanceConfiguration(),
                mock(IraceIntegration.class),
                (InstanceManager<TestInstance>) mock(InstanceManager.class),
                List.of((SolutionBuilder<TestSolution, TestInstance>) mock(SolutionBuilder.class)),
                List.of((AlgorithmBuilder<TestSolution, TestInstance>) mock(AlgorithmBuilder.class)),
                Optional.empty(),
                Optional.empty(),
                mock(es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator.class),
                eventPublisher,
                lifecycle,
                resultsSerializer
        );

        orchestrator.run("--follower");

        verifyNoInteractions(eventPublisher, resultsSerializer);
        verify(lifecycle, never()).complete(anyLong());
    }

    @Test
    void testNParallel() {
        var config = new SolverConfig();
        config.setParallelExecutor(true);
        config.setnWorkers(8);
        Assertions.assertEquals("8", IraceOrchestrator.nParallel(config));
        config.setnWorkers(-1);
        Assertions.assertTrue(Integer.parseInt(IraceOrchestrator.nParallel(config)) > 0);
        config.setParallelExecutor(false);
        Assertions.assertEquals("1", IraceOrchestrator.nParallel(config));
    }

    @Test
    void testCalculateMaxExperiments() {
        var config = new SolverConfig();
        Assertions.assertEquals(String.valueOf(DEFAULT_IRACE_EXPERIMENTS), IraceOrchestrator.calculateMaxExperiments(false, config, -1));
        config.setExperimentsPerParameter(10);
        Assertions.assertThrows(IllegalArgumentException.class, () -> IraceOrchestrator.calculateMaxExperiments(true, config, -1));
        Assertions.assertEquals("100000", IraceOrchestrator.calculateMaxExperiments(true, config, 10_000));
        Assertions.assertEquals(String.valueOf(config.getMinimumNumberOfExperiments()), IraceOrchestrator.calculateMaxExperiments(true, config, 10));
    }

    @Test
    void testToIraceRuntimeConfig() {
        var exampleCmdLine = new String[]{
                "testConfig1",
                "2",
                "1234567",
                "instances/benchmark/40-02.txt",
                "ROOT=VNS",
                "ROOT_VNS.constructive=DRFPRandomConstructive",
                "ROOT_VNS.improver=NullImprover",
                "ROOT_VNS.maxK=429922341",
                "ROOT_VNS.shake=DestroyRebuild",
                "ROOT_VNS.shake_DestroyRebuild.constructive=DRFPRandomConstructive",
                "ROOT_VNS.shake_DestroyRebuild.destructive=NullDestructive"
        };
        var parsedConfig = IraceUtil.toIraceRuntimeConfig(exampleCmdLine);
        Assertions.assertEquals("testConfig1", parsedConfig.getCandidateConfiguration());
        Assertions.assertEquals("2", parsedConfig.getInstanceId());
        Assertions.assertEquals(1234567, parsedConfig.getSeed());
        Assertions.assertEquals("instances/benchmark/40-02.txt", parsedConfig.getInstanceName());
        var algConfig = parsedConfig.getAlgorithmConfig();
        Assertions.assertEquals(7, algConfig.getConfig().size());
        Assertions.assertEquals("DRFPRandomConstructive", algConfig.getValue("ROOT_VNS.shake_DestroyRebuild.constructive", "fail"));
    }

    @Test
    void singleRequest() {
        var correctKey = "SuperSikretPassword";
        var incorrectKey = "Password123";
        var singleReq = new SingleExecuteRequest(correctKey, IraceExecuteConfig.of(
                "testConfig1",
                2,
                "/Users/rmartin/IdeaProjects/DRFP/instances/benchmark/40-02.txt",
                1234567,
                Map.of("ROOT", "VNS",
                        "ROOT_VNS.constructive", "DRFPRandomConstructive",
                        "ROOT_VNS.improver", "NullImprover",
                        "ROOT_VNS.maxK", "429922341",
                        "ROOT_VNS.shake", "DestroyRebuild",
                        "ROOT_VNS.shake_DestroyRebuild.constructive", "DRFPRandomConstructive",
                        "ROOT_VNS.shake_DestroyRebuild.destructive", "NullDestructive"
                )
        ));
        Assertions.assertThrows(IllegalArgumentException.class, () -> singleReq.checkValid(incorrectKey));
        Assertions.assertDoesNotThrow(() -> singleReq.checkValid(correctKey));
    }

    @Test
    void multiRequest() {
        var correctKey = "SuperSikretPassword";
        var incorrectKey = "Password123";

        var multiReq = new MultiExecuteRequest(correctKey, List.of(IraceExecuteConfig.of(
                "testConfig1",
                2,
                "/Users/rmartin/IdeaProjects/DRFP/instances/benchmark/40-02.txt",
                1234567,
                Map.of("ROOT", "VNS",
                        "ROOT_VNS.constructive", "DRFPRandomConstructive",
                        "ROOT_VNS.improver", "NullImprover",
                        "ROOT_VNS.maxK", "429922341",
                        "ROOT_VNS.shake", "DestroyRebuild",
                        "ROOT_VNS.shake_DestroyRebuild.constructive", "DRFPRandomConstructive",
                        "ROOT_VNS.shake_DestroyRebuild.destructive", "NullDestructive"
                )
        )));
        Assertions.assertThrows(IllegalArgumentException.class, () -> multiReq.checkValid(incorrectKey));
        Assertions.assertDoesNotThrow(() -> multiReq.checkValid(correctKey));
    }


    @Test
    void testFailedResult() {
        var result = new ExecuteResponse().toIraceResultString().split("\\s+");
        Assertions.assertEquals(2, result.length); // Score, time
        Assertions.assertDoesNotThrow(() -> Double.parseDouble(result[1])); // Time is parseable as double
        Assertions.assertEquals("Inf", result[0]); // Score as "Inf" following the Irace manual
    }
}
