package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.mreflp.alg.*;
import es.urjc.etsii.grafo.mreflp.improve.*;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static es.urjc.etsii.grafo.mreflp.MREFLPTestUtil.*;

class MREFLPAlgorithmTest {
    @BeforeEach void setup() { context(1234); }
    @AfterEach void cleanup() { TimeControl.remove(); }

    @Test void paperWorkedExample() {
        long[][] w = new long[8][8];
        for (int u = 0; u < 8; u++) for (int v = u + 1; v < 8; v++) {
            w[u][v] = w[v][u] = u / 2 == v / 2 ? 50 : 1;
        }
        var i = new MREFLPInstance("paper", "test", 3, 4, w, 0, "fixture");
        var s = solution(i, 3, 1, 1, 0, 1, 2, 0, 2);
        assertEquals(328, s.cost());
        var cache = new MREFLPDeltaCache(s);
        assertEquals(-102, cache.relocation(6, 2));
        new MREFLPMove(s, 6, 2, false, cache.relocation(6, 2)).execute(s);
        cache.afterRelocation(s, 6, 0);
        assertEquals(226, s.cost());
        assertEquals(-98, cache.swap(s, 4, 0));
        new MREFLPMove(s, 4, 0, true, cache.swap(s, 4, 0)).execute(s);
        cache.afterSwap(s, 4, 0, 1, 3);
        assertEquals(128, s.cost());
        assertEquals(40, solution(i, 0, 0, 1, 1, 2, 2, 3, 3).cost());
        checkCache(s, cache);
    }

    @Test void exhaustiveFeasibleNeighborhoods() {
        var i = instance(5, 2, 3, 77);
        int count = 0;
        for (int encoding = 0; encoding < 243; encoding++) {
            int value = encoding;
            int[] groups = new int[5], counts = new int[3];
            for (int v = 0; v < 5; v++) { groups[v] = value % 3; value /= 3; counts[groups[v]]++; }
            if (counts[0] > 2 || counts[1] > 2 || counts[2] > 2) continue;
            count++;
            var s = solution(i, groups);
            var cache = new MREFLPDeltaCache(s);
            checkCache(s, cache);
            for (int v = 0; v < 5; v++) for (int g = 0; g < 3; g++) {
                if (g == s.group(v) || s.occupancy(g) == 2) continue;
                var copy = s.cloneSolution();
                var updated = new MREFLPDeltaCache(copy);
                int old = copy.group(v);
                long delta = cache.relocation(v, g);
                int[] changed = groups.clone(); changed[v] = g;
                assertEquals(solution(i, changed).cost() - s.cost(), delta);
                new MREFLPMove(copy, v, g, false, delta).execute(copy);
                updated.afterRelocation(copy, v, old);
                checkCache(copy, updated);
            }
            for (int u = 0; u < 5; u++) for (int v = u + 1; v < 5; v++) {
                if (s.group(u) == s.group(v)) continue;
                var copy = s.cloneSolution();
                var updated = new MREFLPDeltaCache(copy);
                int oldU = copy.group(u), oldV = copy.group(v);
                long delta = cache.swap(s, u, v);
                int[] changed = groups.clone(); changed[u] = oldV; changed[v] = oldU;
                assertEquals(solution(i, changed).cost() - s.cost(), delta);
                new MREFLPMove(copy, u, v, true, delta).execute(copy);
                updated.afterSwap(copy, u, v, oldU, oldV);
                checkCache(copy, updated);
            }
        }
        assertEquals(90, count);
    }

    @Test void longMixedMoveSequencesAndCloneIndependence() {
        var i = instance(12, 3, 8, 44);
        var s = solution(i, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3);
        var original = s.cloneSolution();
        int[] originalAssignments = original.assignments();
        var cache = new MREFLPDeltaCache(s);
        var random = new Random(22);
        for (int iteration = 0; iteration < 1000; iteration++) {
            int u = random.nextInt(i.n()), v = random.nextInt(i.n()), g = random.nextInt(i.groups());
            if (iteration % 2 == 0 && s.group(u) != s.group(v)) {
                int oldU = s.group(u), oldV = s.group(v);
                new MREFLPMove(s, u, v, true, cache.swap(s, u, v)).execute(s);
                cache.afterSwap(s, u, v, oldU, oldV);
            } else if (s.group(u) != g && s.occupancy(g) < i.capacity()) {
                int old = s.group(u);
                new MREFLPMove(s, u, g, false, cache.relocation(u, g)).execute(s);
                cache.afterRelocation(s, u, old);
            }
            checkCache(s, cache);
        }
        assertArrayEquals(originalAssignments, original.assignments());
        assertEquals(original.recalculateCost(), original.cost());
    }

    @Test void costsAndDeltasExceedSignedInteger() {
        long[][] w = {{0, 2_000_000_000L, 1_000_000_000L}, {2_000_000_000L, 0, 1_000_000_000L}, {1_000_000_000L, 1_000_000_000L, 0}};
        var s = solution(new MREFLPInstance("large", "test", 2, 5, w, 0, "fixture"), 0, 4, 3);
        assertEquals(12_000_000_000L, s.cost());
        assertTrue(Math.abs(MREFLPEvaluationUtil.relocation(s, 0, 4)) > Integer.MAX_VALUE);
        checkCache(s, new MREFLPDeltaCache(s));
    }

