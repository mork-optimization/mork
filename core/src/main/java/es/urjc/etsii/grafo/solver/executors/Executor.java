package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.services.IOManager;
import es.urjc.etsii.grafo.solver.services.MorkLifecycle;
import es.urjc.etsii.grafo.solver.services.SolutionValidator;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.ErrorEvent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Processes work units and returns results
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class Executor<S extends Solution<S,I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(Executor.class.getName());

    private final Optional<SolutionValidator<S,I>> validator;
    private final IOManager<S, I> io;

    protected Executor(Optional<SolutionValidator<S, I>> validator, IOManager<S, I> io) {
        if(validator.isEmpty()){
            log.warning("No SolutionValidator implementation has been found, solution CORRECTNESS WILL NOT BE CHECKED");
        } else {
            log.info("SolutionValidator implementation found: " + validator.get().getClass().getSimpleName());
        }

        this.validator = validator;
        this.io = io;
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

    /**
     * Run both user specific validations and our own.
     * @param solution Solution to check.
     */
    public void validate(S solution){
        ValidationUtil.positiveTTB(solution);
        this.validator.ifPresent(validator -> validator.validate(solution));
    }

    protected void doWork(String experimentName, I instance, SolutionBuilder<S, I> solutionBuilder, Algorithm<S, I> algorithm, int i, ExceptionHandler<S,I> exceptionHandler) {
        S solution = null;
        try {
            // If app is stopping do not run algorithm
            if(MorkLifecycle.stop()) {
                return;
            }

            RandomManager.reset(i);
            long starTime = System.nanoTime();
            solution = algorithm.algorithm(instance);
            long endTime = System.nanoTime();
            long timeToTarget = solution.getLastModifiedTime() - starTime;
            long executionTime = endTime - starTime;
            validate(solution);
            io.exportSolution(experimentName, algorithm, solution);
            EventPublisher.publishEvent(new SolutionGeneratedEvent<>(i, solution, experimentName, algorithm, executionTime, timeToTarget));
            log.info(String.format("\t%s.\tT(s): %.3f \tTTB(s): %.3f \t%s", i +1, executionTime / 1_000_000_000D, timeToTarget / 1000_000_000D, solution));
        } catch (Exception e) {
            exceptionHandler.handleException(experimentName, e, Optional.ofNullable(solution), instance, algorithm, io);
            EventPublisher.publishEvent(new ErrorEvent(e));
        }
    }
}
