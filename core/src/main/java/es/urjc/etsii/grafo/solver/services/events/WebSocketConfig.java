package es.urjc.etsii.grafo.solver.services.events;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * <p>WebSocketConfig class.</p>
 *
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** {@inheritDoc} */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /** {@inheritDoc} */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // CORS from localhost and testing tool
        registry.addEndpoint("/websocket").setAllowedOrigins("http://localhost:8080", "https://jxy.me");
    }
}
