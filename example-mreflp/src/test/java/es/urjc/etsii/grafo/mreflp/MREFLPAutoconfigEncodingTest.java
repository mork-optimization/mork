package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpecJsonCodec;
import es.urjc.etsii.grafo.autoconfig.factories.GRGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.factories.RGGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.fill.ObjectiveParamProvider;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationTree;
import es.urjc.etsii.grafo.autoconfig.generator.DefaultExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.FilterConfig;
import es.urjc.etsii.grafo.autoconfig.generator.IExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.IInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.InitialConfigurationUtil;
import es.urjc.etsii.grafo.autoconfig.irace.IraceParameterFileUtil;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.mreflp.autoconfig.MREFLPFocusedExplorationFilterNew;
import es.urjc.etsii.grafo.mreflp.autoconfig.MREFLPFocusedInventoryFilterNew;
import es.urjc.etsii.grafo.mreflp.autoconfig.MREFLPInventoryFilterNew;
import es.urjc.etsii.grafo.mreflp.create.MREFLPConstructiveNew;
import es.urjc.etsii.grafo.mreflp.create.MREFLPReconstructiveNew;
import es.urjc.etsii.grafo.mreflp.model.MREFLPInstance;
import es.urjc.etsii.grafo.mreflp.model.MREFLPInstanceUtil;
import es.urjc.etsii.grafo.mreflp.model.MREFLPSolution;
import es.urjc.etsii.grafo.mreflp.model.MREFLPSolutionValidator;
import es.urjc.etsii.grafo.shake.DestroyRebuild;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.mreflp.MREFLPTestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MREFLPAutoconfigEncodingTest {
    @Test
    void reducesDeclarationsWithoutPruningComponentsDomainsOrPhaseOrders() throws Exception {
        context(1234);
        var inventory = new AlgorithmInventoryService(new MREFLPInventoryFilterNew(),
                List.of(new GRGraspConstructiveFactory(), new RGGraspConstructiveFactory()),
                List.of(new AlgorithmNameParam(), new ObjectiveParamProvider()));
        inventory.runComponentDiscovery("es.urjc.etsii");
        var generator = new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter());
        var config = new SolverConfig();
        config.setTreeDepth(1000);
        config.setMaxDerivationRepetition(0);
        var space = new AutoconfigSearchSpace(config, generator);
        var mapper = new JsonMapper();
        var baseline = mapper.readTree(Files.readString(Path.of("src/test/resources/autoconfig/encoding-baseline.json")));
        var current = mapper.valueToTree(space.snapshot());
        assertEquals(baseline.get("roots"), current.get("roots"));
        assertEquals(baseline.get("components"), current.get("components"), "Choices, domains and collection bounds must stay identical");
        assertEquals(baseline.get("limits"), current.get("limits"));
        assertEquals(9, space.roots().size());
        assertEquals(baseline.get("summary"), current.get("summary"));
        assertEquals(1016, space.iraceParameters().size());
        assertEquals(60, space.iraceParameterSpace().forbiddenExpressions().size());
        assertFalse(generator.componentParams().keySet().contains(es.urjc.etsii.grafo.mreflp.improve.SequentialImproverNew.class));

        var report = space.snapshot().encodingDiagnostics();
        assertEquals(BigInteger.valueOf(1016), report.total().declarations());
        assertEquals(BigInteger.valueOf(158012), report.total().prefixDeclarationEstimate());
        assertEquals(BigInteger.valueOf(60), report.total().forbiddenConstraints());
        assertEquals(BigInteger.valueOf(156996), report.avoidedDeclarations());
        assertEquals(6, report.collections().size());
        for (var collection : report.collections()) {
            assertTrue(collection.path().endsWith("_VND.improvers"));
            assertEquals(2, collection.minItems());
            assertEquals(5, collection.maxItems());
            assertEquals(8, collection.candidateCount());
            assertEquals(BigInteger.valueOf(2081), collection.prefixSelectorsEstimate());
            assertEquals(BigInteger.valueOf(5), collection.positionSelectors());
            assertEquals(BigInteger.valueOf(22), collection.candidateDeclarationsPerPosition());
            assertEquals(BigInteger.valueOf(26282), collection.counts().prefixDeclarationEstimate());
            assertEquals(BigInteger.valueOf(116), collection.counts().declarations());
        }
        var automatic = new AutomaticAlgorithmBuilder<>(space, new AlgorithmBuilderService(inventory));
        TreeNode simple = null;
        for (var root : space.roots()) if (root.className().equals("SimpleAlgorithm")) simple = root;
        TreeNode vnd = null;
        for (var child : simple.children().get("improver")) if (child.className().equals("VND")) vnd = child;
        var combination = vnd.combinations().get("improvers");
        var constructive = new ComponentSpec("MREFLPConstructiveNew", Map.of("policy", "RCL", "order", "FLOW_DESCENDING", "rcl", 0.25));
        var template = new ComponentSpec("SimpleAlgorithm", Map.of("constructive", constructive));
        int orders = assertPhaseOrders(combination, new ArrayList<>(), new HashSet<>(), template, inventory, generator, automatic);
        assertEquals(8792, orders);
        var rootsTotal = BigInteger.ONE;
        for (var root : report.roots()) rootsTotal = rootsTotal.add(root.counts().declarations());
        assertEquals(report.total().declarations(), rootsTotal);

        var seeds = new ComponentSpecJsonCodec().parseList(Files.readString(Path.of("src/main/resources/irace/initial-configurations.json")));
        assertEquals(1, seeds.size());
        assertEquals("LMLS", seeds.getFirst().component());
        assertEquals("LMLS", seeds.getFirst().parameters().get("variant"));
        var table = InitialConfigurationUtil.toIraceTable(space, seeds).split("\n");
        assertEquals(2, table.length);
        assertEquals(1016, table[0].split("\t").length);

        for (int repetition : List.of(0, 1, 2)) {
            assertEquals(space.iraceParameterSpace(), generator.toIraceParameterSpace(generator.buildTree(1000, repetition)));
        }
        assertEquals(space.iraceParameterSpace(), generator.toIraceParameterSpace(generator.buildTree(4, 2)));
        writeArtifacts("broad", space, String.join("\n", table) + "\n");
        System.out.println("MREFLP declarations: prefix estimate 158,012 -> 1,016; constraints: 60; roots: 9");
        for (var collection : report.collections()) {
            System.out.println(collection.path() + ": selectors 2,081 -> 5; declarations 26,282 -> 116; phase orders: 8,792");
        }
    }

    @Test
    void focusedInventoryPreservesSotaAndAllSixtyOrdersWith287Declarations() throws Exception {
        context(1234);
        var filter = new MREFLPFocusedInventoryFilterNew();
        var inventory = newInventory(filter);
        inventory.runComponentDiscovery("es.urjc.etsii");
        var generator = new AlgorithmCandidateGenerator(inventory, new MREFLPFocusedExplorationFilterNew(filter));
        var space = new AutoconfigSearchSpace(generationConfig(), generator);
        assertEquals(List.of("IteratedGreedy", "LMLS", "LMLSNew", "VNS"), space.snapshot().roots());
        assertEquals(287, space.iraceParameters().size());
        assertEquals(18, space.iraceParameterSpace().forbiddenExpressions().size());
        var diagnostics = space.snapshot().encodingDiagnostics();
        assertEquals(BigInteger.valueOf(902), diagnostics.total().prefixDeclarationEstimate());
        assertEquals(BigInteger.valueOf(615), diagnostics.avoidedDeclarations());
        assertEquals(3, diagnostics.collections().size());
        for (var collection : diagnostics.collections()) {
            assertEquals(2, collection.minItems());
            assertEquals(4, collection.maxItems());
            assertEquals(4, collection.candidateCount());
            assertEquals(BigInteger.valueOf(41), collection.prefixSelectorsEstimate());
            assertEquals(BigInteger.valueOf(4), collection.positionSelectors());
            assertEquals(BigInteger.valueOf(14), collection.candidateDeclarationsPerPosition());
            assertEquals(BigInteger.valueOf(266), collection.counts().prefixDeclarationEstimate());
            assertEquals(BigInteger.valueOf(61), collection.counts().declarations());
        }
        var automatic = new AutomaticAlgorithmBuilder<>(space, new AlgorithmBuilderService(inventory));
        for (var root : space.roots()) {
            if (root.className().equals("LMLS")) continue;
            var constructives = root.children().get("constructive");
            assertEquals(1, constructives.size());
            assertEquals(MREFLPConstructiveNew.class, constructives.getFirst().clazz());
            assertEquals(root.className().equals("LMLSNew") ? 0 : 1, countReconstructionPaths(root, null));
            for (var improver : root.children().get("improver")) {
                if (!improver.className().equals("VND")) continue;
                var phases = improver.combinations().get("improvers");
                var candidates = new HashSet<String>();
                for (var candidate : phases.candidates()) candidates.add(candidate.className());
                assertEquals(Set.of("OneMoveDescentNew", "OneMoveTabuSearchNew", "SwapDescentNew", "MREFLPSimulatedAnnealingNew"), candidates);
                assertEquals(60, assertPhaseOrders(phases, new ArrayList<>(), new HashSet<>(),
                        defaultSpecification(root, generator), inventory, generator, automatic));
            }
        }
        var seeds = new ComponentSpecJsonCodec().parseList(Files.readString(Path.of("src/main/resources/irace/initial-configurations.json")));
        assertEquals(1, seeds.size());
        assertEquals("LMLS", seeds.getFirst().component());
        assertEquals("LMLS", seeds.getFirst().parameters().get("variant"));
        var table = InitialConfigurationUtil.toIraceTable(space, seeds);
        assertEquals(2, table.split("\n").length);
        assertEquals(287, table.split("\n")[0].split("\t").length);
        assertNotNull(automatic.buildFromConfig(flatConfiguration(seeds.getFirst(), inventory, generator)));
        for (int repetition : List.of(0, 1, 2)) {
            assertEquals(space.iraceParameterSpace(), generator.toIraceParameterSpace(generator.buildTree(1000, repetition)));
        }
        assertEquals(space.iraceParameterSpace(), generator.toIraceParameterSpace(generator.buildTree(4, 2)));
        writeArtifacts("focused", space, table);
        System.out.println("MREFLP focused: 287 declarations, 18 constraints, 4 roots, 60 orders per VND");
    }

    @Test
    void reconstructionRestrictionAppliesOnlyToTheFocusedInventoryAndDirectParent() {
        for (var inventory : List.of(new MREFLPFocusedInventoryFilterNew(), new MREFLPInventoryFilterNew(), new DefaultInventoryFilter())) {
            boolean focused = inventory instanceof MREFLPFocusedInventoryFilterNew;
            var filter = new MREFLPFocusedExplorationFilterNew(inventory);
            var context = new TreeContext(1000, 0);
            assertEquals(focused, filter.reject(context, MREFLPReconstructiveNew.class));
            assertFalse(filter.reject(context, MREFLPConstructiveNew.class));
            context.push(DestroyRebuild.class);
            assertFalse(filter.reject(context, MREFLPReconstructiveNew.class));
            context.push(MREFLPConstructiveNew.class);
            assertEquals(focused, filter.reject(context, MREFLPReconstructiveNew.class));
        }
    }

    @Test
    void springResolvesBothNamedInventoriesAndUsesOneExplorationFilter() {
        context(1234);
        for (String profile : List.of("Focused", "")) {
            new ApplicationContextRunner()
                    .withUserConfiguration(FilterDiscovery.class, FilterConfig.class, AlgorithmCandidateGenerator.class, AutoconfigSearchSpace.class)
                    .withPropertyValues("advanced.scan-pkgs=es.urjc.etsii", "blacklist=MREFLP" + profile + "InventoryFilterNew")
                    .run(application -> {
                        assertThat(application).hasNotFailed();
                        assertEquals(1, application.getBeansOfType(IExplorationFilter.class).size());
                        assertInstanceOf(MREFLPFocusedExplorationFilterNew.class, application.getBean(IExplorationFilter.class));
                        var space = application.getBean(AutoconfigSearchSpace.class);
                        assertEquals(profile.isEmpty() ? 1016 : 287, space.iraceParameters().size());
                        assertEquals(profile.isEmpty() ? 9 : 4, space.roots().size());
                    });
        }
    }

    @Test
    @Timeout(30)
    @EnabledIfSystemProperty(named = "mreflp.autoconfig.samples", matches = ".+")
    void iraceSamplesBuildAndReturnValidSolutionsOnRealInstances() throws Exception {
        context(1234);
        var filter = new MREFLPFocusedInventoryFilterNew();
        var inventory = newInventory(filter);
        inventory.runComponentDiscovery("es.urjc.etsii");
        var generator = new AlgorithmCandidateGenerator(inventory, new MREFLPFocusedExplorationFilterNew(filter));
        var automatic = new AutomaticAlgorithmBuilder<MREFLPSolution, MREFLPInstance>(
                new AutoconfigSearchSpace(generationConfig(), generator), new AlgorithmBuilderService(inventory));
        var samples = new JsonMapper().readTree(Files.readString(Path.of(System.getProperty("mreflp.autoconfig.samples"))));
        assertEquals(250, samples.size());
        var instances = List.of(MREFLPInstanceUtil.read(Path.of("instances/raw/small/A-10-90.txt"), 2),
                MREFLPInstanceUtil.read(Path.of("instances/raw/medium/A-25-50.txt"), 3));
        int runs = 0;
        var roots = new HashSet<String>();
        try {
            for (int index = 0; index < samples.size(); index++) {
                var values = new LinkedHashMap<String, String>();
                for (var property : samples.get(index).properties()) {
                    if (!property.getValue().isNull()) values.put(property.getKey(), property.getValue().asText());
                }
                roots.add(values.get("ROOT"));
                var algorithm = withBuilder(automatic.buildFromConfig(new AlgorithmConfiguration(values)));
                if (index >= 25) continue;
                for (var instance : instances) for (long seed : new long[]{83, 999}) {
                    context(seed);
                    TimeControl.setMaxExecutionTime(5, TimeUnit.MILLISECONDS);
                    TimeControl.start();
                    var solution = algorithm.algorithm(instance);
                    new MREFLPSolutionValidator().validate(solution).throwIfFail();
                    assertEquals(solution.recalculateCost(), solution.cost());
                    runs++;
                }
            }
        } finally {
            TimeControl.remove();
        }
        assertEquals(Set.of("IteratedGreedy", "LMLS", "LMLSNew", "VNS"), roots);
        assertEquals(100, runs);
        System.out.println("MREFLP irace samples: 250 builds, 100 validated real-instance executions");
    }

    private static AlgorithmInventoryService newInventory(IInventoryFilter filter) {
        return new AlgorithmInventoryService(filter, List.of(new GRGraspConstructiveFactory(), new RGGraspConstructiveFactory()),
                List.of(new AlgorithmNameParam(), new ObjectiveParamProvider()));
    }

    private static SolverConfig generationConfig() {
        var config = new SolverConfig();
        config.setTreeDepth(1000);
        config.setMaxDerivationRepetition(1);
        return config;
    }

    private static int countReconstructionPaths(TreeNode node, Class<?> parent) {
        int count = 0;
        if (node.clazz() == MREFLPReconstructiveNew.class) {
            assertEquals(DestroyRebuild.class, parent);
            count++;
        }
        for (var children : node.children().values()) for (var child : children) count += countReconstructionPaths(child, node.clazz());
        return count;
    }

    private static void writeArtifacts(String profile, AutoconfigSearchSpace space, String table) throws Exception {
        var directory = Path.of("target/autoconfig-space", profile);
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("parameters.txt"), IraceParameterFileUtil.toFileContents(space.iraceParameterSpace()));
        Files.writeString(directory.resolve("initial-configurations.txt"), table);
        Files.writeString(directory.resolve("report.json"), new JsonMapper().writerWithDefaultPrettyPrinter().writeValueAsString(space.snapshot()) + "\n");
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = MREFLPFocusedExplorationFilterNew.class, useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = es.urjc.etsii.grafo.annotations.InheritedComponent.class))
    static class FilterDiscovery {
        @Bean AlgorithmInventoryService inventory(IInventoryFilter filter) { return newInventory(filter); }
        @Bean SolverConfig solverConfig() { return generationConfig(); }
    }

    private static int assertPhaseOrders(CombinationTree combination, List<ComponentSpec> phases, Set<Class<?>> used,
                                         ComponentSpec template, AlgorithmInventoryService inventory,
                                         AlgorithmCandidateGenerator generator, AutomaticAlgorithmBuilder<?, ?> automatic) {
        int orders = 0;
        if (phases.size() >= combination.min()) {
            assertOrderRoundTrip(phases, template, inventory, generator, automatic);
            orders++;
        }
        if (phases.size() == combination.max()) return orders;
        for (var candidate : combination.candidates()) {
            if (!used.add(candidate.clazz())) continue;
            phases.add(defaultSpecification(candidate, generator));
            orders += assertPhaseOrders(combination, phases, used, template, inventory, generator, automatic);
            phases.removeLast();
            used.remove(candidate.clazz());
        }
        return orders;
    }
    private static ComponentSpec defaultSpecification(TreeNode node, AlgorithmCandidateGenerator generator) {
        var values = new LinkedHashMap<String, Object>();
        for (var parameter : generator.componentParams().get(node.clazz())) {
            if (parameter.getType() == ParameterType.PROVIDED) continue;
            assertFalse(parameter.combination(), "VND leaf phases must not contain another phase list");
            values.put(parameter.getName(), parameter.recursive()
                    ? defaultSpecification(node.children().get(parameter.getName()).getFirst(), generator)
                    : parameter.getValues()[0]);
        }
        return new ComponentSpec(node.className(), values);
    }

    private static void assertOrderRoundTrip(List<ComponentSpec> phases, ComponentSpec template,
                                            AlgorithmInventoryService inventory, AlgorithmCandidateGenerator generator,
                                            AutomaticAlgorithmBuilder<?, ?> automatic) {
        var parameters = new LinkedHashMap<>(template.parameters());
        parameters.put("improver", new ComponentSpec("VND", Map.of("improvers", List.copyOf(phases))));
        var specification = new ComponentSpec(template.component(), parameters);
        var configuration = flatConfiguration(specification, inventory, generator);
        assertEquals(specification, automatic.asComponentSpec(configuration));
        assertNotNull(automatic.buildFromConfig(configuration));
    }
}
