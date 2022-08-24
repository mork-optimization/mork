package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.create.grasp.GRASPConstructive;
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
    void componentDoesNotExist(){
        String component = """
        DoesNotExist{}
        """;
        Assertions.assertThrows(AlgorithmParsingException.class, () -> algComponent.buildAlgorithmComponentFromString(component));
    }

    @Test
    public void trickyNulls() {
        String alg = """
        SimpleAlgorithm{
            constructive=FakeGRASPConstructive{
                alpha=0.5,
                maximizing=false,
                candidateListManager=NullGraspListManager{}
            },
            improvers=null
        }
        """;
        var algorithm = algComponent.buildAlgorithmFromString(alg);
        Assertions.assertNotNull(algorithm);
    }

    @Test
    public void failNullInPrimitive() {
        String alg = """
        SimpleAlgorithm{
            constructive=FakeGRASPConstructive{
                alpha=null,
                maximizing=false,
                candidateListManager=NullGraspListManager{}
            },
            improvers=null
        }
        """;
        Assertions.assertThrows(AlgorithmParsingException.class, () -> algComponent.buildAlgorithmFromString(alg));
    }

    @Test
    public void duplicatedNames(){
        // Fail because component is known
        Assertions.assertThrows(IllegalArgumentException.class, () -> algComponent.registerAlias("AlgorithmA", "Any"));

        algComponent.registerAlias("A", "AlgorithmA");
        algComponent.registerFactory("B", params -> params);

        // Fail because alias does not point to a valid component
        Assertions.assertThrows(IllegalArgumentException.class, () -> algComponent.registerAlias("C", "doesNotExist123"));

        // Fail because target is already an alias
        Assertions.assertThrows(IllegalArgumentException.class, () -> algComponent.registerAlias("B", "A"));

        // Fail because factory name is already used
        Assertions.assertThrows(IllegalArgumentException.class, () -> algComponent.registerFactory("A", params -> params));
        Assertions.assertThrows(IllegalArgumentException.class, () -> algComponent.registerFactory("B", params -> params));
    }

    @Test
    public void usingOnlyFactory(){
        String alg = """
        GraspConstructive{
            alpha=0.2,
            maximizing=false,
            candidateListManager=NullGraspListManager{},
            strategy="greedyRandom"
        }
        """;
        var component = algComponent.buildAlgorithmComponentFromString(alg);
        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }

    @Test
    public void failBecauseNotAlgorithm(){
        String alg = """
        GraspConstructive{
            alpha=0.2,
            maximizing=false,
            candidateListManager=NullGraspListManager{},
            strategy="greedyRandom"
        }
        """;
        var component =
        Assertions.assertThrows(AlgorithmParsingException.class, () -> algComponent.buildAlgorithmFromString(alg));
    }

    @Test
    public void usingFactoryAndAliasAlphaValue(){
        String alg = """
        GRASP{
            alpha=0.2,
            maximizing=false,
            candidateListManager=NullGraspListManager{},
            strategy="greedyRandom"
        }
        """;
        var component = algComponent.buildAlgorithmComponentFromString(alg);
        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }

    @Test
    public void usingFactoryAndAliasAlphaRange(){
        String alg = """
        GRASP{
            minAlpha=0.2,
            maxAlpha=0.4,
            maximizing=false,
            candidateListManager=NullGraspListManager{},
            strategy="greedyRandom"
        }
        """;
        var component = algComponent.buildAlgorithmComponentFromString(alg);
        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }

    @Test
    public void failUsingAliasInvalidStrategy(){
        String alg = """
        GRASP{
            alpha=0.2,
            maximizing=false,
            candidateListManager=NullGraspListManager{},
            strategy="doesNotExist"
        }
        """;
        Assertions.assertThrows(AlgorithmParsingException.class, () -> algComponent.buildAlgorithmComponentFromString(alg));
    }

    @Test
    public void failUsingAliasMissingStrategy(){
        String alg = """
        GRASP{
            alpha=0.2,
            maximizing=false,
            candidateListManager=NullGraspListManager{}
        }
        """;
        Assertions.assertThrows(AlgorithmParsingException.class, () -> algComponent.buildAlgorithmComponentFromString(alg));
    }

}
