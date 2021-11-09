package es.urjc.etsii.grafo.solver.services;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.etsii.grafo.ErrorConfig;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceImporter;
import es.urjc.etsii.grafo.io.SerializerSolutionCommonConfig;
import es.urjc.etsii.grafo.io.serializers.json.DefaultJSONSolutionSerializer;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.configuration.InstanceConfiguration;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static es.urjc.etsii.grafo.util.IOUtil.*;

@Service
public class IOManager<S extends Solution<S,I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(IOManager.class.toString());

    private final InstanceConfiguration instanceConfiguration;
    private final ErrorConfig errorConfig;
    private final SerializerSolutionCommonConfig solCommonConfig;

    private final InstanceImporter<I> instanceImporter;
    private SolutionSerializer<S, I> solutionSerializer;

    public IOManager(InstanceConfiguration instanceConfiguration, ErrorConfig errorConfig, SerializerSolutionCommonConfig serializerSolutionCommonConfig, InstanceImporter<I> instanceImporter, List<SolutionSerializer<S, I>> solutionSerializers) {
        this.instanceConfiguration = instanceConfiguration;
        this.errorConfig = errorConfig;
        this.solCommonConfig = serializerSolutionCommonConfig;
        this.instanceImporter = instanceImporter;
        this.solutionSerializer = Orquestrator.decideImplementation(solutionSerializers, DefaultJSONSolutionSerializer.class);

        log.info("Using solution exporter: "+this.solutionSerializer.getClass().getTypeName());
    }

    public Stream<I> getInstances(String experimentName){
        String instancePath = this.instanceConfiguration.getPath(experimentName);
        try {
            checkExists(instancePath);
            createFolder(this.solCommonConfig.getFolder());

            return Files.walk(Path.of(instancePath)).filter(IOManager::filesFilter).sorted(Comparator.comparing(f -> f.toFile().getName().toLowerCase())).map(this::loadInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean filesFilter(Path p){
        try {
            if(Files.isHidden(p)) {
                return false;
            }
        } catch (IOException e) {
            log.warning("Error while reading file attributes, skipping instance file: " + p.toAbsolutePath());
            return false;
        }
        return Files.isRegularFile(p);
    }

    public I loadInstance(Path p){
        return this.instanceImporter.importInstance(p.toFile());
    }

    public void exportSolution(String experimentName, Algorithm<S,I> alg, S s){
        log.fine(String.format("Exporting solution for algorithm %s using %s", alg.getClass().getSimpleName(), solutionSerializer.getClass().getSimpleName()));
        String filename = experimentName + "__" + s.getInstance().getName() + "__" + alg.getShortName();
        File f = new File(this.solCommonConfig.getFolder(), filename);
        solutionSerializer.export(f, s);
    }

    public synchronized void exportError(String experimentName, Algorithm<S,I> alg, I i, Throwable t, String stacktrace){
        if(!errorConfig.isErrorsToFile()){
            log.fine("Skipping exporting exception or error to disk, disabled in config.");
            return;
        }
        createFolder(this.errorConfig.getFolder());

        // Directamente desde aqui, si se quiere customizar se puede pisar el DefaultExceptionHandler
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss.SSS");
        Date d = new Date();
        String filename = experimentName + "_" + sdf.format(d) + "_.json";
        var errorData = Map.of("Algorithm", alg, "InstanceName", i.getName(), "StackTrace", stacktrace, "Error", t);
        var p = Path.of(this.errorConfig.getFolder(), filename);
        try (var outputStream = Files.newOutputStream(p)){
            var writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
            writer.writeValue(outputStream, errorData);
        } catch (IOException e) {
            throw new RuntimeException("Additional error while writting errors to path " + p, e);
        }

    }
}
