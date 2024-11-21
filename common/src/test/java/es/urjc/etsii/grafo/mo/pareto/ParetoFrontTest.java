package es.urjc.etsii.grafo.mo.pareto;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMoveWithMultipleObjectives;
import es.urjc.etsii.grafo.testutil.TestSolutionWithMultipleObjectives;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ParetoFrontTest {
    private final TestInstance instance = new TestInstance("testInstance");

    public void setMaxTrackedSolutions(int max) {
        Context.Pareto.MAX_TRACKED_SOLS = max;
    }

    public static Stream<Arguments> paretoSetsImpls(){
        int nObjectives = 2;
        Objective<TestMoveWithMultipleObjectives, TestSolutionWithMultipleObjectives, TestInstance>[] objectives = new Objective[nObjectives];

        for (int i = 0; i < nObjectives; i++) {
            int finalI = i;
            objectives[i] = Objective.of("obj" + i, FMode.MINIMIZE, s -> s.getObjective(finalI), m -> m.getScoreChanges()[finalI]);
        }

        Context.Configurator.setObjectives(true, objectives);
        return Stream.of(
                Arguments.of(new ParetoSimpleList<>(nObjectives)),
                //Arguments.of(new ParetoByFirstObjective<>(nObjectives)), // NOTE: UNTESTED, does not behave like a real Pareto front, allows dominated solutions by design
                Arguments.of(new NDTree<>(nObjectives))
        );
    }


    @ParameterizedTest
    @MethodSource("paretoSetsImpls")
    public void testSimple(ParetoSet<TestSolutionWithMultipleObjectives, TestInstance> paretoSet) {
        assertTrue(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{0.0, 0.0})));
        assertTrue(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{-1.0, -1.0})));
        assertEquals(1, paretoSet.size());
    }

    @ParameterizedTest
    @MethodSource("paretoSetsImpls")
    public void testTwo(ParetoSet<TestSolutionWithMultipleObjectives, TestInstance> paretoSet) {
        assertTrue(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{0.0, 0.0})));
        assertFalse(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{0.0, 0.0})));
        assertFalse(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{1.0, 0.0})));
        assertFalse(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{0.0, 1.0})));
        assertEquals(1, paretoSet.size());

        assertTrue(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{0.0, -1.0})));
        assertFalse(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{0.0, -1.0})));
        assertEquals(1, paretoSet.size());

        assertTrue(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{-1.0, 0.0})));
        assertFalse(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{-1.0, 0.0})));
        assertEquals(2, paretoSet.size());

        // Check that solutions are correctly removed by the front
        assertTrue(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{-1.0, -1.0})));
        assertFalse(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{-1.0, -1.0})));
        assertEquals(1, paretoSet.size());
    }

    @ParameterizedTest
    @MethodSource("paretoSetsImpls")
    public void testAddingNonDominatedSolutions(ParetoSet<TestSolutionWithMultipleObjectives, TestInstance> paretoSet) {
        int additions = 20;
        for (int i = 0; i < additions; i++) {
            assertTrue(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{0.0 - i, 0.0 + i})));
        }

        assertEquals(additions, paretoSet.size());
        assertTrue(paretoSet.add(new TestSolutionWithMultipleObjectives(instance, new double[]{0.0 - additions, 0.0 - additions})));
        assertEquals(1, paretoSet.size());
    }

    @ParameterizedTest
    @MethodSource("paretoSetsImpls")
    public void testGetTrackedSolutions(ParetoSet<TestSolutionWithMultipleObjectives, TestInstance> paretoSet) {
        int additions = 20;
        setMaxTrackedSolutions(additions);
        Set<TestSolutionWithMultipleObjectives> solutions = HashSet.newHashSet(additions);

        for (int i = 0; i < additions; i++) {
            TestSolutionWithMultipleObjectives sol = new TestSolutionWithMultipleObjectives(instance, new double[]{0.0 - i, 0.0 + i});
            solutions.add(sol);
            assertTrue(paretoSet.add(sol));
        }

        for (TestSolutionWithMultipleObjectives trackedSolution : paretoSet.getTrackedSolutions()) {
            assertTrue(solutions.contains(trackedSolution));
            solutions.remove(trackedSolution);
        }

        assertTrue(solutions.isEmpty());

        TestSolutionWithMultipleObjectives sol = new TestSolutionWithMultipleObjectives(instance, new double[]{0.0 - additions, 0.0 + additions});
        assertTrue(paretoSet.add(sol));

        for (TestSolutionWithMultipleObjectives trackedSolution : paretoSet.getTrackedSolutions()) {
            Assertions.assertNotEquals(sol, trackedSolution);
        }

        assertEquals(additions + 1, paretoSet.size());

        sol = new TestSolutionWithMultipleObjectives(instance, new double[]{Integer.MIN_VALUE, Integer.MIN_VALUE});
        assertTrue(paretoSet.add(sol));
        assertEquals(1, paretoSet.size());
        for (TestSolutionWithMultipleObjectives trackedSolution : paretoSet.getTrackedSolutions()) {
            assertEquals(sol, trackedSolution);
        }
    }
}
