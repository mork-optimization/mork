package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyPermitTest {
    @Test void cancelledWaiterDoesNotNeedTheCurrentHolderToFinish() throws Exception {
        var semaphore = new Semaphore(0); var cancelled = new AtomicBoolean(); var entered = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var waiting = executor.submit(() -> { entered.countDown(); return ConcurrencyUtil.acquirePermit(semaphore, cancelled::get); });
            assertTrue(entered.await(1, TimeUnit.SECONDS)); cancelled.set(true);
            var failure = assertThrows(ExecutionException.class, () -> waiting.get(2, TimeUnit.SECONDS));
            assertInstanceOf(CancellationException.class, failure.getCause()); assertEquals(0, semaphore.availablePermits());
        }
    }
    @Test void publicationFailureAndCancellationCheckFailureBothReleaseThePermit() {
        var semaphore = new Semaphore(1);
        assertThrows(IllegalStateException.class, () -> {
            try (var permit = ConcurrencyUtil.acquirePermit(semaphore, () -> false)) { throw new IllegalStateException("checkpoint failed"); }
        });
        assertEquals(1, semaphore.availablePermits());
        var calls = new AtomicInteger();
        assertThrows(IllegalStateException.class, () -> ConcurrencyUtil.acquirePermit(semaphore, () -> {
            if (calls.incrementAndGet() == 2) throw new IllegalStateException("cancel check failed"); return false;
        }));
        assertEquals(1, semaphore.availablePermits());
    }
}
