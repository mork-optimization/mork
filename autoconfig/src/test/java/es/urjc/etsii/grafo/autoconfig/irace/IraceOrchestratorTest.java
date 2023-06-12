package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.controller.ExecutionController;
import es.urjc.etsii.grafo.autoconfig.controller.IraceUtil;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator.DEFAULT_IRACE_EXPERIMENTS;
import static es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator.MINIMUM_IRACE_EXPERIMENTS;

class IraceOrchestratorTest {
    @Test
    void testNParallel(){
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
    void testCalculateMaxExperiments(){
        var config = new SolverConfig();
        Assertions.assertEquals(String.valueOf(DEFAULT_IRACE_EXPERIMENTS), IraceOrchestrator.calculateMaxExperiments(false, config, -1));
        config.setIterationsPerParameter(10);
        Assertions.assertThrows(IllegalArgumentException.class, () -> IraceOrchestrator.calculateMaxExperiments(true, config, -1));
        Assertions.assertEquals("100000", IraceOrchestrator.calculateMaxExperiments(true, config, 10_000));
        Assertions.assertEquals(String.valueOf(MINIMUM_IRACE_EXPERIMENTS), IraceOrchestrator.calculateMaxExperiments(true, config, 10));
    }

    @Test
    void testToIraceRuntimeConfig(){
        String exampleCmdLine = "testConfig1 2 1234567 /Users/rmartin/IdeaProjects/DRFP/instances/benchmark/40-02.txt   ROOT=VNS ROOT_VNS.constructive=DRFPRandomConstructive ROOT_VNS.improver=NullImprover ROOT_VNS.maxK=429922341 ROOT_VNS.shake=DestroyRebuild ROOT_VNS.shake_DestroyRebuild.constructive=DRFPRandomConstructive ROOT_VNS.shake_DestroyRebuild.destructive=NullDestructive";
        var parsedConfig = IraceUtil.toIraceRuntimeConfig(exampleCmdLine);
        Assertions.assertEquals("testConfig1", parsedConfig.getCandidateConfiguration());
        Assertions.assertEquals("2", parsedConfig.getInstanceId());
        Assertions.assertEquals("1234567", parsedConfig.getSeed());
        Assertions.assertEquals("/Users/rmartin/IdeaProjects/DRFP/instances/benchmark/40-02.txt", parsedConfig.getInstanceName());
        var algConfig = parsedConfig.getAlgorithmConfig();
        Assertions.assertEquals(7, algConfig.getConfig().size());
        Assertions.assertEquals("DRFPRandomConstructive", algConfig.getValue("ROOT_VNS.shake_DestroyRebuild.constructive", "fail"));
    }

    @Test
    void testBuildConfig(){
        var encodedCofig = "dGVzdENvbmZpZzEgMiAxMjM0NTY3IC9Vc2Vycy9ybWFydGluL0lkZWFQcm9qZWN0cy9EUkZQL2luc3RhbmNlcy9iZW5jaG1hcmsvNDAtMDIudHh0ICAgUk9PVD1WTlMgUk9PVF9WTlMuY29uc3RydWN0aXZlPURSRlBSYW5kb21Db25zdHJ1Y3RpdmUgUk9PVF9WTlMuaW1wcm92ZXI9TnVsbEltcHJvdmVyIFJPT1RfVk5TLm1heEs9NDI5OTIyMzQxIFJPT1RfVk5TLnNoYWtlPURlc3Ryb3lSZWJ1aWxkIFJPT1RfVk5TLnNoYWtlX0Rlc3Ryb3lSZWJ1aWxkLmNvbnN0cnVjdGl2ZT1EUkZQUmFuZG9tQ29uc3RydWN0aXZlIFJPT1RfVk5TLnNoYWtlX0Rlc3Ryb3lSZWJ1aWxkLmRlc3RydWN0aXZlPU51bGxEZXN0cnVjdGl2ZQ==";
        var correctKey = "SuperSikretPassword";
        var incorrectKey = "Password123";
        var correctRequest = new ExecuteRequest(correctKey, encodedCofig);
        var incorrectRequest = new ExecuteRequest(incorrectKey, encodedCofig);
        Assertions.assertThrows(IllegalArgumentException.class, () -> ExecutionController.validateAndPrepare(incorrectRequest, correctKey));

        Assertions.assertDoesNotThrow(() -> ExecutionController.validateAndPrepare(correctRequest, correctKey));
    }

    @Test
    void testFailedResult(){
        var result = new ExecuteResponse().toIraceResultString().split("\\s+");
        Assertions.assertEquals(2, result.length); // Score, time
        Assertions.assertDoesNotThrow(() -> Double.parseDouble(result[1])); // Time is parseable as double
        Assertions.assertEquals("Inf", result[0]); // Score as "Inf" following the Irace manual
    }
}