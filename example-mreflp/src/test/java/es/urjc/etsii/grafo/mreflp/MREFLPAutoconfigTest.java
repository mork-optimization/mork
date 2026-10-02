package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.DefaultExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.mreflp.alg.LMLS;
import es.urjc.etsii.grafo.mreflp.alg.LMLSVariant;
import es.urjc.etsii.grafo.mreflp.create.MREFLPConstructive;
import es.urjc.etsii.grafo.mreflp.improve.OneMoveTabuSearch;
import es.urjc.etsii.grafo.mreflp.improve.SwapDescent;
import es.urjc.etsii.grafo.mreflp.model.MREFLPInstance;
import es.urjc.etsii.grafo.mreflp.model.MREFLPSolution;
import es.urjc.etsii.grafo.mreflp.model.MREFLPSolutionValidator;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;

import static es.urjc.etsii.grafo.mreflp.MREFLPTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class MREFLPAutoconfigTest {
    private AlgorithmCandidateGenerator generator;
    private AlgorithmBuilderService builder;
    private AutomaticAlgorithmBuilder<MREFLPSolution, MREFLPInstance> automatic;

    @BeforeEach void setup() {
        context(1234);
        var components = Set.of(LMLS.class, SimpleAlgorithm.class, MREFLPConstructive.class,
                OneMoveTabuSearch.class, SwapDescent.class);
        var inventory = new AlgorithmInventoryService(components::contains, List.of(), List.of(new AlgorithmNameParam()));
        inventory.runComponentDiscovery("es.urjc.etsii.grafo.mreflp,es.urjc.etsii.grafo.algorithms");
        generator = new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter());
        builder = new AlgorithmBuilderService(inventory);
        automatic = new AutomaticAlgorithmBuilder<>(new AutoconfigSearchSpace(new SolverConfig(), generator), builder);
    }

    @AfterEach void cleanup() { TimeControl.remove(); }

    @Test void generatedSearchSpaceIncludesEveryComponentAndBoundedScalarParameters() {
        var metadata = generator.componentParams();
        for (var component : List.of(LMLS.class, MREFLPConstructive.class, OneMoveTabuSearch.class, SwapDescent.class)) {
            assertTrue(metadata.containsKey(component), component.getSimpleName());
            for (var parameter : metadata.get(component)) {
                assertNotEquals(ParameterType.PROVIDED, parameter.getType());
                assertFalse(parameter.recursive(), "Runtime learning state must not be a constructor dependency");
            }
        }
        String parameters = String.join("\n", generator.toIraceParameterSpace(generator.buildTree(4, 1)).parameters());
        assertTrue(parameters.contains("ROOT_LMLS.epsilon"));
        assertTrue(parameters.contains("(0.0, 1.0)"));
        assertTrue(parameters.contains("(0.01, 0.99)"));
        assertTrue(parameters.contains("(0, 1000)"));
        for (var component : List.of(LMLS.class, OneMoveTabuSearch.class)) {
            for (var parameter : metadata.get(component)) {
                if (parameter.getName().equals("maxIter")) {
                    assertEquals(ParameterType.INTEGER, parameter.getType());
                    assertArrayEquals(new Object[]{1, 1000}, parameter.getValues());
                } else if (parameter.getName().equals("tenure")) {
                    assertEquals(ParameterType.INTEGER, parameter.getType());
                    assertArrayEquals(new Object[]{1, 100}, parameter.getValues());
                }
            }
        }
        assertTrue(parameters.contains("ROOT_SimpleAlgorithm.constructive_MREFLPConstructive.variant"));
        assertTrue(parameters.contains("ROOT_SimpleAlgorithm.improver_OneMoveTabuSearch.cached"));
        assertTrue(parameters.contains("ROOT_SimpleAlgorithm.improver_SwapDescent.cached"));
        assertFalse(parameters.contains("LearningMatrix"));
    }

    @Test void allLmlsVariantsBuildFromIraceAndJsonAndMatchManualPaperConfiguration() {
        var i = instance(10, 3, 6, 31);
        for (var variant : LMLSVariant.values()) {
            String description = "ROOT=LMLS ROOT_LMLS.variant=" + variant.name()
                    + " ROOT_LMLS.epsilon=0.6 ROOT_LMLS.maxIter=20 ROOT_LMLS.tenure=3"
                    + " ROOT_LMLS.alpha=0.1 ROOT_LMLS.beta=0.2 ROOT_LMLS.gamma=0.3"
                    + " ROOT_LMLS.rho=0.3 ROOT_LMLS.maxRestarts=4";
            var configured = assertInstanceOf(LMLS.class, automatic.buildFromStringParams(description));
            var json = builder.toJson(automatic.asComponentSpec(
                    new AlgorithmConfiguration(description.split("\\s+"))));
            var rebuilt = assertInstanceOf(LMLS.class, automatic.buildFromJson(json));
            context(83);
            var expected = algorithm(variant, 4).algorithm(i);
            for (var actualAlgorithm : List.of(configured, rebuilt)) {
                context(83);
                var actual = withBuilder(actualAlgorithm).algorithm(i);
                assertEquals(expected.cost(), actual.cost(), variant.name());
                assertArrayEquals(expected.assignments(), actual.assignments(), variant.name());
            }
        }
    }

    @Test @Timeout(30)
    void expandedParameterBoundariesRunFeasiblyForEveryVariant() {
        var i = instance(6, 2, 4, 31);
        for (var variant : LMLSVariant.values()) {
            for (double epsilon : new double[]{0, 1}) {
                for (int maxIter : new int[]{1, 1000}) {
                    for (int tenure : new int[]{1, 100}) {
                        for (int corner = 0; corner < 16; corner++) {
                            double alpha = (corner & 1) == 0 ? .01 : .99;
                            double beta = (corner & 2) == 0 ? .01 : .99;
                            double gamma = (corner & 4) == 0 ? .01 : .99;
                            double rho = (corner & 8) == 0 ? .01 : .99;
                            String description = "ROOT=LMLS ROOT_LMLS.variant=" + variant.name()
                                    + " ROOT_LMLS.epsilon=" + epsilon + " ROOT_LMLS.maxIter=" + maxIter
                                    + " ROOT_LMLS.tenure=" + tenure + " ROOT_LMLS.alpha=" + alpha
                                    + " ROOT_LMLS.beta=" + beta + " ROOT_LMLS.gamma=" + gamma
                                    + " ROOT_LMLS.rho=" + rho + " ROOT_LMLS.maxRestarts=2";
                            context(83);
                            var configured = automatic.buildFromStringParams(description);
                            var actual = withBuilder(configured).algorithm(i);
                            new MREFLPSolutionValidator().validate(actual).throwIfFail();
                            assertEquals(actual.recalculateCost(), actual.cost(), description);
                        }
                    }
                }
            }
        }
    }

    @Test void standaloneConstructiveAndBothEvaluatorsBuildAsSimpleAlgorithms() {
        var i = instance(10, 3, 6, 31);
        for (String variant : List.of("LMLS", "RANDOM", "GREEDY")) {
            for (String improver : List.of("OneMoveTabuSearch", "SwapDescent")) {
                for (boolean cached : new boolean[]{false, true}) {
                    String description = "ROOT=SimpleAlgorithm"
                            + " ROOT_SimpleAlgorithm.constructive=MREFLPConstructive"
                            + " ROOT_SimpleAlgorithm.constructive_MREFLPConstructive.variant=" + variant
                            + " ROOT_SimpleAlgorithm.constructive_MREFLPConstructive.epsilon=0.6"
                            + " ROOT_SimpleAlgorithm.improver=" + improver
                            + " ROOT_SimpleAlgorithm.improver_" + improver + ".cached=" + cached;
                    if (improver.equals("OneMoveTabuSearch")) {
                        description += " ROOT_SimpleAlgorithm.improver_OneMoveTabuSearch.maxIter=20"
                                + " ROOT_SimpleAlgorithm.improver_OneMoveTabuSearch.tenure=3";
                    }
                    context(1234);
                    var configured = automatic.buildFromStringParams(description);
                    assertInstanceOf(SimpleAlgorithm.class, configured);
                    var actual = withBuilder(configured).algorithm(i);
                    new MREFLPSolutionValidator().validate(actual).throwIfFail();
                    assertEquals(actual.recalculateCost(), actual.cost());
                }
            }
        }
    }
}
