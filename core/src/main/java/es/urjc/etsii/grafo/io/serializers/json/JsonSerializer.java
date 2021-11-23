//package es.urjc.etsii.grafo.io.serializers;
//
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.databind.*;
//import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
//import es.urjc.etsii.grafo.io.Instance;
//import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
//import org.springframework.core.type.filter.AssignableTypeFilter;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//import java.io.IOException;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//
///**
// * Generic JSON serializer / deserializer
// */
//@Service
//public class JsonSerializer {
//
//    private static final Logger log = Logger.getLogger(JsonSerializer.class.getCanonicalName());
//    private static final Pattern FILE_PATTERN = Pattern.compile("(.*)\\.(.*)");
//    private static final String JSON_EXTENSION = ".json";
//
//    private final ObjectMapper objectMapper;
//
//    public JsonSerializer() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.findAndRegisterModules();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        mapper.enable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
//        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
//        mapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
//                .allowIfBaseType("es.urjc.etsii.grafo.io.Instance")
//                .build());
//        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
//                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
//                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
//                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
//                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
//        this.objectMapper = mapper;
//    }
//
//    @PostConstruct
//    private void findInstanceClasses(){
//        var provider = new ClassPathScanningCandidateComponentProvider(false);
//        provider.addIncludeFilter(new AssignableTypeFilter(Instance.class));
//
//        var components = provider.findCandidateComponents("es/urjc/etsii/grafo");
//        for (var component : components)
//        {
//            try {
//                objectMapper.registerSubtypes(Class.forName(component.getBeanClassName()));
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    public Instance loadInstance(File f) {
//        try {
//            return objectMapper.readValue(f, Instance.class);
//        } catch (IOException e) {
//            throw new RuntimeException(String.format("Jackson serializer failed while reading file: %s", f.getAbsolutePath()), e);
//        }
//    }
//
//    public Instance loadInstance(String s){
//        return loadInstance(new File(s));
//    }
//
//    public void saveInstance(Instance i, String s){
//        saveInstance(i, new File(s));
//    }
//
//    public void saveInstance(Instance i, File f) {
//        try {
//            objectMapper.writeValue(f, i);
//        } catch (IOException e) {
//            throw new RuntimeException(String.format("Jackson serializer failed while writing instance file: %s", f.getAbsolutePath()), e);
//        }
//    }
//}
