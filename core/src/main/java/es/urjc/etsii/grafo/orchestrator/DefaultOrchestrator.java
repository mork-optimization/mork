package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentStartedEvent;
import es.urjc.etsii.grafo.exception.ResourceLimitException;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.ExperimentManager;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializerListener;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.util.BenchmarkUtil;
import es.urjc.etsii.grafo.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * <p>UserExperimentOrchestrator class.</p>
 */
public class DefaultOrchestrator<S extends Solution<S, I>, I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DefaultOrchestrator.class);
    public static final int MAX_WORKLOAD = 1_000_000;

    private final BlockConfig blockConfig;
    private final InstanceManager<I> instanceManager;
    private final ExperimentManager<S, I> experimentManager;
    private final Optional<SolutionValidator<S, I>> validator;
    private final Executor<S, I> executor;
    private final SolverConfig solverConfig;
    private final MorkEventPublisher eventPublisher;
    private final ExecutionLifecycleCoordinator lifecycleCoordinator;
    private final ResultsSerializerListener<S, I> resultsSerializer;

    /**
     * <p>Constructor for UserExperimentOrchestrator.</p>
     *
     * @param solverConfig      a {@link SolverConfig} object.
     * @param instanceManager   a {@link InstanceManager} object.
     * @param experimentManager a {@link ExperimentManager} object.
     * @param executor          a {@link Executor} object.
     */
    public DefaultOrchestrator(
            SolverConfig solverConfig,
            BlockConfig blockConfig,
            InstanceManager<I> instanceManager,
            ExperimentManager<S, I> experimentManager,
            Optional<SolutionValidator<S,I>> validator,
            Executor<S, I> executor,
            MorkEventPublisher eventPublisher,
            ExecutionLifecycleCoordinator lifecycleCoordinator,
            ResultsSerializerListener<S, I> resultsSerializer
    ) {
        this.solverConfig = solverConfig;
        this.blockConfig = blockConfig;
        this.instanceManager = instanceManager;
        this.experimentManager = experimentManager;
        this.validator = validator;
        this.executor = executor;
        this.eventPublisher = eventPublisher;
        this.lifecycleCoordinator = lifecycleCoordinator;
        this.resultsSerializer = resultsSerializer;
    }

    protected void runBenchmark() {
        if (solverConfig.isBenchmark()) {
            double score = BenchmarkUtil.getBenchmarkScore(this.solverConfig.getSeed());
            log.info("Benchmark score: {}", score);
        } else {
            log.info("Skipping CPU benchmark");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(String... args) {
        Context.Configurator.setSolverConfig(solverConfig);
        Context.Configurator.setBlockConfig(blockConfig);
        if (validator.isEmpty()) {
            log.warn("No SolutionValidator implementation has been found, solution CORRECTNESS WILL NOT BE CHECKED");
        } else {
            log.info("SolutionValidator implementation found: {}", validator.get().getClass().getSimpleName());
        }
        this.experimentManager.runValidations();
        runBenchmark();
        log.info("Ready to start solving!");
        var experiments = this.experimentManager.getExperiments();
        if(experiments.size() > 1){
            log.info("Experiments to execute: {}", experiments.keySet());
        }
        eventPublisher.publish(new ExecutionStartedEvent(Context.getObjectivesW(), new ArrayList<>(experiments.keySet())));
        long startTime = System.nanoTime();
        try {
            executor.startup();
            for(var experiment : experiments.values()){
                experimentWrapper(experiment);
            }
        } finally {
            long totalExecutionTime = System.nanoTime() - startTime;
            executor.shutdown();
            lifecycleCoordinator.complete(totalExecutionTime);
            log.info("Total execution time: {} (s)", String.format("%.2f", nanosToSecs(totalExecutionTime)));
        }
    }

    private void experimentWrapper(Experiment<S, I> experiment) {
        long startTimestamp = System.currentTimeMillis();
        long startTime = System.nanoTime();
        log.info("Running experiment: {}", experiment.name());

        var instancePaths = instanceManager.getInstanceSolveOrder(experiment.name());
        verifyWorkloadLimit(solverConfig, instancePaths, experiment.algorithms());
        if (solverConfig.getWarmup().isEnabled()) {
            var warmupInstancePath = instanceManager.getWarmupInstancePath(experiment.name(), instancePaths);
            executor.warmup(experiment, warmupInstancePath);
        }
        eventPublisher.publish(new ExperimentStartedEvent(experiment.name(), instancePaths));
        executor.executeExperiment(experiment, instancePaths, startTimestamp);
        long experimentExecutionTime = System.nanoTime() - startTime;
        try {
            resultsSerializer.serializeAtExperimentEnd(experiment.name(), startTimestamp);
        } catch (RuntimeException e) {
            log.error("Final result serialization failed for experiment {}", experiment.name(), e);
            eventPublisher.publish(new ErrorEvent(e));
        }
        eventPublisher.publish(new ExperimentEndedEvent(experiment.name(), experimentExecutionTime, startTimestamp));
        log.info("Finished running experiment: {}", experiment.name());
    }

    public static void verifyWorkloadLimit(SolverConfig config, List<String> instancePaths, List<?> experiment) {
        int calculatedWorkload = instancePaths.size() * experiment.size() * config.getRepetitions();
        if(calculatedWorkload >= MAX_WORKLOAD){
            throw new ResourceLimitException(String.format("Maximum workload exceeded, reduce instances, number of algorithms or repetitions: %s * %s * %s = %s >= %s%nTip: You may decrease the number of iterations using a multistart algorithm without changing the result", instancePaths.size(), experiment.size(), config.getRepetitions(), calculatedWorkload, MAX_WORKLOAD));
        }
    }
}
