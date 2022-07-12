package es.urjc.etsii.grafo.solver.irace;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.restcontroller.dto.ExecuteRequest;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.configuration.InstanceConfiguration;
import es.urjc.etsii.grafo.solver.create.builder.ReflectiveSolutionBuilder;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.AbstractOrchestrator;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentStartedEvent;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.StringUtil;
import es.urjc.etsii.grafo.util.TimeUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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

    private final SolverConfig solverConfig;
    private final InstanceConfiguration instanceConfiguration;
    private final IraceIntegration iraceIntegration;
    private final SolutionBuilder<S, I> solutionBuilder;
    private final IraceAlgorithmGenerator<S,I> algorithmGenerator;
    private final InstanceManager<I> instanceManager;
    private final Environment env;

    /**
     * <p>Constructor for IraceOrchestrator.</p>
     *
     * @param solverConfig          a {@link SolverConfig} object.
     * @param instanceConfiguration
     * @param iraceIntegration      a {@link IraceIntegration} object.
     * @param instanceManager       a {@link InstanceManager} object.
     * @param solutionBuilders      a {@link List} object.
     * @param algorithmGenerator    a {@link Optional} object.
     * @param env                   a {@link Environment} object.
     */
    public IraceOrchestrator(
            SolverConfig solverConfig,
            InstanceConfiguration instanceConfiguration, IraceIntegration iraceIntegration,
            InstanceManager<I> instanceManager,
            List<SolutionBuilder<S, I>> solutionBuilders,
            Optional<IraceAlgorithmGenerator<S, I>> algorithmGenerator,
            Environment env) {
        this.solverConfig = solverConfig;
        this.instanceConfiguration = instanceConfiguration;
        this.iraceIntegration = iraceIntegration;
        this.solutionBuilder = decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
        this.instanceManager = instanceManager;
        this.env = env;
        log.info("Using SolutionBuilder implementation: " + this.solutionBuilder.getClass().getSimpleName());

        this.algorithmGenerator = algorithmGenerator.orElseThrow(() -> new RuntimeException("IRACE mode enabled but no implementation of IraceAlgorithmGenerator has been found. Check the Mork docs section about IRACE."));
        log.info("IRACE mode enabled, using generator: " + this.algorithmGenerator.getClass().getSimpleName());

    }


    /** {@inheritDoc} */
    @Override
    public void run(String... args) {
        log.info("App started in IRACE mode, ready to start solving!");
        long startTime = System.nanoTime();
        var experimentName = List.of(IRACE_EXPNAME);
        EventPublisher.getInstance().publishEvent(new ExecutionStartedEvent(solverConfig.isMaximizing(), experimentName));
        try{
            launchIrace();
        } finally {
            long totalExecutionTime = System.nanoTime() - startTime;
            EventPublisher.getInstance().publishEvent(new ExecutionEndedEvent(totalExecutionTime));
            log.info(String.format("Total execution time: %s (s)", nanosToSecs(totalExecutionTime)));
        }
    }

    private void launchIrace() {
        log.info("Running experiment: IRACE autoconfig" );
        EventPublisher.getInstance().publishEvent(new ExperimentStartedEvent(IRACE_EXPNAME, new ArrayList<>()));
        var referenceClass = algorithmGenerator.getClass();
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
                "__MAX_EXPERIMENTS__", maxExperiments
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


    /**
     * <p>iraceCallback.</p>
     *
     * @param request a {@link es.urjc.etsii.grafo.restcontroller.dto.ExecuteRequest} object.
     * @return a double.
     */
    public String iraceCallback(ExecuteRequest request){
        var config = buildConfig(request);
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
        String[] args = decoded.split("\\s+");

        String candidateConfiguration = args[0];
        String instanceId = args[1];
        String seed = args[2];
        String instance = args[3];

        int length = candidateConfiguration.length() + instanceId.length() + seed.length() + instance.length();
        String algParams = decoded.substring(length);

        return new IraceRuntimeConfiguration(candidateConfiguration, instanceId, seed, instance, new AlgorithmConfiguration(algParams));
    }


    private String singleExecution(Algorithm<S,I> algorithm, I instance) {
        long startTime = System.nanoTime();
        var result = algorithm.algorithm(instance);
        long endTime = System.nanoTime();
        double score = result.getScore();
        double elapsedSeconds = TimeUtil.nanosToSecs(endTime - startTime);
        if(Mork.isMaximizing()){
            score *= -1; // Irace only minimizes
        }
        log.debug(String.format("IRACE Iteration: %s %.2g%n", score, elapsedSeconds));
        return String.format("%s %s", score, elapsedSeconds);
    }
}
