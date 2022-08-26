package es.urjc.etsii.grafo.patches;

import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.exception.InvalidRandomException;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DisableMathRandomPatchTest {

    @Order(1)
    @Test
    public void testMathRandomPatchDisabled(){
        BlockConfig config = new BlockConfig();
        config.setBlockMathRandom(false);
        var patch = new PatchMathRandom(config);
        Assertions.assertDoesNotThrow(Math::random);
        patch.patch();
        Assertions.assertDoesNotThrow(Math::random);
    }

    @Order(2)
    @Test
    public void testMathRandomPatchEnabled(){
        BlockConfig config = new BlockConfig();
        config.setBlockMathRandom(true);
        var patch = new PatchMathRandom(config);
        Assertions.assertDoesNotThrow(Math::random);
        patch.patch();
        Assertions.assertThrows(InvalidRandomException.class, Math::random);
    }
}
