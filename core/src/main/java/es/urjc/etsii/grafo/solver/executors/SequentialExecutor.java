package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.services.IOManager;
import es.urjc.etsii.grafo.solver.services.SolutionValidator;
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
     */
    public SequentialExecutor(Optional<SolutionValidator<S, I>> validator, IOManager<S, I> io) {
        super(validator, io);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(String experimentname, I ins, int repetitions, List<Algorithm<S,I>> list, ExceptionHandler<S,I> exceptionHandler) {

        for(var algorithm: list){
            logger.info("Algorithm: "+ algorithm.getShortName());
            for (int i = 0; i < repetitions; i++) {
                doWork(experimentname, ins, algorithm, i, exceptionHandler);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        logger.info("Shutdown executor");
    }
}
