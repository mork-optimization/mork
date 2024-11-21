package es.urjc.etsii.grafo.mo.pareto;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMoveWithMultipleObjectives;
import es.urjc.etsii.grafo.testutil.TestSolutionWithMultipleObjectives;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class NDTreeTest {

    static ParetoSet<TestSolutionWithMultipleObjectives, TestInstance> paretoSet;

    public static void initializeParetoSet(int n){
        paretoSet = new NDTree<>(n);
    }

    public static void setNObjectives(int n){
        Objective<TestMoveWithMultipleObjectives,
                TestSolutionWithMultipleObjectives,
                TestInstance>[] objectives = new Objective[n];

        for (int i = 0; i < n; i++) {
            int finalI = i;
            objectives[i] = Objective.of(
                    "obj" + i,
                    FMode.MINIMIZE,
                    s -> s.getObjective(finalI),
                    m -> m.getScoreChanges()[finalI]
            );
        }

        Context.Configurator.setObjectives(true, objectives);
    }

    public static void setMaxTrackedSolutions(int max){
        Context.Pareto.MAX_TRACKED_SOLS = max;
    }

    @Test
    public void testAddingDominatingSolution(){
        int numberOfObjectives = 2;
        setNObjectives(numberOfObjectives);
        initializeParetoSet(numberOfObjectives);

        TestInstance instance = new TestInstance("testInstance");

        Assertions.assertTrue(
                paretoSet.add(
                        new TestSolutionWithMultipleObjectives(
                                instance,
                                new double[]{0.0, 0.0}
                        )
                )
        );

        Assertions.assertTrue(
                paretoSet.add(
                        new TestSolutionWithMultipleObjectives(
                                instance,
                                new double[]{-1.0, -1.0}
                        )
                )
        );

        Assertions.assertEquals(1, paretoSet.size());
    }

    @Test
    public void testAddingDominatedSolution(){
        int numberOfObjectives = 2;
        setNObjectives(numberOfObjectives);
        initializeParetoSet(numberOfObjectives);

        TestInstance instance = new TestInstance("testInstance");

        Assertions.assertTrue(
                paretoSet.add(
                        new TestSolutionWithMultipleObjectives(
                                instance,
                                new double[]{0.0, 0.0}
                        )
                )
        );

        Assertions.assertFalse(
                paretoSet.add(
                        new TestSolutionWithMultipleObjectives(
                                instance,
                                new double[]{1.0, 0.0}
                        )
                )
        );

        Assertions.assertEquals(1, paretoSet.size());
    }

    @Test
    public void testAddingNonDominatedSolutions(){
        int numberOfObjectives = 2;
        setNObjectives(numberOfObjectives);
        initializeParetoSet(numberOfObjectives);

        TestInstance instance = new TestInstance("testInstance");
        int additions = 20;
        for (int i = 0; i < additions; i++) {
            Assertions.assertTrue(
                    paretoSet.add(
                            new TestSolutionWithMultipleObjectives(
                                    instance,
                                    new double[]{0.0 - i, 0.0 + i}
                            )
                    )
            );
        }

        Assertions.assertEquals(additions, paretoSet.size());

        Assertions.assertTrue(
                paretoSet.add(
                        new TestSolutionWithMultipleObjectives(
                                instance,
                                new double[]{0.0 - additions, 0.0 - additions}
                        )
                )
        );

        Assertions.assertEquals(1, paretoSet.size());
    }

    @Test
    public void testGetTrackedSolutions(){
        int numberOfObjectives = 2;
        setNObjectives(numberOfObjectives);
        initializeParetoSet(numberOfObjectives);

        TestInstance instance = new TestInstance("testInstance");
        int additions = 20;
        setMaxTrackedSolutions(additions);
        Set<TestSolutionWithMultipleObjectives> solutions = HashSet.newHashSet(additions);

        for (int i = 0; i < additions; i++) {
            TestSolutionWithMultipleObjectives sol = new TestSolutionWithMultipleObjectives(
                    instance,
                    new double[]{0.0 - i, 0.0 + i}
            );
            solutions.add(sol);
            Assertions.assertTrue(
                    paretoSet.add(
                            sol
                    )
            );
        }

        for (TestSolutionWithMultipleObjectives trackedSolution : paretoSet.getTrackedSolutions()) {
            Assertions.assertTrue(solutions.contains(trackedSolution));
            solutions.remove(trackedSolution);
        }

        Assertions.assertTrue(solutions.isEmpty());

        TestSolutionWithMultipleObjectives sol = new TestSolutionWithMultipleObjectives(
                instance,
                new double[]{0.0 - additions, 0.0 + additions}
        );
        Assertions.assertTrue(
                paretoSet.add(
                        sol
                )
        );

        for (TestSolutionWithMultipleObjectives trackedSolution : paretoSet.getTrackedSolutions()) {
            Assertions.assertNotEquals(sol, trackedSolution);
        }

        Assertions.assertEquals(additions + 1, paretoSet.size());

        sol = new TestSolutionWithMultipleObjectives(
                instance,
                new double[]{Integer.MIN_VALUE, Integer.MIN_VALUE}
        );
        Assertions.assertTrue(
                paretoSet.add(
                        sol
                )
        );
        Assertions.assertEquals(1, paretoSet.size());
        for (TestSolutionWithMultipleObjectives trackedSolution : paretoSet.getTrackedSolutions()) {
            Assertions.assertEquals(sol, trackedSolution);
        }
    }

}
