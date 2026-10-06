package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.monitoring.NormalExperimentRunState;

import es.urjc.etsii.grafo.metrics.timing.TimeStatsService;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.AlgorithmProcessingEndedEvent;
import es.urjc.etsii.grafo.events.types.AlgorithmProcessingStartedEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingStartedEvent;
import es.urjc.etsii.grafo.exception.ExceptionHandler;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultManager;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializerListener;
import es.urjc.etsii.grafo.services.IOManager;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import es.urjc.etsii.grafo.results.ResultStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Concurrent executor, execute multiple runs in parallel for a given instance-algorithm pair
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@ConditionalOnExpression(value = "${solver.parallelExecutor}")
public class ConcurrentExecutor<S extends Solution<S, I>, I extends Instance> extends Executor<S, I> {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentExecutor.class);

    private final int nWorkers;
    private ExecutorService executor;

    /**
     * Create a new ConcurrentExecutor. Do not create executors manually, inject them.
     *
     * @param solverConfig Solver configuration instance
     * @param validator    Solution validator
     * @param io           IOManager
     */
    public ConcurrentExecutor(
            SolverConfig solverConfig,
            Optional<SolutionValidator<S, I>> validator,
            Optional<TimeLimitCalculator<S, I>> timeLimitCalculator,
            IOManager<S, I> io,
            InstanceManager<I> instanceManager,
            List<ExceptionHandler<S,I>> exceptionHandlers,
            ReferenceResultManager referenceResultManager,
            MorkEventPublisher eventPublisher,
            ResultStore<S, I> resultStore,
            ResultsSerializerListener<S, I> resultsSerializer, TimeStatsService timeStats, NormalExperimentRunState<S, I> runState
    ) {
        super(validator, timeLimitCalculator, io, instanceManager, solverConfig, exceptionHandlers, referenceResultManager, eventPublisher, resultStore, resultsSerializer, timeStats, runState);
        this.nWorkers = solverConfig.getnWorkers();
    }

    private Map<String, Map<Algorithm<S, I>, List<Future<WorkUnitResult<S, I>>>>> submitAll(Map<String, Map<Algorithm<S, I>, List<WorkUnit<S, I>>>> workUnits) {
        var result = new LinkedHashMap<String, Map<Algorithm<S, I>, List<Future<WorkUnitResult<S, I>>>>>();
        for (var e : workUnits.entrySet()) {
            var perAlgorithm = new LinkedHashMap<Algorithm<S, I>, List<Future<WorkUnitResult<S, I>>>>();
            for (var algorithmWork : e.getValue().entrySet()) {
                var algorithm = algorithmWork.getKey();
                var list = new ArrayList<Future<WorkUnitResult<S, I>>>();
                for (var workUnit : algorithmWork.getValue()) {
                    var future = this.executor.submit(() -> doWork(workUnit));
                    list.add(future);
                }
                perAlgorithm.put(algorithm, list);
            }
            result.put(e.getKey(), perAlgorithm);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeExperiment(Experiment<S, I> experiment, List<String> instancePaths, long startTimestamp) {
        var algorithms = experiment.algorithms();
        var experimentName = experiment.name();
        var workUnits = getOrderedWorkUnits(experiment, instancePaths, solverConfig.getRepetitions());

        // Parent events describe processing/export scopes. WorkUnitStartedEvent and the run-state hooks
        // describe actual concurrent activity; solution events are emitted directly on completion.
        for (var instancePath : instancePaths) {
            var name = instanceName(instancePath);
            var refs = referenceResultManager.getRefValueForAllObjectives(name, false);
            runState.instanceStarted(experimentName, instancePath, name, refs);
            eventPublisher.publish(new InstanceProcessingStartedEvent(experimentName, name, algorithms, solverConfig.getRepetitions(), refs));
            for (var algorithm : algorithms) eventPublisher.publish(new AlgorithmProcessingStartedEvent(experimentName, name, algorithm, solverConfig.getRepetitions()));
        }

        try (var pb = getGlobalSolvingProgressBar(experimentName, workUnits)) {
            // Launch execution of all work units in parallel
            var futures = submitAll(workUnits);

            // Preserve scheduled export order, without delaying result storage or monitoring updates.
            // K: Instance load path --> V: List of WorkUnits
            for (var e : futures.entrySet()) {
                WorkUnitResult<S, I> instanceBest = null;
                var instancePath = e.getKey();
                var instanceName = instanceName(instancePath);
                long instanceStartTime = System.nanoTime();

                pb.setExtraMessage(instanceName);
                for (var algorithmWork : e.getValue().entrySet()) {
                    WorkUnitResult<S, I> algorithmBest = null;
                    var algorithm = algorithmWork.getKey();
                    log.debug("Running algorithm {} for instance {}", algorithm.getName(), instanceName);
                    for (var workUnit : algorithmWork.getValue()) {
                        var workUnitResult = ConcurrencyUtil.await(workUnit);
                        this.processWorkUnitResult(workUnitResult, pb);
                        if (improves(workUnitResult, algorithmBest)) {
                            algorithmBest = workUnitResult;
                        }
                        if (improves(workUnitResult, instanceBest)) {
                            instanceBest = workUnitResult;
                        }
                    }
                    assert algorithmBest != null;
                    if(solverConfig.getRepetitions() > 1){
                        exportAlgorithmInstanceSolution(algorithmBest);
                    }
                    eventPublisher.publish(new AlgorithmProcessingEndedEvent(experimentName, instanceName, algorithm, solverConfig.getRepetitions()));
                    runState.algorithmFinalized(experimentName, instancePath, algorithm.getName());

                }

                assert instanceBest != null;
                if(algorithms.size() > 1){
                    exportInstanceSolution(instanceBest);
                }
                long totalInstanceTime = System.nanoTime() - instanceStartTime;
                finishInstance(experimentName, instanceName, totalInstanceTime, startTimestamp);
                runState.instanceFinalized(experimentName, instancePath);
            }
        }

    }

    @Override
    public void startup() {
        int availableCores = Runtime.getRuntime().availableProcessors();
        if(this.nWorkers > availableCores){
            log.warn("Configured number of workers ({}) is higher than available processor cores ({}). This may lead to performance degradation due to excessive context switching.", this.nWorkers, availableCores);
        }
        this.executor = Executors.newFixedThreadPool(this.nWorkers);
        log.debug("Allocating threadpool with {} workers", this.nWorkers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        log.debug("Requesting threadpool shutdown");
        if (this.executor == null) return;
        this.executor.shutdownNow();
        try {
            if (!this.executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS))
                throw new IllegalStateException("Solver tasks did not terminate after cancellation");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for solver shutdown", e);
        }
    }
}
