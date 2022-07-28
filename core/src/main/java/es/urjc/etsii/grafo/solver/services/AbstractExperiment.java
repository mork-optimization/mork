package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.InheritedComponent;

import java.util.List;

/**
 * Defines an experiment to execute.
 * Each experiment is defined by extending this class, multiple experiments can be defined at the same time.
 * By default, all experiments are executed at runtime, but this behaviour can be changed in the application.yml configuration file.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class AbstractExperiment<S extends Solution<S,I>, I extends Instance> {

    /**
     * Initialize common fields for all experiments
     *
     */
    protected AbstractExperiment() {}

    /**
     * Get list of algorithms defined in this experiment. Each experiment can define a different set of algorithms.
     * Although the same algorithm can be used several times in the same experiment,
     * if you return the same configuration for the same algorithm an exception will be thrown.
     *
     * @return list of algorithms defined in this experiment
     */
    public abstract List<Algorithm<S, I>> getAlgorithms();

    /**
     * Return the current experiment name. If not overridden, defaults to the Java class name.
     *
     * @return Experiment name as string
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
