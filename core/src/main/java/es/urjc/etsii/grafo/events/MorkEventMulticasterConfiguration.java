package es.urjc.etsii.grafo.events;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Installs Mork's synchronous, listener-isolating Spring event multicaster.
 */
@Configuration(proxyBeanMethods = false)
class MorkEventMulticasterConfiguration {

    @Bean(name = AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    ApplicationEventMulticaster applicationEventMulticaster() {
        return new MorkApplicationEventMulticaster();
    }
}
