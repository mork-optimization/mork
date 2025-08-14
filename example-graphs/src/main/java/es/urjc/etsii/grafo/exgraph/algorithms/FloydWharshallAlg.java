package es.urjc.etsii.grafo.exgraph.algorithms;

import es.urjc.etsii.grafo.exgraph.model.MSTInstance;
import es.urjc.etsii.grafo.exgraph.model.MSTSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;

public class FloydWharshallAlg extends Algorithm<MSTSolution, MSTInstance> {

    static final int BIG_VALUE = 1_000_000;

    public FloydWharshallAlg() {
        super("FloydWharshall");
    }

    @Override
    public MSTSolution algorithm(MSTInstance instance) {
        double[][] d = initializeDistances(instance);
        for(int k=0;k<d.length;k++) {
            for(int i=0;i<d.length;i++) {
                for(int j=0;j<d.length;j++) {
                    d[i][j] = Math.min(d[i][j], d[i][k]+d[k][j]);
                }
            }
        }
        MSTSolution solution = new MSTSolution(instance);
        solution.setScoreDist(d);
        solution.notifyUpdate();
        return solution;
    }

    private static double[][] initializeDistances(MSTInstance instance) {
        double[][] d = new double[instance.v()][instance.v()];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d.length; j++) {
                d[i][j] = BIG_VALUE;
            }
        }
        for (int i = 0; i < d.length; i++) {
            d[i][i] = 0;
        }
        var edges = instance.getEdges();
        for (var e: edges) {
            d[e.from()][e.to()] = e.weight();
            d[e.to()][e.from()] = e.weight();
        }
        return d;
    }
}
