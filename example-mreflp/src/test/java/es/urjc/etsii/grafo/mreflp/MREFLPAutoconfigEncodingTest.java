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
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.InitialConfigurationUtil;
import es.urjc.etsii.grafo.autoconfig.irace.IraceParameterFileUtil;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.mreflp.autoconfig.MREFLPInventoryFilterNew;
import org.junit.jupiter.api.Test;
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

import static es.urjc.etsii.grafo.mreflp.MREFLPTestUtil.*;
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
        int orders = assertPhaseOrders(combination, new ArrayList<>(), new HashSet<>(), constructive, inventory, generator, automatic);
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
        Files.createDirectories(Path.of("target/autoconfig-space"));
        Files.writeString(Path.of("target/autoconfig-space/parameters.txt"), IraceParameterFileUtil.toFileContents(space.iraceParameterSpace()));
        Files.writeString(Path.of("target/autoconfig-space/initial-configurations.txt"), String.join("\n", table) + "\n");
        Files.writeString(Path.of("target/autoconfig-encoding-report.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(space.snapshot()) + "\n");
        System.out.println("MREFLP declarations: prefix estimate 158,012 -> 1,016; constraints: 60; roots: 9");
        for (var collection : report.collections()) {
            System.out.println(collection.path() + ": selectors 2,081 -> 5; declarations 26,282 -> 116; phase orders: 8,792");
        }
    }

    private static int assertPhaseOrders(CombinationTree combination, List<ComponentSpec> phases, Set<Class<?>> used,
                                         ComponentSpec constructive, AlgorithmInventoryService inventory,
                                         AlgorithmCandidateGenerator generator, AutomaticAlgorithmBuilder<?, ?> automatic) {
        int orders = 0;
        if (phases.size() >= combination.min()) {
            assertOrderRoundTrip(phases, constructive, inventory, generator, automatic);
            orders++;
        }
        if (phases.size() == combination.max()) return orders;
        for (var candidate : combination.candidates()) {
            if (!used.add(candidate.clazz())) continue;
            phases.add(defaultSpecification(candidate, generator));
            orders += assertPhaseOrders(combination, phases, used, constructive, inventory, generator, automatic);
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

    private static void assertOrderRoundTrip(List<ComponentSpec> phases, ComponentSpec constructive,
                                            AlgorithmInventoryService inventory, AlgorithmCandidateGenerator generator,
                                            AutomaticAlgorithmBuilder<?, ?> automatic) {
        var specification = new ComponentSpec("SimpleAlgorithm", Map.of("constructive", constructive,
                "improver", new ComponentSpec("VND", Map.of("improvers", List.copyOf(phases)))));
        var configuration = flatConfiguration(specification, inventory, generator);
        assertEquals(specification, automatic.asComponentSpec(configuration));
        assertNotNull(automatic.buildFromConfig(configuration));
    }
}
