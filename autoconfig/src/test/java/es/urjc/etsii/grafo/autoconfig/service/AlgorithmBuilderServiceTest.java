package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.testutil.TestUtil;
import es.urjc.etsii.grafo.create.grasp.GRASPConstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AlgorithmBuilderServiceTest {

    static AlgorithmInventoryService algComponent;
    static AlgorithmBuilderService builderService;

    @BeforeAll
    static void initialize(){
        algComponent = new AlgorithmInventoryService(new DefaultInventoryFilter(), TestUtil.getTestFactories(), TestUtil.getTestProviders());
        algComponent.runComponentDiscovery("es.urjc.etsii");
        builderService = new AlgorithmBuilderService(algComponent);
    }

    @Test
    void componentDoesNotExist(){
        String component = """
        DoesNotExist{}
        """;
        Assertions.assertThrows(AlgorithmParsingException.class, () -> builderService.buildAlgorithmComponentFromString(component));
    }

    @Test
    public void trickyNulls() {
        String alg = """
        SimpleAlgorithm{
            constructive=FakeGRASPConstructive{
                alpha=0.5,
                ofmode="MINIMIZE",
                candidateListManager=NullGraspListManager{}
            },
            improver=null,
            algorithmName="trickyNullAlg"
        }
        """;
        var algorithm = builderService.buildAlgorithmFromString(alg);
        Assertions.assertNotNull(algorithm);
        Assertions.assertEquals("trickyNullAlg", algorithm.getName());
    }

    @Test
    void failNullInPrimitive() {
        String alg = """
        SimpleAlgorithm{
            constructive=FakeGRASPConstructive{
                alpha=null,
                fmode="MINIMIZE",
                candidateListManager=NullGraspListManager{}
            },
            improver=null
        }
        """;
        Assertions.assertThrows(AlgorithmParsingException.class, () -> builderService.buildAlgorithmFromString(alg));
    }

    @Test
    public void duplicatedNames(){
        // Fail because component is known
        Assertions.assertThrows(IllegalArgumentException.class, () -> algComponent.registerAlias("AlgorithmA", "Any"));

        algComponent.registerAlias("A", "TestAlgorithmA");
        var factoryB = new AlgorithmComponentFactory() {
            @Override
            public Object buildComponent(Map<String, Object> params) {
                return params;
            }

            @Override
            public List<ComponentParameter> getRequiredParameters() {
                return new ArrayList<>();
            }

            @Override
            public Class<?> produces() {
                return ImproverB.class;
            }
        };
        algComponent.registerFactory(factoryB);

        // Fail because alias does not point to a valid component
        Assertions.assertThrows(IllegalArgumentException.class, () -> algComponent.registerAlias("C", "doesNotExist123"));

        // Fail because target is already an alias
        Assertions.assertThrows(IllegalArgumentException.class, () -> algComponent.registerAlias("ImproverB", "A"));

        // Fail because factory name is already used
        Assertions.assertThrows(IllegalArgumentException.class, () -> algComponent.registerFactory(factoryB));
    }

    @Test
    void usingOnlyFactory(){
        String alg = """
        GraspConstructive{
            alpha=0.2,
            fmode="MINIMIZE",
            candidateListManager=NullGraspListManager{}
        }
        """;
        var component = builderService.buildAlgorithmComponentFromString(alg);
        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }

    @Test
    void failBecauseNotAlgorithm(){
        String alg = """
        GraspConstructive{
            alpha=0.2,
            fmode="MINIMIZE",
            candidateListManager=NullGraspListManager{}
        }
        """;
        Assertions.assertThrows(AlgorithmParsingException.class, () -> builderService.buildAlgorithmFromString(alg));
    }

    @Test
    void usingFactoryAndAliasAlphaValue(){
        String alg = """
        GRASP{
            alpha=0.2,
            fmode="MINIMIZE",
            candidateListManager=NullGraspListManager{}
        }
        """;
        var component = builderService.buildAlgorithmComponentFromString(alg);
        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }

    @Test
    void usingFactoryAndAliasAlphaRange(){
        String alg = """
        GRASP{
            minAlpha=0.2,
            maxAlpha=0.4,
            fmode="MINIMIZE",
            candidateListManager=NullGraspListManager{}
        }
        """;
        var component = builderService.buildAlgorithmComponentFromString(alg);
        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }

    @Test
    void failUsingAliasInvalidAlpha(){
        String alg = """
        GRASP{
            alpha=-0.9,
            fmode="MINIMIZE",
            candidateListManager=NullGraspListManager{}
        }
        """;
        Assertions.assertThrows(IllegalArgumentException.class, () -> builderService.buildAlgorithmComponentFromString(alg));
    }

    @Test
    void failUsingAliasMissingCL(){
        String alg = """
        GRASP{
            fmode="MINIMIZE"
        }
        """;
        Assertions.assertThrows(NullPointerException.class, () -> builderService.buildAlgorithmComponentFromString(alg));
    }


    private static class ImproverB extends Improver<TestSolution, TestInstance> {
        protected ImproverB(FMode ofMaximize) {
            super(ofMaximize);
        }

        @Override
        protected TestSolution _improve(TestSolution solution) {
            return solution;
        }
    }

}
