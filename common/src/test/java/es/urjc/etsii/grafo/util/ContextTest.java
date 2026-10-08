package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {

    @BeforeEach
    void resetContext() {
        Context.reset();
    }

    @Test
    void suspendObjectiveTracking() {
        assertFalse(Context.isObjectiveTrackingSuspended());

        try (var ignored = Context.suspendObjectiveTracking()) {
            assertTrue(Context.isObjectiveTrackingSuspended());
        }

        assertFalse(Context.isObjectiveTrackingSuspended());
    }

    @Test
    void nestedObjectiveTrackingSuspensions() {
        try (var outer = Context.suspendObjectiveTracking()) {
            assertTrue(Context.isObjectiveTrackingSuspended());
            try (var inner = Context.suspendObjectiveTracking()) {
                assertTrue(Context.isObjectiveTrackingSuspended());
            }
            assertTrue(Context.isObjectiveTrackingSuspended());
        }

        assertFalse(Context.isObjectiveTrackingSuspended());
    }

    @Test
    void suspensionClosesAfterException() {
        assertThrows(IllegalStateException.class, () -> {
            try (var ignored = Context.suspendObjectiveTracking()) {
                throw new IllegalStateException("Expected test exception");
            }
        });

        assertFalse(Context.isObjectiveTrackingSuspended());
    }

    @Test
    void closingScopeTwiceIsHarmless() {
        var scope = Context.suspendObjectiveTracking();
        scope.close();
        assertDoesNotThrow(scope::close);
        assertFalse(Context.isObjectiveTrackingSuspended());
    }

    @Test
    void scopeMustBeClosedByItsOwner() throws InterruptedException {
        var scope = Context.suspendObjectiveTracking();
        var failure = new AtomicReference<Throwable>();
        var thread = new Thread(() -> {
            try {
                scope.close();
            } catch (Throwable e) {
                failure.set(e);
            }
        });

        thread.start();
        thread.join();

        assertInstanceOf(IllegalStateException.class, failure.get());
        assertTrue(Context.isObjectiveTrackingSuspended());
        scope.close();
        assertFalse(Context.isObjectiveTrackingSuspended());
    }

    @Test
    void suspensionIsNotInheritedByChildThreads() throws InterruptedException {
        var childSuspended = new AtomicBoolean(true);

        try (var ignored = Context.suspendObjectiveTracking()) {
            var thread = new Thread(() -> childSuspended.set(Context.isObjectiveTrackingSuspended()));
            thread.start();
            thread.join();

            assertTrue(Context.isObjectiveTrackingSuspended());
        }

        assertFalse(childSuspended.get());
    }

    @Test
    void resetStartsAnUnsuspendedContext() {
        var oldScope = Context.suspendObjectiveTracking();
        Context.reset();

        assertFalse(Context.isObjectiveTrackingSuspended());
        assertDoesNotThrow(oldScope::close);
        assertFalse(Context.isObjectiveTrackingSuspended());
    }
}
