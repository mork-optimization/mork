package es.urjc.etsii.grafo.patches;

import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Collections;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DisableShufflePatch {

    @Order(1)
    @Test
    public void testCollectionsPatchDisabled(){
        BlockConfig config = new BlockConfig();
        config.setBlockCollectionsShuffle(false);
        var patch = new PatchCollections(config);
        Assertions.assertDoesNotThrow(() -> Collections.shuffle(Arrays.asList(0, 1, 2, 3)));
        patch.patch();
        Assertions.assertDoesNotThrow(() -> Collections.shuffle(Arrays.asList(0, 1, 2, 3)));
    }

    @Order(2)
    @Test
    public void testCollectionsPatchEnabled(){
        BlockConfig config = new BlockConfig();
        config.setBlockCollectionsShuffle(true);
        var patch = new PatchCollections(config);
        Assertions.assertDoesNotThrow(() -> Collections.shuffle(Arrays.asList(0, 1, 2, 3)));
        patch.patch();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> Collections.shuffle(Arrays.asList(0, 1, 2, 3)));
    }
}
