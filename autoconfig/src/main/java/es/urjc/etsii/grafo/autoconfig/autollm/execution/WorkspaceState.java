package es.urjc.etsii.grafo.autoconfig.autollm.execution;
import es.urjc.etsii.grafo.autoconfig.autollm.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.io.*;
import java.util.*;
import java.util.function.BooleanSupplier;

public final class WorkspaceState {
    private WorkspaceState() {}
    public static Path directory(Path workspace) {
        Path path = AutoLlmNormalization.localPath(workspace).resolve(".mork");
        try { Files.createDirectories(path); return path; } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static synchronized String identity(Path workspace) {
        Path root = directory(workspace);
        try (var channel = FileChannel.open(root.resolve("identity.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.lock()) {
            Path file = root.resolve("identity.json");
            if (!Files.exists(file)) AutoLlmFileUtil.write(file, Map.of("id", UUID.randomUUID().toString()));
            return AutoLlmFileUtil.JSON.readTree(Files.readString(file)).path("id").asString();
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public record Lock(FileChannel channel, FileLock lock) implements AutoCloseable {
        public void close() throws IOException { try { lock.release(); } finally { channel.close(); } }
    }
    /** Held only during execution. A stale activity must prove all submissions terminated. */
    public record Execution(Lock lock, Path activity, Path root) implements AutoCloseable {
        public void close() throws IOException {
            try { AutoLlmProcessRunner.requireTerminated(root); Files.deleteIfExists(activity); }
            finally { lock.close(); }
        }
    }
    public static Execution execute(Path workspace, Path root, BooleanSupplier cancelled, Runnable waiting) throws IOException {
        Lock lock = acquire(workspace, cancelled, waiting);
        try {
            Path activity = directory(workspace).resolve("active-execution.json");
            reconcileActivity(workspace);
            root = AutoLlmNormalization.localPath(root);
            AutoLlmFileUtil.write(activity, Map.of("root", root.toString(), "fileKey", fileKey(root)));
            return new Execution(lock, activity, root);
        } catch (RuntimeException | IOException failure) { lock.close(); throw failure; }
    }
    /** Caller must hold execution.lock, including when removing an owned execution directory. */
    public static void reconcileActivity(Path workspace) throws IOException {
        Path activity = directory(workspace).resolve("active-execution.json");
        if (!Files.isRegularFile(activity)) return;
        var saved = AutoLlmFileUtil.JSON.readTree(Files.readString(activity));
        Path root = Path.of(saved.path("root").asString());
        if (!Files.isDirectory(root) || !fileKey(root).equals(saved.path("fileKey").asString()))
            throw new IllegalStateException("Workspace execution boundary changed: " + root);
        AutoLlmProcessRunner.requireTerminated(root);
        Files.delete(activity);
    }
    private static String fileKey(Path root) throws IOException {
        Object key = Files.readAttributes(root, java.nio.file.attribute.BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).fileKey();
        if (key == null) throw new IOException("Filesystem does not provide process evidence directory identity: " + root);
        return key.toString();
    }
    public static Lock acquire(Path workspace, BooleanSupplier cancelled, Runnable waiting) throws IOException {
        Files.createDirectories(workspace);
        var channel = FileChannel.open(workspace.resolve("execution.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            while (true) {
                if (cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) throw new java.util.concurrent.CancellationException();
                FileLock lock = null;
                try { lock = channel.tryLock(); } catch (OverlappingFileLockException ignored) { }
                if (lock != null) return new Lock(channel, lock);
                waiting.run();
                try { Thread.sleep(250); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new java.util.concurrent.CancellationException(); }
            }
        } catch (RuntimeException | IOException ex) { channel.close(); throw ex; }
    }
}
