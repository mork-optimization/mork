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
            IOManager io,
            AlgorithmsManager<S,I> algorithmsManager,
            ExceptionHandler<S,I> exceptionHandler,
            ConcurrentExecutor<S,I> concurrentExecutor,
            SequentialExecutor<S,I> sequentialExecutor,
            SolutionBuilder<S,I> solutionBuilder
    ) {
        this.repetitions = repetitions;
        this.io = io;
        this.algorithmsManager = algorithmsManager;
        this.exceptionHandler = exceptionHandler;
        this.solutionBuilder = solutionBuilder;
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
        log.info("App started, lets rock & roll...");
        log.info("Available algorithms: " + this.algorithmsManager.getAlgorithms());

        List<Result> results = Collections.synchronizedList(new ArrayList<>());
        io.getInstances().forEach(instance -> runAlgorithmsForInstance(results, (I) instance));
        // TODO Update results file in disk after each instance is solved, not in the end
        // Resume functionality: Define work units, each workunit reurns what
        this.io.saveResults(results);
    }


    public void runAlgorithmsForInstance(List<Result> results, I i){
        log.info("Running algorithms for instance: " + i.getName());
        var result = executor.execute(i, repetitions, algorithmsManager.getAlgorithms(), solutionBuilder, exceptionHandler);
        results.addAll(result);
    }
}
