package es.urjc.etsii.grafo.services;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.config.ErrorConfig;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.serializers.SolutionExportFrequency;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.util.IOUtil.createFolder;

/**
 * IO Service to export solutions, errors and load instances
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@Service
public class IOManager<S extends Solution<S, I>, I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(IOManager.class);

    private final ErrorConfig errorConfig;

    private final List<SolutionSerializer<S, I>> solutionSerializers;

    /**
     * Initialize IOManager
     *
     * @param errorConfig         error configuration
     * @param solutionSerializers solution serializers
     */
    public IOManager(ErrorConfig errorConfig, List<SolutionSerializer<S, I>> solutionSerializers) {
        this.errorConfig = errorConfig;
        this.solutionSerializers = solutionSerializers;

        log.debug("Detected solution serializers: " + this.solutionSerializers);
    }

    /**
     * Write a solution to disk.
     *
     * @param r       work unit result
     * @param current current frequency
     */
    public void exportSolution(WorkUnitResult<S, I> r, SolutionExportFrequency current) {
        // String experimentName, Algorithm<S,I> alg, S solution, String iteration,
        for (var serializer : this.solutionSerializers) {
            if (serializer.isEnabled() && serializer.getConfig().getFrequency().matches(current)) {
                serializer.exportSolution(r);
            }
        }
    }

    /**
     * Export an error to disk
     *
     * @param experimentName current experiment name
     * @param alg            algorithm where this error generated from
     * @param i              instance being solved when the error was thrown
     * @param t              Error thrown
     * @param stacktrace     Error stacktrace as string
     */
    public synchronized void exportError(String experimentName, Algorithm<S, I> alg, I i, Throwable t, String stacktrace) {
        if (!errorConfig.isErrorsToFile()) {
            log.debug("Skipping exporting exception or error to disk, disabled in config.");
            return;
        }
        createFolder(this.errorConfig.getFolder());

        // Directamente desde aqui, si se quiere customizar se puede pisar el DefaultExceptionHandler
        var formatter = DateTimeFormatter.ofPattern("HH.mm.ss.SSS");
        LocalDateTime d = LocalDateTime.now();
        String filename = experimentName + "_" + formatter.format(d) + "_.json";
        var errorData = Map.of("Algorithm", alg, "InstanceName", i.getId(), "StackTrace", stacktrace, "Error", t);
        var p = Path.of(this.errorConfig.getFolder(), filename);
        try (var outputStream = Files.newOutputStream(p)) {
            var writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
            writer.writeValue(outputStream, errorData);
        } catch (IOException e) {
            throw new RuntimeException("Additional error while writting errors to path " + p, e);
        }

    }
}
