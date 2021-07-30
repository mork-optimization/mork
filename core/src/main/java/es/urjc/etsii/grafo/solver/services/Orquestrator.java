package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.ReflectiveSolutionBuilder;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.executors.ConcurrentExecutor;
import es.urjc.etsii.grafo.solver.executors.Executor;
import es.urjc.etsii.grafo.solver.executors.SequentialExecutor;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.types.*;
import es.urjc.etsii.grafo.util.BenchmarkUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class Orquestrator<S extends Solution<I>, I extends Instance> implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final boolean doBenchmark;
    private final IOManager<S,I> io;
    private final ExperimentManager<S, I> experimentManager;
    private final ExceptionHandler<S, I> exceptionHandler;
    private final SolutionBuilder<S, I> solutionBuilder;
    private final Optional<ReferenceResultProvider> referenceResultProvider;
    private final Executor<S, I> executor;
    private final int repetitions;

    public Orquestrator(
            @Value("${solver.repetitions:1}") int repetitions,
            @Value("${solver.parallelExecutor:false}") boolean useParallelExecutor,
            @Value("${solver.benchmark:false}") boolean doBenchmark,
            IOManager<S,I> io,
            ExperimentManager<S,I> experimentManager,
            List<ExceptionHandler<S,I>> exceptionHandlers,
            ConcurrentExecutor<S,I> concurrentExecutor,
            SequentialExecutor<S,I> sequentialExecutor,
            List<SolutionBuilder<S,I>> solutionBuilders,
            Optional<ReferenceResultProvider> referenceResultProvider
    ) {
        this.repetitions = repetitions;
        this.doBenchmark = doBenchmark;
        this.io = io;
        this.experimentManager = experimentManager;
        this.exceptionHandler = decideImplementation(exceptionHandlers, DefaultExceptionHandler.class);
        this.solutionBuilder = decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
        this.referenceResultProvider = referenceResultProvider;
        log.info("Using SolutionBuilder implementation: "+this.solutionBuilder.getClass().getSimpleName());

        if(useParallelExecutor){
            executor = concurrentExecutor;
        } else {
            executor = sequentialExecutor;
        }
    }

    private void runBenchmark(){
        if(doBenchmark){
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
        var instanceNames = io.getInstances(experimentName).map(Instance::getName).collect(Collectors.toList());
        EventPublisher.publishEvent(new ExperimentStartedEvent(experimentName, instanceNames));
        io.getInstances(experimentName).forEach(instance -> processInstance(experimentName, algorithms, instance));
        long experimenExecutionTime = System.nanoTime() - startTime;
        EventPublisher.publishEvent(new ExperimentEndedEvent(experimentName, experimenExecutionTime));
        log.info("Finished running experiment: " + experimentName);
    }

    private void processInstance(String experimentName, List<Algorithm<S, I>> algorithms, Instance instance) {
        long startTime = System.nanoTime();
        Optional<Double> referenceValue = this.referenceResultProvider.isPresent()? this.referenceResultProvider.get().getValueFor(instance.getName()):Optional.empty();
        EventPublisher.publishEvent(new InstanceProcessingStartedEvent(experimentName, instance.getName(), algorithms, repetitions, referenceValue));
        log.info("Running algorithms for instance: " + instance.getName());
        executor.execute(experimentName, (I) instance, repetitions, algorithms, solutionBuilder, exceptionHandler);
        long totalTime = System.nanoTime() - startTime;
        EventPublisher.publishEvent(new InstanceProcessingEndedEvent(experimentName, instance.getName(), totalTime));
    }


    public static <T> T decideImplementation(List<? extends T> list, Class<? extends T> defaultClass){
        //String qualifiedDefaultname = defaultClass.getName();
        T defaultImpl = null;
        for(var e: list){
            if(!e.getClass().equals(defaultClass)){
                return e;
            } else {
                defaultImpl = e;
            }
        }
        if(defaultImpl == null) throw new IllegalStateException("Where is the default implementation???");
        return defaultImpl;
    }
}
