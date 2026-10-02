package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.metrics.*;
import es.urjc.etsii.grafo.mreflp.alg.*;
import es.urjc.etsii.grafo.mreflp.create.*;
import es.urjc.etsii.grafo.mreflp.improve.*;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.mreflp.neighborhood.*;
import es.urjc.etsii.grafo.mreflp.scatter.*;
import es.urjc.etsii.grafo.mreflp.shake.*;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static es.urjc.etsii.grafo.mreflp.MREFLPTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class MREFLPNewComponentsTest {
    @BeforeEach void setup() { context(1234); }
    @AfterEach void cleanup() { TimeControl.remove(); Metrics.disableMetrics(); }

    @Test void newCacheMatchesDirectDeltasAcrossExhaustiveFeasibleMoves() {
        var instance = instance(5, 2, 3, 77);
        int feasible = 0;
        for (int encoding = 0; encoding < 243; encoding++) {
            int[] groups = new int[5], counts = new int[3];
            int value = encoding;
            for (int v = 0; v < 5; v++) { groups[v] = value % 3; value /= 3; counts[groups[v]]++; }
            if (counts[0] > 2 || counts[1] > 2 || counts[2] > 2) continue;
            feasible++;
            var original = solution(instance, groups);
            checkCache(original, new MREFLPDeltaCacheNew(original));
            for (int v = 0; v < 5; v++) for (int g = 0; g < 3; g++) {
                if (g == original.group(v) || original.occupancy(g) == 2) continue;
                var s = original.cloneSolution();
                var cache = new MREFLPDeltaCacheNew(s);
                int old = s.group(v);
                new MREFLPMove(s, v, g, false, cache.relocation(v, g)).execute(s);
                cache.afterRelocation(s, v, old);
                checkCache(s, cache);
            }
            for (int u = 0; u < 5; u++) for (int v = u + 1; v < 5; v++) {
                if (original.group(u) == original.group(v)) continue;
                var s = original.cloneSolution();
                var cache = new MREFLPDeltaCacheNew(s);
                int oldU = s.group(u), oldV = s.group(v);
                new MREFLPMove(s, u, v, true, cache.swap(s, u, v)).execute(s);
                cache.afterSwap(s, u, v, oldU, oldV);
                checkCache(s, cache);
            }
        }
        assertEquals(90, feasible);
    }

    @Test void cacheHandlesSparseLargeFlowsAndLongMixedSequences() {
        long[][] flows = new long[12][12];
        var random = new Random(22);
        for (int u = 0; u < 12; u++) for (int v = u + 1; v < 12; v++) {
            if (random.nextBoolean()) flows[u][v] = flows[v][u] = 2_000_000_000L + random.nextInt(100);
        }
        var s = solution(new MREFLPInstance("sparse", "test", 3, 8, flows, 0, "test"), 0,0,0,1,1,1,2,2,2,3,3,3);
        var untouched = s.cloneSolution();
        var cache = new MREFLPDeltaCacheNew(s);
        for (int step = 0; step < 500; step++) {
            int u = random.nextInt(12), v = random.nextInt(12), g = random.nextInt(8);
            if (step % 2 == 0 && s.group(u) != s.group(v)) {
                int oldU = s.group(u), oldV = s.group(v);
                new MREFLPMove(s, u, v, true, cache.swap(s, u, v)).execute(s);
                cache.afterSwap(s, u, v, oldU, oldV);
            } else if (s.group(u) != g && s.occupancy(g) < 3) {
                int old = s.group(u);
                new MREFLPMove(s, u, g, false, cache.relocation(u, g)).execute(s);
                cache.afterRelocation(s, u, old);
            }
            checkCache(s, cache);
        }
        assertArrayEquals(new int[]{0,0,0,1,1,1,2,2,2,3,3,3}, untouched.assignments());
        assertTrue(untouched.cost() > Integer.MAX_VALUE);
    }

    @Test void allConstructionPoliciesAndBoundariesAreFeasible() {
        for (var i : List.of(instance(10, 3, 6, 31), instance(4, 2, 2, 9), instance(1, 1, 1, 8))) {
            for (var policy : ConstructionPolicyNew.values()) for (var order : FacilityOrderNew.values()) {
                for (double rcl : new double[]{0, .25, 1}) {
                    context(83);
                    var s = new MREFLPConstructiveNew(policy, order, rcl).construct(new MREFLPSolution(i));
                    validate(s);
                }
            }
        }
    }

    @Test void maintainedMarginalsSelectActualGreedyMinimumAndPreservePartialAssignments() {
        var i = instance(10, 3, 6, 31);
        for (var orderMode : FacilityOrderNew.values()) {
            context(83);
            int[] order = MREFLPReconstructionUtil.facilityOrder(i, orderMode);
            context(83);
            var s = new MREFLPConstructiveNew(ConstructionPolicyNew.GREEDY, orderMode, 0).construct(new MREFLPSolution(i));
            var partial = new MREFLPSolution(i);
            for (int v : order) {
                long best = Long.MAX_VALUE, chosenCost = 0;
                for (int g = 0; g < i.groups(); g++) {
                    if (partial.occupancy(g) == i.capacity()) continue;
                    long marginal = 0;
                    for (int u = 0; u < i.n(); u++) if (partial.group(u) >= 0) marginal += i.flow(u, v) * Math.abs(g - partial.group(u));
                    best = Math.min(best, marginal);
                    if (g == s.group(v)) chosenCost = marginal;
                }
                assertEquals(best, chosenCost);
                partial.assign(v, s.group(v));
            }
        }
        var partial = new MREFLPSolution(i);
        partial.assign(0, 3); partial.assign(5, 2);
        var repaired = new MREFLPReconstructiveNew(ConstructionPolicyNew.RCL, FacilityOrderNew.FLOW_DESCENDING, .25).reconstruct(partial);
        validate(repaired);
        assertEquals(3, repaired.group(0)); assertEquals(2, repaired.group(5));
        assertEquals(-1, partial.group(1));
    }

    @Test void exactNewTabuAndSwapPreserveOriginalSeededTrajectories() {
        var i = instance(12, 3, 8, 71);
        var input = solution(i, 0,0,0,1,1,1,2,2,2,3,3,3);
        for (long seed : new long[]{83, 999, 1234}) for (boolean cached : new boolean[]{false, true}) {
            context(seed);
            var expected = new OneMoveTabuSearch(20, 3, cached).improve(input);
            context(seed);
            var actual = new OneMoveTabuSearchNew(20, 3, cached).improve(input);
            assertArrayEquals(expected.assignments(), actual.assignments());
            context(seed);
            expected = new SwapDescent(cached).improve(input);
            context(seed);
            actual = new SwapDescentNew(ImprovementPolicyNew.BEST, cached, 1).improve(input);
            assertArrayEquals(expected.assignments(), actual.assignments());
        }
    }

    @Test void descentPoliciesAreNonworseningAndFinishAtTheirFullLocalOptimum() {
        var input = solution(instance(10, 3, 6, 31), 0,0,0,1,1,1,2,2,2,3);
        int[] original = input.assignments();
        for (var policy : ImprovementPolicyNew.values()) for (boolean cached : new boolean[]{false, true}) {
            var relocated = new OneMoveDescentNew(policy, cached).improve(input);
            validate(relocated);
            assertTrue(relocated.cost() <= input.cost());
            for (int v = 0; v < 10; v++) for (int g = 0; g < 6; g++) {
                if (g != relocated.group(v) && relocated.occupancy(g) < 3) assertTrue(MREFLPEvaluationUtil.relocation(relocated, v, g) >= 0);
            }
            var swapped = new SwapDescentNew(policy, cached, 1).improve(input);
            validate(swapped);
            for (int u = 0; u < 10; u++) for (int v = u + 1; v < 10; v++) {
                if (swapped.group(u) != swapped.group(v)) assertTrue(MREFLPEvaluationUtil.swap(swapped, u, v) >= 0);
            }
            var sampled = new SwapDescentNew(policy, cached, .05).improve(input);
            validate(sampled); assertTrue(sampled.cost() <= input.cost());
        }
        assertArrayEquals(original, input.assignments());
    }

    @Test void neighborhoodsEnumerateEveryFeasibleMoveAndSamplingReachesEachOne() {
        var s = solution(instance(4, 2, 3, 9), 0,0,1,1);
        for (var neighborhood : neighborhoods()) {
            var enumerated = new HashSet<String>();
            try (var moves = neighborhood.explore(s).moves()) {
                var iterator = moves.iterator();
                while (iterator.hasNext()) {
                    var move = iterator.next();
                    assertTrue(enumerated.add(move.toString()));
                    var copy = s.cloneSolution();
                    move.execute(copy); validate(copy);
                    assertEquals(copy.cost() - s.cost(), move.delta());
                }
            }
            int expected = neighborhood instanceof MREFLPMixedNeighborhoodNew ? 8 : 4;
            assertEquals(expected, enumerated.size());
            var sampled = new HashSet<String>();
            for (int sample = 0; sample < 2000; sample++) sampled.add(neighborhood.getRandomMove(s).orElseThrow().toString());
            assertEquals(enumerated, sampled);
        }
    }

    @Test void mixedShakeFallsBackToSwapsAndHandlesNoMovesAndStaleEnumerations() {
        var full = solution(instance(4, 2, 2, 9), 0,0,1,1);
        assertTrue(new MREFLPRelocationNeighborhoodNew().getRandomMove(full).isEmpty());
        long version = full.getVersion();
        new MREFLPShakeNew(.01, 1).shake(full, 0);
        validate(full); assertEquals(version + 1, full.getVersion());
        var onlyGroup = solution(instance(1, 1, 1, 9), 0);
        for (var neighborhood : neighborhoods()) {
            assertTrue(neighborhood.getRandomMove(onlyGroup).isEmpty());
            assertEquals(0, neighborhood.explore(onlyGroup).moves().count());
        }
        new MREFLPShakeNew(.2, 0).shake(onlyGroup, Integer.MAX_VALUE);
        validate(onlyGroup);
        var iterator = new MREFLPSwapNeighborhoodNew().explore(full).moves().iterator();
        assertTrue(iterator.hasNext());
        new MREFLPSwapNeighborhoodNew().getRandomMove(full).orElseThrow().execute(full);
        // Stream iterators may buffer a move at hasNext; Mork still refuses to execute it.
        assertThrows(AssertionError.class, () -> iterator.next().execute(full));
        assertThrows(IllegalStateException.class, iterator::hasNext);
    }

    @Test void destructionAndRepairRetainUnremovedFacilitiesWithoutChangingTheParent() {
        var parent = solution(instance(10, 2, 5, 31), 0,0,1,1,2,2,3,3,4,4);
        int[] original = parent.assignments();
        for (var policy : RemovalPolicyNew.values()) for (double fraction : new double[]{.05, .3, 1}) {
            var partial = new MREFLPDestructiveNew(fraction, policy).destroy(parent, 1);
            int removed = 0;
            for (int v = 0; v < 10; v++) if (partial.group(v) < 0) removed++;
            assertEquals((int) Math.ceil(fraction * 10), removed);
            var repaired = new MREFLPReconstructiveNew(ConstructionPolicyNew.RCL, FacilityOrderNew.RANDOM, .5).reconstruct(partial);
            validate(repaired);
            for (int v = 0; v < 10; v++) if (partial.group(v) >= 0) assertEquals(partial.group(v), repaired.group(v));
            assertArrayEquals(original, parent.assignments());
        }
    }

    @Test void reflectionDistanceAndCapacityAwareCombinationPreserveModelSemantics() {
        var i = instance(6, 2, 3, 71);
        var left = solution(i, 0,0,1,1,2,2);
        var reflected = solution(i, 2,2,1,1,0,0);
        var right = solution(i, 0,1,0,2,1,2);
        var distance = new MREFLPSolutionDistanceNew();
        assertEquals(left.cost(), reflected.cost());
        assertEquals(0, distance.distances(left, reflected));
        assertEquals(distance.distances(left, right), distance.distances(right, left));
        int[] original = left.assignments();
        for (double bias : new double[]{0, .5, 1}) for (double rcl : new double[]{0, 1}) {
            var combinator = new MREFLPSolutionCombinatorNew(bias, rcl);
            for (int seed = 0; seed < 20; seed++) {
                context(seed);
                for (var child : combinator.apply(left, right)) validate(child);
            }
        }
        assertArrayEquals(original, left.assignments());
        assertThrows(IllegalArgumentException.class, () -> distance.distances(left, solution(instance(6, 2, 3, 72), 0,0,1,1,2,2)));
    }

    @Test void learningCopyMatchesLiteralEquationsAndNormalizedRowsRemainDistributions() {
        var i = instance(2, 2, 4, 7);
        for (int corner = 0; corner < 16; corner++) {
            double alpha = (corner & 1) == 0 ? .01 : .99, beta = (corner & 2) == 0 ? .01 : .99;
            double gamma = (corner & 4) == 0 ? .01 : .99, rho = (corner & 8) == 0 ? .01 : .99;
            var original = new LearningMatrix(2, 4, alpha, beta, gamma, rho);
            var literal = new LearningMatrixNew(2, 4, alpha, beta, gamma, rho, false);
            var normalized = new LearningMatrixNew(2, 4, alpha, beta, gamma, rho, true);
            for (int update = 0; update < 100; update++) {
                int origin = update % 4;
                var improved = solution(i, origin, (origin + 1) % 4);
                int[] initial = {origin, origin};
                original.update(initial, improved); literal.update(initial, improved); normalized.update(initial, improved);
                for (int v = 0; v < 2; v++) {
                    double sum = 0;
                    for (int g = 0; g < 4; g++) {
                        assertEquals(original.value(v, g), literal.value(v, g), 1e-12);
                        assertTrue(Double.isFinite(normalized.value(v, g)) && normalized.value(v, g) >= 0);
                        sum += normalized.value(v, g);
                    }
                    assertEquals(1, sum, 1e-12);
                }
            }
        }
    }

    @Test void annealingAndLearningResetPerCallAndReturnTheIncumbent() {
        var i = instance(10, 3, 6, 31);
        var input = solution(i, 0,0,0,1,1,1,2,2,2,3);
        var sa = new MREFLPSimulatedAnnealingNew(new MREFLPMixedNeighborhoodNew(.5), 1, .95, 32, 30);
        context(83);
        var first = sa.improve(input);
        validate(first); assertTrue(first.cost() <= input.cost());
        context(83);
        assertArrayEquals(first.assignments(), sa.improve(input).assignments());
        assertArrayEquals(new int[]{0,0,0,1,1,1,2,2,2,3}, input.assignments());
        for (boolean learning : new boolean[]{false, true}) for (boolean normalize : new boolean[]{false, true}) {
            var lmls = withBuilder(new LMLSNew("new", new MREFLPConstructiveNew(ConstructionPolicyNew.RCL, FacilityOrderNew.FLOW_DESCENDING, .25),
                    new VND<>(List.of(new OneMoveDescentNew(ImprovementPolicyNew.FIRST, true), new SwapDescentNew(ImprovementPolicyNew.BEST, true, 1)), Main.COST),
                    learning, normalize, .6, .1, .2, .3, .3, 4));
            context(83); var expected = lmls.algorithm(i); validate(expected);
            context(999); lmls.algorithm(instance(12, 2, 8, 63));
            context(83); assertArrayEquals(expected.assignments(), lmls.algorithm(i).assignments());
        }
    }

    @Test @Timeout(10) void expiredDeadlinesStillYieldFeasibleConstructionRepairAndLearning() {
        var i = instance(256, 2, 170, 18);
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS); TimeControl.start();
        var constructive = new MREFLPConstructiveNew(ConstructionPolicyNew.RCL, FacilityOrderNew.FLOW_DESCENDING, .25);
        var s = constructive.construct(new MREFLPSolution(i)); validate(s);
        var partial = new MREFLPDestructiveNew(.3, RemovalPolicyNew.RELATED).destroy(s, 1);
        var repaired = new MREFLPReconstructiveNew(ConstructionPolicyNew.GREEDY, FacilityOrderNew.FLOW_DESCENDING, 0).reconstruct(partial); validate(repaired);
        var lmls = withBuilder(new LMLSNew("new", constructive, Improver.nul(), true, true, .6, .1, .2, .3, .3, 0));
        validate(lmls.algorithm(i));
        assertArrayEquals(s.assignments(), new OneMoveDescentNew(ImprovementPolicyNew.BEST, true).improve(s).assignments());
        assertArrayEquals(s.assignments(), new MREFLPSimulatedAnnealingNew(new MREFLPMixedNeighborhoodNew(.5), 1, .95, 32, 30).improve(s).assignments());
    }

    @Test void incumbentMetricsAreFeasibleMonotoneAndIncludeTheReturnedSolution() {
        Metrics.enableMetrics();
        Metrics.register("Cost", reference -> new DeclaredObjective("Cost", Main.COST.getFMode(), reference));
        Metrics.resetMetrics();
        var i = instance(10, 3, 6, 31);
        var lmls = withBuilder(new LMLSNew("new", new MREFLPConstructiveNew(ConstructionPolicyNew.RANDOM, FacilityOrderNew.RANDOM, 1),
                new OneMoveTabuSearchNew(20, 3, true), true, true, .6, .1, .2, .3, .3, 4));
        var returned = lmls.algorithm(i); validate(returned);
        var values = Metrics.get("Cost").getValues();
        assertFalse(values.isEmpty());
        double previous = Double.POSITIVE_INFINITY;
        for (var value : values) { assertTrue(value.value() <= previous); previous = value.value(); }
        assertEquals(returned.cost(), values.last().value());
    }

    @Test void constructorsRejectInvalidDomains() {
        assertThrows(IllegalArgumentException.class, () -> new MREFLPConstructiveNew(ConstructionPolicyNew.RCL, FacilityOrderNew.RANDOM, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new SwapDescentNew(ImprovementPolicyNew.FIRST, true, 0));
        assertThrows(IllegalArgumentException.class, () -> new MREFLPShakeNew(0, .5));
        assertThrows(IllegalArgumentException.class, () -> new MREFLPDestructiveNew(-1, RemovalPolicyNew.RANDOM));
        assertThrows(IllegalArgumentException.class, () -> new MREFLPMixedNeighborhoodNew(2));
        assertThrows(IllegalArgumentException.class, () -> new MREFLPSimulatedAnnealingNew(new MREFLPSwapNeighborhoodNew(), 1, 1, 1, 1));
        var unbounded = withBuilder(new LMLSNew("new", new MREFLPConstructiveNew(ConstructionPolicyNew.RANDOM, FacilityOrderNew.RANDOM, 1),
                Improver.nul(), true, false, .6, .1, .2, .3, .3, 0));
        assertThrows(IllegalStateException.class, () -> unbounded.algorithm(instance(4, 2, 3, 9)));
    }

    private static List<RandomizableNeighborhood<MREFLPMove, MREFLPSolution, MREFLPInstance>> neighborhoods() {
        return List.of(new MREFLPRelocationNeighborhoodNew(), new MREFLPSwapNeighborhoodNew(), new MREFLPMixedNeighborhoodNew(.5));
    }
    private static void validate(MREFLPSolution s) {
        new MREFLPSolutionValidator().validate(s).throwIfFail();
        assertEquals(s.recalculateCost(), s.cost());
    }
    private static void checkCache(MREFLPSolution s, MREFLPDeltaCacheNew cache) {
        validate(s);
        for (int v = 0; v < s.getInstance().n(); v++) for (int g = 0; g < s.getInstance().groups(); g++) {
            assertEquals(MREFLPEvaluationUtil.relocation(s, v, g), cache.relocation(v, g));
        }
        for (int u = 0; u < s.getInstance().n(); u++) for (int v = u + 1; v < s.getInstance().n(); v++) {
            assertEquals(MREFLPEvaluationUtil.swap(s, u, v), cache.swap(s, u, v));
        }
    }
}
