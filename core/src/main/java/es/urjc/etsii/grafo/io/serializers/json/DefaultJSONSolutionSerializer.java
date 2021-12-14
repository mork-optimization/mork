package es.urjc.etsii.grafo.io.serializers.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
import es.urjc.etsii.grafo.solution.Solution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

/**
 * This class converts solution objects into their JSON equivalent by mapping the solution object variable names and value to JSON properties
 *
 * @param <S> type of the problem solution
 * @param <I> type of the problem instance
 */
public class DefaultJSONSolutionSerializer<S extends Solution<S,I>, I extends Instance> extends SolutionSerializer<S,I> {

    ObjectWriter writer;

    private final JSONConfig config;

    /**
     * Construct a DefaultJSONSolutionSerializer object given the properties indicated in {@see JSONSerializerConfig.java}
     *
     * @param config configuration of the JSON Serializer
     */
    public DefaultJSONSolutionSerializer(JSONConfig config) {
        super(config);
        var mapper = new ObjectMapper();
        if(config.isPretty()){
            writer = mapper.enable(SerializationFeature.INDENT_OUTPUT).writerWithDefaultPrettyPrinter();
        } else {
            writer = mapper.writer();
        }
        this.config = config;
    }

    /**
     * {@inheritDoc}
     *
     * JSON export method. Exports the given solution to the provided file in JSON format.
     */
    @Override
    public void export(BufferedWriter writer, S s) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * JSON export method. Exports the given solution to the provided file in JSON format.
     */
    @Override
    public void export(File f, S s) {
        if(config.isEnabled()){
            try {
                writer.writeValue(f,s);
            } catch (IOException e){
                throw new RuntimeException("IOException while writing to file: "+f.getAbsolutePath(), e);
            }
        }
    }
}
