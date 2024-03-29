package es.urjc.etsii.grafo.experiment;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;

/**
 * Declared experiment DTO. Contains experiment name, class where experiment was declared, and list of algorithms declared
 * @param <S> Solution class
 * @param <I> Instance class
 */
public record Experiment<S extends Solution<S, I>, I extends Instance>(String name, Class<?> experimentClass, List<Algorithm<S,I>> algorithms) {

}
