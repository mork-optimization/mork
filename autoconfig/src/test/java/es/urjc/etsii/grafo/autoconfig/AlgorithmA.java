package es.urjc.etsii.grafo.autoconfig;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;

public class AlgorithmA extends Algorithm<TestSolution, TestInstance> {

    private final int tetha;
    private final double alpha;

    @Override
    public TestSolution algorithm(TestInstance instance) {
        throw new UnsupportedOperationException();
    }


    public AlgorithmA() {
        this(0, 0);
    }

    public AlgorithmA(double alpha) {
        this(0, alpha);
    }

    public AlgorithmA(int tetha, double alpha) {
        this.tetha = tetha;
        this.alpha = alpha;
    }

    public AlgorithmA(int tetha, String alpha) {
        this(tetha, Double.parseDouble(alpha));
    }

    public AlgorithmA(int tetha) {
        this(tetha, 0);
    }

    @Override
    public String toString() {
        return "AlgorithmA{" +
                "alpha=" + alpha +
                '}';
    }

}
