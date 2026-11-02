package es.urjc.etsii.grafo.util.random;

import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ParkMillerRandomTest {
    @AfterEach
    void cleanup() {
        Context.reset();
    }

    @Test
    void matchesTheNativeParkMillerSequenceAndConsumesBoundOne() {
        var random = new ParkMillerRandom(1);
        for (long state : new long[]{16807, 282475249, 1622650073, 984943658, 1144108930}) {
            assertEquals(state * (1.0 / 2147483647), random.nextDouble());
            assertEquals(state, random.getState());
        }
        assertEquals(0, random.nextInt(1));
        assertEquals(470211272, random.getState());
        assertThrows(IllegalArgumentException.class, () -> random.nextInt(0));
    }

    @Test
    void validatesSeedsAndSupportsIndependentCopiesAndJumps() {
        assertThrows(IllegalArgumentException.class, () -> new ParkMillerRandom(0));
        assertThrows(IllegalArgumentException.class, () -> new ParkMillerRandom(2147483647));
        var random = new ParkMillerRandom(1234);
        random.nextInt(80);
        var expected = random.copy();
        var saved = random.copyAndJump();
        assertEquals(expected.nextLong(), saved.nextLong());
        var iterated = new ParkMillerRandom(1234);
        iterated.nextInt(80);
        for (int i = 0; i < random.jumpDistance(); i++) iterated.nextDouble();
        assertEquals(iterated.getState(), random.getState());
    }

    @Test
    void frameworkSeedingAndChildThreadsWorkWithTheNewProvider() throws InterruptedException {
        Context.reset();
        Context.Configurator.resetRandom(RandomType.PARK_MILLER, 1234);
        var parent = (ParkMillerRandom) Context.getRandom();
        var expectedChild = parent.copy();
        var expectedParent = parent.copy();
        expectedParent.jump();
        var observedChild = new AtomicReference<ParkMillerRandom>();
        var thread = new Thread(() -> observedChild.set((ParkMillerRandom) Context.getRandom()));
        thread.start();
        thread.join();
        assertEquals(expectedChild.getState(), observedChild.get().getState());
        assertEquals(expectedParent.getState(), parent.getState());
        Context.Configurator.resetRandom(RandomType.PARK_MILLER, 1234);
        assertEquals(1234, ((ParkMillerRandom) Context.getRandom()).getState());
    }
}
