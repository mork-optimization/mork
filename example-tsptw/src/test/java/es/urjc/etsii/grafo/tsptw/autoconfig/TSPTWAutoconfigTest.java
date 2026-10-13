package es.urjc.etsii.grafo.tsptw.autoconfig;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.autoconfig.generator.FilterConfig;
import es.urjc.etsii.grafo.autoconfig.generator.IExplorationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TSPTWAutoconfigTest {

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
