package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;

public class TimedAlgorithm extends Algorithm<TestSolution, TestInstance> {

    private static void sleep(int n){
        try {
            Thread.sleep(n);
        } catch (InterruptedException ignored) {}
    }

    final TestTimeConstructive constructive;
    final TestLocalSearch ls;
    final int mySleep;

    protected TimedAlgorithm(int mySleep, int constSleep, int lsSleep) {
        super("TimedAlgorithm");
        this.mySleep = mySleep;
        this.constructive = new TestTimeConstructive(constSleep);
        this.ls = new TestLocalSearch(constSleep);
    }

    // this method should be timed by the framework
    @Override
    public TestSolution algorithm(TestInstance instance) {
        var sol = new TestSolution(instance);
        looseSomeTime();
        sol = constructive.construct(sol);
        sol = ls.improve(sol);
        return sol;
    }

    // this method should not be timed by the framework
    void looseSomeTime(){
        sleep(mySleep);
    }

    public static class TestTimeConstructive extends Constructive<TestSolution, TestInstance> {
        private final int sleep;

        public TestTimeConstructive(int sleep) {
            this.sleep = sleep;
        }


        @Override
        public TestSolution construct(TestSolution solution) {
            sleep(sleep);
            return solution;
        }
    }

    public static class TestLocalSearch extends Improver<TestSolution, TestInstance> {
        final int sleep;

        public TestLocalSearch(int sleep) {
            super(Objective.ofMinimizing("Test", TestSolution::getScore, TestMove::getScoreChange));
            this.sleep = sleep;
        }

        // this method is timed by the framework
        @Override
        public TestSolution improve(TestSolution solution) {
            work1();
            work2();
            return solution;
        }

        @TimeStats // this method is timed
        protected void work1(){
            sleep(sleep);
        }

        // this method is not
        protected void work2(){
            sleep(sleep);
        }
    }
}
