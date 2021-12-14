package es.urjc.etsii.grafo.io.serializers.csv;

import es.urjc.etsii.grafo.io.serializers.AbstractResultSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * This class is used to configure the CSV serializer by the properties specified in the application.yml
 * {@see application.yml}
 */
@Configuration
@ConfigurationProperties(prefix = "serializers.csv")
public class CSVConfig extends AbstractResultSerializerConfig {

    /**
     * character to separate columns
     */
    private char separator;

    /**
     * Get configured column separator. In TSV, separator is '\t', in CSV ','.
     *
     * @return column separator
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Change column separatr
     *
     * @param separator Examples: in TSV, separator is '\t', in CSV ','. Other separators can be used.
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }
}
