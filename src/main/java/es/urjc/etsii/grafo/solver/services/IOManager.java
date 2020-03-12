package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.DataImporter;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.JsonSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
public class IOManager {

    private static final String CACHE_SUFFIX = ".cached";
    private static final Logger log = Logger.getLogger(IOManager.class.toString());

    @Value("${instances.path.in}")
    String instanceIn;

    @Value("${instances.path.cache}")
    String instanceCache;

    @Value("${solutions.path.temp}")
    String solutionsTemp;

    @Value("${solutions.path.out}")
    String solutionsOut;

    private final JsonSerializer serializer;
    private final DataImporter<?> dataImporter;

    public IOManager(JsonSerializer serializer, DataImporter<?> dataImporter) {
        this.serializer = serializer;
        this.dataImporter = dataImporter;
    }

    public Stream<? extends Instance> getInstances(){
        try {
            errorIfNotExists(this.instanceIn);

            createIfNotExists(this.solutionsOut);
            createIfNotExists(this.solutionsTemp);
            createIfNotExists(this.instanceCache);

            return Files.walk(Path.of(this.instanceIn)).filter(Files::isRegularFile).map(this::tryGetFromCache);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Instance tryGetFromCache(Path p){
        // Check if exists in cache, if it does not convert then load.
//        File cacheFile = new File(p.toFile().getName() + CACHE_SUFFIX);
//        if(!cacheFile.exists()){
//            log.info("Saving cached file: " + cacheFile.getAbsolutePath());
//            Instance imported = this.dataImporter.importInstance(p.toFile());
//            this.serializer.saveInstance(imported, cacheFile);
//        }
//
//        return this.serializer.loadInstance(cacheFile);
        return this.dataImporter.importInstance(p.toFile());
    }

    private static void createIfNotExists(String path) {
        File dir = new File(path);
        dir.mkdir();
        errorIfNotExists(path);
    }

    private static void errorIfNotExists(String path){
        File dir = new File(path);
        if(!dir.isDirectory()){
            throw new IllegalArgumentException("Path does not exist or not a folder: " + dir.getAbsolutePath());
        }
    }
}
