package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.IOUtil.createFolder;

/**
 * Subclass to provide a custom implementation to export solutions to file.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class SolutionSerializer<S extends Solution<S, I>, I extends Instance> {
    private static final Logger log = Logger.getLogger(SolutionSerializer.class.getName());

    private final AbstractSerializerConfig config;

    /**
     * Create a new solution serializer with the given config
     *
     * @param config
     */
    public SolutionSerializer(AbstractSerializerConfig config) {
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
     * @param s              solution to serialize to disk
     */
    public void exportSolution(String experimentName, Algorithm<S, I> alg, S s) {
        log.fine(String.format("Exporting solution for (exp, instance, algorithm) = (%s, %s, %s) using %s", experimentName, s.getInstance().getName(), alg.getClass().getSimpleName(), this.getClass().getSimpleName()));
        String filename = getFilename(experimentName, s.getInstance().getName(), alg.getShortName());
        var solutionFolder = this.config.getFolder();
        createFolder(solutionFolder);
        File f = new File(solutionFolder, filename);
        this.export(f, s);
    }

    /**
     * Get filename
     *
     * @param experimentName experiment name
     * @return the file name
     */
    protected String getFilename(String experimentName, String instanceName, String shortAlgName) {
        String prefix = experimentName + "_" + instanceName + "_" + shortAlgName + "_";
        String name = new SimpleDateFormat(config.getFormat()).format(new Date()); // Use current date
        return prefix + name;
    }

    /**
     * Custom export implementation. Exports the given solution to the provided file.
     *
     * @param f Destination file
     * @param s Solution to export
     */
    public void export(File f, S s) {
        try (var bw = new BufferedWriter(new FileWriter(f))) {
            this.export(bw, s);
        } catch (IOException e) {
            log.severe("IOException exporting solution, skipping: " + e);
        }
    }

    /**
     * Custom export implementation. Exports the given solution to disk.
     * You do not need to handle IOExceptions. If an IOException occurs,
     * the given solution export is skipped.
     *
     * @param writer Output, write data here
     * @param s      Solution to export
     * @throws java.io.IOException exception thrown in case something goes wrong
     */
    public abstract void export(BufferedWriter writer, S s) throws IOException;
}
