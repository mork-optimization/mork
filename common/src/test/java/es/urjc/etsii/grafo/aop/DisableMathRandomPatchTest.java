package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.exception.InvalidRandomException;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DisableMathRandomPatchTest {

    public static void setup(boolean blockMathRandom, boolean blockShuffle){
        var blockConfig = new BlockConfig();
        blockConfig.setBlockMathRandom(blockMathRandom);
        blockConfig.setBlockCollectionsShuffle(blockShuffle);
        Context.Configurator.setBlockConfig(blockConfig);
    }

    @AfterAll
    static void clean(){
        Context.Configurator.setBlockConfig(null);
    }

    @Order(1)
    @Test
    void testMathRandomPatchDisabled(){
        setup(false, false);
        Assertions.assertDoesNotThrow(() -> Math.random());
        setup(false, true);
        Assertions.assertDoesNotThrow(() -> Math.random());
    }

    @Order(2)
    @Test
    void testMathRandomPatchEnabled(){
        setup(true, false);
        Assertions.assertThrows(InvalidRandomException.class, () -> Math.random());
        setup(true, true);
        Assertions.assertThrows(InvalidRandomException.class, () -> Math.random());
    }
}
