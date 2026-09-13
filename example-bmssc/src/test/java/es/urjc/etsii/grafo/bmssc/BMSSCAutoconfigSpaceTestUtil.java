package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.autoconfig.factories.GRGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.factories.RGGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.fill.ObjectiveParamProvider;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationNode;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.bmssc.experiment.BlacklistedComponents;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class BMSSCAutoconfigSpaceTestUtil {
    static final String COMPONENT_PACKAGES = "es.urjc.etsii.grafo.algorithms,es.urjc.etsii.grafo.create,es.urjc.etsii.grafo.improve,"
            + "es.urjc.etsii.grafo.shake,es.urjc.etsii.grafo.solution.neighborhood,es.urjc.etsii.grafo.bmssc";

    private BMSSCAutoconfigSpaceTestUtil() {}

    static AlgorithmInventoryService inventory(boolean blacklistEnabled) {
        var inventory = newInventory(blacklistEnabled);
        inventory.runComponentDiscovery(COMPONENT_PACKAGES);
        return inventory;
    }

    static AlgorithmInventoryService newInventory(boolean blacklistEnabled) {
        return new AlgorithmInventoryService(blacklistEnabled ? new BlacklistedComponents() : new DefaultInventoryFilter(),
                List.of(new GRGraspConstructiveFactory(), new RGGraspConstructiveFactory()),
                List.of(new AlgorithmNameParam(), new ObjectiveParamProvider()));
    }

    static SolverConfig generationConfig() {
        var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        var properties = yaml.getObject();
        var config = new SolverConfig();
        config.setTreeDepth(Integer.parseInt(properties.getProperty("solver.tree-depth")));
        config.setMaxDerivationRepetition(Integer.parseInt(properties.getProperty("solver.max-derivation-repetition")));
        return config;
    }

    /** Count exported rows without constructing millions of long parameter-name strings. */
    static long countParameters(AlgorithmCandidateGenerator generator, List<TreeNode> roots) {
        var counter = new Counter(generator.componentParams());
        long result = 1; // Root selector
        for (var root : roots) result = Math.addExact(result, counter.count(root));
        return result;
    }

    private static final class Counter {
        private final Map<Class<?>, List<ComponentParameter>> parameters;
        private final Map<TreeNode, Long> counts = new IdentityHashMap<>();

        private Counter(Map<Class<?>, List<ComponentParameter>> parameters) { this.parameters = parameters; }

        private long count(TreeNode node) {
            var known = counts.get(node);
            if (known != null) return known;
            long total = 0;
            for (var parameter : parameters.get(node.clazz())) {
                if (parameter.getType() == ParameterType.PROVIDED) continue;
                if (parameter.combination()) {
                    var combination = node.combinations().get(parameter.getName());
                    if (combination.min() != combination.max()) total = Math.addExact(total, 1);
                    total = Math.addExact(total, count(combination.root()));
                } else {
                    total = Math.addExact(total, 1);
                    if (parameter.recursive()) {
                        for (var child : node.children().get(parameter.getName())) total = Math.addExact(total, count(child));
                    }
                }
            }
            counts.put(node, total);
            return total;
        }

        private long count(CombinationNode node) {
            if (node == null) return 0;
            long total = 1;
            for (var choice : node.choices()) {
                total = Math.addExact(total, count(choice.component()));
                total = Math.addExact(total, count(choice.next()));
            }
            return total;
        }
    }
}
