package es.urjc.etsii.grafo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "errors")
public class ErrorConfig {
    private boolean errorsToFile;
    private String folder;

    public boolean isErrorsToFile() {
        return errorsToFile;
    }

    public void setErrorsToFile(boolean errorsToFile) {
        this.errorsToFile = errorsToFile;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
