package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.*;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static es.urjc.etsii.grafo.util.IOUtil.createIfNotExists;
import static es.urjc.etsii.grafo.util.IOUtil.errorIfNotExists;

@Service
public class IOManager<S extends Solution<I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(IOManager.class.toString());

    @Value("${instances.path.in}")
    String instanceIn;

    @Value("${instances.path.cache}")
    String instanceCache;

    @Value("${solutions.path.out}")
    String solutionsOut;

    @Value("${errors.path}")
    String errorFolder;

    // Results CSV --> date to string
    // Solution file --> instance_algoritm
    private final JsonSerializer jsonSerializer;
    private final List<ResultsSerializer> resultsSerializers;
    private final InstanceImporter<?> instanceImporter;
    private Optional<SolutionExporter<S, I>> solutionExporter;

    public IOManager(JsonSerializer jsonSerializer, List<ResultsSerializer> resultsSerializers, InstanceImporter<?> instanceImporter, Optional<SolutionExporter<S, I>> solutionExporter) {
        this.jsonSerializer = jsonSerializer;
        this.instanceImporter = instanceImporter;
        this.solutionExporter = solutionExporter;
        this.resultsSerializers = resultsSerializers;
    }

    public Stream<? extends Instance> getInstances(){
        try {
            errorIfNotExists(this.instanceIn);
            createIfNotExists(this.solutionsOut);
            createIfNotExists(this.instanceCache);

            return Files.walk(Path.of(this.instanceIn)).filter(Files::isRegularFile).sorted(Comparator.comparing(f -> f.toFile().getName().toLowerCase())).map(this::tryGetFromCache);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveResults(List<Result> result){
        for (ResultsSerializer serializer : resultsSerializers) {
            serializer.serializeResults(result);
        }
    }

    private Instance tryGetFromCache(Path p){
        return this.instanceImporter.importInstance(p.toFile());
    }

    // TODO call method
    private void exportSolution(Algorithm<S,I> alg, S s){
        if(this.solutionExporter.isEmpty()){
            log.fine("Skipping solution export, no implementation of SolutionExporter found");
            return;
        }
        String filename = s.getInstance().getName() + "__" + alg.getShortName();
        File f = new File(solutionsOut, filename);
        SolutionExporter<S,I> exporter = solutionExporter.get();
        exporter.export(f, s);
    }

    //    private Instance tryGetFromCache(Path p){
//         Check if exists in cache, if it does not convert then load.
//        File cacheFile = new File(p.toFile().getName() + CACHE_SUFFIX);
//        if(!cacheFile.exists()){
//            log.info("Saving cached file: " + cacheFile.getAbsolutePath());
//            Instance imported = this.dataImporter.importInstance(p.toFile());
//            this.serializer.saveInstance(imported, cacheFile);
//        }
//
//        return this.serializer.loadInstance(cacheFile);
//    }
}
