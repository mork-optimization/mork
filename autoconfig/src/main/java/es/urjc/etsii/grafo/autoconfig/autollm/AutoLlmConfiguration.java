package es.urjc.etsii.grafo.autoconfig.autollm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

@Configuration
@EnableConfigurationProperties(AutoLlmConfig.class)
public class AutoLlmConfiguration {
    @Bean
    WebServerFactoryCustomizer<ConfigurableWebServerFactory> autoLlmLoopback(ApplicationArguments arguments) {
        return factory -> {
            if (arguments.containsOption("autollm")) {
                try { factory.setAddress(java.net.InetAddress.getByAddress(new byte[]{127, 0, 0, 1})); }
                catch (java.net.UnknownHostException e) { throw new IllegalStateException(e); }
            }
        };
    }
}