    @Test void learningEquationsAndStrictSmoothingThresholds() {
        var s = solution(instance(2, 2, 4, 7), 0, 1);
        var eta = new LearningMatrix(2, 4, .1, .2, .3, .3);
        eta.update(new int[]{0, 0}, s);
        assertEquals(.325, eta.value(0, 0), 1e-12);
        assertEquals(.225, eta.value(0, 1), 1e-12);
        assertEquals(.14, eta.value(1, 0), 1e-12);
        assertEquals(.48666666666666664, eta.value(1, 1), 1e-12);
        assertEquals(.18666666666666665, eta.value(1, 2), 1e-12);
        assertEquals(.05, LearningMatrix.smooth(.05, .3));
        assertEquals(.95, LearningMatrix.smooth(.95, .3));
        assertEquals(.328, LearningMatrix.smooth(.04, .3), 1e-12);
        assertEquals(.672, LearningMatrix.smooth(.96, .3), 1e-12);
        var wide = new LearningMatrix(1, 170, .1, .2, .3, .3);
        wide.update(new int[]{0}, solution(instance(1, 2, 170, 0), 0));
        double sum = 0;
        for (int g = 0; g < 170; g++) sum += wide.value(0, g);
        assertTrue(sum > 50, "Equation 16 is a literal smoothing pass, without normalization");
    }

    @Test void tabuExpiryAndAspiration() {
        assertFalse(OneMoveTabuSearch.admissible(100, 100, 2, 3));
        assertTrue(OneMoveTabuSearch.admissible(99, 100, 2, 3));
        assertTrue(OneMoveTabuSearch.admissible(110, 100, 3, 3));
    }

    @Test void searchReturnsBestAndSwapTerminatesAtLocalOptimum() {
        var s = solution(instance(8, 3, 5, 18), 0, 0, 1, 1, 2, 2, 3, 3);
        long original = s.cost();
        var tabu = new OneMoveTabuSearch(20, 3, true).improve(s);
        assertTrue(tabu.cost() <= original);
        assertEquals(original, s.cost());
        var descent = new SwapDescent(true).improve(tabu);
        assertTrue(descent.cost() <= tabu.cost());
        for (int u = 0; u < 8; u++) for (int v = u + 1; v < 8; v++) {
            if (descent.group(u) != descent.group(v)) assertTrue(MREFLPEvaluationUtil.swap(descent, u, v) >= 0);
        }
        var full = solution(instance(4, 2, 2, 9), 0, 0, 1, 1);
        assertEquals(full.cost(), new OneMoveTabuSearch(20, 3, true).improve(full).cost());
        var zero = solution(new MREFLPInstance("zero", "test", 2, 3, new long[4][4], 0, "fixture"), 0, 0, 1, 1);
        assertEquals(0, new SwapDescent(true).improve(zero).cost());
        assertEquals(0, new OneMoveTabuSearch(20, 3, true).improve(zero).cost());
    }

    @Test void allVariantsRepeatAndLearningResetsBetweenCalls() {
        var i = instance(10, 3, 6, 31);
        for (var variant : LMLSVariant.values()) {
            var algorithm = algorithm(variant, 12);
            context(1234);
            var first = algorithm.algorithm(i);
            context(999);
            algorithm.algorithm(instance(12, 2, 8, 63));
            context(1234);
            var second = algorithm.algorithm(i);
            assertEquals(first.cost(), second.cost(), variant.name());
            assertArrayEquals(first.assignments(), second.assignments(), variant.name());
            new MREFLPSolutionValidator().validate(first).throwIfFail();
        }
    }

    @Test void directAblationsPreserveFixedRestartTrace() {
        var i = instance(12, 3, 8, 71);
        context(83);
        var expected = algorithm(LMLSVariant.LMLS, 25).algorithm(i);
        for (var variant : List.of(LMLSVariant.DIRECT_ONE_MOVE, LMLSVariant.DIRECT_SWAP)) {
            context(83);
            var actual = algorithm(variant, 25).algorithm(i);
            assertEquals(expected.cost(), actual.cost());
            assertArrayEquals(expected.assignments(), actual.assignments());
        }
    }

    @Test void expiredDeadlineStillReturnsFeasibleConstruction() {
        var i = instance(256, 2, 170, 18);
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS);
        TimeControl.start();
        long start = System.nanoTime();
        var result = algorithm(LMLSVariant.LMLS, 0).algorithm(i);
        assertTrue(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(1));
        new MREFLPSolutionValidator().validate(result).throwIfFail();
    }

    @Test void timeLimitedRunReturnsPromptly() {
        var i = instance(256, 2, 170, 18);
        TimeControl.setMaxExecutionTime(20, TimeUnit.MILLISECONDS);
        TimeControl.start();
        long start = System.nanoTime();
        var result = algorithm(LMLSVariant.LMLS, 0).algorithm(i);
        assertTrue(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(1));
        new MREFLPSolutionValidator().validate(result).throwIfFail();
    }

    @Test void unboundedAlgorithmRequiresDeadline() {
        assertThrows(IllegalStateException.class, () -> algorithm(LMLSVariant.LMLS, 0).algorithm(instance(5, 2, 3, 0)));
    }

    private static void checkCache(MREFLPSolution s, MREFLPDeltaCache cache) {
        assertEquals(s.recalculateCost(), s.cost());
        new MREFLPSolutionValidator().validate(s).throwIfFail();
        for (int v = 0; v < s.getInstance().n(); v++) for (int g = 0; g < s.getInstance().groups(); g++) {
            assertEquals(MREFLPEvaluationUtil.relocation(s, v, g), cache.relocation(v, g));
        }
        for (int u = 0; u < s.getInstance().n(); u++) for (int v = u + 1; v < s.getInstance().n(); v++) {
            assertEquals(MREFLPEvaluationUtil.swap(s, u, v), cache.swap(s, u, v));
        }
    }
}
