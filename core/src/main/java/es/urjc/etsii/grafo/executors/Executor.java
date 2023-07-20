package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.EmptyAlgorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.exception.ExceptionHandler;
import es.urjc.etsii.grafo.exceptions.DefaultExceptionHandler;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.SolutionExportFrequency;
import es.urjc.etsii.grafo.services.IOManager;
import es.urjc.etsii.grafo.services.SolutionValidator;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.metrics.MetricsManager;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.TimeUtil;
import es.urjc.etsii.grafo.util.ValidationUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;
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

    protected final Optional<SolutionValidator<S, I>> validator;
    protected final Optional<TimeLimitCalculator<S, I>> timeLimitCalculator;
    protected final IOManager<S, I> io;
    protected final InstanceManager<I> instanceManager;
    protected final List<ReferenceResultProvider> referenceResultProviders;
    protected final SolverConfig solverConfig;
    protected final FMode ofmode = Mork.getFMode();

    private final ExceptionHandler<S, I> exceptionHandler;



    /**
     * If time control is enabled, remove it and check ellapsed time to see if too many time has been spent
     *
     * @param <S>                 Solution class
     * @param <I>                 Instance class
     * @param timeLimitCalculator time limit calculator if implemented
     * @param workUnit            current work unit
     */
    public static <S extends Solution<S, I>, I extends Instance> void endTimeControl(Optional<TimeLimitCalculator<S, I>> timeLimitCalculator, WorkUnit<S, I> workUnit) {
        if (timeLimitCalculator.isPresent()) {
            if (TimeControl.remaining() < -TimeUtil.secsToNanos(EXTRA_SECS_BEFORE_WARNING)) {
                log.warn("Algorithm takes too long to stop after time is up. Instance {}, algorithm {}", workUnit.instancePath(), workUnit.algorithm());
            }
            TimeControl.remove();
        }
    }

    /**
     * Fill common values used by all executors
     *
     * @param validator                solution validator if available
     * @param timeLimitCalculator      time limit calculator if exists
     * @param io                       IO manager
     * @param referenceResultProviders list of all reference value providers implementations
     * @param exceptionHandlers list of exception handlers available
     */
    protected Executor(
            Optional<SolutionValidator<S, I>> validator,
            Optional<TimeLimitCalculator<S, I>> timeLimitCalculator,
            IOManager<S, I> io,
            InstanceManager<I> instanceManager,
            List<ReferenceResultProvider> referenceResultProviders,
            SolverConfig solverConfig,
            List<ExceptionHandler<S, I>> exceptionHandlers) {
        this.timeLimitCalculator = timeLimitCalculator;
        this.referenceResultProviders = referenceResultProviders;
        this.solverConfig = solverConfig;
        this.exceptionHandler = decideImplementation(exceptionHandlers, DefaultExceptionHandler.class);

        if (validator.isEmpty()) {
            log.warn("No SolutionValidator implementation has been found, solution CORRECTNESS WILL NOT BE CHECKED");
        } else {
            log.info("SolutionValidator implementation found: {}", validator.get().getClass().getSimpleName());
        }

        this.validator = validator;
        this.io = io;
        this.instanceManager = instanceManager;
    }

    public abstract void executeExperiment(Experiment<S, I> experiment, List<String> instanceNames, long startTimestamp);

    /**
     * Finalize and destroy all resources, we have finished and are shutting down now.
     */
    public abstract void shutdown();

    /**
     * Run both user specific validations and our own.
     *
     * @param solution Solution to check.
     */
    public void validate(S solution) {
        ValidationUtil.positiveTTB(solution);
        var instanceName = solution.getInstance().getId();
        var optimalValue = this.getOptionalReferenceValue(instanceName, true);
        if (optimalValue.isPresent()) {
            // Check that solution score is not better than optimal value
            double solutionScore = solution.getScore();
            if(ofmode.isBetter(solutionScore, optimalValue.get())){
                throw new AssertionError("Solution score (%s) improves optimal value (%s) in ReferenceResultProvider".formatted(solutionScore, optimalValue.get()));
            }
        }
        // Run user validations if used implemented them
        this.validator.ifPresent(v -> v.validate(solution));
    }

    /**
     * Execute a single iteration for the given (experiment, instance, algorithm, iterationId)
     *
     * @param workUnit Minimum unit of work, cannot be divided further.
     */
    protected WorkUnitResult<S, I> doWork(WorkUnit<S, I> workUnit) {
        S solution = null;
        I instance = this.instanceManager.getInstance(workUnit.instancePath());
        Algorithm<S, I> algorithm = workUnit.algorithm();

        try {
            // Preparate current work unit
            RandomManager.reset(workUnit.i());
            if (this.timeLimitCalculator.isPresent()) {
                long maxDuration = this.timeLimitCalculator.get().timeLimitInMillis(instance, algorithm);
                TimeControl.setMaxExecutionTime(maxDuration, TimeUnit.MILLISECONDS);
                TimeControl.start();
            }

            if (solverConfig.isMetrics()) {
                MetricsManager.enableMetrics();
                MetricsManager.resetMetrics();
            }

            // Do real work
            long starTime = System.nanoTime();
            solution = algorithm.algorithm(instance);
            long endTime = System.nanoTime();

            // Prepare work unit results and cleanup
            endTimeControl(timeLimitCalculator, workUnit);
            validate(solution);

            long timeToTarget = solution.getLastModifiedTime() - starTime;
            long executionTime = endTime - starTime;
            return new WorkUnitResult<>(workUnit, solution, executionTime, timeToTarget, MetricsManager.rawMetrics());
        } catch (Exception e) {
            exceptionHandler.handleException(workUnit.experimentName(), workUnit.i(), e, Optional.ofNullable(solution), instance, workUnit.algorithm());
            EventPublisher.getInstance().publishEvent(new ErrorEvent(e));
            return null;
        }
    }

    protected void processWorkUnitResult(WorkUnitResult<S, I> r, ProgressBar pb) {
        pb.step();
        io.exportSolution(r, SolutionExportFrequency.ALL);
        var solutionGenerated = new SolutionGeneratedEvent<>(r.iteration(), r.solution(), r.experimentName(), r.algorithm(), r.executionTime(), r.timeToTarget(), r.metrics());
        EventPublisher.getInstance().publishEvent(solutionGenerated);
        if (log.isDebugEnabled()) {
            log.debug(String.format("\t%s.\tT(s): %.3f \tTTB(s): %.3f \t%s", r.iteration(), nanosToSecs(r.executionTime()), nanosToSecs(r.timeToTarget()), r.solution()));
        }
    }

    protected void exportAlgorithmInstanceSolution(WorkUnitResult<S, I> r) {
        // replace best iteration number with generic text to avoid overwriting the corresponding work unit result
        var modifiedWorkUnit = new WorkUnitResult<>(r.experimentName(), r.algorithm(), "bestiter", r.solution(), r.executionTime(), r.timeToTarget(), r.metrics());
        io.exportSolution(modifiedWorkUnit, SolutionExportFrequency.BEST_PER_ALG_INSTANCE);
    }

    protected void exportInstanceSolution(WorkUnitResult<S, I> r) {
        // replace best iteration number and algorithm name with generic text to avoid overwriting the corresponding work unit result
        var modifiedWorkUnit = new WorkUnitResult<>(r.experimentName(), new EmptyAlgorithm<>("bestalg"), "bestiter", r.solution(), r.executionTime(), r.timeToTarget(), r.metrics());
        io.exportSolution(modifiedWorkUnit, SolutionExportFrequency.BEST_PER_INSTANCE);
    }

    protected Optional<Double> getOptionalReferenceValue(String instanceName, boolean onlyOptimal) {
        double best = Mork.isMaximizing() ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (var r : referenceResultProviders) {
            double score = Double.NaN;
            var ref = r.getValueFor(instanceName);
            if (ref != null) {
                score = ref.getScoreOrNan();
            }
            // Ignore if not valid value
            if (Double.isFinite(score) && (!onlyOptimal || ref.isOptimalValue())) {
                if (Mork.isMaximizing()) {
                    best = Math.max(best, score);
                } else {
                    best = Math.min(best, score);
                }
            }
        }
        if (best == Integer.MAX_VALUE || best == Integer.MIN_VALUE) {
            return Optional.empty();
        } else {
            return Optional.of(best);
        }
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
        if (best == null) {
            return true;
        }
        if (Mork.isMaximizing()) {
            return DoubleComparator.isGreater(candidate.solution().getScore(), best.solution().getScore());
        } else {
            return DoubleComparator.isLess(candidate.solution().getScore(), best.solution().getScore());
        }
    }

    public String instanceName(String instancePath) {
        return this.instanceManager.getInstance(instancePath).getId();
    }

    public static ProgressBarBuilder getPBarBuilder(String taskname) {
        return new ProgressBarBuilder()
                .setUpdateIntervalMillis(50)
                .continuousUpdate()
                .setTaskName(taskname)
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR);
    }

    public ProgressBar getGlobalSolvingProgressBar(String expName, Map<String, Map<Algorithm<S, I>, List<WorkUnit<S, I>>>> workUnits) {
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
