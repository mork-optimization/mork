package io;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.*;

import java.io.File;
import java.io.IOException;

/**
 * Generic JSON serializer / deserializer
 */
public class JsonSerializer implements InstanceLoader, ResultsExporter {

    private final ObjectReader instanceReader;
    private final ObjectWriter instanceWriter;
    private final ObjectWriter resultWriter;

    public JsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        instanceReader = mapper.readerFor(Instance.class);
        instanceWriter = mapper.writerFor(Instance.class);
        resultWriter = mapper.writerFor(Result.class);
    }

    @Override
    public Instance loadInstance(File f) {
        try {
            return instanceReader.readValue(f);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Jackson serializer failed while reading file: %s", f.getAbsolutePath()), e);
        }
    }

    public void saveInstance(Instance i, String s){
        saveInstance(i, new File(s));
    }

    public void saveInstance(Instance i, File f) {
        try {
            instanceWriter.writeValue(f, i);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Jackson serializer failed while writing instance file: %s", f.getAbsolutePath()), e);
        }
    }

    @Override
    public void saveResult(Result s, File f) {
        try {
            resultWriter.writeValue(f, s);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Jackson serializer failed while writing results file: %s", f.getAbsolutePath()), e);
        }
    }

    public static void ensureValid(File outputDir) {
        outputDir.mkdir();
        if(!outputDir.isDirectory() || outputDir.list().length > 0){
            throw new IllegalArgumentException(outputDir.isDirectory()? "Output is not empty": "Output is not a folder");
        }
    }
}
