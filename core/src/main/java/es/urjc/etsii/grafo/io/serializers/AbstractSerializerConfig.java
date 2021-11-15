package es.urjc.etsii.grafo.io.serializers;

/**
 * This class is used to configure the serializer by the properties specified in the application.yml
 * {@see application.yml}
 */
public abstract class AbstractSerializerConfig {


    /**
     * The serializer is enabled
     */
    private boolean enabled;

    /**
     *  Path where solutions will be exported
     */
    private String folder;

    /**
     *  String formatter for current date using in solution filename
     */
    private String format;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
