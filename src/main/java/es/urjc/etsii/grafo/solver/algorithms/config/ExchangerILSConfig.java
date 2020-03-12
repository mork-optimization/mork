package es.urjc.etsii.grafo.solver.algorithms.config;

import es.urjc.etsii.grafo.solver.algorithms.ExchangerILS;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "algorithms.exchangerils")
public class ExchangerILSConfig {
    private int nRotateRounds;
    private ExchangerILS.ILSConfig[] configs;

    public int getnRotateRounds() {
        return nRotateRounds;
    }

    public ExchangerILS.ILSConfig[] getConfigs() {
        return configs;
    }
}
