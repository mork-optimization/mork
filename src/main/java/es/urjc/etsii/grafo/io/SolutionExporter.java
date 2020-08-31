package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

import java.io.File;

/**
 * Subclass to provide a custom implementation to export solutions to file.
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class SolutionExporter<S extends Solution<I>, I extends Instance> {
    /**
     * Custom export implementation. Exports the given solution to the provided file.
     * @param f Destination file
     * @param s Solution to export
     */
    public abstract void export(File f, S s);
}
