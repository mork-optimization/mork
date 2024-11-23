package es.urjc.etsii.grafo.mo.pareto;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMoveWithMultipleObjectives;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ParetoFrontTest {
    private final TestInstance instance = new TestInstance("testInstance");

    public void setMaxTrackedSolutions(int max) {
        Context.Pareto.MAX_TRACKED_SOLS = max;
    }

    public static Stream<Arguments> paretoSetsImpls(int nObjectives){
        Objective<TestMoveWithMultipleObjectives, TestSolution, TestInstance>[] objectives = new Objective[nObjectives];

        for (int i = 0; i < nObjectives; i++) {
            int finalI = i;
            objectives[i] = Objective.of("obj" + i, FMode.MINIMIZE, s -> s.getScore(finalI), m -> m.getScoreChanges()[finalI]);
        }

        Context.Configurator.setObjectives(true, objectives);
        return Stream.of(
                Arguments.of(new ParetoSimpleList<>(nObjectives)),
                Arguments.of(new NDTree<>(nObjectives))
        );
    }

    public static Stream<Arguments> paretoSetsImpls(){
        return paretoSetsImpls(2);
    }


    @ParameterizedTest
    @MethodSource("paretoSetsImpls")
    public void testSimple(ParetoSet<TestSolution, TestInstance> paretoSet) {
        assertTrue(paretoSet.add(new TestSolution(instance, new double[]{0.0, 0.0})));
        assertTrue(paretoSet.add(new TestSolution(instance, new double[]{-1.0, -1.0})));
        assertEquals(1, paretoSet.size());
    }

    @ParameterizedTest
    @MethodSource("paretoSetsImpls")
    public void testTwo(ParetoSet<TestSolution, TestInstance> paretoSet) {
        assertTrue(paretoSet.add(new TestSolution(instance, new double[]{0.0, 0.0})));
        assertFalse(paretoSet.add(new TestSolution(instance, new double[]{0.0, 0.0})));
        assertFalse(paretoSet.add(new TestSolution(instance, new double[]{1.0, 0.0})));
        assertFalse(paretoSet.add(new TestSolution(instance, new double[]{0.0, 1.0})));
        assertEquals(1, paretoSet.size());

        assertTrue(paretoSet.add(new TestSolution(instance, new double[]{0.0, -1.0})));
        assertFalse(paretoSet.add(new TestSolution(instance, new double[]{0.0, -1.0})));
        assertEquals(1, paretoSet.size());

        assertTrue(paretoSet.add(new TestSolution(instance, new double[]{-1.0, 0.0})));
        assertFalse(paretoSet.add(new TestSolution(instance, new double[]{-1.0, 0.0})));
        assertEquals(2, paretoSet.size());

        // Check that solutions are correctly removed by the front
        assertTrue(paretoSet.add(new TestSolution(instance, new double[]{-1.0, -1.0})));
        assertFalse(paretoSet.add(new TestSolution(instance, new double[]{-1.0, -1.0})));
        assertEquals(1, paretoSet.size());
    }

    @ParameterizedTest
    @MethodSource("paretoSetsImpls")
    public void testAddingNonDominatedSolutions(ParetoSet<TestSolution, TestInstance> paretoSet) {
        int additions = 20;
        for (int i = 0; i < additions; i++) {
            assertTrue(paretoSet.add(new TestSolution(instance, new double[]{0.0 - i, 0.0 + i})));
        }

        assertEquals(additions, paretoSet.size());
        assertTrue(paretoSet.add(new TestSolution(instance, new double[]{0.0 - additions, 0.0 - additions})));
        assertEquals(1, paretoSet.size());
    }

    @ParameterizedTest
    @MethodSource("paretoSetsImpls")
    public void testGetTrackedSolutions(ParetoSet<TestSolution, TestInstance> paretoSet) {
        int additions = 20;
        setMaxTrackedSolutions(additions);
        Set<TestSolution> solutions = HashSet.newHashSet(additions);

        for (int i = 0; i < additions; i++) {
            TestSolution sol = new TestSolution(instance, new double[]{0.0 - i, 0.0 + i});
            solutions.add(sol);
            assertTrue(paretoSet.add(sol));
        }

        for (TestSolution trackedSolution : paretoSet.getTrackedSolutions()) {
            assertTrue(solutions.contains(trackedSolution));
            solutions.remove(trackedSolution);
        }

        assertTrue(solutions.isEmpty());

        TestSolution sol = new TestSolution(instance, new double[]{0.0 - additions, 0.0 + additions});
        assertTrue(paretoSet.add(sol));

        for (TestSolution trackedSolution : paretoSet.getTrackedSolutions()) {
            Assertions.assertNotEquals(sol, trackedSolution);
        }

        assertEquals(additions + 1, paretoSet.size());

        sol = new TestSolution(instance, new double[]{Integer.MIN_VALUE, Integer.MIN_VALUE});
        assertTrue(paretoSet.add(sol));
        assertEquals(1, paretoSet.size());
        for (TestSolution trackedSolution : paretoSet.getTrackedSolutions()) {
            assertEquals(sol, trackedSolution);
        }
    }

    Comparator<double[]> comparator = (o1, o2) -> {
        for (int i = 0; i < o1.length; i++) {
            int comp = Double.compare(o1[i], o2[i]);
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    };

    @Test
    public void validateBehaviourMatches() {
        ParetoSet<TestSolution, TestInstance>[] sets = paretoSetsImpls(3)
                .map(a -> a.get()[0])
                .toArray(ParetoSet[]::new);
        var r = new Random(0);
        for (int i = 0; i < 10_000; i++) {
            double a = r.nextDouble(), b = r.nextDouble(), c = r.nextDouble();
            var sol = new TestSolution(instance, new double[]{a,b,c});
            boolean[] results = new boolean[sets.length];
            for (int j = 0; j < sets.length; j++) {
                results[j] = sets[j].add(sol);
            }
            for (int j = 0; j < results.length - 1; j++) {
                assertEquals(results[j], results[j + 1]);
                assertEquals(sets[j].size(), sets[j + 1].size());
                var l1 = sets[j].stream().sorted(comparator).toList();
                var l2 = sets[j + 1].stream().sorted(comparator).toList();
                for (int k = 0; k < l1.size(); k++) {
                    var p1 = l1.get(k);
                    var p2 = l2.get(k);
                    Assertions.assertArrayEquals(p1, p2);
                }
            }
        }
        assertEquals(sets[0].size(), sets[1].size());
    }
}
