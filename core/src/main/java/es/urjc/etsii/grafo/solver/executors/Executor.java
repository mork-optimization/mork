package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.services.IOManager;
import es.urjc.etsii.grafo.solver.services.MorkLifecycle;
import es.urjc.etsii.grafo.solver.services.SolutionValidator;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.util.RandomManager;

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
    private IOManager<S, I> io;

    protected Executor(Optional<SolutionValidator<S, I>> validator, IOManager<S, I> io) {
        this.validator = validator;
        this.io = io;
        if(validator.isEmpty()){
            log.warning("No SolutionValidator implementation has been found, solution CORRECTNESS WILL NOT BE CHECKED");
        } else {
            log.info("SolutionValidator implementation found: " + this.validator.get().getClass().getSimpleName());
        }
    }

    /**
     * Execute all the available algorithms for the given instance, repeated N times
     * @param ins Instance
     * @param repetitions Number of repetitions
     * @param algorithms Algorithm list
     * @param experimentName Experiment name
     * @param exceptionHandler Exception handler, determines behaviour if anything fails
     * @param solutionBuilder Used to build solutions from instances
     */
    public abstract void execute(String experimentName, I ins, int repetitions, List<Algorithm<S,I>> algorithms, SolutionBuilder<S,I> solutionBuilder, ExceptionHandler<S,I> exceptionHandler);

    /**
     * Finalize and destroy all resources, we have finished and are shutting down now.
     */
    public abstract void shutdown();

    public void validate(S s){
        this.validator.ifPresent(validator -> validator.validate(s));
    }

    protected void doWork(String experimentName, I instance, SolutionBuilder<S, I> solutionBuilder, Algorithm<S, I> algorithm, int i, ExceptionHandler<S,I> exceptionHandler) {
        S solution = null;
        try {
            // If app is stopping do not run algorithm
            if(MorkLifecycle.stop()) {
                return;
            }

            RandomManager.reset(i);
            solution = solutionBuilder.initializeSolution(instance);
            long starTime = System.nanoTime();
            solution = algorithm.algorithm(solution);
            long endTime = System.nanoTime();
            long timeToTarget = solution.getLastModifiedTime() - starTime;
            long executionTime = endTime - starTime;
            solution.setExecutionTimeInNanos(executionTime);
            validate(solution);
            io.exportSolution(experimentName, algorithm, solution);
            EventPublisher.publishEvent(new SolutionGeneratedEvent<>(i, solution, experimentName, algorithm, executionTime, timeToTarget));
            System.out.format("\t%s.\tTime: %.3f (s) \tTTB: %.3f (s) \t%s -- \n", i +1, executionTime / 1_000_000_000D, timeToTarget / 1000_000_000D, solution);
        } catch (Exception e) {
            exceptionHandler.handleException(experimentName, e, Optional.ofNullable(solution), instance, algorithm, io);
        }
    }
}
