package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.autoconfig.factories.GRGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.factories.RGGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.fill.ObjectiveParamProvider;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.AutoconfigEncodingUtil;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.bmssc.experiment.BlacklistedComponents;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

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

    /** Count exported rows without constructing parameter-name strings. */
    static long countParameters(AlgorithmCandidateGenerator generator, List<TreeNode> roots) {
        return AutoconfigEncodingUtil.count(roots, generator.componentParams()).declarations().longValueExact();
    }
}
