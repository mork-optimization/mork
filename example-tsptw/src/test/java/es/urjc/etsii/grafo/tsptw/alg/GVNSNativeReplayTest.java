package es.urjc.etsii.grafo.tsptw.alg;

import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstanceImporter;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.ParkMillerRandom;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class GVNSNativeReplayTest {
    record ReplayCase(String deadlineIndex, long seed, int restarts, double cost, int[] tour, long rngState) {
        @Override public String toString() { return deadlineIndex + ": seed=" + seed + ", restarts=" + restarts; }
    }

    @BeforeEach void setUp() { initialize(); }
    @AfterEach void tearDown() { cleanup(); }

    static List<ReplayCase> nativeCases() {
        // Native C++ observations for n80w200.001, including exact tours and RNG states.
        // Source: search-diagnostics/2026-11-01-n80w200.001/c-{original,fixed}-30x50.jsonl.
        // RNG counters and empty-vector guards were added to those traces; the guards never fired.
        int[] tourA = {
                0, 61, 67, 7, 31, 39, 15, 4, 30, 2, 27, 58,
                5, 77, 22, 54, 37, 48, 10, 60, 74, 73, 24, 46,
                29, 32, 23, 76, 6, 47, 78, 63, 18, 62, 75, 12,
                51, 11, 3, 50, 49, 14, 72, 34, 16, 25, 71, 45,
                26, 55, 9, 65, 1, 43, 36, 68, 42, 70, 28, 21,
                59, 53, 35, 44, 33, 20, 79, 52, 13, 56, 38, 8,
                64, 40, 41, 57, 69, 19, 17, 66, 80, 0
        };
        int[] tourB = {
                0, 61, 67, 7, 31, 39, 15, 4, 30, 2, 27, 58,
                5, 77, 22, 54, 37, 48, 10, 60, 74, 73, 24, 46,
                29, 32, 23, 76, 6, 47, 78, 63, 18, 62, 11, 51,
                12, 75, 72, 34, 14, 49, 16, 25, 71, 45, 26, 55,
                9, 65, 1, 43, 36, 42, 68, 50, 70, 3, 28, 21,
                59, 53, 35, 44, 33, 20, 79, 52, 13, 56, 38, 8,
                64, 40, 41, 57, 69, 19, 17, 66, 80, 0
        };
        int[] tourC = {
                0, 61, 67, 7, 31, 15, 39, 4, 30, 2, 27, 58,
                5, 77, 22, 54, 48, 37, 10, 60, 74, 73, 24, 46,
                32, 29, 23, 76, 6, 47, 78, 63, 18, 62, 11, 12,
                51, 75, 72, 49, 14, 34, 16, 25, 71, 45, 26, 55,
                9, 65, 1, 43, 36, 42, 68, 50, 70, 3, 28, 21,
                59, 53, 35, 44, 33, 20, 79, 52, 13, 56, 38, 8,
                64, 40, 41, 57, 69, 19, 17, 66, 80, 0
        };
        int[] tourD = {
                0, 61, 67, 7, 31, 15, 39, 4, 48, 54, 22, 37,
                10, 60, 74, 73, 24, 46, 32, 29, 23, 76, 6, 47,
                78, 63, 18, 62, 11, 51, 12, 75, 77, 30, 27, 2,
                58, 5, 16, 25, 71, 45, 26, 55, 9, 65, 49, 14,
                34, 72, 50, 68, 1, 43, 36, 42, 70, 3, 28, 21,
                59, 53, 35, 44, 33, 20, 79, 52, 13, 56, 38, 8,
                64, 40, 41, 57, 69, 19, 17, 66, 80, 0
        };
        int[] tourE = {
                0, 61, 67, 7, 31, 15, 39, 4, 10, 60, 74, 73,
                24, 46, 29, 32, 23, 76, 6, 47, 78, 63, 18, 62,
                22, 77, 5, 58, 27, 2, 30, 48, 37, 54, 75, 12,
                51, 11, 72, 34, 14, 49, 16, 25, 71, 45, 26, 55,
                9, 65, 1, 43, 36, 42, 68, 50, 70, 3, 28, 21,
                59, 53, 35, 44, 33, 20, 79, 52, 13, 56, 38, 8,
                64, 40, 41, 57, 69, 19, 17, 66, 80, 0
        };
        int[] tourF = {
                0, 61, 67, 7, 31, 39, 15, 4, 30, 2, 27, 58,
                5, 77, 22, 54, 37, 48, 10, 60, 74, 73, 24, 46,
                32, 29, 23, 76, 6, 47, 78, 63, 18, 62, 75, 12,
                51, 11, 3, 50, 49, 14, 72, 34, 16, 25, 71, 45,
                26, 55, 9, 65, 1, 43, 36, 68, 42, 70, 28, 21,
                59, 53, 35, 44, 33, 20, 79, 52, 13, 56, 38, 8,
                64, 40, 41, 57, 69, 19, 17, 66, 80, 0
        };
        int[] tourG = {
                0, 61, 67, 7, 31, 39, 15, 4, 48, 22, 54, 37,
                10, 60, 74, 73, 24, 46, 29, 32, 23, 76, 6, 47,
                78, 63, 18, 62, 11, 51, 12, 75, 77, 30, 27, 2,
                58, 5, 16, 25, 71, 45, 26, 55, 9, 65, 49, 14,
                34, 72, 50, 68, 1, 43, 36, 42, 70, 3, 28, 21,
                59, 53, 35, 44, 33, 20, 79, 52, 13, 56, 38, 8,
                64, 40, 41, 57, 69, 19, 17, 66, 80, 0
        };
        int[] tourH = {
                0, 61, 67, 7, 31, 15, 39, 4, 30, 2, 27, 58,
                5, 77, 22, 54, 37, 48, 10, 60, 74, 73, 24, 46,
                29, 32, 23, 76, 6, 47, 78, 63, 18, 62, 75, 51,
                12, 11, 72, 34, 14, 49, 16, 25, 71, 45, 26, 55,
                9, 65, 1, 43, 36, 42, 68, 50, 70, 3, 28, 21,
                59, 53, 35, 44, 33, 20, 79, 52, 13, 56, 38, 8,
                64, 40, 41, 57, 69, 19, 17, 66, 80, 0
        };
        var observations = List.of(
                new ReplayCase("ORIGINAL_C", 1234L, 1, 492, tourA, 1035635210L),
                new ReplayCase("ORIGINAL_C", 1234L, 3, 492, tourA, 1574668108L),
                new ReplayCase("ORIGINAL_C", 1234L, 50, 492, tourA, 249592272L),
                new ReplayCase("ORIGINAL_C", 1236L, 1, 492, tourB, 546398883L),
                new ReplayCase("ORIGINAL_C", 1236L, 3, 492, tourB, 1869033978L),
                new ReplayCase("ORIGINAL_C", 1236L, 50, 492, tourB, 57595114L),
                new ReplayCase("ORIGINAL_C", 1237L, 1, 492, tourC, 142518734L),
                new ReplayCase("ORIGINAL_C", 1237L, 3, 492, tourC, 20621884L),
                new ReplayCase("ORIGINAL_C", 1237L, 50, 490, tourD, 967235024L),
                new ReplayCase("CORRECTED", 1234L, 1, 492, tourB, 1568325866L),
                new ReplayCase("CORRECTED", 1234L, 3, 492, tourB, 503078032L),
                new ReplayCase("CORRECTED", 1234L, 50, 492, tourB, 833769593L),
                new ReplayCase("CORRECTED", 1236L, 1, 497, tourE, 1421625977L),
                new ReplayCase("CORRECTED", 1236L, 3, 492, tourF, 355126285L),
                new ReplayCase("CORRECTED", 1236L, 50, 490, tourG, 1325179201L),
                new ReplayCase("CORRECTED", 1237L, 1, 492, tourH, 876901950L),
                new ReplayCase("CORRECTED", 1237L, 3, 492, tourH, 1757057812L),
                new ReplayCase("CORRECTED", 1237L, 50, 492, tourH, 872597717L));
        var cases = new ArrayList<ReplayCase>();
        for (var replay : observations) {
            if (replay.deadlineIndex().equals(TSPTWUtil.repairDeadlineIndex())) cases.add(replay);
        }
        assertFalse(cases.isEmpty());
        return cases;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nativeCases")
    @Timeout(30)
    void publicGvnsReproducesNativeBestTourAndRandomState(ReplayCase expected) {
        var instance = new TSPTWInstanceImporter().importInstance("instances/GendreauDumasExtended.zip!n80w200.001.txt");
        Context.Configurator.resetRandom(RandomType.PARK_MILLER, expected.seed());
        startTimeLimit();

        var solution = new GVNS().runRestarts(instance, expected.restarts());

        assertEquals(expected.cost(), solution.cost());
        assertArrayEquals(expected.tour(), solution.getTour());
        assertEquals(expected.rngState(), ((ParkMillerRandom) Context.getRandom()).getState());
        assertTrue(Context.validate(solution));
    }

    @Test
    void configurationSelectsTheNativeRandomProviderAndDescriptionsIdentifyTheCompiledRepairPolicy() {
        var binder = new Binder(new MapConfigurationPropertySource(Map.of(
                "solver.random-type", "park-miller")));
        assertEquals(RandomType.PARK_MILLER, binder.bind("solver", Bindable.of(SolverConfig.class)).get().getRandomType());
        assertTrue(new GVNS().toString().contains("deadlineIndex=" + TSPTWUtil.repairDeadlineIndex()));
    }

    @Test
    void rejectsInvalidRestartLimitsBeforeStartingSearch() throws Exception {
        var instance = instance("best");
        assertThrows(IllegalArgumentException.class, () -> new GVNS().runRestarts(instance, 0));
        assertFalse(TimeControl.isEnabled());
    }
}
