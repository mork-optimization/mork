package es.urjc.etsii.grafo.flayouts.shake;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashSet;

import static es.urjc.etsii.grafo.flayouts.model.FLPNewTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class FLPRandomRemoveDestructiveTest {
    @BeforeEach void setup() { initialize(1234); }
    @AfterEach void teardown() { cleanup(); }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
    void bulkRemovalPreservesSurvivorOrderAndSource(int removalMask) {
        var source = fixture(instance(4, 2), new int[]{0, 1, 2}, new int[]{3});
        var before = source.cloneSolution();
        var removed = new HashSet<Integer>();
        for (int facility = 0; facility < 4; facility++) {
            if ((removalMask & (1 << facility)) != 0) {
                removed.add(facility);
            }
        }

        var result = new RandomRemoveDestructive(0.25).bulkRemove(source, removed);

        var originalRows = layout(before);
        var remainingRows = layout(result);
        for (int row = 0; row < originalRows.length; row++) {
            int[] expected = new int[originalRows[row].length];
            int size = 0;
            for (int facility : originalRows[row]) {
                if (!removed.contains(facility)) {
                    expected[size++] = facility;
                }
            }
            assertArrayEquals(Arrays.copyOf(expected, size), remainingRows[row]);
        }
        assertEquals(removed, result.getNotAssignedFacilities());
        assertEquals(4 - removed.size(), result.nAssigned());
        assertState(result, false);
        assertUnchanged(before, source);
    }
}
