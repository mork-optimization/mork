package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.Result;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.services.InheritedComponent;

import java.util.Collection;
import java.util.List;

/**
 * Processes work units and returns results
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class Executor<S extends Solution<I>, I extends Instance> {

    /**
     * Execute all the available algorithms for the given instance, repeated N times
     * @param ins Instance
     * @param repetitions Number of repetitions
     * @param algorithms Algorithm list
     * @return Experiment results for the given instance
     */
    public abstract Collection<Result> execute(I ins, int repetitions, List<Algorithm<S,I>> algorithms, SolutionBuilder<S,I> solutionBuilder,  ExceptionHandler<S,I> exceptionHandler);
}
