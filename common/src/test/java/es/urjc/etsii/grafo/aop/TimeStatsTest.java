package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.metrics.DeclaredObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.metrics.timing.TimeStatsMethod;
import es.urjc.etsii.grafo.metrics.timing.TimeStatsRecorder;
import es.urjc.etsii.grafo.util.TimeStatsUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

public class TimeStatsTest {

    @BeforeEach
    void setUp() {
        Metrics.enableMetrics();
        Metrics.resetMetrics();
    }

    @AfterEach
    void tearDown() {
        Metrics.disableMetrics();
    }

    @Test
    void annotatedImproverOverloadIsRecorded() {
        var recorder = new CapturingRecorder();
        var solution = new TestSolution(new TestInstance("test"));
        try (var ignored = TimeStatsUtil.bind(recorder)) {
            new TimedAlgorithm.TestLocalSearch(0).improve(solution, 1);
        }
        Assertions.assertEquals(1, recorder.calls.size());
        Assertions.assertTrue(recorder.calls.getFirst().method.signature().contains(".improve("));
    }

    @Test
    void annotatedRuntimeSolutionOverloadIsRecordedOnce() {
        Metrics.disableMetrics();
        var recorder = new CapturingRecorder();
        var solution = new TestSolution(new TestInstance("test"));
        try (var ignored = TimeStatsUtil.bind(recorder)) {
            Assertions.assertSame(solution, new TimedAlgorithm.TestLocalSearch(0).improve((Object) solution));
        }
        Assertions.assertEquals(1, recorder.calls.size());
        Assertions.assertTrue(recorder.calls.getFirst().method.signature().contains(".improve(java.lang.Object)"));
    }

    @Test
    void onlyRequiredShakeSignatureIsRecordedAutomatically() {
        Context.Configurator.setObjectives(Objective.ofMinimizing("DefaultMinimize", TestSolution::getScore, TestMove::getScoreChange));
        var recorder = new CapturingRecorder();
        var solution = new TestSolution(new TestInstance("test"));
        var shake = new TimedAlgorithm.UnannotatedTestShake();
        try (var ignored = TimeStatsUtil.bind(recorder)) {
            Assertions.assertSame(solution, shake.shake(solution, 1));
            Assertions.assertEquals(1, recorder.calls.size());
            Assertions.assertSame(solution, shake.shake(solution));
            Assertions.assertSame(solution, shake.shake(solution, 1L));
            Assertions.assertEquals(1, recorder.calls.size());
        }
        Assertions.assertTrue(recorder.calls.getFirst().method.signature().contains(".shake("));
    }

    @Test
    void annotatedShakeMethodsAreRecordedOnce() {
        Context.Configurator.setObjectives(Objective.ofMinimizing("DefaultMinimize", TestSolution::getScore, TestMove::getScoreChange));
        var recorder = new CapturingRecorder();
        var solution = new TestSolution(new TestInstance("test"));
        var shake = new TimedAlgorithm.TestShake();
        try (var ignored = TimeStatsUtil.bind(recorder)) {
            Assertions.assertSame(solution, shake.shake(solution));
            Assertions.assertSame(solution, shake.shake(solution, 1));
        }
        Assertions.assertEquals(2, recorder.calls.size());
        Assertions.assertNotEquals(recorder.calls.get(0).method, recorder.calls.get(1).method);
        for (var call : recorder.calls) Assertions.assertTrue(call.method.signature().contains(".shake("));
    }

    @Test
    void testTimedAlgorithm() {
        Context.Configurator.setObjectives(Objective.ofMinimizing("DefaultMinimize", TestSolution::getScore, TestMove::getScoreChange));
        Metrics.register("DefaultMinimize", ref -> new DeclaredObjective("DefaultMinimize", FMode.MINIMIZE, ref));
        // total time is algorithm + constructive + 2 * local search
        var alg = new TimedAlgorithm(3, 5, 1);
        var testInstance = new TestInstance("Test");
        var recorder = new CapturingRecorder();
        try (var scope = TimeStatsUtil.bind(recorder)) {
            alg.algorithm(testInstance);
        }
        Assertions.assertEquals(4, recorder.calls.size());
        for (String method : List.of("algorithm", "construct", "improve", "work1")) {
            int count = 0;
            for (var call : recorder.calls) if (call.method.signature().contains("." + method + "(")) count++;
            Assertions.assertEquals(1, count);
        }
        Assertions.assertTrue(recorder.closed);
        Assertions.assertNull(TimeStatsUtil.current());
    }

    @Test
    void staticOverloadedAndThrowingCallsAndNonInheritedScopes() throws Exception {
        var recorder = new CapturingRecorder();
        try (var scope = TimeStatsUtil.bind(recorder)) {
            staticWork();
            staticWork(1);
            var expected = new IllegalArgumentException("original");
            Assertions.assertSame(expected, Assertions.assertThrows(IllegalArgumentException.class, () -> throwingWork(expected)));
            Thread child = new Thread(TimeStatsTest::staticWork);
            child.start();
            child.join();
        }
        Assertions.assertEquals(3, recorder.calls.size());
        Assertions.assertNotEquals(recorder.calls.get(0).method, recorder.calls.get(1).method);
        for (var call : recorder.calls) Assertions.assertTrue(call.exit >= call.enter);
    }

    @Test
    void inactiveFastPathDoesNotInspectSignature() throws Throwable {
        var point = org.mockito.Mockito.mock(org.aspectj.lang.ProceedingJoinPoint.class);
        org.mockito.Mockito.when(point.proceed()).thenReturn("value");
        Assertions.assertEquals("value", new TimedAspect().commonLog(point));
        org.mockito.Mockito.verify(point).proceed();
        org.mockito.Mockito.verifyNoMoreInteractions(point);
    }

    @TimeStats
    static void staticWork() {}
    @TimeStats
    static void staticWork(int ignored) {}
    @TimeStats
    static void throwingWork(RuntimeException error) { throw error; }

    record Call(TimeStatsMethod method, long enter, long exit) {}
    static class CapturingRecorder implements TimeStatsRecorder {
        final List<Call> calls = new ArrayList<>();
        boolean closed;
        public boolean isActive() { return !closed; }
        public void record(TimeStatsMethod method, long enter, long exit) { calls.add(new Call(method, enter, exit)); }
        public void close() { closed = true; }
    }
}
