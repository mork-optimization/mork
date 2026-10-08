package es.urjc.etsii.grafo.autoconfig.r;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Fallback;

/**
 * Default R runner configuration.
 */
@Configuration
public class RRunnerConfiguration {

    /**
     * Supply the system {@code Rscript} runner as the fallback candidate. A
     * regular application bean implementing {@link RLangRunner} is selected in
     * preference to this bean regardless of registration order.
     *
     * @return default R runner
     */
    @Bean
    @Fallback
    public RLangRunner rLangRunner() {
        return new RScriptRunner();
    }
}
