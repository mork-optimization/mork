package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

import java.io.*;
import java.util.logging.Logger;

/**
 * Subclass to provide a custom implementation to export solutions to file.
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class SolutionSerializer<S extends Solution<I>, I extends Instance> {
    private static final Logger log = Logger.getLogger(SolutionSerializer.class.getName());

    /**
     * Custom export implementation. Exports the given solution to the provided file.
     * @param f Destination file
     * @param s Solution to export
     */
    public void export(File f, S s){
        try (var bw = new BufferedWriter(new FileWriter(f))){
            this.export(bw, s);
        } catch (IOException e) {
            log.severe("IOException exporting solution, skipping: " + e);
        }
    }

    /**
     * Custom export implementation. Exports the given solution to disk.
     * You do not need to handle IOExceptions. If an IOException occurs,
     * the given solution export is skipped.
     * @param writer Output, write data here
     * @param s Solution to export
     */
    public abstract void export(BufferedWriter writer, S s) throws IOException;
}
