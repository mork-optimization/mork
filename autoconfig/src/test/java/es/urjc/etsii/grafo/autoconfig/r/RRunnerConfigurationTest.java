package es.urjc.etsii.grafo.autoconfig.r;

import es.urjc.etsii.grafo.autoconfig.irace.IraceConfig;
import es.urjc.etsii.grafo.autoconfig.irace.IraceIntegration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RRunnerConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(RRunnerConfiguration.class);

    @Test
    void suppliesRScriptRunnerByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RLangRunner.class);
            assertThat(context.getBean(RLangRunner.class)).isInstanceOf(RScriptRunner.class);
        });
    }

    @Test
    void preservesCustomRunnerWhenDefaultConfigurationIsRegisteredFirst() {
        assertCustomRunnerSelected(new ApplicationContextRunner()
                .withUserConfiguration(
                        RRunnerConfiguration.class,
                        CustomRunnerConfiguration.class,
                        IraceConsumerConfiguration.class
                ));
    }

    @Test
    void preservesCustomRunnerWhenUserConfigurationIsRegisteredFirst() {
        assertCustomRunnerSelected(new ApplicationContextRunner()
                .withUserConfiguration(
                        CustomRunnerConfiguration.class,
                        RRunnerConfiguration.class,
                        IraceConsumerConfiguration.class
                ));
    }

    private void assertCustomRunnerSelected(ApplicationContextRunner runner) {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(IraceIntegration.class);
            assertThat(context).getBeans(RLangRunner.class).hasSize(2);
            assertThat(context.getBean(RLangRunner.class))
                    .isSameAs(context.getBean("customRunner", RLangRunner.class));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import({IraceConfig.class, IraceIntegration.class})
    static class IraceConsumerConfiguration {
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomRunnerConfiguration {
        @Bean
        RLangRunner customRunner() {
            return request -> new RExecutionResult(
                    0,
                    Duration.ZERO,
                    Path.of("stdout"),
                    Path.of("stderr")
            );
        }
    }
}
