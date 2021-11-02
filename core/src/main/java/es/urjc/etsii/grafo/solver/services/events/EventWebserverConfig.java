package es.urjc.etsii.grafo.solver.services.events;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "event.webserver")
public class EventWebserverConfig {
    private boolean stopOnExecutionEnd = true;

    public boolean isStopOnExecutionEnd() {
        return stopOnExecutionEnd;
    }

    public void setStopOnExecutionEnd(boolean stopOnExecutionEnd) {
        this.stopOnExecutionEnd = stopOnExecutionEnd;
    }
}
