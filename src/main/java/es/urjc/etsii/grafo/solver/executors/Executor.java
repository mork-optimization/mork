package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.Result;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.services.InheritedComponent;
import es.urjc.etsii.grafo.solver.services.SolutionValidator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Processes work units and returns results
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class Executor<S extends Solution<I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(Executor.class.getName());

    private final Optional<SolutionValidator<S,I>> validator;

    protected Executor(Optional<SolutionValidator<S,I>> validator) {
        this.validator = validator;
        if(validator.isEmpty()){
            log.warning("No SolutionValidator implementation has been found, solution correctness will not be validated");
        } else {
            log.info("SolutionValidator implementation found: " + this.validator.get().getClass().getSimpleName());
        }
    }

    /**
     * Execute all the available algorithms for the given instance, repeated N times
     * @param ins Instance
     * @param repetitions Number of repetitions
     * @param algorithms Algorithm list
     * @return Experiment results for the given instance
     */
    public abstract Collection<Result> execute(I ins, int repetitions, List<Algorithm<S,I>> algorithms, SolutionBuilder<S,I> solutionBuilder,  ExceptionHandler<S,I> exceptionHandler);

    /**
     * Finalize and destroy all resources, we have finished and are shutting down now.
     */
    public abstract void shutdown();

    public void validate(S s){
        this.validator.ifPresent(validator -> validator.validate(s));
    }
}
