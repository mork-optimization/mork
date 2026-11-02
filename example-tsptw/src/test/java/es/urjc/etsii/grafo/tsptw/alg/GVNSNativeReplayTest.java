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
import tools.jackson.databind.json.JsonMapper;

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

    static List<ReplayCase> nativeCases() throws Exception {
        try (var source = GVNSNativeReplayTest.class.getResourceAsStream("/native-replay/n80w200.001.json")) {
            assertNotNull(source);
            var cases = new ArrayList<ReplayCase>();
            for (var replay : new JsonMapper().readValue(source, ReplayCase[].class)) {
                if (replay.deadlineIndex().equals(TSPTWUtil.repairDeadlineIndex())) cases.add(replay);
            }
            assertFalse(cases.isEmpty());
            return cases;
        }
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
