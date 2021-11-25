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

    /**
     * <p>Getter for the field <code>folder</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFolder() {
        return folder;
    }

    /**
     * <p>Setter for the field <code>folder</code>.</p>
     *
     * @param folder a {@link java.lang.String} object.
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * <p>Getter for the field <code>dateformat</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDateformat() {
        return dateformat;
    }

    /**
     * <p>Setter for the field <code>dateformat</code>.</p>
     *
     * @param dateformat a {@link java.lang.String} object.
     */
    public void setDateformat(String dateformat) {
        this.dateformat = dateformat;
    }
}
