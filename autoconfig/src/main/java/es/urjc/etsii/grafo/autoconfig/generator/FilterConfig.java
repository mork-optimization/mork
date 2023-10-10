package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.IInventoryFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    @ConditionalOnMissingBean
    public IInventoryFilter getDefaultFilterStrategy(){
        return new DefaultInventoryFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public IExplorationFilter getDefaultExplorationFilter(){
        return new DefaultExplorationFilter();
    }

}
