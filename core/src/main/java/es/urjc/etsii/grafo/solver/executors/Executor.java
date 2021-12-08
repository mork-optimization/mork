package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.experiment.Experiment;
import es.urjc.etsii.grafo.solver.services.*;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.ErrorEvent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Processes work units
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class Executor<S extends Solution<S,I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(Executor.class.getName());

    protected final Optional<SolutionValidator<S,I>> validator;
    protected final IOManager<S, I> io;
    protected final InstanceManager<I> instanceManager;
    protected final List<ReferenceResultProvider> referenceResultProviders;
    protected final SolverConfig solverConfig;


    /**
     * Fill common values used by all executors
     *  @param validator solution validator if available
     * @param io IO manager
     * @param referenceResultProviders
     */
    protected Executor(
            Optional<SolutionValidator<S, I>> validator,
            IOManager<S, I> io,
            InstanceManager<I> instanceManager,
            List<ReferenceResultProvider> referenceResultProviders,
            SolverConfig solverConfig
    ) {
        this.referenceResultProviders = referenceResultProviders;
        this.solverConfig = solverConfig;
        if(validator.isEmpty()){
            log.warning("No SolutionValidator implementation has been found, solution CORRECTNESS WILL NOT BE CHECKED");
        } else {
            log.info("SolutionValidator implementation found: " + validator.get().getClass().getSimpleName());
        }

        this.validator = validator;
        this.io = io;
        this.instanceManager = instanceManager;
    }

    public abstract void executeExperiment(Experiment<S,I> experiment, List<String> instanceNames, ExceptionHandler<S, I> exceptionHandler, long startTimestamp);

    /**
     * Finalize and destroy all resources, we have finished and are shutting down now.
     */
    public abstract void shutdown();

    /**
     * Run both user specific validations and our own.
     *
     * @param solution Solution to check.
     */
    public void validate(S solution){
        ValidationUtil.positiveTTB(solution);
        this.validator.ifPresent(validator -> validator.validate(solution));
    }

    /**
     * Execute a single iteration for the given (experiment, intance, algorithm, iterationId)
     *
     * @param experimentName experiment name
     * @param instance instance
     * @param algorithm current algorithm
     * @param i iteration id
     * @param exceptionHandler exception handler
     */
    protected void doWork(String experimentName, I instance, Algorithm<S, I> algorithm, int i, ExceptionHandler<S,I> exceptionHandler) {
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

    protected Optional<Double> getOptionalReferenceValue(List<ReferenceResultProvider> provider, Instance instance){
        double best = this.solverConfig.isMaximizing()? Double.MIN_VALUE: Double.MAX_VALUE;
        for(var r: referenceResultProviders){
            double score = r.getValueFor(instance.getName()).getScoreOrNan();
            // Ignore if not valid value
            if (Double.isFinite(score)) {
                if(this.solverConfig.isMaximizing()){
                    best = Math.max(best, score);
                } else {
                    best = Math.min(best, score);
                }
            }
        }
        if(best == Double.MAX_VALUE || best == Double.MIN_VALUE){
            return Optional.empty();
        } else {
            return Optional.of(best);
        }
    }
}
