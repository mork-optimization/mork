package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.algorithms.cmsa.*;
import es.urjc.etsii.grafo.algorithms.scattersearch.*;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.mreflp.cmsa.*;
import es.urjc.etsii.grafo.mreflp.create.*;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.mreflp.scatter.*;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static es.urjc.etsii.grafo.mreflp.MREFLPTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class MREFLPCMSANewTest {
    @BeforeEach void setup() { context(1234); }
    @AfterEach void cleanup() { TimeControl.remove(); }

    @Test void restrictedSolverMatchesExhaustiveOptimaIncludingZeroAndSingletonCases() {
        var solver = new MREFLPCMSASolverNew(1_000_000);
        for (int seed = 0; seed < 20; seed++) {
            var instance = instance(5, 2, 3, seed);
            var random = new Random(seed);
            var components = new HashSet<MREFLPAssignmentNew>();
            int[] seedAssignment = {0,0,1,1,2};
            for (int v = 0; v < 5; v++) {
                components.add(new MREFLPAssignmentNew(v, seedAssignment[v]));
                for (int g = 0; g < 3; g++) if (random.nextBoolean()) components.add(new MREFLPAssignmentNew(v, g));
            }
            long expected = bruteForce(instance, components);
            var actual = solver.solve(instance, components, 10_000);
            assertNotNull(actual); validateRestricted(actual, components);
            assertEquals(expected, actual.cost());
        }
        var single = instance(1, 1, 1, 9);
        var components = Set.of(new MREFLPAssignmentNew(0, 0));
        assertEquals(0, solver.solve(single, components, 1000).cost());
        var zero = new MREFLPInstance("zero", "test", 2, 3, new long[5][5], 0, "fixture");
        components = allComponents(zero);
        assertEquals(0, solver.solve(zero, components, 1000).cost());
    }

    @Test void capacityMatchingHandlesAugmentingPathsAndInfeasibleRestrictedSets() {
        var instance = instance(4, 1, 4, 31);
        var components = Set.of(new MREFLPAssignmentNew(0, 0), new MREFLPAssignmentNew(0, 1),
                new MREFLPAssignmentNew(1, 0), new MREFLPAssignmentNew(2, 1), new MREFLPAssignmentNew(2, 2),
                new MREFLPAssignmentNew(3, 2), new MREFLPAssignmentNew(3, 3));
        var solver = new MREFLPCMSASolverNew(1);
        var actual = solver.solve(instance, components, 1000);
        assertNotNull(actual); validateRestricted(actual, components);
        var impossible = new HashSet<MREFLPAssignmentNew>();
        for (int v = 0; v < 4; v++) impossible.add(new MREFLPAssignmentNew(v, 0));
        assertNull(solver.solve(instance, impossible, 1000));
        assertNull(solver.solve(instance, Set.of(), 1000));
        assertNull(solver.solve(instance, components, 0));
        assertThrows(IllegalArgumentException.class, () -> solver.solve(instance, Set.of(new MREFLPAssignmentNew(4, 0)), 1000));
    }

    @Test @Timeout(5) void solverHonorsNodeAndTimeBudgetsWithoutChangingGlobalTimeControl() {
        var instance = instance(30, 2, 20, 71);
        var components = allComponents(instance);
        var solver = new MREFLPCMSASolverNew(1_000_000);
        long start = System.nanoTime();
        var actual = solver.solve(instance, components, 10);
        assertTrue(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(1));
        if (actual != null) validateRestricted(actual, components);
        assertFalse(TimeControl.isEnabled());
        var bounded = new MREFLPCMSASolverNew(1).solve(instance, components, 1000);
        assertNotNull(bounded); validateRestricted(bounded, components);
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS); TimeControl.start();
        assertNull(solver.solve(instance, components, 1000));
        assertTrue(TimeControl.isEnabled());
    }

    @Test void originalCmsaAndNewCopyBuildWithTheNewRestrictedComponents() {
        var i = instance(6, 2, 4, 31);
        var constructive = cmsaConstructive();
        var solver = new MREFLPCMSASolverNew(10000);
        var original = withBuilder(new CMSA<>("original", Main.COST, constructive, solver, 3, 2, 1000, 3));
        var copied = withBuilder(new CMSANew<>("new", Main.COST, constructive, solver, 3, 2, 1000, 3));
        validateRestricted(original.algorithm(i), allComponents(i));
        validateRestricted(copied.algorithm(i), allComponents(i));
    }

    @Test @Timeout(5) void cmsaCopyReturnsFeasibleIncumbentAtExpiredDeadlineAndOriginalFailureIsReproducible() {
        var i = instance(10, 2, 6, 31);
        var constructive = cmsaConstructive();
        var solver = new MREFLPCMSASolverNew(10000);
        var original = withBuilder(new CMSA<>("original", Main.COST, constructive, solver, 3, 2, 1000, 3));
        var copied = withBuilder(new CMSANew<>("new", Main.COST, constructive, solver, 3, 2, 1000, 3));
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS); TimeControl.start();
        assertThrows(IllegalStateException.class, () -> original.algorithm(i));
        validateRestricted(copied.algorithm(i), allComponents(i));
    }

    @Test void scatterCopySupportsQualityOnlyMixedAndPureDiversityPopulations() {
        var i = instance(6, 2, 4, 31);
        var constructive = new MREFLPConstructiveNew(ConstructionPolicyNew.RANDOM, FacilityOrderNew.RANDOM, 1);
        for (double diversity : new double[]{0, .5, 1}) {
            var scatter = scatter(constructive, diversity, false);
            new MREFLPSolutionValidator().validate(scatter.algorithm(i)).throwIfFail();
        }
    }

    @Test void cmsaRetainsBetterConstructedSolutionWhenRestrictedSolverReturnsNoSolution() {
        var i = instance(3, 3, 2, 31);
        var calls = new AtomicInteger();
        var constructive = new CMSAConstructive<MREFLPSolution, MREFLPInstance, MREFLPAssignmentNew>() {
            @Override public MREFLPSolution construct(MREFLPSolution s) {
                return solution(s.getInstance(), calls.getAndIncrement() == 0 ? new int[]{0, 1, 0} : new int[]{0, 0, 0});
            }
            @Override public Set<MREFLPAssignmentNew> usedComponents(MREFLPSolution s) {
                var used = new HashSet<MREFLPAssignmentNew>();
                for (int v = 0; v < i.n(); v++) used.add(new MREFLPAssignmentNew(v, s.group(v)));
                return used;
            }
        };
        var solver = new CMSASolver<MREFLPSolution, MREFLPInstance, MREFLPAssignmentNew>() {
            @Override public MREFLPSolution solve(MREFLPInstance instance, Set<MREFLPAssignmentNew> components, long millis) { return null; }
        };
        var copied = withBuilder(new CMSANew<>("new", Main.COST, constructive, solver, 1, 2, 1000, 1));
        var best = copied.algorithm(i);
        new MREFLPSolutionValidator().validate(best).throwIfFail();
        assertEquals(0, best.cost());
    }

    @Test void scatterRetainsBetterOffspringWhenCombinationConsumesTheRemainingBudget() {
        var i = instance(3, 3, 2, 31);
        var constructive = new Constructive<MREFLPSolution, MREFLPInstance>() {
            @Override public MREFLPSolution construct(MREFLPSolution s) { return solution(s.getInstance(), new int[]{0, 1, 0}); }
        };
        var combinator = new SolutionCombinator<MREFLPSolution, MREFLPInstance>() {
            @Override public Set<MREFLPSolution> newSet(MREFLPSolution[] current, Set<MREFLPSolution> inserted) {
                var offspring = solution(i, new int[]{0, 0, 0});
                TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS); TimeControl.start();
                return Set.of(offspring);
            }
            @Override protected List<MREFLPSolution> apply(MREFLPSolution left, MREFLPSolution right) { throw new UnsupportedOperationException(); }
        };
        var copied = withBuilder(new ScatterSearchNew<>("new", 2, 10, constructive, constructive, Improver.nul(),
                combinator, Main.COST, 1, .5, new MREFLPSolutionDistanceNew(), false));
        var best = copied.algorithm(i);
        new MREFLPSolutionValidator().validate(best).throwIfFail();
        assertEquals(0, best.cost());
    }

    @Test @Timeout(5) void expiredScatterInitializationIsBoundedAndOriginalPureDiversityFailureIsReproducible() {
        var i = instance(10, 2, 6, 31);
        var calls = new AtomicInteger();
        var delegate = new MREFLPConstructiveNew(ConstructionPolicyNew.RANDOM, FacilityOrderNew.RANDOM, 1);
        var counting = new Constructive<MREFLPSolution, MREFLPInstance>() {
            @Override public MREFLPSolution construct(MREFLPSolution s) { calls.incrementAndGet(); return delegate.construct(s); }
        };
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS); TimeControl.start();
        new MREFLPSolutionValidator().validate(scatter(counting, .5, false).algorithm(i)).throwIfFail();
        assertTrue(calls.get() <= 2, "Expired budgets must not generate a full initial population");
        TimeControl.remove();
        var original = withBuilder(new ScatterSearch<>("original", 2, 10, delegate, delegate, Improver.nul(),
                new MREFLPSolutionCombinatorNew(.5, .25), Main.COST, 1, 1, new MREFLPSolutionDistanceNew(), false));
        assertThrows(IndexOutOfBoundsException.class, () -> original.algorithm(i));
    }

    private static MREFLPCMSAConstructiveNew cmsaConstructive() {
        return new MREFLPCMSAConstructiveNew(new MREFLPConstructiveNew(ConstructionPolicyNew.RCL, FacilityOrderNew.FLOW_DESCENDING, .25));
    }
    private static ScatterSearchNew<MREFLPSolution, MREFLPInstance> scatter(Constructive<MREFLPSolution, MREFLPInstance> constructive,
                                                                        double diversity, boolean restart) {
        return withBuilder(new ScatterSearchNew<>("new", 2, 10, constructive, constructive, Improver.nul(),
                new MREFLPSolutionCombinatorNew(.5, .25), Main.COST, 2, diversity, new MREFLPSolutionDistanceNew(), restart));
    }
    private static Set<MREFLPAssignmentNew> allComponents(MREFLPInstance instance) {
        var components = new HashSet<MREFLPAssignmentNew>();
        for (int v = 0; v < instance.n(); v++) for (int g = 0; g < instance.groups(); g++) components.add(new MREFLPAssignmentNew(v, g));
        return components;
    }
    private static void validateRestricted(MREFLPSolution s, Set<MREFLPAssignmentNew> components) {
        new MREFLPSolutionValidator().validate(s).throwIfFail();
        for (int v = 0; v < s.getInstance().n(); v++) assertTrue(components.contains(new MREFLPAssignmentNew(v, s.group(v))));
    }
    private static long bruteForce(MREFLPInstance instance, Set<MREFLPAssignmentNew> allowed) {
        long best = Long.MAX_VALUE;
        int combinations = (int) Math.pow(instance.groups(), instance.n());
        for (int encoding = 0; encoding < combinations; encoding++) {
            int value = encoding;
            int[] assignments = new int[instance.n()], counts = new int[instance.groups()];
            boolean feasible = true;
            for (int v = 0; v < assignments.length; v++) {
                int g = value % instance.groups(); value /= instance.groups(); assignments[v] = g;
                if (++counts[g] > instance.capacity() || !allowed.contains(new MREFLPAssignmentNew(v, g))) { feasible = false; break; }
            }
            if (feasible) best = Math.min(best, solution(instance, assignments).cost());
        }
        return best;
    }
}
