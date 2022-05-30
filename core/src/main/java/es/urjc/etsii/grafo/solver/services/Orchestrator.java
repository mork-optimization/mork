package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.exception.ResourceLimitException;
import es.urjc.etsii.grafo.solver.executors.Executor;
import es.urjc.etsii.grafo.solver.experiment.Experiment;
import es.urjc.etsii.grafo.solver.experiment.ExperimentManager;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentStartedEvent;
import es.urjc.etsii.grafo.util.BenchmarkUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * <p>Orchestrator class.</p>
 */
@Service
@ConditionalOnExpression(value = "!${irace.enabled}")
public class Orchestrator<S extends Solution<S, I>, I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = Logger.getLogger(Orchestrator.class.toString());
    public static final int MAX_WORKLOAD = 1_000_000;


    private final IOManager<S, I> io;
    private final InstanceManager<I> instanceManager;
    private final ExperimentManager<S, I> experimentManager;
    private final ExceptionHandler<S, I> exceptionHandler;
    private final Executor<S, I> executor;
    private final SolverConfig solverConfig;

    /**
     * <p>Constructor for Orchestrator.</p>
     *
     * @param solverConfig      a {@link SolverConfig} object.
     * @param io                a {@link IOManager} object.
     * @param instanceManager   a {@link InstanceManager} object.
     * @param experimentManager a {@link ExperimentManager} object.
     * @param exceptionHandlers a {@link List} object.
     * @param executor          a {@link Executor} object.
     */
    public Orchestrator(
            SolverConfig solverConfig,
            IOManager<S, I> io,
            InstanceManager<I> instanceManager,
            ExperimentManager<S, I> experimentManager,
            List<ExceptionHandler<S, I>> exceptionHandlers,
            Executor<S, I> executor
    ) {
        this.solverConfig = solverConfig;
        this.io = io;
        this.instanceManager = instanceManager;
        this.experimentManager = experimentManager;
        this.exceptionHandler = decideImplementation(exceptionHandlers, DefaultExceptionHandler.class);
        this.executor = executor;
    }

    private void runBenchmark() {
        if (solverConfig.isBenchmark()) {
            log.info("Running CPU benchmark...");
            double score = BenchmarkUtil.getBenchmarkScore(this.solverConfig.getSeed());
            log.info("Benchmark score: " + score);
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
        log.info("Experiments to execute: " + experiments.keySet());
        EventPublisher.getInstance().publishEvent(new ExecutionStartedEvent(new ArrayList<>(experiments.keySet())));
        long startTime = System.nanoTime();
        try {
            experiments.values().forEach(this::experimentWrapper);
        } finally {
            executor.shutdown();
            long totalExecutionTime = System.nanoTime() - startTime;
            EventPublisher.getInstance().publishEvent(new ExecutionEndedEvent(totalExecutionTime));
            log.info(String.format("Total execution time: %s (s)", nanosToSecs(totalExecutionTime)));
        }
    }

    private void experimentWrapper(Experiment<S, I> experiment) {
        long startTimestamp = System.currentTimeMillis();
        long startTime = System.nanoTime();
        log.info("Running experiment: " + experiment.name());

        var instancePaths = instanceManager.getInstanceSolveOrder(experiment.name());
        verifyWorkloadLimit(solverConfig, instancePaths, experiment.algorithms());
        EventPublisher.getInstance().publishEvent(new ExperimentStartedEvent(experiment.name(), instancePaths));
        executor.executeExperiment(experiment, instancePaths, exceptionHandler, startTimestamp);
        long experimenExecutionTime = System.nanoTime() - startTime;
        EventPublisher.getInstance().publishEvent(new ExperimentEndedEvent(experiment.name(), experimenExecutionTime, startTimestamp));
        log.info("Finished running experiment: " + experiment.name());
    }

    public static void verifyWorkloadLimit(SolverConfig config, List<String> instanceNames, List<?> experiment) {
        int calculatedWorkload = instanceNames.size() * experiment.size() * config.getRepetitions();
        if(calculatedWorkload >= MAX_WORKLOAD){
            throw new ResourceLimitException(String.format("Maximum workload exceeded, reduce instances, number of algorithms or repetitions: %s * %s * %s = %s >= %s\n Tip: You may decrease the number of iterations using a multistart algorithm without changing the result", instanceNames.size(), experiment.size(), config.getRepetitions(), calculatedWorkload, MAX_WORKLOAD));
        }
    }
}
