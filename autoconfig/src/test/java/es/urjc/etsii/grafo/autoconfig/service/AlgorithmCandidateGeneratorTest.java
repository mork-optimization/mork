package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.testutil.TestUtil;
import org.junit.jupiter.api.Test;

class AlgorithmCandidateGeneratorTest {
    @Test
    void basic(){
        var inventoryService = new AlgorithmInventoryService(TestUtil.getTestFactories());
        inventoryService.runComponentDiscovery("es.urjc.etsii");
        var candidateGenerator = new AlgorithmCandidateGenerator(inventoryService);
        System.out.println(candidateGenerator);
    }

}