package es.urjc.etsii.grafo.tsptw.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

public class TSPTWTimeLimit extends TimeLimitCalculator<TSPTWSolution, TSPTWInstance> {
    @Override
    public long timeLimitInMillis(TSPTWInstance instance, Algorithm<TSPTWSolution, TSPTWInstance> algorithm) {
        return 10_000;
    }
}
