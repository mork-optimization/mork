package es.urjc.etsii.grafo.solver.irace;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used to configure irace by the properties specified in the application.yml
 * {@see application.yml} section irace
 */
@Configuration
@ConfigurationProperties(prefix = "irace")
public class IraceConfig {
    private boolean enabled;
    private boolean shell;

    /**
     * Is irace enabled?
     *
     * @return true if irace is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Is irace enabled?
     *
     * @param enabled true to enable irace, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Execute Irace in shell or using GraalVM?
     *
     * @return true if irace should be executed using ShellRLangRunner, false to use GraalRLangRunner
     */
    public boolean isShell() {
        return shell;
    }

    /**
     * Execute Irace in shell or using GraalVM?
     *
     * @param shell true to execute irace using ShellRLangRunner, false to use GraalRLangRunner
     */
    public void setShell(boolean shell) {
        this.shell = shell;
    }
}
