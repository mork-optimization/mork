package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.autoconfig.generator.*;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.bmssc.create.BMSSCGRASPConstructorNew;
import es.urjc.etsii.grafo.bmssc.create.RandomConstructorNew;
import es.urjc.etsii.grafo.bmssc.create.RegretConstructor;
import es.urjc.etsii.grafo.bmssc.experiment.BMSSCExplorationFilterNew;
import es.urjc.etsii.grafo.bmssc.improve.BestImpLS;
import es.urjc.etsii.grafo.bmssc.improve.FirstImpLS;
import es.urjc.etsii.grafo.bmssc.improve.ShakeImprover;
import es.urjc.etsii.grafo.bmssc.improve.StrategicOscillation;
import es.urjc.etsii.grafo.bmssc.model.sol.*;
import es.urjc.etsii.grafo.bmssc.shake.RandomRemoval;
import es.urjc.etsii.grafo.bmssc.shake.RelatedRemoval;
import es.urjc.etsii.grafo.bmssc.shake.WorstRemoval;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.shake.DestroyRebuild;
import es.urjc.etsii.grafo.shake.RandomMoveShake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static es.urjc.etsii.grafo.bmssc.BMSSCAutoconfigSpaceTestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BMSSCExplorationFilterNewTest {
    private final BMSSCExplorationFilterNew filter = new BMSSCExplorationFilterNew();
    private static final List<Class<?>> SEARCHES = List.of(LocalSearchFirstImprovement.class,
            LocalSearchBestImprovement.class, LocalSearchCachedBestImprovement.class);

    @BeforeEach
    void setup() { BMSSCTestUtil.initialize(1234); }

    @AfterEach
    void teardown() { BMSSCTestUtil.cleanup(); }

    @Test
    void sequencesAllowConfigurableSearchesButNoNestedWrappersOrDuplicateCustomSearches() {
        for (var sequence : List.of(VND.class, Improver.SequentialImprover.class)) {
            var context = new TreeContext(1000, 0);
            context.push(SimpleAlgorithm.class);
            context.push(sequence);
            for (var search : SEARCHES) assertFalse(filter.reject(context, search));
            for (var rejected : List.of(VND.class, Improver.SequentialImprover.class, ShakeImprover.class,
                    Improver.NullImprover.class, FirstImpLS.class, BestImpLS.class)) {
                assertTrue(filter.reject(context, rejected), rejected.getSimpleName());
            }
            // Check ancestors, not just the immediate parent: an intervening wrapper cannot bypass the rule.
            context.push(ShakeImprover.class);
            assertTrue(filter.reject(context, VND.class));
            assertTrue(filter.reject(context, Improver.SequentialImprover.class));
        }
    }

    @Test
    void standaloneComponentsAndShakeAroundAFlatSequenceRemainAvailable() {
        var context = new TreeContext(1000, 0);
        context.push(SimpleAlgorithm.class);
        for (var component : List.of(VND.class, Improver.SequentialImprover.class, ShakeImprover.class,
                FirstImpLS.class, BestImpLS.class, Improver.NullImprover.class)) assertFalse(filter.reject(context, component));
        context.push(ShakeImprover.class);
        assertTrue(filter.reject(context, ShakeImprover.class));
        assertFalse(filter.reject(context, VND.class));
        assertFalse(filter.reject(context, Improver.SequentialImprover.class));
        assertFalse(filter.reject(context, FirstImpLS.class));
        assertFalse(filter.reject(context, BestImpLS.class));
        context.push(VND.class);
        context.push(LocalSearchFirstImprovement.class);
        for (var component : List.of(SwapNeighborhood.class, SwapNeighborhoodNew.class, ShuffledSwapNeighborhood.class,
                ThreeCycleNeighborhood.class, SampledTwoForTwoNeighborhood.class, RandomMoveShake.class,
                DestroyRebuild.class, StrategicOscillation.class, BMSSCGRASPConstructorNew.class,
                RegretConstructor.class, RandomConstructorNew.class, RandomRemoval.class, WorstRemoval.class, RelatedRemoval.class)) {
            assertFalse(filter.reject(context, component), component.getSimpleName());
        }
    }

    @Test
    void completeExportStaysBoundedAndPreservesRootsAndFlatSearchPermutations() {
        var inventory = inventory(true);
        var config = generationConfig();
        var unrestricted = new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter());
        var oldRoots = unrestricted.buildTree(config.getTreeDepth(), config.getMaxDerivationRepetition());
        long oldCount = countParameters(unrestricted, oldRoots);
        assertTrue(oldCount > 1_000_000, "Fixture should reproduce the startup parameter explosion");

        var generator = new AlgorithmCandidateGenerator(inventory, filter);
        var roots = generator.buildTree(config.getTreeDepth(), config.getMaxDerivationRepetition());
        long boundedCount = countParameters(generator, roots);
        // Check before exporting so a regression fails rather than exhausting the test JVM's heap.
        assertTrue(boundedCount < 2000, "Too many generated parameters: " + boundedCount);
        var space = new AutoconfigSearchSpace(config, generator);
        assertEquals(boundedCount, space.iraceParameters().size());
        assertEquals(Set.of("SimpleAlgorithm", "VNS", "IteratedGreedy", "MultistartOnlyBestAppliesLS"), rootNames(roots));
        assertEquals(rootNames(oldRoots), rootNames(roots));
        String completeExport = String.join("\n", space.iraceParameters());
        for (String component : List.of("BMSSCGRASPConstructorNew", "RandomConstructorNew", "RegretConstructor",
                "SwapNeighborhoodNew", "ShuffledSwapNeighborhood", "ThreeCycleNeighborhood", "SampledTwoForTwoNeighborhood",
                "RandomRemoval", "WorstRemoval", "RelatedRemoval", "FirstImpLS", "BestImpLS", "ShakeImprover")) {
            assertTrue(completeExport.contains(component), component);
        }
        for (var root : roots) {
            var improvers = root.children().get("improver");
            for (var improver : improvers) {
                if (improver.clazz() == VND.class || improver.clazz() == Improver.SequentialImprover.class) {
                    var combination = improver.combinations().get("improvers");
                    assertEquals(2, combination.min());
                    assertEquals(3, combination.max());
                    assertSearchPermutations(combination.root(), new HashSet<>());
                }
            }
        }
        System.out.printf("BMSSC complete parameter export: %,d -> %,d%n", oldCount, boundedCount);
    }

    @Test
    void springDiscoversTheFilterAndStartsTheFullSearchSpaceWithOrWithoutBlacklist() {
        for (boolean blacklist : new boolean[]{true, false}) {
            new ApplicationContextRunner()
                    .withUserConfiguration(FilterDiscovery.class, FilterConfig.class, AlgorithmCandidateGenerator.class, AutoconfigSearchSpace.class)
                    .withPropertyValues("advanced.scan-pkgs=" + COMPONENT_PACKAGES)
                    .withBean(SolverConfig.class, BMSSCAutoconfigSpaceTestUtil::generationConfig)
                    .withBean(AlgorithmInventoryService.class, () -> newInventory(blacklist))
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        assertEquals(1, context.getBeansOfType(IExplorationFilter.class).size());
                        assertInstanceOf(BMSSCExplorationFilterNew.class, context.getBean(IExplorationFilter.class));
                        var space = context.getBean(AutoconfigSearchSpace.class);
                        assertEquals(4, space.roots().size());
                        assertTrue(space.iraceParameters().size() < 2000);
                        System.out.printf("BMSSC Spring startup, blacklist=%s: %,d parameters%n", blacklist, space.iraceParameters().size());
                    });
        }
    }

    private void assertSearchPermutations(CombinationNode node, Set<Class<?>> prefix) {
        if (node == null) return;
        assertEquals(SEARCHES.size() - prefix.size(), node.choices().size());
        for (var choice : node.choices()) {
            var clazz = choice.component().clazz();
            assertTrue(SEARCHES.contains(clazz));
            assertTrue(prefix.add(clazz), "Repeated search implementation");
            assertSearchPermutations(choice.next(), prefix);
            prefix.remove(clazz);
        }
    }

    private Set<String> rootNames(List<TreeNode> roots) {
        var names = new HashSet<String>();
        for (var root : roots) names.add(root.className());
        return names;
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = BMSSCExplorationFilterNew.class, useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = es.urjc.etsii.grafo.annotations.InheritedComponent.class),
            excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AbstractExperiment.class))
    static class FilterDiscovery {}
}
