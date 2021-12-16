package es.urjc.etsii.grafo.solver.services;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.etsii.grafo.ErrorConfig;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.IOUtil.createFolder;

/**
 * IO Service to export solutions, errors and load instances
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@Service
public class IOManager<S extends Solution<S,I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(IOManager.class.toString());

    private final ErrorConfig errorConfig;

    private List<SolutionSerializer<S, I>> solutionSerializers;

    /**
     * Initialize IOManager
     *
     * @param errorConfig error configuration
     * @param solutionSerializers solution serializers
     */
    public IOManager(ErrorConfig errorConfig, List<SolutionSerializer<S, I>> solutionSerializers) {
        this.errorConfig = errorConfig;
        this.solutionSerializers = solutionSerializers;

        log.info("Using solution serializers: "+this.solutionSerializers);
    }

    /**
     * Write a solution to disk.
     *
     * @param experimentName current experiment name
     * @param alg algorithm that generated this solution
     * @param s solution to serialize to disk
     */
    public void exportSolution(String experimentName, Algorithm<S,I> alg, S s){
        for(var serializer: this.solutionSerializers){
            if(serializer.isEnabled()){
                serializer.exportSolution(experimentName, alg, s);
            }
        }
    }

    /**
     * Export an error to disk
     *
     * @param experimentName current experiment name
     * @param alg algorithm where this error generated from
     * @param i instance being solved when the error was thrown
     * @param t Error thrown
     * @param stacktrace Error stacktrace as string
     */
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
