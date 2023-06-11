package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.service.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentStartedEvent;
import es.urjc.etsii.grafo.exception.IllegalAlgorithmConfigException;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator;
import es.urjc.etsii.grafo.services.ReflectiveSolutionBuilder;
import es.urjc.etsii.grafo.services.SolutionValidator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.metrics.MetricsManager;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.StringUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.TimeUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.solution.metrics.Metrics.BEST_OBJECTIVE_FUNCTION;
import static es.urjc.etsii.grafo.util.IOUtil.*;
import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * <p>IraceOrchestrator class.</p>
 */
@Service
@Profile({"irace", "autoconfig"})
public class IraceOrchestrator<S extends Solution<S, I>, I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(IraceOrchestrator.class);
    private static final String IRACE_EXPNAME = "irace autoconfig";
    public static final String K_INTEGRATION_KEY = "__INTEGRATION_KEY__";
    public static final String K_INSTANCES_PATH = "__INSTANCES_PATH__";
    public static final String K_TARGET_RUNNER = "__TARGET_RUNNER__";
    public static final String K_PARALLEL = "__PARALLEL__";
    public static final String K_MAX_EXP = "__MAX_EXPERIMENTS__";
    public static final String K_SEED = "__SEED__";
    public static final String K_PORT = "__PORT__";
    public static final String F_PARAMETERS = "parameters.txt";
    public static final String F_SCENARIO = "scenario.txt";
    public static final String F_FORBIDDEN = "forbidden.txt";
    public static final int DEFAULT_IRACE_EXPERIMENTS = 10_000;
    public static final int MINIMUM_IRACE_EXPERIMENTS = 10_000;
    public static final int MAX_HISTORIC_CONFIG_SIZE = 1_000;


    private final SolverConfig solverConfig;
    private final InstanceConfiguration instanceConfiguration;
    private final IraceIntegration iraceIntegration;
    private final ServerProperties serverProperties;
    private final SolutionBuilder<S, I> solutionBuilder;
    private final AlgorithmBuilder<S, I> algorithmBuilder;
    private final InstanceManager<I> instanceManager;
    private final Optional<SolutionValidator<S, I>> validator;

    private final AlgorithmCandidateGenerator algorithmCandidateGenerator;
    private final ConcurrentLinkedQueue<IraceRuntimeConfiguration> configHistoric = new ConcurrentLinkedQueue<>();
    private final boolean isAutoconfigEnabled;
    private int nIraceParameters = -1;

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
     * @param algorithmCandidateGenerator
     */
    public IraceOrchestrator(
            Environment env,
            SolverConfig solverConfig,
            ServerProperties serverProperties,
            InstanceConfiguration instanceConfiguration, IraceIntegration iraceIntegration,
            InstanceManager<I> instanceManager,
            List<SolutionBuilder<S, I>> solutionBuilders,
            List<AlgorithmBuilder<S, I>> algorithmBuilders,
            Optional<SolutionValidator<S, I>> validator,
            AlgorithmCandidateGenerator algorithmCandidateGenerator
    ) {
        this.solverConfig = solverConfig;
        this.instanceConfiguration = instanceConfiguration;
        this.serverProperties = serverProperties;
        this.iraceIntegration = iraceIntegration;
        this.solutionBuilder = decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
        this.instanceManager = instanceManager;
        log.info("Using SolutionBuilder implementation: {}", this.solutionBuilder.getClass().getSimpleName());

        this.algorithmBuilder = decideImplementation(algorithmBuilders, AutomaticAlgorithmBuilder.class);
        log.info("Using AlgorithmBuilder implementation: {}", this.algorithmBuilder.getClass().getSimpleName());

        this.algorithmCandidateGenerator = algorithmCandidateGenerator;

        if (validator.isEmpty()) {
            log.warn("No SolutionValidator implementation has been found, solution CORRECTNESS WILL NOT BE CHECKED");
        } else {
            log.info("SolutionValidator implementation found: {}", validator.get().getClass().getSimpleName());
        }

        this.validator = validator;
        this.isAutoconfigEnabled = Arrays.asList(env.getActiveProfiles()).contains("autoconfig");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void run(String... args) {
        log.info("App started in IRACE mode, ready to start solving!");
        long startTime = System.nanoTime();
        var experimentName = List.of(IRACE_EXPNAME);
        EventPublisher.getInstance().publishEvent(new ExecutionStartedEvent(Mork.isMaximizing(), experimentName));
        try {
            launchIrace();
        } finally {
            long totalExecutionTime = System.nanoTime() - startTime;
            EventPublisher.getInstance().publishEvent(new ExecutionEndedEvent(totalExecutionTime));
            log.info("Total execution time: {} (s)", nanosToSecs(totalExecutionTime));
        }
    }

    private void launchIrace() {
        log.info("Running experiment: IRACE autoconfig");
        EventPublisher.getInstance().publishEvent(new ExperimentStartedEvent(IRACE_EXPNAME, new ArrayList<>()));
        // Users must implement an Instance Importer to explain how to load instances
        // Use that class to see if the project is executing inside a JAR file or inside an IDE, to appropriately fix path
        // TODO: Review and improve
        var referenceClass = instanceManager.getUserImporterImplementation().getClass();
        var isJAR = IOUtil.isJAR(referenceClass);
        extractIraceFiles(isJAR);

        long start = System.nanoTime();
        long startTimestamp = System.currentTimeMillis();
        iraceIntegration.runIrace(isJAR);
        long end = System.nanoTime();
        log.info("Finished running experiment: IRACE autoconfig");
        EventPublisher.getInstance().publishEvent(new ExperimentEndedEvent(IRACE_EXPNAME, end - start, startTimestamp));
    }

    private void extractIraceFiles(boolean isJar) {
        Path paramsPath = Path.of(F_PARAMETERS);
        try {
            if (isAutoconfigEnabled) {
                var nodes = this.algorithmCandidateGenerator.buildTree(solverConfig.getTreeDepth());
                var iraceParams = this.algorithmCandidateGenerator.toIraceParams(nodes);
                this.nIraceParameters = iraceParams.size();
                Files.write(paramsPath, iraceParams);
            }

            var substitutions = getSubstitutions(integrationKey, solverConfig, instanceConfiguration, serverProperties);
            if (!isAutoconfigEnabled) {
                copyWithSubstitutions(getInputStreamFor(F_PARAMETERS, isJar), paramsPath, substitutions);
            }
            copyWithSubstitutions(getInputStreamFor(F_SCENARIO, isJar), Path.of(F_SCENARIO), substitutions);
            copyWithSubstitutions(getInputStreamFor(F_FORBIDDEN, isJar), Path.of(F_FORBIDDEN), substitutions);
        } catch (IOException e) {
            throw new RuntimeException("Failed extracting irace config files", e);
        }
    }

    private final String integrationKey = StringUtil.generateSecret();

    private Map<String, String> getSubstitutions(String integrationKey, SolverConfig solverConfig, InstanceConfiguration instanceConfiguration, ServerProperties server) {
        return Map.of(
                K_INTEGRATION_KEY, integrationKey,
                K_INSTANCES_PATH, instanceConfiguration.getPath("irace"),
                K_TARGET_RUNNER, "./middleware.sh",
                K_PARALLEL, nParallel(solverConfig),
                K_MAX_EXP, calculateMaxExperiments(isAutoconfigEnabled, solverConfig, nIraceParameters),
                K_SEED, String.valueOf(solverConfig.getSeed()),
                K_PORT, String.valueOf(server.getPort())
        );
    }

    protected static String calculateMaxExperiments(boolean autoconfigEnabled, SolverConfig solverConfig, int nIraceParameters) {
        int maxExperiments;
        if (autoconfigEnabled) {
            if (nIraceParameters < 1) {
                throw new IllegalArgumentException("nIraceParameters must be positive");
            }
            maxExperiments = Math.max(MINIMUM_IRACE_EXPERIMENTS, solverConfig.getIterationsPerParameter() * nIraceParameters);
        } else {
            maxExperiments = DEFAULT_IRACE_EXPERIMENTS; // 10k experiments by default if not specified otherwise
        }
        return String.valueOf(maxExperiments);
    }

    protected static String nParallel(SolverConfig solverConfig) {
        if (solverConfig.isParallelExecutor()) {
            int n = solverConfig.getnWorkers();
            if (n < 1) {
                n = Runtime.getRuntime().availableProcessors() / 2;
            }
            return String.valueOf(n);
        } else {
            return "1";
        }
    }

    public Iterable<IraceRuntimeConfiguration> getConfigHistoric() {
        return configHistoric;
    }

    /**
     * <p>iraceCallback.</p>
     *
     * @param request a {@link ExecuteRequest} object.
     * @return a double.
     */
    public String iraceCallback(ExecuteRequest request) {
        var config = buildConfig(request, integrationKey);
        if(configHistoric.size() == MAX_HISTORIC_CONFIG_SIZE){
            configHistoric.remove();
        }
        this.configHistoric.add(config);
        var instancePath = config.getInstanceName();
        var instance = instanceManager.getInstance(instancePath);
        Algorithm<S, I> algorithm = null;
        try {
            algorithm = this.algorithmBuilder.buildFromConfig(config.getAlgorithmConfig());
        } catch (IllegalAlgorithmConfigException e) {
            log.debug("Invalid config, reason {}, config: {}", e.getMessage(), config);
            return failedResult();
        }
        algorithm.setBuilder(this.solutionBuilder);
        if (this.isAutoconfigEnabled) {
            // Because autoconfig iterations executes between two time intervals, if the algorithm finishes
            // but there is extra time available, keep executing the same algorithm until the timelimit is reached.
            // If not, we could be penalizing faster simpler algorithms against more complex ones.
            int iterations = Integer.MAX_VALUE / 2;
            algorithm = new MultiStartAlgorithm<>(algorithm.getShortName(), algorithm, iterations, iterations, iterations);
            algorithm.setBuilder(this.solutionBuilder);
        }

        log.debug("Config {}. Built algorithm: {}", config, algorithm);

        // Configure randoms for reproducible experimentation
        long seed = Long.parseLong(config.getSeed());
        RandomManager.localConfiguration(this.solverConfig.getRandomType(), seed);

        // Execute
        String result = singleExecution(algorithm, instance);
        return result;
    }

    protected static String failedResult() {
//        double score = Mork.isMaximizing()? Integer.MIN_VALUE: Integer.MAX_VALUE;
//        double time = 0;
//        return "%s %s".formatted(score, time);

        // Translate failures so Irace understands what has happened
        // See "10.8 Unreliable target algorithms and immediate rejection" of the Irace Manual for full details
        return "Inf 0";
    }

    protected static IraceRuntimeConfiguration buildConfig(ExecuteRequest request, String integrationKey) {
        if (!request.getKey().equals(integrationKey)) {
            throw new IllegalArgumentException(String.format("Invalid integration key, got %s", request.getKey()));
        }
        String decoded = StringUtil.b64decode(request.getConfig());
        return toIraceRuntimeConfig(decoded);
    }

    public static IraceRuntimeConfiguration toIraceRuntimeConfig(String commandline) {
        String[] args = commandline.split("\\s+");

        String candidateConfiguration = args[0];
        String instanceId = args[1];
        String seed = args[2];
        String instance = args[3];

        String[] algParams = Arrays.copyOfRange(args, 4, args.length);

        return new IraceRuntimeConfiguration(candidateConfiguration, instanceId, seed, instance, new AlgorithmConfiguration(algParams));
    }


    private String singleExecution(Algorithm<S, I> algorithm, I instance) {
        long maxExecTime = this.solverConfig.getIgnoreInitialMillis() + this.solverConfig.getIntervalDurationMillis();
        if (isAutoconfigEnabled) {
            MetricsManager.enableMetrics();
            MetricsManager.resetMetrics();
            TimeControl.setMaxExecutionTime(maxExecTime, TimeUnit.MILLISECONDS);
            TimeControl.start();
        }

        long startTime = System.nanoTime();

        var solution = algorithm.algorithm(instance);
        long endTime = System.nanoTime();

        // If the user has implemented a solution validator, check solution correctness
        validator.ifPresent(v -> v.validate(solution).throwIfFail());

        double score;
        if (isAutoconfigEnabled) {
            checkExecutionTime(algorithm, instance);
            TimeControl.remove();
            var metrics = MetricsManager.getInstance();
            try {
                score = metrics.areaUnderCurve(BEST_OBJECTIVE_FUNCTION,
                        TimeUtil.convert(solverConfig.getIgnoreInitialMillis(), TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS),
                        TimeUtil.convert(solverConfig.getIntervalDurationMillis(), TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS)
                );
                score /= TimeUtil.NANOS_IN_MILLISECOND;
            } catch (IllegalArgumentException e){
                // Failure to calculate AUC --> Invalid algorithm, one cause may be algorithm too complex for instance and cannot generate results in time.
                log.warn("Error while calculating AUC: ", e);
                return failedResult();
            }

        } else {
            score = solution.getScore();
        }
        if (Mork.isMaximizing()) {
            score *= -1; // Irace only minimizes. Applies to area under the metric curve too.
        }
        double elapsedSeconds = TimeUtil.nanosToSecs(endTime - startTime);
        log.debug("IRACE Iteration: {} {}", score, elapsedSeconds);
        return String.format("%s %s", score, elapsedSeconds);
    }

    private void checkExecutionTime(Algorithm<S, I> algorithm, I instance) {
        if (TimeControl.remaining() < -TimeUtil.secsToNanos(Executor.EXTRA_SECS_BEFORE_WARNING)) {
            log.warn("Algorithm takes too long to stop after time is up in instance {}. Algorithm::toString {}", instance.getId(), algorithm);
            slowExecutions.add(new SlowExecution(TimeControl.remaining(), instance.getId(), algorithm));
        }
    }

    private final List<SlowExecution> slowExecutions = Collections.synchronizedList(new ArrayList<>());

    public List<SlowExecution> getSlowRuns() {
        return Collections.unmodifiableList(this.slowExecutions);
    }

    public record SlowExecution(long relativeTime, String instanceName, Algorithm<?, ?> algorithm) {
    }
}
