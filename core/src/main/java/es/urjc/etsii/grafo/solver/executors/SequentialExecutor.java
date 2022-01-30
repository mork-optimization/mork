package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.experiment.Experiment;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.services.IOManager;
import es.urjc.etsii.grafo.solver.services.InstanceManager;
import es.urjc.etsii.grafo.solver.services.SolutionValidator;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.AlgorithmProcessingEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.AlgorithmProcessingStartedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.InstanceProcessingStartedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.*;

/**
 * Processes work units sequentially
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@ConditionalOnExpression(value = "!${solver.parallelExecutor} && !${irace.enabled}")
public class SequentialExecutor<S extends Solution<S,I>, I extends Instance> extends Executor<S,I>{

    private static final Logger logger = LoggerFactory.getLogger(SequentialExecutor.class);

    /**
     * Create new sequential executor
     *
     * @param validator solution validator if present
     * @param io IO manager
     * @param instanceManager Instance Manager
     */
    public SequentialExecutor(
            Optional<SolutionValidator<S, I>> validator,
            IOManager<S, I> io,
            InstanceManager<I> instanceManager,
            List<ReferenceResultProvider> referenceResultProviders,
            SolverConfig solverConfig
    ) {
        super(validator, io, instanceManager, referenceResultProviders, solverConfig);
    }

    /** {@inheritDoc} */
    @Override
    public void executeExperiment(Experiment<S,I> experiment, List<String> instanceNames, ExceptionHandler<S, I> exceptionHandler, long startTimestamp) {
        var events = EventPublisher.getInstance();

        var algorithms = experiment.algorithms();
        var experimentName = experiment.name();
        var workUnits = getOrderedWorkUnits(experiment, instanceNames, exceptionHandler, solverConfig.getRepetitions());

        // K: Instance name --> V: List of WorkUnits
        for(var instanceWork: workUnits.entrySet()){
            var instanceName = instanceWork.getKey();
            long instanceStartTime = System.nanoTime();
            var referenceValue = getOptionalReferenceValue(this.referenceResultProviders, instanceName);
            events.publishEvent(new InstanceProcessingStartedEvent(experimentName, instanceName, algorithms, solverConfig.getRepetitions(), referenceValue));
            logger.info("Running algorithms for instance: {}", instanceName);

            for(var algorithmWork: instanceWork.getValue().entrySet()){
                var algorithm = algorithmWork.getKey();
                events.publishEvent(new AlgorithmProcessingStartedEvent<>(experimentName, instanceName, algorithm, solverConfig.getRepetitions()));
                logger.info("Running algorithm {} for instance {}", algorithm.getShortName(), instanceName);
                for(var workUnit: algorithmWork.getValue()){
                    var workUnitResult = doWork(workUnit);
                    this.processWorkUnitResult(workUnitResult);
                }
                events.publishEvent(new AlgorithmProcessingEndedEvent<>(experimentName, instanceName, algorithm, solverConfig.getRepetitions()));
            }

            long totalInstanceTime = System.nanoTime() - instanceStartTime;
            events.publishEvent(new InstanceProcessingEndedEvent(experimentName, instanceName, totalInstanceTime, startTimestamp));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        logger.info("Shutdown executor");
    }
}
