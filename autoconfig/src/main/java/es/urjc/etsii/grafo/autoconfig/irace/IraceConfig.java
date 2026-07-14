package es.urjc.etsii.grafo.autoconfig.irace;

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
    private Boolean shell;
    private boolean auc = false;
    private boolean timecontrol = false;

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
     * Legacy R execution option. R scripts are now always executed through
     * the configured {@code Rscript} command.
     *
     * @return true unless the removed embedded execution mode was explicitly requested
     * @deprecated remove this property from application configuration
     */
    @Deprecated(forRemoval = true)
    public boolean isShell() {
        return shell == null || shell;
    }

    /**
     * Bind the legacy R execution option for an actionable migration error.
     *
     * @param shell legacy option value
     * @deprecated remove this property from application configuration
     */
    @Deprecated(forRemoval = true)
    public void setShell(boolean shell) {
        this.shell = shell;
    }

    void validateRScriptExecution() {
        if (Boolean.FALSE.equals(shell)) {
            throw new IllegalStateException(
                    "irace.shell=false is no longer supported. Install GNU R so that Rscript is available " +
                            "and remove the irace.shell property."
            );
        }
    }

    /**
     * Decide if we should use the objective function directly or if we should calculate the AUC over the objective function
     * @return trye if we should calculate the AUC, false to use the objective function applied to the solution
     */
    public boolean isAuc() {
        return auc;
    }

    /**
     * Decide if we should use the objective function directly or if we should calculate the AUC over the objective function
     * @param auc true to calculate the AUC, false to use the objective function applied to the solution
     */
    public void setAuc(boolean auc) {
        this.auc = auc;
    }

    /**
     * Is time control enabled?
     * @return true if enabled, false otherwise
     */
    public boolean isTimecontrol() {
        return timecontrol;
    }

    /**
     * Enable or disable time control.
     * @param timecontrol true to enable, false to disable
     */
    public void setTimecontrol(boolean timecontrol) {
        this.timecontrol = timecontrol;
    }
}
