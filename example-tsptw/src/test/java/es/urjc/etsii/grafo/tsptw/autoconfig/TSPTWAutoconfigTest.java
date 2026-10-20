package es.urjc.etsii.grafo.tsptw.autoconfig;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpecJsonCodec;
import es.urjc.etsii.grafo.autoconfig.factories.GRGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.factories.RGGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.FilterConfig;
import es.urjc.etsii.grafo.autoconfig.generator.IExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.inventory.BlacklistInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.irace.InitialConfigurationUtil;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.create.RandomConstructive;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.testutil.TestNeighborhood;
import es.urjc.etsii.grafo.tsptw.constructives.grasp.TSPTWListManager;
import es.urjc.etsii.grafo.tsptw.shake.ExampleTSPTWShake;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TSPTWAutoconfigTest {

    @Test
    void campaignBlacklistPrunesGraspWhilePreservingTheSotaSeed() throws Exception {
        var blacklist = new BlacklistInventoryFilter() {
            @Override public Set<Class<?>> getBlacklist() {
                return Set.of(Improver.SequentialImprover.class, Constructive.NullConstructive.class,
                        RandomConstructive.class, GRASPListManager.NullGraspListManager.class,
                        TSPTWListManager.class, ExampleTSPTWShake.class, Shake.NullShake.class,
                        Destructive.NullDestructive.class, TestNeighborhood.class, Neighborhood.EmptyNeighborhood.class);
            }
        };
        var inventory = new AlgorithmInventoryService(blacklist,
                List.of(new GRGraspConstructiveFactory(), new RGGraspConstructiveFactory()), List.of());
        inventory.runComponentDiscovery("es.urjc.etsii.grafo");
        assertTrue(inventory.getInventory().componentsByType().get(GRASPListManager.class).isEmpty());
        var solver = new SolverConfig();
        solver.setTreeDepth(1000);
        solver.setMaxDerivationRepetition(1);
        var generator = new AlgorithmCandidateGenerator(inventory, new TSPTWExplorationFilter());
        var space = new AutoconfigSearchSpace(solver, generator);
        assertFalse(space.roots().isEmpty());
        for (var parameter : space.iraceParameters()) assertFalse(parameter.contains("GRASPConstructive"), parameter);
        try (var resource = getClass().getResourceAsStream("/gvns-default.json")) {
            assertNotNull(resource);
            var sota = new ComponentSpecJsonCodec().parse(new String(resource.readAllBytes(), StandardCharsets.UTF_8));
            assertDoesNotThrow(() -> InitialConfigurationUtil.toIraceTable(space, List.of(sota)));
        }
    }

    @Test
    void springDiscoversProjectExplorationFilter() {
        try (var context = new AnnotationConfigApplicationContext()) {
            var scanner = new ClassPathBeanDefinitionScanner(context, false);
            // Use the same inherited-component filter as Mork's application scan.
            scanner.addIncludeFilter(new AnnotationTypeFilter(InheritedComponent.class));
            scanner.scan(TSPTWExplorationFilter.class.getPackageName());
            context.register(FilterConfig.class);
            context.refresh();
            var filters = context.getBeansOfType(IExplorationFilter.class);
            assertEquals(1, filters.size());
            assertInstanceOf(TSPTWExplorationFilter.class, filters.values().iterator().next());
        }
    }
}
