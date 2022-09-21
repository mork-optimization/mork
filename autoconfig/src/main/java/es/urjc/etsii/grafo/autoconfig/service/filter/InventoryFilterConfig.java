package es.urjc.etsii.grafo.autoconfig.service.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryFilterConfig {
    @Bean
    @ConditionalOnMissingBean
    public InventoryFilterStrategy getDefaultFilterStrategy(){
        return new DefaultFilterStrategy();
    }
}
