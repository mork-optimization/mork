package es.urjc.etsii.grafo.algorithms.cmsa;

import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CMSATest {

    private final TestInstance testInstance = new TestInstance("testinstance");

    @BeforeAll
    public static void init() {
        Metrics.disableMetrics();
        Context.Configurator.setObjectives(Objective.ofMinimizing("Test", TestSolution::getScore, TestMove::getScoreChange));
    }

    @AfterEach
    void cleanup() {
        TimeControl.remove();
    }

    private CMSABuilder<TestSolution, TestInstance> builder() {
        return new CMSABuilder<TestSolution, TestInstance>()
                .withDefaultObjective()
                .withSolutionsPerIteration(1)
                .withAgeMax(5)
                .withSolverTimeLimitInMillis(1000);
    }

    private void withBuilder(CMSA<TestSolution, TestInstance> cmsa) {
        cmsa.setBuilder(new SolutionBuilder<>() {
            @Override
            public TestSolution initializeSolution(TestInstance instance) {
                return new TestSolution(instance);
            }
        });
    }

    @Test
    void testIllegalParameters() {
        @SuppressWarnings("unchecked")
        CMSAConstructive<TestSolution, TestInstance> constructive = mock(CMSAConstructive.class);
        @SuppressWarnings("unchecked")
        CMSASolver<TestSolution, TestInstance> solver = mock(CMSASolver.class);
        Objective<?, TestSolution, TestInstance> objective = Context.getMainObjective();

        Assertions.assertDoesNotThrow(() ->
                new CMSA<>("Test", objective, constructive, solver, 1, 0, 1, 10));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new CMSA<>("Test", objective, constructive, solver, 0, 0, 1, 10));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new CMSA<>("Test", objective, constructive, solver, 1, -1, 1, 10));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new CMSA<>("Test", objective, constructive, solver, 1, 0, 0, 10));
    }

    @Test
    void testBuilderRequiresConstructiveAndSolver() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CMSABuilder<TestSolution, TestInstance>().build());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testStopsAtMaxIterations() {
        var solution = new TestSolution(testInstance, 10);
        CMSAConstructive<TestSolution, TestInstance> constructive = mock(CMSAConstructive.class);
        when(constructive.construct(any(TestSolution.class))).thenReturn(solution);
        when(constructive.usedComponents(any(TestSolution.class))).thenReturn(Set.of("c1"));

        CMSASolver<TestSolution, TestInstance> solver = mock(CMSASolver.class);
        when(solver.solve(eq(testInstance), anySet(), anyLong())).thenReturn(solution);

        int maxIterations = 5;
        var cmsa = builder().withConstructive(constructive).withSolver(solver).withMaxIterations(maxIterations).build("Test");
        withBuilder(cmsa);

        var result = cmsa.algorithm(testInstance);

        Assertions.assertEquals(solution, result);
        verify(solver, times(maxIterations)).solve(eq(testInstance), anySet(), anyLong());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testKeepsBestSolutionFoundAcrossIterations() {
        var worse = new TestSolution(testInstance, 20);
        var best = new TestSolution(testInstance, 5);
        var worseAgain = new TestSolution(testInstance, 15);

        CMSAConstructive<TestSolution, TestInstance> constructive = mock(CMSAConstructive.class);
        when(constructive.construct(any(TestSolution.class))).thenReturn(worse);
        when(constructive.usedComponents(any(TestSolution.class))).thenReturn(Set.of("c1"));

        CMSASolver<TestSolution, TestInstance> solver = mock(CMSASolver.class);
        when(solver.solve(eq(testInstance), anySet(), anyLong())).thenReturn(worse, best, worseAgain);

        var cmsa = builder().withConstructive(constructive).withSolver(solver).withMaxIterations(3).build("Test");
        withBuilder(cmsa);

        var result = cmsa.algorithm(testInstance);

        Assertions.assertEquals(best, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testThrowsWhenSolverNeverFindsAFeasibleSolution() {
        var solution = new TestSolution(testInstance, 10);
        CMSAConstructive<TestSolution, TestInstance> constructive = mock(CMSAConstructive.class);
        when(constructive.construct(any(TestSolution.class))).thenReturn(solution);
        when(constructive.usedComponents(any(TestSolution.class))).thenReturn(Set.of("c1"));

        CMSASolver<TestSolution, TestInstance> solver = mock(CMSASolver.class);
        when(solver.solve(eq(testInstance), anySet(), anyLong())).thenReturn(null);

        var cmsa = builder().withConstructive(constructive).withSolver(solver).withMaxIterations(3).build("Test");
        withBuilder(cmsa);

        Assertions.assertThrows(IllegalStateException.class, () -> cmsa.algorithm(testInstance));
    }

    @Test
    void testStopByMaxTime() {
        var solution = new TestSolution(testInstance, 10);
        @SuppressWarnings("unchecked")
        CMSAConstructive<TestSolution, TestInstance> constructive = mock(CMSAConstructive.class);
        when(constructive.construct(any(TestSolution.class))).thenReturn(solution);
        when(constructive.usedComponents(any(TestSolution.class))).thenReturn(Set.of("c1"));

        @SuppressWarnings("unchecked")
        CMSASolver<TestSolution, TestInstance> solver = mock(CMSASolver.class);
        when(solver.solve(eq(testInstance), anySet(), anyLong())).thenReturn(solution);

        TimeControl.setMaxExecutionTime(10, TimeUnit.MILLISECONDS);
        TimeControl.start();

        var cmsa = builder().withConstructive(constructive).withSolver(solver).withMaxIterations(10_000_000).build("Test");
        withBuilder(cmsa);

        long startTime = System.nanoTime();
        cmsa.algorithm(testInstance);
        long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000;

        Assertions.assertTrue(elapsedMillis < 500, "Does not stop shortly after the time budget is exhausted");
    }

    /**
     * Verifies the "Adapt" step: components that keep being selected by the solver stay in the
     * sub-instance, while components that are consistently ignored age out and get dropped once
     * their age exceeds ageMax, keeping the size of the restricted sub-instance bounded.
     */
    @Test
    void testAdaptDropsStaleComponents() {
        int ageMax = 2;
        int iterations = 8;
        AtomicInteger counter = new AtomicInteger();

        var constructive = new CMSAConstructive<TestSolution, TestInstance>() {
            @Override
            public TestSolution construct(TestSolution solution) {
                return solution;
            }

            @Override
            public Set<Object> usedComponents(TestSolution solution) {
                // Every constructed solution introduces exactly one new, never-repeated component
                return Set.of("component-" + counter.getAndIncrement());
            }
        };

        Deque<Set<Object>> capturedRestrictions = new ArrayDeque<>();
        var solver = new CMSASolver<TestSolution, TestInstance>() {
            @Override
            public TestSolution solve(TestInstance instance, Set<Object> restrictedComponents, long maxDurationInMillis) {
                capturedRestrictions.add(new HashSet<>(restrictedComponents));
                // Never selects any component: everything currently in the sub-instance ages this round
                return new TestSolution(instance, 1);
            }
        };

        var cmsa = builder().withConstructive(constructive).withSolver(solver)
                .withAgeMax(ageMax).withMaxIterations(iterations).build("Test");
        withBuilder(cmsa);

        cmsa.algorithm(testInstance);

        Assertions.assertEquals(iterations, capturedRestrictions.size());
        // Sub-instance size grows until it reaches steady state at ageMax + 1, then stays bounded
        var lastRestriction = capturedRestrictions.getLast();
        Assertions.assertEquals(ageMax + 1, lastRestriction.size(),
                "Sub-instance size should stabilize at ageMax + 1 once components start aging out");
    }
}
