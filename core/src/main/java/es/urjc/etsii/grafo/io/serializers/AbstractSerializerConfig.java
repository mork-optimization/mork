package es.urjc.etsii.grafo.io.serializers;

/**
 * This class is used to configure common serializer properties.
 * Includes both solution serializers and results serializers
 * {@see application.yml}
 */
public class AbstractSerializerConfig {

    /**
     * The serializer is enabled
     */
    private boolean enabled;

    /**
     * Path where solutions will be exported
     */
    private String folder;

    /**
     * String formatter for current date using in solution filename
     */
    private String format;

    /**
     * Get filename format. The serializer will use this pattern to decide the filename.
     *
     * @return filename format as a string
     */
    public String getFormat() {
        return format;
    }

    /**
     * Change filename format
     *
     * @param format filename format as a string
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Is the current serializer enabled?
     * If the serializer is not enabled it should not do any operation.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Change serializer enabled status
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get folder where this serializer should write its results
     *
     * @return Path to folder as string
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Set the folder where the serializer should write
     *
     * @param folder Path to folder as string
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

}
