package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.Algorithm;
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
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.services.AbstractOrchestrator;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.solution.metrics.Metrics.BEST_OBJECTIVE_FUNCTION;
import static es.urjc.etsii.grafo.util.IOUtil.*;
import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * <p>IraceOrchestrator class.</p>
 *
 */
@Service
@ConditionalOnExpression(value = "${irace.enabled}")
public class IraceOrchestrator<S extends Solution<S,I>, I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(IraceOrchestrator.class);
    private static final String IRACE_EXPNAME = "irace autoconfig";

    private static final long IGNORE_MILLIS = 10_000;
    private static final long MAX_EXECTIME_MILLIS = 60_000;
    private static final long INTERVAL_DURATION = MAX_EXECTIME_MILLIS - IGNORE_MILLIS;

    private final SolverConfig solverConfig;
    private final InstanceConfiguration instanceConfiguration;
    private final IraceIntegration iraceIntegration;
    private final SolutionBuilder<S, I> solutionBuilder;
    private final IraceAlgorithmGenerator<S,I> algorithmGenerator;
    private final InstanceManager<I> instanceManager;
    private final Optional<SolutionValidator<S,I>> validator;
    private final Environment env;

    private final AlgorithmCandidateGenerator algorithmCandidateGenerator;
    private CopyOnWriteArrayList<IraceRuntimeConfiguration> configHistoric = new CopyOnWriteArrayList<>();

    /**
     * <p>Constructor for IraceOrchestrator.</p>
     *
     * @param solverConfig                a {@link SolverConfig} object.
     * @param instanceConfiguration
     * @param iraceIntegration            a {@link IraceIntegration} object.
     * @param instanceManager             a {@link InstanceManager} object.
     * @param solutionBuilders            a {@link List} object.
     * @param algorithmGenerator          a {@link Optional} object.
     * @param validator
     * @param env                         a {@link Environment} object.
     * @param algorithmCandidateGenerator
     */
    public IraceOrchestrator(
            SolverConfig solverConfig,
            InstanceConfiguration instanceConfiguration, IraceIntegration iraceIntegration,
            InstanceManager<I> instanceManager,
            List<SolutionBuilder<S, I>> solutionBuilders,
            Optional<IraceAlgorithmGenerator<S, I>> algorithmGenerator,
            Optional<SolutionValidator<S, I>> validator, Environment env, AlgorithmCandidateGenerator algorithmCandidateGenerator) {
        this.solverConfig = solverConfig;
        this.instanceConfiguration = instanceConfiguration;
        this.iraceIntegration = iraceIntegration;
        this.solutionBuilder = decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
        this.instanceManager = instanceManager;
        this.env = env;
        log.info("Using SolutionBuilder implementation: {}", this.solutionBuilder.getClass().getSimpleName());

        this.algorithmGenerator = algorithmGenerator.orElseThrow(() -> new RuntimeException("IRACE mode enabled but no implementation of IraceAlgorithmGenerator has been found. Check the Mork docs section about IRACE."));
        log.info("Using IraceAlgorithmGenerator implementation: {}", this.algorithmGenerator.getClass().getSimpleName());

        this.algorithmCandidateGenerator = algorithmCandidateGenerator;

        if(validator.isEmpty()){
            log.warn("No SolutionValidator implementation has been found, solution CORRECTNESS WILL NOT BE CHECKED");
        } else {
            log.info("SolutionValidator implementation found: {}", validator.get().getClass().getSimpleName());
        }

        this.validator = validator;

        if(this.validator.isEmpty()){
            log.warn("");
        }

    }


    /** {@inheritDoc} */
    @Override
    public void run(String... args) {
        log.info("App started in IRACE mode, ready to start solving!");
        long startTime = System.nanoTime();
        var experimentName = List.of(IRACE_EXPNAME);
        EventPublisher.getInstance().publishEvent(new ExecutionStartedEvent(Mork.isMaximizing(), experimentName));
        try{
            launchIrace();
        } finally {
            long totalExecutionTime = System.nanoTime() - startTime;
            EventPublisher.getInstance().publishEvent(new ExecutionEndedEvent(totalExecutionTime));
            log.info("Total execution time: {} (s)", nanosToSecs(totalExecutionTime));
        }
    }

    private void launchIrace() {
        log.info("Running experiment: IRACE autoconfig" );
        EventPublisher.getInstance().publishEvent(new ExperimentStartedEvent(IRACE_EXPNAME, new ArrayList<>()));
        // Users must implement an Instance Importer to explain how to load instances
        // Use that class to see if the project is executing inside a JAR file or inside an IDE, to appropriately fix path
        // TODO: Review and improve
        var referenceClass = instanceManager.getUserImporterImplementation().getClass();
        var isJAR = IOUtil.isJAR(referenceClass);
        extractIraceFiles(isJAR);
        long start = System.nanoTime();
        long startTimestamp = System.currentTimeMillis();
        if(solverConfig.isAutoconfig()){
            overrideIraceParameters();
        }
        iraceIntegration.runIrace(isJAR);
        long end = System.nanoTime();
        log.info("Finished running experiment: IRACE autoconfig");
        EventPublisher.getInstance().publishEvent(new ExperimentEndedEvent(IRACE_EXPNAME, end - start, startTimestamp));
    }

    private void overrideIraceParameters() {
        var destination = Path.of("parameters.txt");
        var nodes = this.algorithmCandidateGenerator.buildTree(solverConfig.getTreeDepth());
        var iraceParams = this.algorithmCandidateGenerator.toIraceParams(nodes);
        try {
            Files.write(destination, iraceParams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void extractIraceFiles(boolean isJar) {
        try {
            var substitutions = getSubstitutions(integrationKey, solverConfig, instanceConfiguration, env);
            copyWithSubstitutions(getInputStreamFor("scenario.txt", isJar), Path.of("scenario.txt"), substitutions);
            copyWithSubstitutions(getInputStreamFor("parameters.txt", isJar), Path.of("parameters.txt"), substitutions);
            copyWithSubstitutions(getInputStreamFor("forbidden.txt", isJar), Path.of("forbidden.txt"), substitutions);
            copyWithSubstitutions(getInputStreamFor("middleware.sh", isJar), Path.of("middleware.sh"), substitutions);
            markAsExecutable("middleware.sh");

        } catch (IOException e){
            throw new RuntimeException("Failed extracting irace config files", e);
        }
    }

    private final String integrationKey = StringUtil.generateSecret();

    private Map<String, String> getSubstitutions(String integrationKey, SolverConfig solverConfig, InstanceConfiguration instanceConfiguration, Environment env){
        String maxExperiments = env.getProperty("irace.maxExperiments", "10000");
        return Map.of(
                "__INTEGRATION_KEY__", integrationKey,
                "__INSTANCES_PATH__", instanceConfiguration.getPath("irace"),
                "__TARGET_RUNNER__", "./middleware.sh",
                "__PARALLEL__", nParallel(solverConfig),
                "__MAX_EXPERIMENTS__", maxExperiments,
                "__SEED__", String.valueOf(solverConfig.getSeed())
        );
    }

    private String nParallel(SolverConfig solverConfig){
        if(solverConfig.isParallelExecutor()){
            int n = solverConfig.getnWorkers();
            if(n < 1){
                n = Runtime.getRuntime().availableProcessors() / 2;
            }
            return String.valueOf(n);
        } else {
            return "1";
        }
    }

    public List<IraceRuntimeConfiguration> getConfigHistoric() {
        return configHistoric;
    }

    /**
     * <p>iraceCallback.</p>
     *
     * @param request a {@link ExecuteRequest} object.
     * @return a double.
     */
    public String iraceCallback(ExecuteRequest request){
        var config = buildConfig(request);
        this.configHistoric.add(config);
        var instancePath = config.getInstanceName();
        var instance = instanceManager.getInstance(instancePath);
        var algorithm = this.algorithmGenerator.buildAlgorithm(config.getAlgorithmConfig());
        algorithm.setBuilder(this.solutionBuilder);
        log.debug("Config {}. Built algorithm: {}", config, algorithm);

        // Configure randoms for reproducible experimentation
        long seed = Long.parseLong(config.getSeed());
        RandomManager.localConfiguration(this.solverConfig.getRandomType(), seed);

        // Execute
        String result = singleExecution(algorithm, instance);
        return result;
    }

    private IraceRuntimeConfiguration buildConfig(ExecuteRequest request){
        if(!request.getKey().equals(integrationKey)){
            throw new IllegalArgumentException(String.format("Invalid integration key, got %s", request.getKey()));
        }
        String decoded = StringUtil.b64decode(request.getConfig());
        return toIraceRuntimeConfig(decoded);
    }

    public static IraceRuntimeConfiguration toIraceRuntimeConfig(String commandline){
        String[] args = commandline.split("\\s+");

        String candidateConfiguration = args[0];
        String instanceId = args[1];
        String seed = args[2];
        String instance = args[3];

        String[] algParams = Arrays.copyOfRange(args, 4, args.length);

        return new IraceRuntimeConfiguration(candidateConfiguration, instanceId, seed, instance, new AlgorithmConfiguration(algParams));
    }


    private String singleExecution(Algorithm<S,I> algorithm, I instance) {
        if(solverConfig.isAutoconfig()){
            MetricsManager.enableMetrics();
            MetricsManager.resetMetrics();
            TimeControl.setMaxExecutionTime(MAX_EXECTIME_MILLIS, TimeUnit.MILLISECONDS);
            TimeControl.start();
        }

        long startTime = System.nanoTime();

        var solution = algorithm.algorithm(instance);
        long endTime = System.nanoTime();

        // If the user has implemented a solution validator, check solution correctness
        validator.ifPresent(v -> v.validate(solution).throwIfFail());

        // TODO create custom exception if the params are not valid, and return really bad score
        double score;
        if(solverConfig.isAutoconfig()){
            TimeControl.remove();
            var metrics = MetricsManager.getInstance();
            score = metrics.hypervolume(BEST_OBJECTIVE_FUNCTION,
                    TimeUtil.convert(IGNORE_MILLIS, TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS),
                    TimeUtil.convert(INTERVAL_DURATION, TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS)
            );
            score /= TimeUtil.NANOS_IN_MILLISECOND;
        } else {
            score = solution.getScore();
        }
        if(Mork.isMaximizing()){
            score *= -1; // Irace only minimizes. Applies to area under the metric curve too.
        }
        double elapsedSeconds = TimeUtil.nanosToSecs(endTime - startTime);
        log.debug("IRACE Iteration: {} {}", score, elapsedSeconds);
        return String.format("%s %s", score, elapsedSeconds);
    }
}
