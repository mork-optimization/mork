package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.AlgorithmProcessingEndedEvent;
import es.urjc.etsii.grafo.events.types.AlgorithmProcessingStartedEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingStartedEvent;
import es.urjc.etsii.grafo.exceptions.ExceptionHandler;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.services.IOManager;
import es.urjc.etsii.grafo.services.SolutionValidator;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;

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
@Profile("user-experiment")
@ConditionalOnExpression(value = "${solver.parallelExecutor}")
public class ConcurrentExecutor<S extends Solution<S, I>, I extends Instance> extends Executor<S, I> {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentExecutor.class);

    private final int nWorkers;
    private final ExecutorService executor;

    /**
     * Create a new ConcurrentExecutor. Do not create executors manually, inject them.
     *
     * @param solverConfig Solver configuration instance
     * @param validator    Solution validator
     * @param io           IOManager
     */
    public ConcurrentExecutor(SolverConfig solverConfig, Optional<SolutionValidator<S, I>> validator, Optional<TimeLimitCalculator<S, I>> timeLimitCalculator, IOManager<S, I> io, InstanceManager<I> instanceManager, List<ReferenceResultProvider> referenceResultProviders) {
        super(validator, timeLimitCalculator, io, instanceManager, referenceResultProviders, solverConfig);
        if (solverConfig.getnWorkers() == -1) {
            this.nWorkers = Runtime.getRuntime().availableProcessors() / 2;
        } else {
            this.nWorkers = solverConfig.getnWorkers();
        }
        this.executor = Executors.newFixedThreadPool(this.nWorkers);
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
    public void executeExperiment(Experiment<S, I> experiment, List<String> instanceNames, ExceptionHandler<S, I> exceptionHandler, long startTimestamp) {
        var algorithms = experiment.algorithms();
        var experimentName = experiment.name();
        var workUnits = getOrderedWorkUnits(experiment, instanceNames, exceptionHandler, solverConfig.getRepetitions());

        var events = EventPublisher.getInstance();

        try (var pb = getGlobalSolvingProgressBar(experimentName, workUnits)) {
            // Launch execution of all work units in parallel
            var futures = submitAll(workUnits);

            // Simulate sequential execution to trigger all events in correct order
            // K: Instance name --> V: List of WorkUnits
            for (var e : futures.entrySet()) {
                WorkUnitResult<S, I> instanceBest = null;
                var instancePath = e.getKey();
                var instanceName = instanceName(instancePath);
                long instanceStartTime = System.nanoTime();
                var referenceValue = getOptionalReferenceValue(instanceName, false);
                events.publishEvent(new InstanceProcessingStartedEvent(experimentName, instanceName, algorithms, solverConfig.getRepetitions(), referenceValue));
                log.debug("Running algorithms for instance: {}", instanceName);

                pb.setExtraMessage(instanceName);
                for (var algorithmWork : e.getValue().entrySet()) {
                    WorkUnitResult<S, I> algorithmBest = null;
                    var algorithm = algorithmWork.getKey();
                    events.publishEvent(new AlgorithmProcessingStartedEvent<>(experimentName, instanceName, algorithm, solverConfig.getRepetitions()));
                    log.debug("Running algorithm {} for instance {}", algorithm.getShortName(), instanceName);
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
                    exportAlgorithmInstanceSolution(algorithmBest);
                    events.publishEvent(new AlgorithmProcessingEndedEvent<>(experimentName, instanceName, algorithm, solverConfig.getRepetitions()));

                }

                assert instanceBest != null;
                exportInstanceSolution(instanceBest);
                long totalInstanceTime = System.nanoTime() - instanceStartTime;
                events.publishEvent(new InstanceProcessingEndedEvent(experimentName, instanceName, totalInstanceTime, startTimestamp));
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        log.debug("Requesting threadpool shutdown");
        this.executor.shutdown();
    }
}
