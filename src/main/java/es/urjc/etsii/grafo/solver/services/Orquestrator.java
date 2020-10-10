package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.Result;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.executors.ConcurrentExecutor;
import es.urjc.etsii.grafo.solver.executors.Executor;
import es.urjc.etsii.grafo.solver.executors.SequentialExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class Orquestrator<S extends Solution<I>, I extends Instance> implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final IOManager io;
    private final AlgorithmsManager<S, I> algorithmsManager;
    private final ExceptionHandler<S, I> exceptionHandler;
    private final SolutionBuilder<S, I> solutionBuilder;
    private final Executor<S, I> executor;
    private final int repetitions;

    public Orquestrator(
            @Value("${solver.repetitions:1}") int repetitions,
            @Value("${solver.parallelExecutor:false}") boolean useParallelExecutor,
            IOManager<S,I> io,
            AlgorithmsManager<S,I> algorithmsManager,
            ExceptionHandler<S,I> exceptionHandler,
            ConcurrentExecutor<S,I> concurrentExecutor,
            SequentialExecutor<S,I> sequentialExecutor,
            List<SolutionBuilder<S,I>> solutionBuilders
    ) {
        this.repetitions = repetitions;
        this.io = io;
        this.algorithmsManager = algorithmsManager;
        this.exceptionHandler = exceptionHandler;
        this.solutionBuilder = decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
        log.info("Using SolutionBuilder implementation: "+this.solutionBuilder.getClass().getSimpleName());

        if(useParallelExecutor){
            executor = concurrentExecutor;
        } else {
            executor = sequentialExecutor;
        }
    }

    // TODO TimeProvider starts a new Thread that changes algorithms stopping conditions
    // DANGER time should only start counting when the algorithm starts executing not when it is enqueued

    @Override
    public void run(String... args) {
        log.info("App started, calculating workload...");
        log.info("Available algorithms: " + this.algorithmsManager.getAlgorithms());
        log.info("Ready to start solving!");
        long startTime = System.nanoTime();
        try{
            List<Result> results = Collections.synchronizedList(new ArrayList<>());
            io.getInstances().forEach(instance -> runAlgorithmsForInstance(results, (I) instance));
            // TODO Update results file in disk after each instance is solved, not in the end
            // Resume functionality: Define work units, each workunit reurns what
            log.info("Saving all results...");
            this.io.saveResults(results);
        } finally {
            executor.shutdown();
            log.info(String.format("Total execution time: %s (s)", (System.nanoTime() - startTime) / 1_000_000_000));
        }
    }


    public void runAlgorithmsForInstance(List<Result> results, I i){
        log.info("Running algorithms for instance: " + i.getName());
        var result = executor.execute(i, repetitions, algorithmsManager.getAlgorithms(), solutionBuilder, exceptionHandler);
        results.addAll(result);
    }

    public static <T> T decideImplementation(List<? extends T> list, Class<? extends T> defaultClass){
        //String qualifiedDefaultname = defaultClass.getName();
        for(var e: list){
            if(!e.getClass().equals(defaultClass)){
                return e;
            }
        }
        throw new IllegalStateException("Where is the default implementation???");
    }
}
