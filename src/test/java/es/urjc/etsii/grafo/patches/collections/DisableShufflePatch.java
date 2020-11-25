package es.urjc.etsii.grafo.patches.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;


public class DisableShufflePatch {
    @Test
    public void testCollectionsPatch(){
        var patch = new PatchCollections();
        Assertions.assertDoesNotThrow(() -> Collections.shuffle(Arrays.asList(0, 1, 2, 3)));
        patch.patch();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> Collections.shuffle(Arrays.asList(0, 1, 2, 3)));
    }
}
