package es.urjc.etsii.grafo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used to configure the behaviour on error using the properties specified in the application.yml
 * {@see application.yml}
 */
@Configuration
@ConfigurationProperties(prefix = "errors")
public class ErrorConfig {
    private boolean errorsToFile;
    private String folder;

    /**
     * Export errors to file?
     *
     * @return True to export, false to skip
     */
    public boolean isErrorsToFile() {
        return errorsToFile;
    }

    /**
     * Export errors to file?
     *
     * @param errorsToFile  True to export, false to skip
     */
    public void setErrorsToFile(boolean errorsToFile) {
        this.errorsToFile = errorsToFile;
    }

    /**
     * Where should serialized errors be stored?
     *
     * @return path as string
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Where should serialized errors be stored?
     *
     * @param folder  path as string
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }
}
