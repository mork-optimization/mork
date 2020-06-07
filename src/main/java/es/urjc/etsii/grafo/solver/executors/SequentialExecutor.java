package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.Result;
import es.urjc.etsii.grafo.io.WorkingOnResult;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;
import es.urjc.etsii.grafo.util.RandomManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class SequentialExecutor<S extends Solution<I>, I extends Instance> extends Executor<S,I>{

    private static final Logger logger = Logger.getLogger(SequentialExecutor.class.getName());

    @Override
    public Collection<Result> execute(I ins, int repetitions, List<Algorithm<S,I>> list, SolutionBuilder<S,I> solutionBuilder, ExceptionHandler<S,I> exceptionHandler) {
        List<Result> results = new ArrayList<>();

        for(var algorithm: list){
            try {
                logger.info("Algorithm: "+ algorithm);
                var workingOnResult = new WorkingOnResult(repetitions, algorithm.getShortName(), ins.getName());
                for (int i = 0; i < repetitions; i++) {
                    RandomManager.reset(i);
                    long starTime = System.nanoTime();
                    var solution = algorithm.algorithm(ins, solutionBuilder);
                    long endTime = System.nanoTime();
                    long timeToTarget = solution.getLastModifiedTime() - starTime;
                    workingOnResult.addSolution(solution, endTime - starTime, timeToTarget);
                }
                results.add(workingOnResult.finish());
            } catch (Exception e){
                exceptionHandler.handleException(e, ins, algorithm);
            }
        }
        //logger.info("Tasks submited, awaiting termination for instance: "+ins.getName());
        return results;
    }
}
