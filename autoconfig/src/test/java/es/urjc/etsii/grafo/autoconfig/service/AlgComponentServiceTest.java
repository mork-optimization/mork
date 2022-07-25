package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.AlgorithmParsingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class AlgComponentServiceTest {

    static AlgComponentService algComponent;

    @BeforeAll
    static void initialize(){
        algComponent = new AlgComponentService();
        algComponent.runComponentDiscovery("es.urjc.etsii");
    }

    @Test
    public void trickyNulls() {
        String alg = """
        SimpleAlgorithm{
            constructive=RandomGreedyGRASPConstructive{
                alpha=0.5,
                maximizing=false,
                candidateListManager=NullGraspListManager{}
            },
            improvers=null
        }
        """;
        var algorithm = algComponent.buildFromString(alg);
        Assertions.assertNotNull(algorithm);
    }

    @Test
    public void failNullInPrimitive() {
        String alg = """
        SimpleAlgorithm{
            constructive=RandomGreedyGRASPConstructive{
                alpha=null,
                maximizing=false,
                candidateListManager=NullGraspListManager{}
            },
            improvers=null
        }
        """;
        Assertions.assertThrows(AlgorithmParsingException.class, () -> algComponent.buildFromString(alg));
    }

}
