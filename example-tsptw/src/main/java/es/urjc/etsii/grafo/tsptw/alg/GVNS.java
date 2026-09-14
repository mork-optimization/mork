package es.urjc.etsii.grafo.tsptw.alg;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.aop.TimeStats;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWFeasibleConstructive;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWVND;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWFeasibleInsertShake;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.Objects;

public class GVNS extends Algorithm<TSPTWSolution, TSPTWInstance> {
    private final int levelMax;
    private final int attemptsPerLevel;
    private final TSPTWFeasibleConstructive constructive;
    private final TSPTWFeasibleInsertShake shake;
    private final TSPTWVND localSearch;

    @AutoconfigConstructor
    public GVNS(@ProvidedParam String algorithmName,
                @IntegerParam(min = 2, max = 16) int levelMax,
                @IntegerParam(min = 1, max = 100) int attemptsPerLevel,
                TSPTWFeasibleConstructive constructive, TSPTWFeasibleInsertShake shake, TSPTWVND localSearch) {
        super(algorithmName);
        if (levelMax < 2 || attemptsPerLevel < 1) {
            throw new IllegalArgumentException("GVNS requires levelMax >= 2 and attemptsPerLevel >= 1");
        }
        this.levelMax = levelMax;
        this.attemptsPerLevel = attemptsPerLevel;
        this.constructive = Objects.requireNonNull(constructive);
        this.shake = Objects.requireNonNull(shake);
        this.localSearch = Objects.requireNonNull(localSearch);
    }

    /** Original GVNS: levels 1 through 7, advancing after 31 unsuccessful attempts. */
    public GVNS() {
        this("GVNS", 8, 31, new TSPTWFeasibleConstructive(), new TSPTWFeasibleInsertShake(), new TSPTWVND());
    }

    @Override
    public TSPTWSolution algorithm(TSPTWInstance instance) {
        TSPTWUtil.requireMinimumSize(instance);
        if (!instance.isSymmetric()) {
            throw new IllegalArgumentException("GVNS requires a symmetric distance matrix");
        }
        if (!TimeControl.isEnabled()) {
            throw new IllegalStateException("GVNS requires a time budget from a Mork TimeLimitCalculator");
        }
        TSPTWSolution best = null;
        do {
            var solution = constructive.construct(new TSPTWSolution(instance));
            if (solution.constraint_violations() == 0) {
                Metrics.addCurrentObjectives(solution);
                refine(solution);
            }
            if (best == null || solution.better_than(best)) {
                best = solution.cloneSolution();
            }
        } while (!TimeControl.isTimeUp());
        return best;
    }

    @TimeStats
    private void refine(TSPTWSolution solution) {
        solution.assert_solution();
        int level = 1;
        int attempts = 0;
        var candidate = solution.cloneSolution();
        while (level < levelMax && !TimeControl.isTimeUp()) {
            candidate = shake.shake(candidate, level);
            candidate = localSearch.improve(candidate);
            if (candidate.cost() < solution.cost()) {
                solution.copy_from(candidate);
                solution.notifyUpdate();
                Metrics.addCurrentObjectives(solution);
                level = 1;
                attempts = 0;
            } else {
                candidate.copy_from(solution);
                attempts++;
                if (attempts >= attemptsPerLevel) {
                    level++;
                    attempts = 0;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "GVNS{levelMax=" + levelMax + ", attemptsPerLevel=" + attemptsPerLevel
                + ", constructive=" + constructive + ", shake=" + shake + ", localSearch=" + localSearch + "}";
    }
}
