package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.Result;
import es.urjc.etsii.grafo.io.WorkingOnResult;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.solver.services.IOManager;
import es.urjc.etsii.grafo.solver.services.SolutionValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class SequentialExecutor<S extends Solution<I>, I extends Instance> extends Executor<S,I>{

    private static final Logger logger = Logger.getLogger(SequentialExecutor.class.getName());

    public SequentialExecutor(Optional<SolutionValidator<S,I>> validator, IOManager<S,I> io) {
        super(validator, io);
    }

    @Override
    public Collection<Result> execute(String experimentname, I ins, int repetitions, List<Algorithm<S,I>> list, SolutionBuilder<S,I> solutionBuilder, ExceptionHandler<S,I> exceptionHandler) {
        List<Result> results = new ArrayList<>();

        for(var algorithm: list){
            logger.info("Algorithm: "+ algorithm);
            var workingOnResult = new WorkingOnResult<>(repetitions, algorithm.toString(), ins.getName());
            for (int i = 0; i < repetitions; i++) {
                WorkUnit workUnit = doWork(experimentname, ins, solutionBuilder, algorithm, i, exceptionHandler);
                if(workUnit!=null) {
                    // TODO no entiendo porque este casting es necesario
                    workingOnResult.addSolution((Solution<Instance>) workUnit.getSolution(), workUnit.getEllapsedTime(), workUnit.getTimeToTarget());
                }
            }
            Optional<Result> finish = workingOnResult.finish();
            finish.ifPresent(results::add);
        }
        //logger.info("Tasks submited, awaiting termination for instance: "+ins.getName());
        return results;
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down executor");
    }
}
