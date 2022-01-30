package es.urjc.etsii.grafo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import javax.annotation.PostConstruct;

@Configuration
public class DisableWebsocketLogging {

    private final WebSocketMessageBrokerStats webSocketMessageBrokerStats;

    public DisableWebsocketLogging(WebSocketMessageBrokerStats webSocketMessageBrokerStats) {
        this.webSocketMessageBrokerStats = webSocketMessageBrokerStats;
    }

    @PostConstruct
    public void init() {
        webSocketMessageBrokerStats.setLoggingPeriod(0);
    }

}
