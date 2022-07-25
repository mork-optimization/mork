package es.urjc.etsii.grafo.autoconfig.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class AlgComponentServiceTest {
// todo instance importer for integration tests

    AlgComponentService algComponent;

    @BeforeEach
    void initialize(){
        this.algComponent = new AlgComponentService();
        algComponent.runComponentDiscovery("es.urjc.etsii");
    }

    @Test
    public void buildGrasp() {
        String alg = "SimpleAlgorithm{constructive=RandomGreedyGRASPConstructive{alpha=0.5, maximizing=false, candidateListManager=NullGraspListManager{}}}";
        var algorithm = algComponent.buildFromString(alg);
    }

}
