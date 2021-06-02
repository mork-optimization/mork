package es.urjc.etsii.grafo.patches;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DisableMathRandomPatch {

    @Test
    public void testMathRandomPatch(){
        var patch = new PatchMathRandom(true);
        Assertions.assertDoesNotThrow(() -> Math.random());
        patch.patch();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> Math.random());
    }
}
