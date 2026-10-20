package mork.startupfixture;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.DefaultExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

/** Fails before any Mork orchestrator or execution-lifecycle bean is instantiated. */
public class StartupFailureFixture {
    public static void main(String[] args) {
        Objective<?, TestSolution, TestInstance> objective = Objective.ofMaximizing("fixture", TestSolution::getScore);
        Mork.start(args, objective);
    }

    @Configuration(proxyBeanMethods = false)
    public static class FailureConfiguration {
        @Bean
        public static BeanFactoryPostProcessor startupFailure(Environment environment) {
            return beanFactory -> {
                if (environment.getProperty("fixture.failure", "component").equals("unknown")) {
                    throw new IllegalArgumentException("unclassified framework failure");
                }
                var inventory = new AlgorithmInventoryService(c -> c == InvalidComponentNew.class, List.of(), List.of());
                inventory.runComponentDiscovery("mork.startupfixture");
                new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter());
            };
        }
    }

    public static class InvalidComponentNew extends Algorithm<TestSolution, TestInstance> {
        @AutoconfigConstructor
        public InvalidComponentNew(@IntegerParam(min = 2, max = 1) int iterations) { super("invalid"); }
        @Override public TestSolution algorithm(TestInstance instance) { return null; }
    }
}
