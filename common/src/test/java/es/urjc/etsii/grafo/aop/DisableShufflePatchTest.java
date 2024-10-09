package es.urjc.etsii.grafo.aop;

import es.urjc.etsii.grafo.exception.InvalidRandomException;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static es.urjc.etsii.grafo.aop.DisableMathRandomPatchTest.setup;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DisableShufflePatchTest {
    @AfterAll
    static void clean(){
        Context.Configurator.setBlockConfig(null);
    }

    @Order(1)
    @Test
    public void testCollectionsPatchDisabled(){
        setup(false, false);
        Assertions.assertDoesNotThrow(() -> Collections.shuffle(Arrays.asList(0, 1, 2, 3)));
        setup(true, false);
        Assertions.assertDoesNotThrow(() -> Collections.shuffle(Arrays.asList(0, 1, 2, 3)));
    }

    @Order(2)
    @Test
    public void testCollectionsPatchEnabled(){
        var list = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
        setup(false, true);
        Assertions.assertThrows(InvalidRandomException.class, () -> Collections.shuffle(list));
        setup(true, true);
        Assertions.assertThrows(InvalidRandomException.class, () -> Collections.shuffle(list));
    }
}
