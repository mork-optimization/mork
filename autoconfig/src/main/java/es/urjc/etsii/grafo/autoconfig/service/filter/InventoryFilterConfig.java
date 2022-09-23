package es.urjc.etsii.grafo.autoconfig.service.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class InventoryFilterConfig {
    @Bean
    @ConditionalOnMissingBean
    @Profile("autoconfig")
    public InventoryFilterStrategy getDefaultFilterStrategy(){
        return new DefaultFilterStrategy();
    }
}
