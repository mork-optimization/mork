package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.util.ExecutionJournalUtil;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.annotation.Order;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@EnableConfigurationProperties(AutoLlmConfig.class)
public class AutoLlmConfiguration {
    @Bean
    @Order(Integer.MIN_VALUE)
    ApplicationRunner workerEvidence(es.urjc.etsii.grafo.config.SolverConfig solver, es.urjc.etsii.grafo.config.InstanceConfiguration instances, org.springframework.core.env.Environment environment) {
        return _ -> {
            String config = environment.getProperty("autollm.child.config-file");
            if (config != null) AutoLlmInputUtil.configureReference(instances, java.nio.file.Path.of(config));
            if (ExecutionJournalUtil.enabled()) ExecutionJournalUtil.write("workers.json",
                    java.util.Map.of("effectiveWorkers", solver.getnWorkers(), "availableProcessors", Runtime.getRuntime().availableProcessors()));
        };
    }
    @Bean
    WebServerFactoryCustomizer<ConfigurableWebServerFactory> autoLlmLoopback(ApplicationArguments arguments) {
        return factory -> {
            if (arguments.containsOption("autollm")) {
                try { factory.setAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1})); }
                catch (UnknownHostException e) { throw new IllegalStateException(e); }
            }
        };
    }
}
