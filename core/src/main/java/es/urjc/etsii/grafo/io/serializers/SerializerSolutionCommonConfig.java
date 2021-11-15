package es.urjc.etsii.grafo.io.serializers;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used to configure the solutions serializers by the properties specified in the application.yml
 * {@see application.yml}
 */
@Configuration
@ConfigurationProperties(prefix = "serializers.solution-common")
public class SerializerSolutionCommonConfig {

    /**
     * Results folder
     */
    private String folder;

    /**
     * Filename format
     */
    private String dateformat;

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getDateformat() {
        return dateformat;
    }

    public void setDateformat(String dateformat) {
        this.dateformat = dateformat;
    }
}
