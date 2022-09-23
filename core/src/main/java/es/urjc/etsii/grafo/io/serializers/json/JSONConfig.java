package es.urjc.etsii.grafo.io.serializers.json;

import es.urjc.etsii.grafo.annotation.SerializerSource;
import es.urjc.etsii.grafo.io.serializers.AbstractSolutionSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SerializerSource
@ConfigurationProperties(prefix = "serializers.solution-json")
public class JSONConfig extends AbstractSolutionSerializerConfig {
    /**
     * If pretty is true, the json file has multiple indented lines
     */
    private boolean pretty = true;


    /**
     * <p>isPretty.</p>
     *
     * @return a boolean.
     */
    public boolean isPretty() {
        return pretty;
    }

    /**
     * <p>Setter for the field <code>pretty</code>.</p>
     *
     * @param pretty a boolean.
     */
    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }

    @Override
    public String toString() {
        return "{" +
                "pretty=" + pretty +
                '}';
    }
}
