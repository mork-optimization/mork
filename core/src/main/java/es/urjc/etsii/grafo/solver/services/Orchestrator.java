package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.ReflectiveSolutionBuilder;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.executors.Executor;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.*;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.util.BenchmarkUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@ConditionalOnExpression(value = "!${irace.enabled}")
public class Orchestrator<S extends Solution<S,I>, I extends Instance> extends AbstractOrchestrator {

    private static final Logger log = Logger.getLogger(Orchestrator.class.toString());

    private final IOManager<S,I> io;
    private final ExperimentManager<S, I> experimentManager;
    private final ExceptionHandler<S, I> exceptionHandler;
    private final List<ReferenceResultProvider> referenceResultProviders;
    private final Executor<S, I> executor;
    private final SolverConfig solverConfig;

    public Orchestrator(
            SolverConfig solverConfig,
            IOManager<S,I> io,
            ExperimentManager<S,I> experimentManager,
            List<ExceptionHandler<S,I>> exceptionHandlers,
            Executor<S,I> executor,
            List<SolutionBuilder<S,I>> solutionBuilders,
            List<ReferenceResultProvider> referenceResultProvider
    ) {
        this.solverConfig = solverConfig;
        this.io = io;
        this.experimentManager = experimentManager;
        this.exceptionHandler = decideImplementation(exceptionHandlers, DefaultExceptionHandler.class);
        this.referenceResultProviders = referenceResultProvider;
        this.executor = executor;
    }

    private void runBenchmark(){
        if(solverConfig.isBenchmark()){
            log.info("Running CPU benchmark...");
            double score = BenchmarkUtil.getBenchmarkScore();
            log.info("Benchmark score: " + score);
        } else {
            log.info("Skipping CPU benchmark");
        }
    }

    @Override
    public void run(String... args) {
        runBenchmark();
        log.info("App started, ready to start solving!");
        var experiments = this.experimentManager.getExperiments();
        log.info("Experiments to execute: " + experiments.keySet());
        EventPublisher.publishEvent(new ExecutionStartedEvent(new ArrayList<>(experiments.keySet())));
        long startTime = System.nanoTime();
        try{
            experiments.forEach(this::runExperiment);
        } finally {
            executor.shutdown();
            long totalExecutionTime = System.nanoTime() - startTime;
            EventPublisher.publishEvent(new ExecutionEndedEvent(totalExecutionTime));
            log.info(String.format("Total execution time: %s (s)", totalExecutionTime / 1_000_000_000));
        }
    }

    private void runExperiment(String experimentName, List<Algorithm<S,I>> algorithms) {
        long startTime = System.nanoTime();
        log.info("Running experiment: " + experimentName);
        // TODO review this, instances are loaded twice, needed to validate them all before executing but careful.
        var instanceNames = io.getInstances(experimentName).map(Instance::getName).collect(Collectors.toList());
        EventPublisher.publishEvent(new ExperimentStartedEvent(experimentName, instanceNames));
        io.getInstances(experimentName).forEach(instance -> processInstance(experimentName, algorithms, instance));
        long experimenExecutionTime = System.nanoTime() - startTime;
        EventPublisher.publishEvent(new ExperimentEndedEvent(experimentName, experimenExecutionTime));
        log.info("Finished running experiment: " + experimentName);
    }

    private void processInstance(String experimentName, List<Algorithm<S, I>> algorithms, Instance instance) {
        long startTime = System.nanoTime();
        var referenceValue = getOptionalReferenceValue(this.referenceResultProviders, instance);
        EventPublisher.publishEvent(new InstanceProcessingStartedEvent(experimentName, instance.getName(), algorithms, solverConfig.getRepetitions(), referenceValue));
        log.info("Running algorithms for instance: " + instance.getName());
        executor.execute(experimentName, (I) instance, solverConfig.getRepetitions(), algorithms, exceptionHandler);
        long totalTime = System.nanoTime() - startTime;
        EventPublisher.publishEvent(new InstanceProcessingEndedEvent(experimentName, instance.getName(), totalTime));
    }

    private Optional<Double> getOptionalReferenceValue(List<ReferenceResultProvider> provider, Instance instance){
        double best = this.solverConfig.isMaximizing()? Double.MIN_VALUE: Double.MAX_VALUE;
        for(var r: referenceResultProviders){
            double score = r.getValueFor(instance.getName()).getScoreOrNan();
            // Ignore if not valid value
            if (Double.isFinite(score)) {
                if(this.solverConfig.isMaximizing()){
                    best = Math.max(best, score);
                } else {
                    best = Math.min(best, score);
                }
            }
        }
        if(best == Double.MAX_VALUE || best == Double.MIN_VALUE){
            return Optional.empty();
        } else {
            return Optional.of(best);
        }
    }
    
}
