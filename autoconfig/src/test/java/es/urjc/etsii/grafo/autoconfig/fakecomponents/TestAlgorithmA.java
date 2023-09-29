package es.urjc.etsii.grafo.autoconfig.fakecomponents;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.StringUtil;

public class TestAlgorithmA extends Algorithm<TestSolution, TestInstance> {

    private final int tetha;
    private final double alpha;

    @Override
    public TestSolution algorithm(TestInstance instance) {
        throw new UnsupportedOperationException();
    }


    public TestAlgorithmA() {
        this(0, 0);
    }

    public TestAlgorithmA(double alpha) {
        this(0, alpha);
    }

    @AutoconfigConstructor
    public TestAlgorithmA(@IntegerParam(min = -5, max = 5) int tetha, @RealParam(min = 0, max = 1) double alpha) {
        super(StringUtil.randomAlgorithmName());
        this.tetha = tetha;
        this.alpha = alpha;
    }

    public TestAlgorithmA(int tetha, String alpha) {
        this(tetha, Double.parseDouble(alpha));
    }

    public TestAlgorithmA(int tetha) {
        this(tetha, 0);
    }

    @Override
    public String toString() {
        return "AlgorithmA{" +
                "alpha=" + alpha +
                '}';
    }

}
