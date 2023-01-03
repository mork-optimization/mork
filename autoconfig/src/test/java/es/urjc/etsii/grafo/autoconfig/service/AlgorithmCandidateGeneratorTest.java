package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.service.filter.DefaultFilterStrategy;
import es.urjc.etsii.grafo.autoconfig.testutil.TestUtil;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class AlgorithmCandidateGeneratorTest {
    @Test
    void basic(){
        var inventoryService = new AlgorithmInventoryService(new DefaultFilterStrategy(), TestUtil.getTestFactories(), TestUtil.getTestProviders());
        inventoryService.runComponentDiscovery("es.urjc.etsii");
        var candidateGenerator = new AlgorithmCandidateGenerator(inventoryService);
        var roots = candidateGenerator.buildTree(4);
        var params = candidateGenerator.toIraceParams(roots);
        Collections.sort(params);
        for(var p: params){
            System.out.println(p);
        }
    }

}