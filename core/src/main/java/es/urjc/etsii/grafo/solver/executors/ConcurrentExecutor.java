package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.experiment.Experiment;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.services.IOManager;
import es.urjc.etsii.grafo.solver.services.InstanceManager;
import es.urjc.etsii.grafo.solver.services.SolutionValidator;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.InstanceProcessingStartedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Concurrent executor, execute multiple runs in parallel for a given instance-algorithm pair
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@ConditionalOnExpression(value = "${solver.parallelExecutor} && !${irace.enabled}")
public class ConcurrentExecutor<S extends Solution<S,I>, I extends Instance> extends Executor<S, I> {

    private static final Logger logger = Logger.getLogger(ConcurrentExecutor.class.getName());

    private final int nWorkers;
    private final ExecutorService executor;

    /**
     * Create a new ConcurrentExecutor. Do not create executors manually, inject them.
     *
     * @param solverConfig Solver configuration instance
     * @param validator Solution validator
     * @param io IOManager
     */
    public ConcurrentExecutor(SolverConfig solverConfig, Optional<SolutionValidator<S, I>> validator, IOManager<S, I> io, InstanceManager<I> instanceManager, List<ReferenceResultProvider> referenceResultProviders) {
        super(validator, io, instanceManager, referenceResultProviders, solverConfig);
        if (solverConfig.getnWorkers() == -1) {
            this.nWorkers = Runtime.getRuntime().availableProcessors() / 2;
        } else {
            this.nWorkers = solverConfig.getnWorkers();
        }
        this.executor = Executors.newFixedThreadPool(this.nWorkers);
    }

    private Map<String, List<Future<WorkUnitResult<S,I>>>> submitAll(Map<String, List<WorkUnit<S,I>>> workUnits){
        var result = new LinkedHashMap<String, List<Future<WorkUnitResult<S,I>>>>();
        for(var e: workUnits.entrySet()){
            var list = new ArrayList<Future<WorkUnitResult<S,I>>>();
            for(var workUnit: e.getValue()){
                var future = this.executor.submit(() -> doWork(workUnit));
                list.add(future);
            }
            result.put(e.getKey(), list);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void executeExperiment(Experiment<S,I> experiment, List<String> instanceNames, ExceptionHandler<S, I> exceptionHandler, long startTimestamp) {
        var algorithms = experiment.algorithms();
        var experimentName = experiment.name();
        var workUnits = getOrderedWorkUnits(experiment, instanceNames, exceptionHandler, solverConfig.getRepetitions());

        // Launch execution of all work units in parallel
        var futures = submitAll(workUnits);

        // Simulate sequential execution to trigger all events in correct order
        // K: Instance name --> V: List of WorkUnits
        for(var e: futures.entrySet()){
            var instanceName = e.getKey();
            long instanceStartTime = System.nanoTime();
            var referenceValue = getOptionalReferenceValue(this.referenceResultProviders, instanceName);
            EventPublisher.getInstance().publishEvent(new InstanceProcessingStartedEvent(experimentName, instanceName, algorithms, solverConfig.getRepetitions(), referenceValue));
            logger.info("Running algorithms for instance: " + instanceName);

            for(var f: e.getValue()){
                var workUnitResult = ConcurrencyUtil.await(f);
                this.processWorkUnitResult(workUnitResult);
            }

            long totalInstanceTime = System.nanoTime() - instanceStartTime;
            EventPublisher.getInstance().publishEvent(new InstanceProcessingEndedEvent(experimentName, instanceName, totalInstanceTime, startTimestamp));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        logger.info("Shutdown executor");
        this.executor.shutdown();
    }
}
