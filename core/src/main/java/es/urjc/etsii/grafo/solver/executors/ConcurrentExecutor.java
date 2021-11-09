package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.services.IOManager;
import es.urjc.etsii.grafo.solver.services.SolutionValidator;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@ConditionalOnExpression(value = "${solver.parallelExecutor} && !${irace.enabled}")
public class ConcurrentExecutor<S extends Solution<S,I>, I extends Instance> extends Executor<S, I> {

    private static final Logger logger = Logger.getLogger(ConcurrentExecutor.class.getName());

    private final int nWorkers;
    private final ExecutorService executor;

    public ConcurrentExecutor(SolverConfig solverConfig, Optional<SolutionValidator<S, I>> validator, IOManager<S, I> io) {
        super(validator, io);
        if (solverConfig.getnWorkers() == -1) {
            this.nWorkers = Runtime.getRuntime().availableProcessors() / 2;
        } else {
            this.nWorkers = solverConfig.getnWorkers();
        }
        this.executor = Executors.newFixedThreadPool(this.nWorkers);
    }

    @Override
    public void execute(String experimentName, I ins, int repetitions, List<Algorithm<S, I>> list, SolutionBuilder<S, I> solutionBuilder, ExceptionHandler<S, I> exceptionHandler) {

        logger.info("Starting solve of instance: " + ins.getName());
        for (var algorithm : list) {
            var futures = new ArrayList<Future<Object>>();
            for (int i = 0; i < repetitions; i++) {
                int _i = i;
                futures.add(executor.submit(() -> {
                    // Run algorithm
                    doWork(experimentName, ins, solutionBuilder, algorithm, _i, exceptionHandler);
                    return null;
                }));
            }
            logger.info(String.format("Enqueued instance %s, algorithm %s ", ins.getName(), algorithm.getShortName()));
            ConcurrencyUtil.awaitAll(futures);
        }
        logger.info("Done processing instance: " + ins.getName());
    }

    @Override
    public void shutdown() {
        logger.info("Shutdown executor");
        this.executor.shutdown();
    }
}
