package es.urjc.etsii.grafo.BMSSC.alg;


import es.urjc.etsii.grafo.BMSSC.model.BMSSCInstance;
import es.urjc.etsii.grafo.BMSSC.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.TimeControl;

import static es.urjc.etsii.grafo.util.DoubleComparator.isLess;

public class MultistartOnlyBestAppliesLS extends Algorithm<BMSSCSolution, BMSSCInstance> {

    private final int iterations;
    private final Constructive<BMSSCSolution, BMSSCInstance> constructor;
    private final Improver<BMSSCSolution, BMSSCInstance> improver;

    @AutoconfigConstructor
    public MultistartOnlyBestAppliesLS(
            @ProvidedParam String name,
            @IntegerParam(min = 1, max = 1_000) int iterations,
            Constructive<BMSSCSolution, BMSSCInstance> constructor,
            Improver<BMSSCSolution, BMSSCInstance> improver
    ) {
        super(name);
        this.iterations = iterations;
        this.constructor = constructor;
        this.improver = improver;
    }

    /**
     * Executes the algorithm for the given instance
     * @param ins Instance the algorithm will process
     * @return Best solution found
     */
    @Override
    public BMSSCSolution algorithm(BMSSCInstance ins) {
        var solution = construct(ins);
        Metrics.addCurrentObjectives(solution);
        for (int i = 0; i < iterations && !TimeControl.isTimeUp(); i++) {
            BMSSCSolution temp = construct(ins);
            if (isLess(temp.getScore(), solution.getScore())){
                solution = temp;
                Metrics.addCurrentObjectives(solution);
            }
        }
        solution = improve(solution);
        return solution;
    }

    BMSSCSolution construct(BMSSCInstance ins) {
        var solution = this.newSolution(ins);
        return this.constructor.construct(solution);
    }

    BMSSCSolution improve(BMSSCSolution s) {
        return improver.improve(s);
    }

    @Override
    public String toString() {
        return "MultistartOnlyBestAppliesLS{" +
                "executions=" + iterations +
                ", constructor=" + constructor +
                ", improver=" + improver +
                '}';
    }
}
