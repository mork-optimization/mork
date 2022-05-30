package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.experiment.Experiment;
import es.urjc.etsii.grafo.solver.services.*;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.ErrorEvent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.util.ValidationUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

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
     * @param validator solution validator if available
     * @param io IO manager
     * @param referenceResultProviders list of all reference value providers implementations
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
     * Execute a single iteration for the given (experiment, instance, algorithm, iterationId)
     *
     * @param workUnit Minimum unit of work, cannot be divided further.
     */
    protected WorkUnitResult<S,I> doWork(WorkUnit<S,I> workUnit) {
        S solution = null;
        I instance = this.instanceManager.getInstance(workUnit.instancePath());

        try {
            // If app is stopping do not run algorithm
            if(Global.stop()) {
                return null;
            }
            RandomManager.reset(workUnit.i());
            long starTime = System.nanoTime();
            solution = workUnit.algorithm().algorithm(instance);
            long endTime = System.nanoTime();
            long timeToTarget = solution.getLastModifiedTime() - starTime;
            long executionTime = endTime - starTime;
            validate(solution);
            return new WorkUnitResult<>(workUnit, solution, executionTime, timeToTarget);
        } catch (Exception e) {
            workUnit.exceptionHandler().handleException(workUnit.experimentName(), e, Optional.ofNullable(solution), instance, workUnit.algorithm(), io);
            EventPublisher.getInstance().publishEvent(new ErrorEvent(e));
            return null;
        }
    }

    protected void processWorkUnitResult(WorkUnitResult<S,I> r){
        io.exportSolution(r.workUnit().experimentName(), r.workUnit().algorithm(), r.solution(), r.workUnit().i());
        EventPublisher.getInstance().publishEvent(new SolutionGeneratedEvent<>(r.workUnit().i(), r.solution(), r.workUnit().experimentName(), r.workUnit().algorithm(), r.executionTime(), r.timeToTarget()));
        log.info(String.format("\t%s.\tT(s): %.3f \tTTB(s): %.3f \t%s", r.workUnit().i() + 1, nanosToSecs(r.executionTime()), nanosToSecs(r.timeToTarget()), r.solution()));
    }

    protected Optional<Double> getOptionalReferenceValue(String instanceName){
        double best = this.solverConfig.isMaximizing()? Double.MIN_VALUE: Double.MAX_VALUE;
        for(var r: referenceResultProviders){
            double score = r.getValueFor(instanceName).getScoreOrNan();
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

    /**
     * Create workunits with solve order
     * @param experiment experiment definition
     * @param instancePaths instance name list
     * @param exceptionHandler what to do if something fails inside the workunit
     * @param repetitions how many times should we repeat the (instance, algorithm) pair
     * @return Map of workunits per instance
     */
    protected Map<String, Map<Algorithm<S,I>, List<WorkUnit<S,I>>>> getOrderedWorkUnits(Experiment<S,I> experiment, List<String> instancePaths, ExceptionHandler<S, I> exceptionHandler, int repetitions){
        var workUnits = new LinkedHashMap<String, Map<Algorithm<S,I>, List<WorkUnit<S,I>>>>();
        for(String instancePath: instancePaths){
            var algWorkUnits = new LinkedHashMap<Algorithm<S,I>, List<WorkUnit<S,I>>>();
            for(var alg: experiment.algorithms()){
                var list = new ArrayList<WorkUnit<S,I>>();
                for (int i = 0; i < repetitions; i++) {
                    var workUnit = new WorkUnit<>(experiment.name(), instancePath, alg, i, exceptionHandler);
                    list.add(workUnit);
                }
                algWorkUnits.put(alg, list);
            }
            workUnits.put(instancePath, algWorkUnits);
        }
        return workUnits;
    }

    public static String instanceName(String instancePath){
        // TODO: review and improve
        String[] parts = instancePath.split(Pattern.quote(File.separator));
        return parts[parts.length - 1];
    }
}
