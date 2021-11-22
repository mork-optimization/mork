package es.urjc.etsii.grafo.solver.services.events;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used to configure the behaviour of the webserver as specified in the application.yml
 * {@see application.yml}
 */
@Configuration
@ConfigurationProperties(prefix = "event.webserver")
public class EventWebserverConfig {
    private boolean stopOnExecutionEnd = true;

    /**
     * Stop webserver when all experiments finish executing?
     *
     * @return true to stop after all experiments end, false to keep backend running
     */
    public boolean isStopOnExecutionEnd() {
        return stopOnExecutionEnd;
    }

    /**
     * Stop webserver when all experiments finish executing?
     *
     * @param stopOnExecutionEnd  true to stop after all experiments end, false to keep backend running
     */
    public void setStopOnExecutionEnd(boolean stopOnExecutionEnd) {
        this.stopOnExecutionEnd = stopOnExecutionEnd;
    }
}
