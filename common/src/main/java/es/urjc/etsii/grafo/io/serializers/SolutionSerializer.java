package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static es.urjc.etsii.grafo.util.IOUtil.createFolder;

/**
 * Subclass to provide a custom implementation to export solutions to file.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class SolutionSerializer<S extends Solution<S, I>, I extends Instance> {
    private static final Logger log = LoggerFactory.getLogger(SolutionSerializer.class);
    private static final long WARN_THRESHOLD = 500 * TimeUtil.NANOS_IN_MILLISECOND; // 500ms

    private volatile boolean warnedSlow = false;

    private final AbstractSolutionSerializerConfig config;

    /**
     * Create a new solution serializer with the given config
     *
     * @param config Common solution serializer configuration
     */
    protected SolutionSerializer(AbstractSolutionSerializerConfig config) {
        this.config = config;
    }

    /**
     * Check if this serializer is enabled
     * @return true if enabled and ready to export, false otherwise.
     */
    public boolean isEnabled(){
        return this.config.isEnabled();
    }

    /**
     * Write a solution to disk.
     *
     * @param experimentName current experiment name
     * @param alg            algorithm that generated this solution
     * @param solution              solution to serialize to disk
     */
    public final void exportSolution(String experimentName, Algorithm<S, I> alg, S solution, String iterationId) {
        log.debug("Exporting solution for (exp, instance, algorithm) = ({}, {}, {}) using {}", experimentName, solution.getInstance().getId(), alg.getShortName(), this.getClass().getSimpleName());
        String filename = getFilename(experimentName, solution.getInstance().getId(), alg.getShortName(), iterationId);
        var solutionFolder = this.config.getFolder();
        createFolder(solutionFolder);
        long start = System.nanoTime();
        this.export(solutionFolder, filename, solution);
        long elapsed = System.nanoTime() - start;
        if(elapsed > WARN_THRESHOLD && !warnedSlow){
            log.warn("Slow serialization detected in {}, last execution took {} ms. Consider using a faster format, or use an async event listener to improve performance", this.getClass().getSimpleName(), elapsed / TimeUtil.NANOS_IN_MILLISECOND);
            warnedSlow = true;
        }
    }

    /**
     * Get filename
     *
     * @param experimentName experiment name
     * @return the file name
     */
    protected String getFilename(String experimentName, String instanceName, String shortAlgName, String iterationId) {
        String prefix = experimentName + "_" + instanceName + "_" + shortAlgName + "_" + iterationId + "_";
        String name = LocalDateTime.now().format(DateTimeFormatter.ofPattern(config.getFormat())); // Use current date
        return prefix + name;
    }

    /**
     * Custom export implementation. Exports the given solution to the provided file.
     *
     * @param folder Folder where solutions should be stored according to the configuration
     * @param suggestedFilename Suggested filename, can be ignored by the implementation
     * @param solution Solution to export
     */
    public void export(String folder, String suggestedFilename, S solution) {
        var f = new File(folder, suggestedFilename);
        try (var bw = new BufferedWriter(new FileWriter(f))) {
            this.export(bw, solution);
        } catch (IOException e) {
            log.warn("IOException exporting solution, skipping.", e);
        }
    }

    /**
     * Custom export implementation. Exports the given solution to disk.
     * You do not need to handle IOExceptions. If an IOException occurs,
     * the given solution export is skipped.
     *
     * @param writer Output, write data here
     * @param solution      Solution to export
     * @throws IOException exception thrown in case something goes wrong
     */
    public abstract void export(BufferedWriter writer, S solution) throws IOException;

    public AbstractSolutionSerializerConfig getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
