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
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Processes work units sequentially
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@ConditionalOnExpression(value = "!${solver.parallelExecutor} && !${irace.enabled}")
public class SequentialExecutor<S extends Solution<S,I>, I extends Instance> extends Executor<S,I>{

    private static final Logger logger = Logger.getLogger(SequentialExecutor.class.getName());

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
        var algorithms = experiment.algorithms();
        var experimentName = experiment.name();
        for(var instanceName: instanceNames){
            I instance = this.instanceManager.getInstance(instanceName);
            long instanceStartTime = System.nanoTime();
            var referenceValue = getOptionalReferenceValue(this.referenceResultProviders, instance);
            EventPublisher.publishEvent(new InstanceProcessingStartedEvent(experimentName, instance.getName(), algorithms, solverConfig.getRepetitions(), referenceValue));
            logger.info("Running algorithms for instance: " + instance.getName());
            this.execute(experimentName, instance, solverConfig.getRepetitions(), algorithms, exceptionHandler);
            long totalInstanceTime = System.nanoTime() - instanceStartTime;
            EventPublisher.publishEvent(new InstanceProcessingEndedEvent(experimentName, instance.getName(), totalInstanceTime, startTimestamp));
        }
    }

    /**
     * Execute all the available algorithms for the given instance, repeated N times
     *
     * @param ins Instance
     * @param repetitions Number of repetitions
     * @param algorithms Algorithm list
     * @param experimentName Experiment name
     * @param exceptionHandler Exception handler, determines behaviour if anything fails
     */
    public void execute(String experimentName, I ins, int repetitions, List<Algorithm<S,I>> algorithms, ExceptionHandler<S,I> exceptionHandler) {
        for(var algorithm: algorithms){
            logger.info("Algorithm: "+ algorithm.getShortName());
            for (int i = 0; i < repetitions; i++) {
                doWork(experimentName, ins, algorithm, i, exceptionHandler);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        logger.info("Shutdown executor");
    }
}
