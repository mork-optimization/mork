package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyUtilTest {

    @Test
    void completedNullResultReturnsEmptyOptional() {
        var handledException = new AtomicReference<Exception>();

        var result = ConcurrencyUtil.await(
                CompletableFuture.completedFuture(null),
                handledException::set
        );

        assertTrue(result.isEmpty());
        assertNull(handledException.get());
    }

    @Test
    void failedFutureInvokesExceptionHandler() {
        var cause = new IllegalStateException();
        var handledException = new AtomicReference<Exception>();

        var result = ConcurrencyUtil.await(
                CompletableFuture.failedFuture(cause),
                handledException::set
        );

        assertTrue(result.isEmpty());
        var executionException = assertInstanceOf(ExecutionException.class, handledException.get());
        assertSame(cause, executionException.getCause());
    }
}
