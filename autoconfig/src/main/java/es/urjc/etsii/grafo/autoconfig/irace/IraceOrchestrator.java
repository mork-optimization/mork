package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceProgressDetails;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentStartedEvent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializerListener;
import es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.util.IOUtil.copyWithSubstitutions;
import static es.urjc.etsii.grafo.util.IOUtil.getInputStreamForIrace;
import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * <p>IraceOrchestrator class.</p>
 */
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
    private static final String IRACE_PARAM_EPILOGUE = """
            
            [global]
            digits = 2
            """;

    public static final int DEFAULT_IRACE_EXPERIMENTS = 10_000;
    private final SolverConfig solverConfig;
    private final BlockConfig blockConfig;
    private final InstanceConfiguration instanceConfiguration;
    private final IraceIntegration iraceIntegration;
    private final ServerProperties serverProperties;
    private final InstanceManager<I> instanceManager;
    private final AutoconfigSearchSpace searchSpace;
    private final MorkEventPublisher eventPublisher;
    private final ExecutionLifecycleCoordinator lifecycleCoordinator;
    private final ResultsSerializerListener<S, I> resultsSerializer;
    private final AutoconfigRunState runState;

    /**
     * <p>Constructor for IraceOrchestrator.</p>
     *
     * @param solverConfig                a {@link SolverConfig} object.
     * @param blockConfig                 block execution configuration
     * @param serverProperties            embedded server configuration
     * @param instanceConfiguration
     * @param iraceIntegration            a {@link IraceIntegration} object.
     * @param instanceManager             a {@link InstanceManager} object.
     * @param searchSpace
     * @param eventPublisher              application event publisher
     * @param lifecycleCoordinator        application lifecycle coordinator
     * @param resultsSerializer           final result serializer
     * @param runState                    shared autoconfig run state
     */
    public IraceOrchestrator(
            SolverConfig solverConfig,
            BlockConfig blockConfig,
            ServerProperties serverProperties,
            InstanceConfiguration instanceConfiguration,
            IraceIntegration iraceIntegration,
            InstanceManager<I> instanceManager,
            AutoconfigSearchSpace searchSpace,
            MorkEventPublisher eventPublisher,
            ExecutionLifecycleCoordinator lifecycleCoordinator,
            ResultsSerializerListener<S, I> resultsSerializer,
            AutoconfigRunState runState
    ) {
        this.solverConfig = solverConfig;
        this.blockConfig = blockConfig;
        this.instanceConfiguration = instanceConfiguration;
        this.serverProperties = serverProperties;
        this.iraceIntegration = iraceIntegration;
        this.instanceManager = instanceManager;
        this.searchSpace = searchSpace;
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
        Context.Configurator.setSolverConfig(solverConfig);
        Context.Configurator.setBlockConfig(blockConfig);
        boolean automaticMode = false;
        boolean follower = false;
        for (String arg : args) {
            if (arg.equals("--autoconfig")) {
                automaticMode = true;
            }
            if (arg.equals("--follower")) {
                follower = true;
            }
        }
        log.info("Starting tuning engine... {isAutoconfig: {}, isFollower: {}}", automaticMode, follower);

        if (follower) {
            this.runState.prepareWorker(automaticMode);
            this.integrationKey = solverConfig.getIntegrationKey();
            log.info("Mork is running in follower mode, waiting for commands...");
            return;
        }

        this.runState.prepareCoordinator(automaticMode);
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
        boolean automaticMode = runState.isAutomaticMode();
        try {
            if (automaticMode) {
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
            if (!automaticMode) {
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
                K_MAX_EXP, calculateMaxExperiments(runState.isAutomaticMode(), solverConfig, parameterCount),
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

}
