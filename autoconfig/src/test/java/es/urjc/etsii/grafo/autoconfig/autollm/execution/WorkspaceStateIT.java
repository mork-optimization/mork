package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkspaceStateIT {
    @TempDir Path temporary;

    public static class Holder {
        public static void main(String[] args) throws Exception {
            Path workspace = Path.of(args[0]), root = workspace.resolve("campaigns/fixture");
            Files.createDirectories(root);
            try (var execution = WorkspaceState.execute(workspace, root, () -> false, () -> {})) {
                if (args[1].equals("crash")) AutoLlmFileUtil.write(root.resolve("process-intent.json"), Map.of("submission", "unknown-child"));
                Files.writeString(workspace.resolve("ready"), "ready");
                while (!Files.exists(workspace.resolve("finish"))) Thread.sleep(25);
            }
        }
    }
    private Process holder(Path workspace, String mode) throws Exception {
        Files.createDirectories(workspace);
        return new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-cp", System.getProperty("java.class.path"),
                Holder.class.getName(), workspace.toString(), mode).redirectErrorStream(true).redirectOutput(workspace.resolve("holder.log").toFile()).start();
    }
    private void ready(Path workspace, Process child) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (child.isAlive() && !Files.exists(workspace.resolve("ready")) && System.nanoTime() < deadline) Thread.sleep(25);
        assertTrue(Files.exists(workspace.resolve("ready")), Files.readString(workspace.resolve("holder.log")));
    }
    @Test void processesShareOnlyTheConfiguredWorkspaceLock() throws Exception {
        Path a = temporary.resolve("a"), b = temporary.resolve("b");
        Process first = holder(a, "normal"), second = holder(b, "normal");
        try {
            ready(a, first); ready(b, second);
            assertNotEquals(WorkspaceState.identity(a), WorkspaceState.identity(b));
            assertEquals(WorkspaceState.identity(a), WorkspaceState.identity(a.resolve("../a")));
            var cancelled = new java.util.concurrent.atomic.AtomicBoolean();
            assertThrows(CancellationException.class, () -> WorkspaceState.acquire(a.resolve("../a"), cancelled::get, () -> cancelled.set(true)));
            Files.writeString(a.resolve("finish"), "done"); assertTrue(first.waitFor(10, TimeUnit.SECONDS)); assertEquals(0, first.exitValue());
            try (var lock = WorkspaceState.acquire(a, () -> false, () -> {})) { assertTrue(lock.lock().isValid()); }
            assertTrue(second.isAlive()); // Another workspace's workload is independent.
        } finally { first.destroyForcibly(); second.destroyForcibly(); first.waitFor(); second.waitFor(); }
    }
    @Test void crashedSupervisorCannotHideAnUncertainSubmission() throws Exception {
        Path workspace = temporary.resolve("worker"); Process child = holder(workspace, "crash");
        try {
            ready(workspace, child); child.destroyForcibly(); assertTrue(child.waitFor(10, TimeUnit.SECONDS));
            Path next = workspace.resolve("campaigns/next"); Files.createDirectories(next);
            assertThrows(IllegalStateException.class, () -> WorkspaceState.execute(workspace, next, () -> false, () -> {}));
            assertTrue(Files.exists(workspace.resolve(".mork/active-execution.json")));
            AutoLlmFileUtil.write(workspace.resolve("campaigns/fixture/process-terminated.json"), Map.of("submission", "unknown-child", "notStarted", true));
            try (var execution = WorkspaceState.execute(workspace, next, () -> false, () -> {})) { assertTrue(execution.lock().lock().isValid()); }
            assertFalse(Files.exists(workspace.resolve(".mork/active-execution.json")));
        } finally { child.destroyForcibly(); child.waitFor(); }
    }
    @Test void replacedExecutionBoundaryBlocksRecovery() throws Exception {
        Path workspace = temporary.resolve("worker"); Process child = holder(workspace, "normal");
        try {
            ready(workspace, child); child.destroyForcibly(); child.waitFor();
            Path original = workspace.resolve("campaigns/fixture"); Files.move(original, original.resolveSibling("saved")); Files.createDirectory(original);
            assertThrows(IllegalStateException.class, () -> WorkspaceState.execute(workspace, original, () -> false, () -> {}));
        } finally { child.destroyForcibly(); child.waitFor(); }
    }
}
