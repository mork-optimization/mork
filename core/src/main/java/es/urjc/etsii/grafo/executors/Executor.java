package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.exception.ExceptionHandler;
import es.urjc.etsii.grafo.exceptions.DefaultExceptionHandler;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultManager;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.SolutionExportFrequency;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.services.IOManager;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.TimeUtil;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator.decideImplementation;
import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * Processes work units
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class Executor<S extends Solution<S, I>, I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(Executor.class);
    public static final int EXTRA_SECS_BEFORE_WARNING = 10;
    public static final int UNDEF_TIME = -1;

    protected final Optional<TimeLimitCalculator<S, I>> timeLimitCalculator;
    protected final IOManager<S, I> io;
    protected final InstanceManager<I> instanceManager;
    protected final ReferenceResultManager referenceResultManager;
    protected final SolverConfig solverConfig;

    private final ExceptionHandler<S, I> exceptionHandler;


    /**
     * If time control is enabled, remove it and check ellapsed time to see if too many time has been spent
     *
     * @param <S>                Solution class
     * @param <I>                Instance class
     * @param timeControlEnabled true if time control was enabled for this work unit
     * @param workUnit           current work unit
     */
    public static <S extends Solution<S, I>, I extends Instance> void endTimeControl(boolean timeControlEnabled, WorkUnit<S, I> workUnit) {
        if (timeControlEnabled) {
            if (TimeControl.remaining() < -TimeUtil.secsToNanos(EXTRA_SECS_BEFORE_WARNING)) {
                log.warn("Algorithm takes too long to stop after time is up. Instance {}, algorithm {}", workUnit.instancePath(), workUnit.algorithm());
            }
            TimeControl.remove();
        }
    }

    /**
     * Fill common values used by all executors
     *
     * @param validator              solution validator if available
     * @param timeLimitCalculator    time limit calculator if exists
     * @param io                     IO manager
     * @param exceptionHandlers      list of exception handlers available
     * @param referenceResultManager reference result manager
     */
    @SuppressWarnings({"unchecked"}) // due to current decideImplementation required cast
    protected Executor(
            Optional<SolutionValidator<S, I>> validator,
            Optional<TimeLimitCalculator<S, I>> timeLimitCalculator,
            IOManager<S, I> io,
            InstanceManager<I> instanceManager,
            SolverConfig solverConfig,
            List<ExceptionHandler<S, I>> exceptionHandlers, ReferenceResultManager referenceResultManager) {
        this.timeLimitCalculator = timeLimitCalculator;
        this.solverConfig = solverConfig;
        this.exceptionHandler = decideImplementation(exceptionHandlers, DefaultExceptionHandler.class);

        validator.ifPresent(Context.Configurator::setValidator);
        this.io = io;
        this.instanceManager = instanceManager;
        this.referenceResultManager = referenceResultManager;
    }

    public abstract void executeExperiment(Experiment<S, I> experiment, List<String> instanceNames, long startTimestamp);

    /**
     * Execute a warm-up pass before measured experiment executions start.
     *
     * @param experiment   experiment definition
     * @param instancePath warm-up instance path
     */
    public void warmup(Experiment<S, I> experiment, String instancePath) {
        var warmupConfig = solverConfig.getWarmup();
        if (!warmupConfig.isEnabled()) {
            return;
        }
        if (instancePath == null || instancePath.isBlank()) {
            throw new IllegalArgumentException("Warm-up is enabled but no warm-up instance path was provided");
        }

        int repetitions = warmupConfig.getRepetitions();
        long startTime = System.nanoTime();
        var events = EventPublisher.getInstance();
        boolean shouldUnblockEvents = events != null && !events.isBlocked();
        log.info("Warming up JVM for experiment {} using instance {} ({} repetitions per algorithm)",
                experiment.name(), instancePath, repetitions);
        if (shouldUnblockEvents) {
            events.block();
        }
        try {
            for (var algorithm : experiment.algorithms()) {
                for (int i = 0; i < repetitions; i++) {
                    var workUnit = new WorkUnit<>(experiment.name(), instancePath, algorithm, i);
                    var result = doWork(workUnit, warmupConfig.getMaxMillis());
                    if (!result.success()) {
                        throw new IllegalStateException("JVM warm-up failed for experiment %s, instance %s, algorithm %s"
                                .formatted(experiment.name(), instancePath, algorithm.getName()));
                    }
                }
            }
        } finally {
            if (shouldUnblockEvents) {
                events.unblock();
            }
        }
        long elapsed = System.nanoTime() - startTime;
        log.info("Finished JVM warm-up for experiment {} in {} (s)", experiment.name(), String.format("%.2f", nanosToSecs(elapsed)));
    }

    /**
     * Allocate resources and prepare for execution
     */
    public abstract void startup();

    /**
     * Finalize and destroy all resources, we have finished and are shutting down now.
     */
    public abstract void shutdown();

    /**
     * Execute a single iteration for the given (experiment, instance, algorithm, iterationId)
     *
     * @param workUnit Minimum unit of work, cannot be divided further.
     */
    protected WorkUnitResult<S, I> doWork(WorkUnit<S, I> workUnit) {
        return doWork(workUnit, 0);
    }

    /**
     * Execute a single iteration for the given (experiment, instance, algorithm, iterationId)
     *
     * @param workUnit             Minimum unit of work, cannot be divided further.
     * @param overrideMaxMillis    if positive, override the regular time limit with this duration in milliseconds
     */
    protected WorkUnitResult<S, I> doWork(WorkUnit<S, I> workUnit, long overrideMaxMillis) {
        S solution = null;
        I instance = this.instanceManager.getInstance(workUnit.instancePath());
        Algorithm<S, I> algorithm = workUnit.algorithm();
        boolean timeControlEnabled = false;

        long startTime = UNDEF_TIME, endTime = UNDEF_TIME;

        try {
            // Preparate current work unit
            Context.Configurator.resetRandom(solverConfig, workUnit.i());
            if (overrideMaxMillis > 0) {
                TimeControl.setMaxExecutionTime(overrideMaxMillis, TimeUnit.MILLISECONDS);
                TimeControl.start();
                timeControlEnabled = true;
            } else if (this.timeLimitCalculator.isPresent()) {
                long maxDuration = this.timeLimitCalculator.get().timeLimitInMillis(instance, algorithm);
                TimeControl.setMaxExecutionTime(maxDuration, TimeUnit.MILLISECONDS);
                TimeControl.start();
                timeControlEnabled = true;
            }

            if (solverConfig.isMetrics()) {
                Metrics.enableMetrics();
                Metrics.resetMetrics();
            }

            if(solverConfig.isTimeStats()){
                Metrics.enableTimeStats();
            } else {
                Metrics.disableTimeStats();
            }

            // Do real work
            startTime = System.nanoTime();
            solution = algorithm.algorithm(instance);
            endTime = System.nanoTime();

            // Prepare work unit results and cleanup
            endTimeControl(timeControlEnabled, workUnit);
            timeControlEnabled = false;
            Context.validate(solution);

            long timeToTarget = solution.getLastModifiedTime() - startTime;
            long executionTime = endTime - startTime;
            var timeData = Context.Configurator.getAndResetTimeEvents();
            var metrics = Metrics.areMetricsEnabled()? Metrics.getCurrentThreadMetrics() : null;
            return WorkUnitResult.ok(workUnit, instance.getId(), solution, executionTime, timeToTarget, metrics, timeData);
        } catch (Exception e) {
            long totalTime = UNDEF_TIME;
            if(startTime != UNDEF_TIME){
                if(endTime == UNDEF_TIME){
                    endTime = System.nanoTime();
                }
                totalTime = endTime - startTime;
            }
            if (timeControlEnabled && TimeControl.isEnabled()) {
                endTimeControl(true, workUnit);
            }
            exceptionHandler.handleException(workUnit.experimentName(), workUnit.i(), e, Optional.ofNullable(solution), instance, workUnit.algorithm());
            EventPublisher.getInstance().publishEvent(new ErrorEvent(e));
            var timeData = Context.Configurator.getAndResetTimeEvents();
            return WorkUnitResult.failure(workUnit, instance.getId(), totalTime, UNDEF_TIME, timeData);
        }
    }

    protected void processWorkUnitResult(WorkUnitResult<S, I> r, ProgressBar pb) {
        pb.step();
        if(r.success()){
            io.exportSolution(r, SolutionExportFrequency.ALL);
        }

        var solutionGenerated = new SolutionGeneratedEvent<>(r.success(), r.iteration(), r.instancePath(), r.solution(), r.experimentName(), r.algorithm(), r.executionTime(), r.timeToTarget(), r.solutionProperties(), r.metrics(), r.timeData());
        EventPublisher.getInstance().publishEvent(solutionGenerated);
        if (log.isDebugEnabled()) {
            log.debug(String.format("\t%s.\tT(s): %.3f \tTTB(s): %.3f \t%s", r.iteration(), nanosToSecs(r.executionTime()), nanosToSecs(r.timeToTarget()), r.solution()));
        }
    }

    protected void exportAlgorithmInstanceSolution(WorkUnitResult<S, I> r) {
        if(!r.success()){
            log.debug("Skipping export of failed WUR: {}", r);
            return;
        }
        // replace best iteration number with generic text to avoid overwriting the corresponding work unit result
        var modifiedWorkUnit = WorkUnitResult.copyBestAlg(r);
        io.exportSolution(modifiedWorkUnit, SolutionExportFrequency.BEST_PER_ALG_INSTANCE);
    }

    protected void exportInstanceSolution(WorkUnitResult<S, I> r) {
        if(!r.success()){
            log.debug("Skipping export of failed WUR: {}", r);
            return;
        }
        // replace best iteration number and algorithm name with generic text to avoid overwriting the corresponding work unit result
        var modifiedWorkUnit = WorkUnitResult.copyBestInstance(r);
        io.exportSolution(modifiedWorkUnit, SolutionExportFrequency.BEST_PER_INSTANCE);
    }


    /**
     * Create workunits with solve order
     *
     * @param experiment       experiment definition
     * @param instancePaths    instance name list
     * @param repetitions      how many times should we repeat the (instance, algorithm) pair
     * @return Map of workunits per instance
     */
    protected Map<String, Map<Algorithm<S, I>, List<WorkUnit<S, I>>>> getOrderedWorkUnits(Experiment<S, I> experiment, List<String> instancePaths, int repetitions) {
        var workUnits = new LinkedHashMap<String, Map<Algorithm<S, I>, List<WorkUnit<S, I>>>>();
        for (String instancePath : instancePaths) {
            var algWorkUnits = new LinkedHashMap<Algorithm<S, I>, List<WorkUnit<S, I>>>();
            for (var alg : experiment.algorithms()) {
                var list = new ArrayList<WorkUnit<S, I>>();
                for (int i = 0; i < repetitions; i++) {
                    var workUnit = new WorkUnit<>(experiment.name(), instancePath, alg, i);
                    list.add(workUnit);
                }
                algWorkUnits.put(alg, list);
            }
            workUnits.put(instancePath, algWorkUnits);
        }
        return workUnits;
    }

    protected boolean improves(WorkUnitResult<S, I> candidate, WorkUnitResult<S, I> best) {
        if (candidate == null) {
            throw new IllegalArgumentException("Null candidate");
        }
        if (best == null || !best.success()) {
            // anything is better than nothing
            return true;
        }
        if(!candidate.success()){
            // do not even compare, failed execution
            return false;
        }
        Objective<?, S, I> objective = Context.getMainObjective();
        return objective.isBetter(candidate.solution(), best.solution());
    }

    public String instanceName(String instancePath) {
        return this.instanceManager.getInstance(instancePath).getId();
    }

    public static ProgressBarBuilder getPBarBuilder(String taskname) {
        return new ProgressBarBuilder()
                .setUpdateIntervalMillis(100)
                .continuousUpdate()
                .setTaskName(taskname)
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR);
    }


    public ProgressBar getGlobalSolvingProgressBar(String expName, Map<String, Map<Algorithm<S, I>, List<WorkUnit<S, I>>>> workUnits) {
        if(!this.solverConfig.isProgressBar()){
            return getPBarBuilder(expName)
                    .setInitialMax(0)
                    .setConsumer(new DelegatingProgressBarConsumer(__ -> {}))
                    .build();
        }
        int totalUnits = 0;
        for (var v1 : workUnits.values()) {
            for (var v2 : v1.values()) {
                totalUnits += v2.size();
            }
        }
        return getPBarBuilder(expName)
                .setInitialMax(totalUnits)
                .build();
    }
}
