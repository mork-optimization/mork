package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.controller.IraceUtil;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;
import es.urjc.etsii.grafo.autoconfig.controller.dto.MultiExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.controller.dto.SingleExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator.DEFAULT_IRACE_EXPERIMENTS;

class IraceOrchestratorTest {
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
                "ROOT=VNS ROOT_VNS.constructive=DRFPRandomConstructive",
                "ROOT_VNS.improver=NullImprover ROOT_VNS.maxK=429922341",
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
        Assertions.assertEquals(5, algConfig.getConfig().size());
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