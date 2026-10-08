package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.testutil.TestUtil;
import es.urjc.etsii.grafo.create.grasp.GRASPConstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AlgorithmBuilderServiceTest {

    static AlgorithmInventoryService algComponent;
    static AlgorithmBuilderService builderService;
    static final Objective<?,?,?> defaultMin = Objective.of("Test", FMode.MINIMIZE, TestSolution::getScore, TestMove::getScoreChange);
    
    @BeforeAll
    static void initialize(){
        Context.Configurator.setObjectives(defaultMin);
        algComponent = new AlgorithmInventoryService(new DefaultInventoryFilter(), TestUtil.getTestFactories(), TestUtil.getTestProviders());
        algComponent.runComponentDiscovery("es.urjc.etsii");
        builderService = new AlgorithmBuilderService(algComponent);
    }

    @Test
    void componentDoesNotExist(){
        String component = """
        {"$component": "DoesNotExist"}
        """;
        Assertions.assertThrows(AlgorithmParsingException.class, () -> builderService.buildAlgorithmComponentFromJson(component));
    }

    @Test
    public void trickyNulls() {
        String alg = """
        {
            "$component": "SimpleAlgorithm",
            "constructive": {
                "$component": "FakeGRASPConstructive",
                "alpha": 0.5,
                "objective": "%s",
                "candidateListManager": {"$component": "NullGraspListManager"}
            },
            "improver": null,
            "algorithmName": "trickyNullAlg"
        }
        """.formatted(defaultMin.getName());
        var algorithm = builderService.buildAlgorithmFromJson(alg);
        Assertions.assertNotNull(algorithm);
        Assertions.assertEquals("trickyNullAlg", algorithm.getName());
    }

    @Test
    public void sameButMissingObjective() {
        String alg = """
        {
            "$component": "SimpleAlgorithm",
            "constructive": {
                "$component": "FakeGRASPConstructive",
                "alpha": 0.5,
                "objective": "ThisObjectiveDoesNotExist",
                "candidateListManager": {"$component": "NullGraspListManager"}
            },
            "improver": null,
            "algorithmName": "trickyNullAlg"
        }
        """;
        Assertions.assertThrows(IllegalArgumentException.class, () -> builderService.buildAlgorithmFromJson(alg));
    }

    @Test
    void failNullInPrimitive() {
        String alg = """
        {
            "$component": "SimpleAlgorithm",
            "constructive": {
                "$component": "FakeGRASPConstructive",
                "alpha": null,
                "objective": "%s",
                "candidateListManager": {"$component": "NullGraspListManager"}
            },
            "improver": null
        }
        """.formatted(defaultMin.getName());
        Assertions.assertThrows(IllegalArgumentException.class, () -> builderService.buildAlgorithmFromJson(alg));
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
        {
            "$component": "GraspConstructive",
            "alpha": 0.2,
            "objective": "%s",
            "candidateListManager": {"$component": "NullGraspListManager"}
        }
        """.formatted(defaultMin.getName());
        var component = builderService.buildAlgorithmComponentFromJson(alg);
        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }

    @Test
    void failBecauseNotAlgorithm(){
        String alg = """
        {
            "$component": "GraspConstructive",
            "alpha": 0.2,
            "objective": "%s",
            "candidateListManager": {"$component": "NullGraspListManager"}
        }
        """.formatted(defaultMin.getName());
        Assertions.assertThrows(AlgorithmParsingException.class, () -> builderService.buildAlgorithmFromJson(alg));
    }

    @Test
    void failInvalidObjBuilder(){
        String alg = """
        {
            "$component": "GraspConstructive",
            "alpha": 0.2,
            "objective": "Asereje",
            "candidateListManager": {"$component": "NullGraspListManager"}
        }
        """;
        Assertions.assertThrows(IllegalArgumentException.class, () -> builderService.buildAlgorithmFromJson(alg));
    }

    @Test
    void usingFactoryAndAliasAlphaValue(){
        String alg = """
        {
            "$component": "GRASP",
            "alpha": 0.2,
            "objective": "%s",
            "candidateListManager": {"$component": "NullGraspListManager"}
        }
        """.formatted(defaultMin.getName());
        var component = builderService.buildAlgorithmComponentFromJson(alg);
        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }

    @Test
    void usingFactoryAndAliasAlphaRange(){
        String alg = """
        {
            "$component": "GRASP",
            "minAlpha": 0.2,
            "maxAlpha": 0.4,
            "objective": "%s",
            "candidateListManager": {"$component": "NullGraspListManager"}
        }
        """.formatted(defaultMin.getName());
        var component = builderService.buildAlgorithmComponentFromJson(alg);
        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }

    @Test
    void failUsingAliasInvalidAlpha(){
        String alg = """
        {
            "$component": "GRASP",
            "alpha": -0.9,
            "objective": "%s",
            "candidateListManager": {"$component": "NullGraspListManager"}
        }
        """.formatted(defaultMin.getName());
        Assertions.assertThrows(IllegalArgumentException.class, () -> builderService.buildAlgorithmComponentFromJson(alg));
    }

    @Test
    void failUsingAliasMissingCL(){
        String alg = """
        {
            "$component": "GRASP",
            "objective": "%s"
        }
        """.formatted(defaultMin.getName());
        Assertions.assertThrows(NullPointerException.class, () -> builderService.buildAlgorithmComponentFromJson(alg));
    }

    @Test
    void buildsProgrammaticComponentSpec() {
        var component = builderService.buildAlgorithmComponent(new ComponentSpec(
                "GRASP",
                Map.of(
                        "alpha", 0.2,
                        "objective", defaultMin.getName(),
                        "candidateListManager", new ComponentSpec("NullGraspListManager")
                )
        ));

        Assertions.assertTrue(GRASPConstructive.class.isAssignableFrom(component.getClass()));
    }


    private static class ImproverB extends Improver<TestSolution, TestInstance> {
        protected ImproverB(Objective<?, TestSolution, TestInstance> objective) {
            super(objective);
        }

        @Override
        public TestSolution improve(TestSolution solution) {
            return solution;
        }
    }

}
