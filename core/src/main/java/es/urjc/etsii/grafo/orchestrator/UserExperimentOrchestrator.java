package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentStartedEvent;
import es.urjc.etsii.grafo.exception.ResourceLimitException;
import es.urjc.etsii.grafo.exceptions.DefaultExceptionHandler;
import es.urjc.etsii.grafo.exception.ExceptionHandler;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.ExperimentManager;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.util.BenchmarkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * <p>UserExperimentOrchestrator class.</p>
 */
@Service
@Profile("user-experiment")
public class UserExperimentOrchestrator<S extends Solution<S, I>, I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(UserExperimentOrchestrator.class);
    public static final int MAX_WORKLOAD = 1_000_000;

    private final InstanceManager<I> instanceManager;
    private final ExperimentManager<S, I> experimentManager;
    private final Executor<S, I> executor;
    private final SolverConfig solverConfig;

    /**
     * <p>Constructor for UserExperimentOrchestrator.</p>
     *
     * @param solverConfig      a {@link SolverConfig} object.
     * @param instanceManager   a {@link InstanceManager} object.
     * @param experimentManager a {@link ExperimentManager} object.
     * @param executor          a {@link Executor} object.
     */
    public UserExperimentOrchestrator(
            SolverConfig solverConfig,
            InstanceManager<I> instanceManager,
            ExperimentManager<S, I> experimentManager,
            Executor<S, I> executor
    ) {
        this.solverConfig = solverConfig;
        this.instanceManager = instanceManager;
        this.experimentManager = experimentManager;
        this.executor = executor;
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
        runBenchmark();
        log.info("App started, ready to start solving!");
        var experiments = this.experimentManager.getExperiments();
        log.info("Experiments to execute: {}", experiments.keySet());
        EventPublisher.getInstance().publishEvent(new ExecutionStartedEvent(Mork.isMaximizing(), new ArrayList<>(experiments.keySet())));
        long startTime = System.nanoTime();
        try {
            experiments.values().forEach(this::experimentWrapper);
        } finally {
            executor.shutdown();
            long totalExecutionTime = System.nanoTime() - startTime;
            EventPublisher.getInstance().publishEvent(new ExecutionEndedEvent(totalExecutionTime));
            log.info("Total execution time: {} (s)", nanosToSecs(totalExecutionTime));
        }
    }

    private void experimentWrapper(Experiment<S, I> experiment) {
        long startTimestamp = System.currentTimeMillis();
        long startTime = System.nanoTime();
        log.info("Running experiment: {}", experiment.name());

        var instancePaths = instanceManager.getInstanceSolveOrder(experiment.name());
        verifyWorkloadLimit(solverConfig, instancePaths, experiment.algorithms());
        EventPublisher.getInstance().publishEvent(new ExperimentStartedEvent(experiment.name(), instancePaths));
        executor.executeExperiment(experiment, instancePaths, startTimestamp);
        long experimentExecutionTime = System.nanoTime() - startTime;
        EventPublisher.getInstance().publishEvent(new ExperimentEndedEvent(experiment.name(), experimentExecutionTime, startTimestamp));
        log.info("Finished running experiment: {}", experiment.name());
    }

    public static void verifyWorkloadLimit(SolverConfig config, List<String> instanceNames, List<?> experiment) {
        int calculatedWorkload = instanceNames.size() * experiment.size() * config.getRepetitions();
        if(calculatedWorkload >= MAX_WORKLOAD){
            throw new ResourceLimitException(String.format("Maximum workload exceeded, reduce instances, number of algorithms or repetitions: %s * %s * %s = %s >= %s%nTip: You may decrease the number of iterations using a multistart algorithm without changing the result", instanceNames.size(), experiment.size(), config.getRepetitions(), calculatedWorkload, MAX_WORKLOAD));
        }
    }
}
