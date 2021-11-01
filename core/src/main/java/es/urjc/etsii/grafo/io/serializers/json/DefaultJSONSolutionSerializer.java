package es.urjc.etsii.grafo.io.serializers.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class DefaultJSONSolutionSerializer<S extends Solution<I>, I extends Instance> extends SolutionSerializer<S,I> {

    ObjectWriter writer;

    private final JSONSerializerConfig config;

    public DefaultJSONSolutionSerializer(JSONSerializerConfig config) {
        var mapper = new ObjectMapper();
        if(config.isPretty()){
            writer = mapper.enable(SerializationFeature.INDENT_OUTPUT).writerWithDefaultPrettyPrinter();
        } else {
            writer = mapper.writer();
        }
        this.config = config;
    }

    @Override
    public void export(BufferedWriter writer, S s) {
        throw new UnsupportedOperationException();
    }

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
