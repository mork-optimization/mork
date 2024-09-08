package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.AlgorithmProcessingEndedEvent;
import es.urjc.etsii.grafo.events.types.AlgorithmProcessingStartedEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingStartedEvent;
import es.urjc.etsii.grafo.exception.ExceptionHandler;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.services.IOManager;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;

/**
 * Processes work units sequentially
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@ConditionalOnExpression(value = "!${solver.parallelExecutor}")
public class SequentialExecutor<S extends Solution<S, I>, I extends Instance> extends Executor<S, I> {

    private static final Logger logger = LoggerFactory.getLogger(SequentialExecutor.class);

    /**
     * Create new sequential executor
     *
     * @param validator       solution validator if present
     * @param io              IO manager
     * @param instanceManager Instance Manager
     */
    public SequentialExecutor(
            Optional<SolutionValidator<S, I>> validator,
            Optional<TimeLimitCalculator<S, I>> timeLimitCalculator,
            IOManager<S, I> io,
            InstanceManager<I> instanceManager,
            List<ReferenceResultProvider> referenceResultProviders,
            SolverConfig solverConfig,
            List<ExceptionHandler<S,I>> exceptionHandlers
    ) {
        super(validator, timeLimitCalculator, io, instanceManager, referenceResultProviders, solverConfig, exceptionHandlers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeExperiment(Experiment<S, I> experiment, List<String> instanceNames, long startTimestamp) {
        var events = EventPublisher.getInstance();

        var algorithms = experiment.algorithms();
        var experimentName = experiment.name();
        var workUnits = getOrderedWorkUnits(experiment, instanceNames, solverConfig.getRepetitions());

        try (var pb = getGlobalSolvingProgressBar(experimentName, workUnits)) {
            // K: Instance name --> V: List of WorkUnits
            for (var instanceWork : workUnits.entrySet()) {
                WorkUnitResult<S, I> instanceBest = null;
                var instancePath = instanceWork.getKey();
                var instanceName = instanceName(instancePath);
                pb.setExtraMessage(instanceName);
                long instanceStartTime = System.nanoTime();
                var referenceValue = getOptionalReferenceValue(instanceName, false);
                events.publishEvent(new InstanceProcessingStartedEvent(experimentName, instanceName, algorithms, solverConfig.getRepetitions(), referenceValue));

                for (var algorithmWork : instanceWork.getValue().entrySet()) {
                    WorkUnitResult<S, I> algorithmBest = null;
                    var algorithm = algorithmWork.getKey();
                    events.publishEvent(new AlgorithmProcessingStartedEvent<>(experimentName, instanceName, algorithm, solverConfig.getRepetitions()));
                    logger.debug("Running algorithm {} for instance {}", algorithm.getName(), instanceName);
                    for (var workUnit : algorithmWork.getValue()) {
                        var workUnitResult = doWork(workUnit);
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

    @Override
    public void startup() {
        // Do nothing, as experiments run in the main thread, just return
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        // Do nothing, as experiments run in the main thread, just return
    }
}
