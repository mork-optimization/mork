package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.metrics.timing.TimeStatsService;
import es.urjc.etsii.grafo.metrics.timing.TimeStatsExecution;
import es.urjc.etsii.grafo.util.ExecutionJournalUtil;
import es.urjc.etsii.grafo.util.TimeStatsUtil;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.events.types.WorkUnitStartedEvent;
import es.urjc.etsii.grafo.monitoring.NormalExperimentRunState;
import es.urjc.etsii.grafo.exception.ExceptionHandler;
import es.urjc.etsii.grafo.exceptions.DefaultExceptionHandler;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultManager;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.SolutionExportFrequency;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializerListener;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.terminal.ProgressAwareProgressBarConsumer;
import es.urjc.etsii.grafo.results.ResultStore;
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
    protected final MorkEventPublisher eventPublisher;
    protected final ResultStore<S, I> resultStore;
    protected final ResultsSerializerListener<S, I> resultsSerializer;
    protected final NormalExperimentRunState<S, I> runState;

    protected final TimeStatsService timeStats;

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
    protected Executor(
            Optional<SolutionValidator<S, I>> validator,
            Optional<TimeLimitCalculator<S, I>> timeLimitCalculator,
            IOManager<S, I> io,
            InstanceManager<I> instanceManager,
            SolverConfig solverConfig,
            List<ExceptionHandler<S, I>> exceptionHandlers,
            ReferenceResultManager referenceResultManager,
            MorkEventPublisher eventPublisher,
            ResultStore<S, I> resultStore,
            ResultsSerializerListener<S, I> resultsSerializer, TimeStatsService timeStats, NormalExperimentRunState<S, I> runState) {
        this.timeLimitCalculator = timeLimitCalculator;
        this.solverConfig = solverConfig;
        this.exceptionHandler = decideImplementation(exceptionHandlers, DefaultExceptionHandler.class);

        validator.ifPresent(Context.Configurator::setValidator);
        this.io = io;
        this.instanceManager = instanceManager;
        this.referenceResultManager = referenceResultManager;
        this.eventPublisher = eventPublisher;
        this.resultStore = resultStore;
        this.resultsSerializer = resultsSerializer;
        this.timeStats = timeStats;
        this.runState = runState;
    }

    public abstract void executeExperiment(Experiment<S, I> experiment, List<String> instancePaths, long startTimestamp);

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
        if (repetitions <= 0) {
            throw new IllegalArgumentException("Warm-up repetitions must be >= 1, was: " + repetitions);
        }
        long startTime = System.nanoTime();
        log.info("Warming up JVM for experiment {} using instance {} ({} repetitions per algorithm)",
                experiment.name(), instancePath, repetitions);
        try (var ignored = eventPublisher.mute()) {
            for (var algorithm : experiment.algorithms()) {
                for (int i = 0; i < repetitions; i++) {
                    var workUnit = new WorkUnit<>(UUID.randomUUID(), experiment.name(), instancePath, algorithm, i);
                    var result = doWork(workUnit, warmupConfig.getMaxMillis(), false);
                    if (!result.success()) {
                        throw new IllegalStateException("JVM warm-up failed for experiment %s, instance %s, algorithm %s"
                                .formatted(experiment.name(), instancePath, algorithm.getName()));
                    }
                }
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
        return doWork(workUnit, 0, true);
    }

    /**
     * Execute a single iteration for the given (experiment, instance, algorithm, iterationId)
     *
     * @param workUnit             Minimum unit of work, cannot be divided further.
     * @param overrideMaxMillis    if positive, override the regular time limit with this duration in milliseconds
     * @param recordTiming         false for warm-up executions
     */
    protected WorkUnitResult<S, I> doWork(WorkUnit<S, I> workUnit, long overrideMaxMillis, boolean recordTiming) {
        if (recordTiming) runState.started(workUnit);
        return recordResult(computeResult(workUnit, overrideMaxMillis, recordTiming), recordTiming);
    }

    private WorkUnitResult<S, I> computeResult(WorkUnit<S, I> workUnit, long overrideMaxMillis, boolean recordTiming) {
        if (ExecutionJournalUtil.enabled()) ExecutionJournalUtil.phase(workUnit.resultId().toString(), "LOADING",
                Long.getLong("mork.execution.phase-timeout", 300_000L), java.util.Map.of("instance", workUnit.instancePath(), "algorithm", workUnit.algorithm().getName(), "measured", recordTiming, "iteration", Integer.toString(workUnit.i()), "seed", solverConfig.getSeed() + workUnit.i()));
        S solution = null;
        I instance = null;
        Algorithm<S, I> algorithm = workUnit.algorithm();
        boolean timeControlEnabled = false;

        long startTime = UNDEF_TIME, endTime = UNDEF_TIME;

        var resultId = workUnit.resultId();
        try {
            instance = this.instanceManager.getInstance(workUnit.instancePath());
            if (recordTiming) eventPublisher.publish(new WorkUnitStartedEvent(resultId, workUnit.experimentName(), instance.getId(), algorithm.getName(), workUnit.i() + 1, solverConfig.getSeed() + workUnit.i()));
            var recorder = recordTiming ? timeStats.open(new TimeStatsExecution(
                    resultId, workUnit.experimentName(), workUnit.instancePath(), instance.getId(),
                    algorithm.getName(), workUnit.i(), solverConfig.getSeed() + workUnit.i())) : null;
            try (var timing = TimeStatsUtil.bind(recorder)) {
            // Preparate current work unit
            Context.Configurator.resetRandom(solverConfig, workUnit.i());
            if (overrideMaxMillis > 0) {
                TimeControl.setMaxExecutionTime(overrideMaxMillis, TimeUnit.MILLISECONDS);
                TimeControl.start();
                timeControlEnabled = true;
            } else if (System.getProperty("mork.execution.time-limits") != null || this.timeLimitCalculator.isPresent()) {
                Long override = ExecutionJournalUtil.timeLimit(workUnit.instancePath());
                long maxDuration = override != null ? override : this.timeLimitCalculator.orElseThrow().timeLimitInMillis(instance, algorithm);
                TimeControl.setMaxExecutionTime(maxDuration, TimeUnit.MILLISECONDS);
                TimeControl.start();
                timeControlEnabled = true;
            }

            if (solverConfig.isMetrics()) {
                Metrics.enableMetrics();
                Metrics.resetMetrics();
            }

            // Do real work
            if (ExecutionJournalUtil.enabled()) ExecutionJournalUtil.phase(workUnit.resultId().toString(), "RUNNING",
                    timeControlEnabled ? Math.max(1, TimeUnit.NANOSECONDS.toMillis(TimeControl.remaining())) : Long.getLong("mork.execution.phase-timeout", 300_000L),
                    java.util.Map.of("instance", workUnit.instancePath(), "algorithm", algorithm.getName()));
            startTime = System.nanoTime();
            solution = algorithm.algorithm(instance);
            endTime = System.nanoTime();

            // Prepare work unit results and cleanup
            ExecutionJournalUtil.phase(workUnit.resultId().toString(), "VALIDATING", Long.getLong("mork.execution.phase-timeout", 300_000L), java.util.Map.of());
            endTimeControl(timeControlEnabled, workUnit);
            timeControlEnabled = false;
            Context.validate(solution);

            long timeToTarget = solution.getLastModifiedTime() - startTime;
            long executionTime = endTime - startTime;
            var metrics = Metrics.areMetricsEnabled()? Metrics.getCurrentThreadMetrics() : null;
            return WorkUnitResult.ok(resultId, workUnit, instance.getId(), solution, executionTime, timeToTarget, metrics);
            }
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
            String name = instance == null ? org.apache.commons.io.FilenameUtils.getName(workUnit.instancePath()) : instance.getId();
            var failed = WorkUnitResult.failure(resultId, workUnit, name, totalTime, UNDEF_TIME);
            if (recordTiming) runState.issue("ERROR", "COMPUTATION", e, resultId, workUnit.experimentName());
            eventPublisher.publish(new ErrorEvent(e));
            if (instance != null) exceptionHandler.handleException(workUnit.experimentName(), workUnit.i(), e, Optional.ofNullable(solution), instance, workUnit.algorithm());
            return failed;
        }
    }

    private WorkUnitResult<S, I> recordResult(WorkUnitResult<S, I> result, boolean measured) {
        result = ExecutionJournalUtil.withMetadata(result);
        ExecutionJournalUtil.result(result, measured, solverConfig.getSeed() + Integer.parseInt(result.iteration()));
        if (measured) {
            resultStore.store(result);
            runState.completed(result);
            eventPublisher.publish(new SolutionGeneratedEvent(result));
        }
        return result;
    }

    protected void processWorkUnitResult(WorkUnitResult<S, I> r, ProgressBar pb) {
        pb.step();
        if(r.success()) exportSolution(r, SolutionExportFrequency.ALL);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%4s.  T(s): %8.3f  TTB(s): %8.3f  %s", r.iteration(), nanosToSecs(r.executionTime()), nanosToSecs(r.timeToTarget()), r.solution()));
        }
    }

    /**
     * Serialize the current experiment snapshot before its instance-ended event.
     * Serialization failures are reported without interrupting workflow finalization.
     */
    protected void serializePerInstance(String experimentName, long experimentStartTimestamp) {
        try {
            resultsSerializer.serializePerInstance(experimentName, experimentStartTimestamp);
        } catch (RuntimeException e) {
            log.error("Per-instance result serialization failed for experiment {}", experimentName, e);
            eventPublisher.publish(new ErrorEvent(e));
            runState.issue("WARNING", "EXPORT", e, null, experimentName);
        }
    }

    /**
     * Complete instance processing after its synchronous serialization attempt.
     */
    protected void finishInstance(
            String experimentName,
            String instanceName,
            long executionTime,
            long experimentStartTimestamp
    ) {
        serializePerInstance(experimentName, experimentStartTimestamp);
        eventPublisher.publish(new InstanceProcessingEndedEvent(
                experimentName,
                instanceName,
                executionTime,
                experimentStartTimestamp
        ));
    }

    protected void exportAlgorithmInstanceSolution(WorkUnitResult<S, I> r) {
        if(!r.success()){
            log.debug("Skipping export of failed WUR: {}", r);
            return;
        }
        // replace best iteration number with generic text to avoid overwriting the corresponding work unit result
        var modifiedWorkUnit = WorkUnitResult.copyBestAlg(r);
        exportSolution(modifiedWorkUnit, SolutionExportFrequency.BEST_PER_ALG_INSTANCE);
    }

    protected void exportInstanceSolution(WorkUnitResult<S, I> r) {
        if(!r.success()){
            log.debug("Skipping export of failed WUR: {}", r);
            return;
        }
        // replace best iteration number and algorithm name with generic text to avoid overwriting the corresponding work unit result
        var modifiedWorkUnit = WorkUnitResult.copyBestInstance(r);
        exportSolution(modifiedWorkUnit, SolutionExportFrequency.BEST_PER_INSTANCE);
    }

    private void exportSolution(WorkUnitResult<S, I> result, SolutionExportFrequency frequency) {
        try {
            io.exportSolution(result, frequency);
        } catch (RuntimeException e) {
            runState.issue("WARNING", "EXPORT", e, result.resultId(), result.experimentName());
            eventPublisher.publish(new ErrorEvent(e));
        }
    }


    /**
     * Create workunits with solve order
     *
     * @param experiment       experiment definition
     * @param instancePaths    instance load path list
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
                    var workUnit = new WorkUnit<>(runState.workId(experiment.name(), instancePath, alg.getName(), i), experiment.name(), instancePath, alg, i);
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
        return objective.isBetter(candidate.mainObjectiveValue(), best.mainObjectiveValue());
    }

    public String instanceName(String instancePath) {
        return this.instanceManager.getInstance(instancePath).getId();
    }

    public static ProgressBarBuilder getPBarBuilder(String taskname) {
        return new ProgressBarBuilder()
                .setUpdateIntervalMillis(100)
                .continuousUpdate()
                .setTaskName(taskname)
                .setConsumer(new ProgressAwareProgressBarConsumer())
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
