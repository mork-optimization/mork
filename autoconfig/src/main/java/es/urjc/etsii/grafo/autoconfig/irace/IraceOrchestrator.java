package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceProgressDetails;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentStartedEvent;
import es.urjc.etsii.grafo.exception.IllegalAlgorithmConfigException;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializerListener;
import es.urjc.etsii.grafo.metrics.MetricUtil;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator;
import es.urjc.etsii.grafo.services.ReflectiveSolutionBuilder;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.util.IOUtil.copyWithSubstitutions;
import static es.urjc.etsii.grafo.util.IOUtil.getInputStreamForIrace;
import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * <p>IraceOrchestrator class.</p>
 */
// TODO split in two orchestrators, one for irace and one for autoconfig. This class has too many responsibilities
public class IraceOrchestrator<S extends Solution<S, I>, I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(IraceOrchestrator.class);
    private static final String IRACE_EXPNAME = "irace autoconfig";
    private static final String IRACE_INSTANCE_PATH_KEY = "irace";
    public static final String K_INTEGRATION_KEY = "__INTEGRATION_KEY__";
    public static final String K_INSTANCES_PATH = "__INSTANCES_PATH__";
    public static final String K_TARGET_RUNNER = "__TARGET_RUNNER__";
    public static final String K_PARALLEL = "__PARALLEL__";
    public static final String K_MAX_EXP = "__MAX_EXPERIMENTS__";
    public static final String K_SEED = "__SEED__";
    public static final String K_PORT = "__PORT__";
    public static final String K_RUN_ID = "__RUN_ID__";
    public static final String F_PARAMETERS = "parameters.txt";
    public static final String F_SCENARIO = "scenario.txt";
    private final String IRACE_PARAM_EPILOGUE = """
            
            [global]
            digits = 2
            """;


    public static final int DEFAULT_IRACE_EXPERIMENTS = 10_000;
    private final IraceConfig iraceConfig;
    private final SolverConfig solverConfig;
    private final InstanceConfiguration instanceConfiguration;
    private final IraceIntegration iraceIntegration;
    private final ServerProperties serverProperties;
    private final SolutionBuilder<S, I> solutionBuilder;
    private final AlgorithmBuilder<S, I> algorithmBuilder;
    private final InstanceManager<I> instanceManager;
    private final Optional<SolutionValidator<S, I>> validator;
    private final MorkEventPublisher eventPublisher;
    private final ExecutionLifecycleCoordinator lifecycleCoordinator;
    private final ResultsSerializerListener<S, I> resultsSerializer;
    private final AutoconfigRunState runState;

    private final Optional<TimeLimitCalculator<S, I>> timeLimitCalculator;



    private final AutoconfigSearchSpace searchSpace;

    private boolean isAutoconfigEnabled;
    private boolean isFollower;

    /**
     * <p>Constructor for IraceOrchestrator.</p>
     *
     * @param solverConfig                a {@link SolverConfig} object.
     * @param instanceConfiguration
     * @param iraceIntegration            a {@link IraceIntegration} object.
     * @param instanceManager             a {@link InstanceManager} object.
     * @param solutionBuilders            a {@link List} object.
     * @param algorithmBuilders           a {@link List} object.
     * @param validator
     * @param searchSpace
     */
    public IraceOrchestrator(
            SolverConfig solverConfig,
            BlockConfig blockConfig,
            IraceConfig iraceConfig,
            ServerProperties serverProperties,
            InstanceConfiguration instanceConfiguration, IraceIntegration iraceIntegration,
            InstanceManager<I> instanceManager,
            List<SolutionBuilder<S, I>> solutionBuilders,
            List<AlgorithmBuilder<S, I>> algorithmBuilders,
            Optional<SolutionValidator<S, I>> validator, Optional<TimeLimitCalculator<S, I>> timeLimitCalculator,
            AutoconfigSearchSpace searchSpace,
            MorkEventPublisher eventPublisher,
            ExecutionLifecycleCoordinator lifecycleCoordinator,
            ResultsSerializerListener<S, I> resultsSerializer,
            AutoconfigRunState runState
    ) {
        this.solverConfig = solverConfig;
        this.iraceConfig = iraceConfig;
        this.timeLimitCalculator = timeLimitCalculator;
        Context.Configurator.setSolverConfig(solverConfig);
        Context.Configurator.setBlockConfig(blockConfig);
        this.instanceConfiguration = instanceConfiguration;
        this.serverProperties = serverProperties;
        this.iraceIntegration = iraceIntegration;
        this.solutionBuilder = decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
        this.instanceManager = instanceManager;
        this.algorithmBuilder = decideImplementation(algorithmBuilders, AutomaticAlgorithmBuilder.class);
        this.searchSpace = searchSpace;
        this.validator = validator;
        this.eventPublisher = eventPublisher;
        this.lifecycleCoordinator = lifecycleCoordinator;
        this.resultsSerializer = resultsSerializer;
        this.runState = runState;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void run(String... args) {
        for (String arg : args) {
            if (arg.equals("--autoconfig")) {
                this.isAutoconfigEnabled = true;
            }
            if(arg.equals("--follower")){
                this.isFollower = true;
            }
        }
        log.info("Starting tuning engine... {isAutoconfig: {}, isFollower: {}}", isAutoconfigEnabled, isFollower);

        log.debug("Using SolutionBuilder implementation: {}", this.solutionBuilder.getClass().getSimpleName());
        log.debug("Using AlgorithmBuilder implementation: {}", this.algorithmBuilder.getClass().getSimpleName());
        if (validator.isEmpty()) {
            log.warn("No SolutionValidator implementation has been found, solution CORRECTNESS WILL NOT BE CHECKED");
        } else {
            log.debug("SolutionValidator implementation found: {}", validator.get().getClass().getSimpleName());
        }
        if(isFollower){
            this.runState.prepareWorker(isAutoconfigEnabled);
            this.integrationKey = solverConfig.getIntegrationKey();
            log.info("Mork is running in follower mode, waiting for commands...");
            return;
        }

        this.runState.prepareCoordinator(isAutoconfigEnabled);
        log.info("Ready to start!");
        long startTime = System.nanoTime();
        var experimentName = List.of(IRACE_EXPNAME);
        eventPublisher.publish(new ExecutionStartedEvent(Context.getObjectivesW(), experimentName));
        try {
            launchIrace();
            this.runState.markCompleted();
        } catch (RuntimeException e) {
            this.runState.markFailed(e);
            throw e;
        } finally {
            long totalExecutionTime = System.nanoTime() - startTime;
            lifecycleCoordinator.complete(totalExecutionTime);
            log.info("Total execution time: {} (s)", nanosToSecs(totalExecutionTime));
        }
    }

    private void launchIrace() {
        log.info("Running experiment: IRACE autoconfig");
        eventPublisher.publish(new ExperimentStartedEvent(IRACE_EXPNAME, new ArrayList<>()));
        // Users must implement an Instance Importer to explain how to load instances
        // Use that class to see if the project is executing inside a JAR file or inside an IDE, to appropriately fix path
        // TODO: Review and improve
        var referenceClass = instanceManager.getUserImporterImplementation().getClass();
        var isJAR = IOUtil.isJAR(referenceClass);
        var substitutions = extractIraceFiles(isJAR);
        this.runState.markRunning(Integer.parseInt(substitutions.get(K_MAX_EXP)));

        long start = System.nanoTime();
        long startTimestamp = System.currentTimeMillis();
        var finalElites = iraceIntegration.runIrace(isJAR, substitutions);
        this.runState.publishFinalElites(this.runState.getRunId(), finalElites);
        long end = System.nanoTime();
        log.info("Finished running experiment: IRACE autoconfig");
        try {
            resultsSerializer.serializeAtExperimentEnd(IRACE_EXPNAME, startTimestamp);
        } catch (RuntimeException e) {
            log.error("Final result serialization failed for experiment {}", IRACE_EXPNAME, e);
            eventPublisher.publish(new ErrorEvent(e));
        }
        eventPublisher.publish(new ExperimentEndedEvent(IRACE_EXPNAME, end - start, startTimestamp));
    }

    private Map<String, String> extractIraceFiles(boolean isJar) {
        Path paramsPath = Path.of(F_PARAMETERS);
        int parameterCount = 0;
        try {
            if (isAutoconfigEnabled) {
                if (searchSpace.roots().isEmpty()) {
                    throw new IllegalStateException("No valid algorithm found, cannot generate irace parameters");
                }
                var iraceParams = searchSpace.iraceParameters();
                parameterCount = iraceParams.size();
                var sb = new StringBuilder();
                for (var p : iraceParams) {
                    sb.append(p).append("\n");
                }
                sb.append(IRACE_PARAM_EPILOGUE);
                Files.writeString(paramsPath, sb.toString());
                this.runState.publishGeneratedSearchSpace(parameterCount);
            }

            var substitutions = getSubstitutions(
                    integrationKey,
                    solverConfig,
                    instanceConfiguration,
                    serverProperties,
                    parameterCount
            );
            if (!isAutoconfigEnabled) {
                copyWithSubstitutions(getInputStreamForIrace(F_PARAMETERS, isJar), paramsPath, substitutions);
            }
            copyWithSubstitutions(getInputStreamForIrace(F_SCENARIO, isJar), Path.of(F_SCENARIO), substitutions);
            return substitutions;
        } catch (IOException e) {
            throw new RuntimeException("Failed extracting irace config files", e);
        }
    }

    private String integrationKey = StringUtil.generateSecret();

    private Map<String, String> getSubstitutions(
            String integrationKey,
            SolverConfig solverConfig,
            InstanceConfiguration instanceConfiguration,
            ServerProperties server,
            int parameterCount
    ) {
        return Map.of(
                K_INTEGRATION_KEY, integrationKey,
                K_INSTANCES_PATH, instanceConfiguration.getPath(IRACE_INSTANCE_PATH_KEY),
                K_TARGET_RUNNER, "./middleware.sh",
                K_PARALLEL, nParallel(solverConfig),
                K_MAX_EXP, calculateMaxExperiments(isAutoconfigEnabled, solverConfig, parameterCount),
                K_SEED, String.valueOf(solverConfig.getSeed()),
                K_PORT, String.valueOf(server.getPort()),
                K_RUN_ID, this.runState.getRunId()
        );
    }

    protected static String calculateMaxExperiments(boolean autoconfigEnabled, SolverConfig solverConfig, int parameterCount) {
        int maxExperiments;
        if (autoconfigEnabled) {
            if (parameterCount < 1) {
                throw new IllegalArgumentException("Parameter count must be positive");
            }
            maxExperiments = Math.max(solverConfig.getMinimumNumberOfExperiments(), solverConfig.getExperimentsPerParameter() * parameterCount);
        } else {
            maxExperiments = DEFAULT_IRACE_EXPERIMENTS; // 10k experiments by default if not specified otherwise
        }
        return String.valueOf(maxExperiments);
    }

    protected static String nParallel(SolverConfig solverConfig) {
        if (solverConfig.isParallelExecutor()) {
            int n = solverConfig.getnWorkers();
            return String.valueOf(n);
        } else {
            return "1";
        }
    }

    /**
     * <p>iraceCallback.</p>
     *
     * @return a double.
     */
    private ExecuteResponse iraceSingleCallback(
            IraceRuntimeConfiguration config,
            boolean recordEvaluation
    ) {
        long evaluationId = recordEvaluation ? this.runState.evaluationStarted(config) : 0;
        try {
            var instancePath = instanceManager.requireConfiguredInstancePath(
                    IRACE_INSTANCE_PATH_KEY,
                    config.getInstanceName()
            );
            var instance = instanceManager.getInstance(instancePath);
            Algorithm<S, I> algorithm;
            try {
                algorithm = buildAlgorithm(config);
            } catch (IllegalAlgorithmConfigException e) {
                log.debug("Invalid config, reason {}, config: {}", e.getMessage(), config);
                if (recordEvaluation) {
                    this.runState.evaluationRejected(
                            evaluationId,
                            "INVALID_CONFIGURATION",
                            e.getMessage(),
                            0
                    );
                }
                return new ExecuteResponse();
            }

            log.debug("Config {}. Built algorithm: {}", config, algorithm);
            Context.Configurator.resetRandom(solverConfig.getRandomType(), config.getSeed());

            var execution = singleExecution(algorithm, instance);
            if (!recordEvaluation) {
                return execution.response();
            }
            if (execution.rejectionCode() != null) {
                this.runState.evaluationRejected(
                        evaluationId,
                        execution.rejectionCode(),
                        execution.rejectionMessage(),
                        execution.slowOverrunMillis()
                );
            } else {
                this.runState.evaluationSucceeded(
                        evaluationId,
                        execution.response().getCost(),
                        execution.response().getTime(),
                        execution.slowOverrunMillis()
                );
            }
            return execution.response();
        } catch (RuntimeException e) {
            if (recordEvaluation) {
                this.runState.evaluationFailed(evaluationId, e);
            }
            throw e;
        }
    }

    private Algorithm<S, I> buildAlgorithm(IraceRuntimeConfiguration config) {
        Algorithm<S, I> algorithm = this.algorithmBuilder.buildFromConfig(config.getAlgorithmConfig());
        algorithm.setBuilder(this.solutionBuilder);
        if (this.isAutoconfigEnabled && this.solverConfig.isAutorestart()) {
            // Because autoconfig iterations executes between two time intervals, if the algorithm finishes
            // but there is extra time available, keep executing the same algorithm until the timelimit is reached.
            // If not, we could be penalizing faster simpler algorithms against more complex ones.
            int iterations = Integer.MAX_VALUE / 2;
            algorithm = new MultiStartAlgorithm<>(algorithm.getName(), Context.getMainObjective(), algorithm, iterations, iterations, iterations);
            algorithm.setBuilder(this.solutionBuilder);
        }
        return algorithm;
    }

    public List<ExecuteResponse> iraceMultiCallback(
            List<IraceExecuteConfig> configs,
            boolean recordEvaluations
    ) {
        if (this.solverConfig.isParallelExecutor()) {
            try(var executor = Executors.newFixedThreadPool(this.solverConfig.getnWorkers())){
                var futures = new ArrayList<Future<ExecuteResponse>>();
                for (IraceExecuteConfig config : configs) {
                    futures.add(executor.submit(() -> {
                        var iraceConfig = new IraceRuntimeConfiguration(config);
                        return iraceSingleCallback(iraceConfig, recordEvaluations);
                    }));
                }
                executor.shutdown();
                return ConcurrencyUtil.awaitAll(futures);
            }
        } else {
            var results = new ArrayList<ExecuteResponse>();
            for (IraceExecuteConfig config : configs) {
                var iraceConfig = new IraceRuntimeConfiguration(config);
                results.add(iraceSingleCallback(iraceConfig, recordEvaluations));
            }
            return results;
        }
    }

    private ExecutionResult singleExecution(Algorithm<S, I> algorithm, I instance) {
        long maxExecTime = solverConfig.getIgnoreInitialMillis() + solverConfig.getIntervalDurationMillis();
        if(!isAutoconfigEnabled && this.iraceConfig.isTimecontrol()){
            if(this.timeLimitCalculator.isEmpty()){
                throw new IllegalStateException("irace.timecontrol is true, but no time limit calculator has been found, time control will not be enabled");
            }
            var timelimit = timeLimitCalculator.get().timeLimitInMillis(instance, algorithm);
            TimeControl.setMaxExecutionTime(timelimit, TimeUnit.MILLISECONDS);
            TimeControl.start();
        }

        if (isAutoconfigEnabled) {
            // Autoconfig requires metrics to be enabled to track algorithm performance
            TimeControl.setMaxExecutionTime(maxExecTime, TimeUnit.MILLISECONDS);
            TimeControl.start();
            Metrics.enableMetrics();
        }

        if(Metrics.areMetricsEnabled()){
            // If metrics are enabled, reset them before each execution
            // This is independent of the autoconfig configuration because users
            // may request metrics to be enabled when manually running Irace
            Metrics.resetMetrics();
        }

        long startTime = System.nanoTime();
        var solution = algorithm.algorithm(instance);
        long endTime = System.nanoTime();

        // If the user has implemented a solution validator, check solution correctness
        validator.ifPresent(v -> v.validate(solution).throwIfFail());

        double score;
        long slowOverrunMillis = 0;
        Objective<?,S,I> mainObj = Context.getMainObjective();
        if(this.isAutoconfigEnabled || iraceConfig.isTimecontrol()){
            slowOverrunMillis = checkExecutionTime(algorithm, instance);
            TimeControl.remove();
        }
        if (isAutoconfigEnabled) {
            try {
                score = MetricUtil.areaUnderCurve(mainObj,
                        TimeUtil.convert(solverConfig.getIgnoreInitialMillis(), TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS),
                        TimeUtil.convert(solverConfig.getIntervalDurationMillis(), TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS),
                        solverConfig.isLogScaleArea()
                );
                if(!solverConfig.isLogScaleArea()){
                    // If not using log scaling, divide by NANOS_IN_MILLISECOND to get an acceptable range
                    score /= TimeUtil.NANOS_IN_MILLISECOND;
                }

            } catch (IllegalArgumentException e) {
                // Failure to calculate AUC --> Invalid algorithm, one cause may be algorithm too complex for instance and cannot generate results in time.
                log.debug("Error while calculating AUC: ", e);
                return new ExecutionResult(
                        new ExecuteResponse(),
                        slowOverrunMillis,
                        "INVALID_AUC",
                        e.getMessage()
                );
            }

        } else {
            if(iraceConfig.isAuc()){
                score = MetricUtil.areaUnderCurve(mainObj,
                        TimeUtil.convert(solverConfig.getIgnoreInitialMillis(), TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS),
                        TimeUtil.convert(solverConfig.getIntervalDurationMillis(), TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS),
                        solverConfig.isLogScaleArea()
                );
            } else {
                score = mainObj.evalSol(solution);
            }
        }

        if (Context.getMainObjective().getFMode() == FMode.MAXIMIZE) {
            score *= -1; // Irace only minimizes. Applies to area under the metric curve too.
        }

        double elapsedSeconds = TimeUtil.nanosToSecs(endTime - startTime);
        log.debug("IRACE Iteration: {} {}", score, elapsedSeconds);
        return new ExecutionResult(
                new ExecuteResponse(score, elapsedSeconds),
                slowOverrunMillis,
                null,
                null
        );
    }

    private long checkExecutionTime(Algorithm<S, I> algorithm, I instance) {
        long remaining = TimeControl.remaining();
        if (remaining < -TimeUtil.secsToNanos(Executor.EXTRA_SECS_BEFORE_WARNING)) {
            log.warn("Algorithm takes too long to stop after time is up in instance {}. Algorithm::toString {}", instance.getId(), algorithm);
            return TimeUnit.NANOSECONDS.toMillis(-remaining);
        }
        return 0;
    }

    public void iraceProgressCallback(
            String runId,
            int iteration,
            List<EliteConfiguration> elites,
            IraceProgressDetails progress
    ) {
        this.runState.publishProgress(runId, iteration, elites, progress);
    }

    public String getIntegrationKey() {
        return this.integrationKey;
    }

    @Override
    public List<String> getNames() {
        return List.of("irace", "autoconfig");
    }

    private record ExecutionResult(
            ExecuteResponse response,
            long slowOverrunMillis,
            String rejectionCode,
            String rejectionMessage
    ) {
    }
}
