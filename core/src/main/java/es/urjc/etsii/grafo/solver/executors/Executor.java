package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.SimplifiedResult;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.services.IOManager;
import es.urjc.etsii.grafo.solver.services.MorkLifecycle;
import es.urjc.etsii.grafo.solver.services.SolutionValidator;
import es.urjc.etsii.grafo.solver.services.events.AbstractSolutionGeneratedHandler;
import es.urjc.etsii.grafo.solver.services.events.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.util.RandomManager;

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
    private final List<AbstractSolutionGeneratedHandler<S,I>> solutionGeneratedEventHandlers;
    private IOManager<S, I> io;

    protected Executor(Optional<SolutionValidator<S, I>> validator, List<AbstractSolutionGeneratedHandler<S, I>> solutionGeneratedEventHandlers, IOManager<S, I> io) {
        this.validator = validator;
        this.solutionGeneratedEventHandlers = solutionGeneratedEventHandlers;
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
     * @return Experiment results for the given instance
     */
    public abstract Collection<SimplifiedResult> execute(String experimentName, I ins, int repetitions, List<Algorithm<S,I>> algorithms, SolutionBuilder<S,I> solutionBuilder, ExceptionHandler<S,I> exceptionHandler);

    /**
     * Finalize and destroy all resources, we have finished and are shutting down now.
     */
    public abstract void shutdown();

    public void validate(S s){
        this.validator.ifPresent(validator -> validator.validate(s));
    }

    protected WorkUnit doWork(String experimentName, I ins, SolutionBuilder<S, I> solutionBuilder, Algorithm<S, I> algorithm, int i, ExceptionHandler<S,I> exceptionHandler) {
        try {
            // If app is stopping do not run algorithm
            if(MorkLifecycle.stop()) {
                return null;
            }

            RandomManager.reset(i);
            long starTime = System.nanoTime();
            var solution = algorithm.algorithm(ins, solutionBuilder);
            long endTime = System.nanoTime();
            long timeToTarget = solution.getLastModifiedTime() - starTime;
            long ellapsedTime = endTime - starTime;
            solution.setExecutionTimeInNanos(ellapsedTime);
            validate(solution);
            io.exportSolution(experimentName, algorithm, solution);
            dispatchEvents(new SolutionGeneratedEvent<>(i, ellapsedTime, solution, experimentName, algorithm));
            System.out.format("\t%s.\tTime: %.3f (s) \tTTB: %.3f (s) \t%s -- \n", i +1, ellapsedTime / 1_000_000_000D, timeToTarget / 1000_000_000D, solution);
            return new WorkUnit(ellapsedTime, timeToTarget, solution);
        } catch (Exception e) {
            exceptionHandler.handleException(experimentName, e, ins, algorithm, io);
            // todo more fields for WorkUnit, resume operations, failed, etc
            return null;
        }
    }

    private void dispatchEvents(SolutionGeneratedEvent<S, I> solutionGeneratedEvent) {
        log.fine("Dispatching solution generated events iteration %s");
        for (AbstractSolutionGeneratedHandler<S, I> handler : solutionGeneratedEventHandlers) {
            long start = System.nanoTime();
            handler.onSolutionGenerated(solutionGeneratedEvent);
            long end = System.nanoTime();
            log.fine(String.format("Handler %s took %s milliseconds", handler.getClass().getSimpleName(), (end-start)/1_000_000));
        }
    }

    class WorkUnit {
        private long ellapsedTime;
        private long timeToTarget;
        private S s;

        public WorkUnit(long ellapsedTime, long timeToTarget, S s) {
            this.ellapsedTime = ellapsedTime;
            this.timeToTarget = timeToTarget;
            this.s = s;
        }

        public long getEllapsedTime() {
            return ellapsedTime;
        }

        public long getTimeToTarget() {
            return timeToTarget;
        }

        public S getSolution() {
            return s;
        }
    }
}
