package es.urjc.etsii.grafo.solver.services;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceImporter;
import es.urjc.etsii.grafo.io.Result;
import es.urjc.etsii.grafo.io.serializers.DefaultJSONSolutionSerializer;
import es.urjc.etsii.grafo.io.serializers.JsonSerializer;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
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

import static es.urjc.etsii.grafo.util.IOUtil.createIfNotExists;
import static es.urjc.etsii.grafo.util.IOUtil.errorIfNotExists;

@Service
public class IOManager<S extends Solution<I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(IOManager.class.toString());

    @Value("${instances.path}")
    Map<String, String> instanceFolders;

    @Value("${solutions.path.out}")
    String solutionsOut;

    @Value("${errors.path}")
    String errorFolder;

    // Results CSV --> date to string
    // Solution file --> instance_algoritm
    private final JsonSerializer jsonSerializer;
    private final List<ResultsSerializer> resultsSerializers;
    private final InstanceImporter<?> instanceImporter;
    private SolutionSerializer<S, I> solutionSerializer;

    public IOManager(JsonSerializer jsonSerializer, List<ResultsSerializer> resultsSerializers, InstanceImporter<?> instanceImporter, List<SolutionSerializer<S, I>> solutionSerializers) {
        this.jsonSerializer = jsonSerializer;
        this.instanceImporter = instanceImporter;
        this.solutionSerializer = Orquestrator.decideImplementation(solutionSerializers, DefaultJSONSolutionSerializer.class);
        this.resultsSerializers = resultsSerializers;

        log.info("Using solution exporter: "+this.solutionSerializer.getClass().getTypeName());
    }

    public Stream<? extends Instance> getInstances(String experimentName){
        String instancePath = this.instanceFolders.getOrDefault(experimentName, this.instanceFolders.get("default"));
        try {
            errorIfNotExists(instancePath);
            createIfNotExists(this.solutionsOut);
            createIfNotExists(this.errorFolder);

            return Files.walk(Path.of(instancePath)).filter(Files::isRegularFile).sorted(Comparator.comparing(f -> f.toFile().getName().toLowerCase())).map(this::loadInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveResults(String experimentName, List<Result> result){
        for (ResultsSerializer serializer : resultsSerializers) {
            serializer.serializeResults(experimentName, result);
        }
    }

    private Instance loadInstance(Path p){
        return this.instanceImporter.importInstance(p.toFile());
    }

    public void exportSolution(String experimentName, Algorithm<S,I> alg, S s){
        log.fine(String.format("Exporting solution for algorithm %s using %s", alg.getClass().getSimpleName(), solutionSerializer.getClass().getSimpleName()));
        String filename = experimentName + "__" + s.getInstance().getName() + "__" + alg.getShortName();
        File f = new File(solutionsOut, filename);
        solutionSerializer.export(f, s);
    }

    public synchronized void exportError(String experimentName, Algorithm<S,I> alg, I i, Throwable t, String stacktrace){
        // Directamente desde aqui, si se quiere customizar se puede pisar el DefaultExceptionHandler
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss.SSS");
        Date d = new Date();
        String filename = experimentName + "_" + sdf.format(d) + "_.json";
        var errorData = Map.of("Algorithm", alg, "InstanceName", i.getName(), "StackTrace", stacktrace, "Error", t);
        var p = Path.of(errorFolder, filename);
        try (var outputStream = Files.newOutputStream(p)){
            var writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
            writer.writeValue(outputStream, errorData);
        } catch (IOException e) {
            throw new RuntimeException("Additional error while writting errors to path " + p, e);
        }

    }
}
