package es.urjc.etsii.grafo.solver.irace;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "irace")
public class IraceConfig {
    private boolean enabled;
    private boolean shell;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isShell() {
        return shell;
    }

    public void setShell(boolean shell) {
        this.shell = shell;
    }
}
