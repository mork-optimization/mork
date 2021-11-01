package es.urjc.etsii.grafo.patches;

import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DisableMathRandomPatch {

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
        Assertions.assertThrows(UnsupportedOperationException.class, Math::random);
    }
}
