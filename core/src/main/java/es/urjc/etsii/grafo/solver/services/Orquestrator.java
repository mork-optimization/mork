package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.SimplifiedResult;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.ReflectiveSolutionBuilder;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.executors.ConcurrentExecutor;
import es.urjc.etsii.grafo.solver.executors.Executor;
import es.urjc.etsii.grafo.solver.executors.SequentialExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service
public class Orquestrator<S extends Solution<I>, I extends Instance> implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final IOManager<S,I> io;
    private final ExperimentManager<S, I> experimentManager;
    private final ExceptionHandler<S, I> exceptionHandler;
    private final SolutionBuilder<S, I> solutionBuilder;
    private final Executor<S, I> executor;
    private final int repetitions;

    public Orquestrator(
            @Value("${solver.repetitions:1}") int repetitions,
            @Value("${solver.parallelExecutor:false}") boolean useParallelExecutor,
            IOManager<S,I> io,
            ExperimentManager<S,I> experimentManager,
            List<ExceptionHandler<S,I>> exceptionHandlers,
            ConcurrentExecutor<S,I> concurrentExecutor,
            SequentialExecutor<S,I> sequentialExecutor,
            List<SolutionBuilder<S,I>> solutionBuilders
    ) {
        this.repetitions = repetitions;
        this.io = io;
        this.experimentManager = experimentManager;
        this.exceptionHandler = decideImplementation(exceptionHandlers, DefaultExceptionHandler.class);
        this.solutionBuilder = decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
        log.info("Using SolutionBuilder implementation: "+this.solutionBuilder.getClass().getSimpleName());

        if(useParallelExecutor){
            executor = concurrentExecutor;
        } else {
            executor = sequentialExecutor;
        }
    }

    @Override
    public void run(String... args) {
        log.info("App started, ready to start solving!");
        log.info("Experiments to execute: " + this.experimentManager.getExperiments().keySet());
        long startTime = System.nanoTime();
        try{
            this.experimentManager.getExperiments().forEach(this::runExperiment);
        } finally {
            executor.shutdown();
            log.info(String.format("Total execution time: %s (s)", (System.nanoTime() - startTime) / 1_000_000_000));
        }
    }

    private void runExperiment(String experimentName, List<Algorithm<S,I>> algorithms) {
        log.info("Running experiment: " + experimentName);
        List<SimplifiedResult> results = Collections.synchronizedList(new ArrayList<>());
        io.getInstances(experimentName).forEach(instance -> {
            log.info("Running algorithms for instance: " + instance.getName());
            var result = executor.execute(experimentName, (I) instance, repetitions, algorithms, solutionBuilder, exceptionHandler);
            results.addAll(result);
        });
        // TODO Update results file in disk after each instance is solved, not in the end
        // Resume functionality: Define work units, each workunit reurns what
        log.info("Saving all results...");
        this.io.saveResults(experimentName, results);
        log.info("Finished running experiment: " + experimentName);
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
