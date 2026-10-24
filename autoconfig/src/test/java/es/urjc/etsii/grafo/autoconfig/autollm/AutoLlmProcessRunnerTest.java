package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmProcessRunnerTest {
    @TempDir Path temp;
    private Process process;
    private ProcessHandle handle;
    private ProcessHandle.Info info;
    private AtomicBoolean alive;

    @BeforeEach
    void setUp() throws Exception {
        process = mock(Process.class);
        handle = mock(ProcessHandle.class);
        info = mock(ProcessHandle.Info.class);
        alive = new AtomicBoolean(true);
        when(process.pid()).thenReturn(123L);
        when(process.info()).thenReturn(info);
        when(info.startInstant()).thenReturn(Optional.empty());
        when(process.getOutputStream()).thenReturn(OutputStream.nullOutputStream());
        when(process.toHandle()).thenReturn(handle);
        when(process.isAlive()).thenAnswer(invocation -> alive.get());
        when(process.waitFor(anyLong(), any(TimeUnit.class))).thenAnswer(invocation -> {
            alive.set(false);
            return true;
        });
        when(handle.descendants()).thenAnswer(invocation -> Stream.empty());
        when(handle.isAlive()).thenAnswer(invocation -> alive.get());
        when(handle.destroy()).thenAnswer(invocation -> {
            alive.set(false);
            return true;
        });
        when(handle.onExit()).thenReturn(CompletableFuture.completedFuture(handle));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7})
    void acceptsExitWhileProcessReaperStillReportsChildAlive(int exitCode) throws Exception {
        // The OS has dropped the process metadata, but its reaper has not published completion yet.
        assertTrue(process.isAlive());
        assertTrue(process.info().startInstant().isEmpty());
        when(process.exitValue()).thenReturn(exitCode);

        var result = run();

        assertEquals(exitCode, result.exitCode());
        assertFalse(result.plannedDeadline());
        var owner = AutoLlmFileUtil.JSON.readTree(Files.readString(temp.resolve("step/owner.json")));
        assertEquals(123L, owner.path("pid").longValue());
        assertEquals("", owner.path("start").stringValue());
        verify(handle, never()).destroy();
    }

    @Test
    void rejectsUnidentifiedChildThatRemainsAlive() throws Exception {
        doReturn(false).when(process).waitFor(anyLong(), any(TimeUnit.class));

        var error = assertThrows(IllegalStateException.class, this::run);

        assertEquals("Cannot establish child process identity", error.getMessage());
        assertFalse(Files.exists(temp.resolve("step/owner.json")));
        verify(handle).destroy();
    }

    @Test
    void recordsAvailableProcessIdentity() throws Exception {
        var start = Instant.parse("2026-10-24T12:00:00Z");
        when(info.startInstant()).thenReturn(Optional.of(start));

        assertEquals(0, run().exitCode());

        var owner = AutoLlmFileUtil.JSON.readTree(Files.readString(temp.resolve("step/owner.json")));
        assertEquals(start.toString(), owner.path("start").stringValue());
        verify(handle, never()).destroy();
    }

    @Test
    void interruptionWhileAwaitingIdentityStopsTheChild() throws Exception {
        doThrow(new InterruptedException()).when(process).waitFor(anyLong(), any(TimeUnit.class));
        try {
            assertThrows(CancellationException.class, this::run);
            assertTrue(Thread.currentThread().isInterrupted());
            verify(handle).destroy();
        } finally {
            Thread.interrupted();
        }
    }

    private AutoLlmProcessRunner.Result run() {
        try (var builders = mockConstruction(ProcessBuilder.class, withSettings().defaultAnswer(RETURNS_SELF),
                (builder, context) -> when(builder.start()).thenReturn(process))) {
            return new AutoLlmProcessRunner().run(List.of("git", "rev-parse", "--verify", "HEAD^{commit}"),
                    temp, temp.resolve("step"), 5000, 0, 50, () -> false, false, 0);
        }
    }
}
