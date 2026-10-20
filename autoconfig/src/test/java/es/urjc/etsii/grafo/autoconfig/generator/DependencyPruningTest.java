package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.autoconfig.builder.*;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.InitialConfigurationUtil;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DependencyPruningTest {
    @Test
    void missingDependenciesPruneFactoriesConstructorsAndParentsButKeepOptionalCollections() {
        var inventory = new FixtureInventory(List.of(Healthy.class, Implicit.class, Explicit.class, FactoryRoot.class,
                Parent.class, OptionalList.class, OptionalArray.class, RequiredList.class),
                Map.of(Implicit.class, List.of(Implicit.class)));
        var generator = new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter());
        var roots = generator.buildTree(4, 2);
        assertEquals(Set.of("Healthy", "OptionalList", "OptionalArray"), names(roots));
        for (var root : roots) for (var combination : root.combinations().values()) {
            assertEquals(0, combination.min());
            assertEquals(0, combination.max());
            assertTrue(combination.candidates().isEmpty());
        }
        var space = new AutoconfigSearchSpace(new SolverConfig(), generator);
        var builder = new AutomaticAlgorithmBuilder<TestSolution, TestInstance>(space, new AlgorithmBuilderService(inventory));
        assertInstanceOf(OptionalList.class, builder.buildFromConfig(new AlgorithmConfiguration(Map.of("ROOT", "OptionalList"))));
        assertInstanceOf(OptionalArray.class, builder.buildFromConfig(new AlgorithmConfiguration(Map.of("ROOT", "OptionalArray"))));
        assertDoesNotThrow(() -> InitialConfigurationUtil.toIraceTable(space, List.of(new ComponentSpec("Healthy"))));
        assertThrows(IllegalArgumentException.class, () -> InitialConfigurationUtil.toIraceTable(space,
                List.of(new ComponentSpec("Healthy"), new ComponentSpec("Explicit"))));
        for (var parameter : space.iraceParameters()) {
            assertFalse(parameter.contains("FactoryRoot"));
            assertFalse(parameter.contains("RequiredList"));
        }
    }

    @Test
    void collectionMinimumIsCheckedAfterRecursivePruning() {
        for (var candidates : List.of(List.<Class<?>>of(Leaf.class), List.<Class<?>>of(Leaf.class, BrokenLeaf.class))) {
            var inventory = new FixtureInventory(List.of(Healthy.class, RequiredList.class, OptionalList.class),
                    Map.of(Dependency.class, candidates));
            var roots = new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter()).buildTree(4, 2);
            assertEquals(Set.of("Healthy", "OptionalList"), names(roots));
            for (var root : roots) if (root.clazz() == OptionalList.class) {
                var combination = root.combinations().get("dependencies");
                assertEquals(1, combination.max());
                assertEquals(Set.of("Leaf"), names(combination.candidates()));
            }
        }
    }

    @Test
    void discoveryRetainsUnannotatedParentTypesOfExcludedImplementations() {
        var inventory = new AlgorithmInventoryService(c -> c == Healthy.class, List.of(), List.of());
        inventory.runComponentDiscovery(getClass().getPackageName());
        assertTrue(inventory.getInventory().componentsByType().containsKey(UnmarkedParent.class));
        assertTrue(inventory.getInventory().componentsByType().get(UnmarkedParent.class).isEmpty());
        assertFalse(inventory.getInventory().componentByName().containsKey("MarkedChild"));
        assertFalse(inventory.getInventory().allComponents().contains(MarkedChild.class));
    }

    @Test
    void absentAlgorithmInventoryProducesAnEmptySpace() {
        var inventory = new AlgorithmInventoryService(c -> true, List.of(), List.of());
        var space = new AutoconfigSearchSpace(new SolverConfig(), new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter()));
        assertTrue(space.roots().isEmpty());
        assertTrue(space.iraceParameters().isEmpty());
        assertEquals(BigInteger.ZERO, space.snapshot().encodingDiagnostics().total().declarations());
    }

    @Test
    void emptyAutomaticSpaceStillAllowsManualJsonConstruction() {
        var inventory = new FixtureInventory(List.of(Manual.class), Map.of());
        var space = new AutoconfigSearchSpace(new SolverConfig(), new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter()));
        assertTrue(space.roots().isEmpty());
        assertTrue(space.iraceParameters().isEmpty());
        assertEquals(0, space.snapshot().summary().generatedIraceParameterCount());
        var builder = new AutomaticAlgorithmBuilder<TestSolution, TestInstance>(space, new AlgorithmBuilderService(inventory));
        assertInstanceOf(Manual.class, builder.buildFromJson("{\"$component\":\"Manual\",\"iterations\":1}"));
    }

    private static Set<String> names(List<TreeNode> roots) {
        var names = new HashSet<String>();
        for (var root : roots) names.add(root.className());
        return names;
    }

    private static class FixtureInventory extends AlgorithmInventoryService {
        FixtureInventory(List<Class<?>> roots, Map<Class<?>, Collection<Class<?>>> dependencies) {
            super(c -> true, List.of(new Factory()), List.of());
            componentsByType.put(Algorithm.class, roots);
            componentsByType.putAll(dependencies);
            for (var root : roots) componentByName.put(root.getSimpleName(), root);
            for (var candidates : dependencies.values()) for (var candidate : candidates) componentByName.put(candidate.getSimpleName(), candidate);
        }
    }

    @AlgorithmComponent public abstract static class Dependency {}
    @AlgorithmComponent public abstract static class MissingDependency {}
    public static class Leaf extends Dependency { @AutoconfigConstructor public Leaf() {} }
    public static class BrokenLeaf extends Dependency { @AutoconfigConstructor public BrokenLeaf(MissingDependency missing) {} }
    public abstract static class UnmarkedParent {}
    @AlgorithmComponent public static class MarkedChild extends UnmarkedParent {}
    public abstract static class Base extends Algorithm<TestSolution, TestInstance> {
        protected Base() { super("test"); }
        @Override public TestSolution algorithm(TestInstance instance) { return null; }
    }
    public static class Healthy extends Base { @AutoconfigConstructor public Healthy() {} }
    public static class Implicit extends Base { @AutoconfigConstructor public Implicit(Dependency dependency) {} }
    public static class Explicit extends Base { @AutoconfigConstructor public Explicit(@ComponentParam Dependency dependency) {} }
    public static class Parent extends Base { @AutoconfigConstructor public Parent(Implicit child) {} }
    public static class OptionalList extends Base {
        @AutoconfigConstructor public OptionalList(@ComponentParam(min = 0, max = 2) List<Dependency> dependencies) {}
    }
    public static class OptionalArray extends Base {
        @AutoconfigConstructor public OptionalArray(@ComponentParam(min = 0, max = 2) Dependency[] dependencies) {}
    }
    public static class RequiredList extends Base {
        @AutoconfigConstructor public RequiredList(@ComponentParam(min = 2, max = 3) List<Dependency> dependencies) {}
    }
    public static class Manual extends Base { public Manual(int iterations) {} }
    public static class FactoryRoot extends Base {}
    public static class Factory extends AlgorithmComponentFactory {
        @Override public Object buildComponent(Map<String, Object> parameters) { throw new UnsupportedOperationException(); }
        @Override public Class<?> produces() { return FactoryRoot.class; }
        @Override public List<ComponentParameter> getRequiredParameters() {
            return List.of(ComponentParameter.from("dependency", Dependency.class, List.of()));
        }
    }
}
