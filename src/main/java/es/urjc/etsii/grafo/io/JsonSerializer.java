package es.urjc.etsii.grafo.io;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic JSON serializer / deserializer
 */
@Service
public class JsonSerializer {

    private static final Logger log = Logger.getLogger(JsonSerializer.class.getCanonicalName());
    private static final Pattern FILE_PATTERN = Pattern.compile("(.*)\\.(.*)");
    private static final String JSON_EXTENSION = ".json";

    private final ObjectMapper mapper;

    public JsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("es.urjc.etsii.grafo.io.Instance")
                .build());
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        this.mapper = mapper;
    }

    @PostConstruct
    private void findInstanceClases(){
        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Instance.class));

        var components = provider.findCandidateComponents("es/urjc/etsii/grafo");
        for (var component : components)
        {
            try {
                mapper.registerSubtypes(Class.forName(component.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    public Instance loadInstance(File f) {
        try {
            return mapper.readValue(f, Instance.class);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Jackson serializer failed while reading file: %s", f.getAbsolutePath()), e);
        }
    }

    public Instance loadInstance(String s){
        return loadInstance(new File(s));
    }

    public void saveInstance(Instance i, String s){
        saveInstance(i, new File(s));
    }

    public void saveInstance(Instance i, File f) {
        try {
            mapper.writeValue(f, i);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Jackson serializer failed while writing instance file: %s", f.getAbsolutePath()), e);
        }
    }

    public void saveResult(Result s, File f) {
        try {
            mapper.writeValue(f, s);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Jackson serializer failed while writing results file: %s", f.getAbsolutePath()), e);
        }
    }

//    /**
//     * Standarize all raw instance files to JSON format, using the user provided converted
//     * @param path Folder where instances are stored
//     * @param importer User implemented importer
//     * @param force Delete converted files
//     */
//    public static void importAll(String path, DataImporter<? extends Instance> importer, boolean force){
//        var serializer = new JsonSerializer();
//
//        var dir = new File(path);
//        if(!dir.isDirectory()){
//            throw new IllegalArgumentException(String.format("Path (%s) is not a directory", path));
//        }
//        for(File f: Objects.requireNonNull(dir.listFiles())){
//            Matcher m = FILE_PATTERN.matcher(f.getAbsolutePath());
//            if(!m.matches()){
//                throw new IllegalArgumentException("Invalid filename: " + f.getName());
//            }
//            var folder = f.getParent();
//            var filename = m.group(1);
//            var extension = m.group(2);
//            log.fine(String.format("Processing file with path (%s), name (%s), extension (%s)", folder, filename, extension));
//            File destination = new File(filename + JSON_EXTENSION);
//            if(destination.exists()){
//                if(force && !destination.delete()){
//                    throw new IllegalStateException(String.format("Failed delete of file %s", destination.getAbsolutePath()));
//                } else {
//                    log.fine(String.format("Already converted, ignoring file: %s",destination.getAbsolutePath()));
//                    continue;
//                }
//            }
//            log.info("Importing file: " + f.getName());
//            Instance importedInstance = importer.importInstance(f);
//            serializer.saveInstance(importedInstance, destination);
//        }
//    }
}
