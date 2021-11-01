package es.urjc.etsii.grafo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "errors")
public class ErrorConfig {
    private boolean errorsToFile;
    private String path;

    public boolean isErrorsToFile() {
        return errorsToFile;
    }

    public void setErrorsToFile(boolean errorsToFile) {
        this.errorsToFile = errorsToFile;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
