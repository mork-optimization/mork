package es.urjc.etsii.grafo.patches;

import es.urjc.etsii.grafo.exception.InvalidRandomException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DisableShufflePatchTest {

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
        var list = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));

        Assertions.assertDoesNotThrow(() -> Collections.shuffle(list));
        patch.patch();
        Assertions.assertThrows(InvalidRandomException.class, () -> Collections.shuffle(list));
    }
}
