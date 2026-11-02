package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

/** Durable host-wide reservations. Timeouts never confer ownership. Provision once per host. */
public final class WorkerRegistry {
    public record Lease(String workerId, String controller, String operation, String assignment, String token, long generation) {}
    public record ProcessIdentity(long pid, String start) {
        public static ProcessIdentity of(ProcessHandle p) { return new ProcessIdentity(p.pid(), p.info().startInstant().orElseThrow().toString()); }
        public boolean alive() {
            var process = ProcessHandle.of(pid);
            if (process.isEmpty() || !process.get().isAlive()) return false;
            return process.get().info().startInstant().map(i -> i.toString().equals(start)).orElse(true);
        }
    }
    public record Activity(String root, String id, String state, ProcessIdentity process) {}
    public record Status(String workerId, long generation, Lease lease, Map<String,Activity> jobs, String observedAt) {}
    public static final class Busy extends IllegalStateException {
        public final Status owner;
        public Busy(Status owner) { super("Worker reserved by " + owner.lease()); this.owner = owner; }
    }
    private static final Map<Path,java.util.concurrent.locks.ReentrantLock> LOCKS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Set<Path> VERIFIED_LOCKS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Path root;
    public WorkerRegistry(Path root) { this.root = root.toAbsolutePath().normalize(); }
    public Path directory() { return root; }
    public static WorkerRegistry system() {
        // Only tests set this JVM property; no campaign configuration may relocate the registry.
        String test = System.getProperty("mork.test.workerRegistry");
        if (test != null) return new WorkerRegistry(Path.of(test));
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return new WorkerRegistry(os.contains("win") ? Path.of(System.getenv().getOrDefault("ProgramData", "C:\\ProgramData"), "Mork", "worker")
                : os.contains("mac") ? Path.of("/Library/Application Support/Mork/worker") : Path.of("/var/lib/mork/worker"));
    }
    /** Explicit administrator provisioning; callers arrange shared account permissions. */
    public synchronized Status provision() {
        try { Files.createDirectories(root); }
        catch (IOException e) { throw new UncheckedIOException(e); }
        var status = transaction(true, state -> state); verifyLocks(); return status;
    }
    private synchronized <T> T transaction(boolean initialize, Function<Status,T> operation) {
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(root))
            throw new IllegalStateException("Provision the shared worker registry and grant participating accounts access: " + root);
        var mutex = LOCKS.computeIfAbsent(root, ignored -> new java.util.concurrent.locks.ReentrantLock()); mutex.lock();
        try (var channel = FileChannel.open(root.resolve("registry.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             var lock = channel.lock()) {
            Path file = root.resolve("registry.json");
            if (!Files.exists(file)) {
                if (!initialize) throw new IllegalStateException("Worker registry is not provisioned: " + root);
                save(new Status(UUID.randomUUID().toString(), 0, null, Map.of(), Instant.now().toString()));
            }
            var state = AutoLlmFileUtil.read(file, Status.class);
            if (state.lease() != null && Files.isRegularFile(root.resolve("released-" + UUID.fromString(state.lease().token())))) {
                state = new Status(state.workerId(), state.generation(), null, Map.of(), Instant.now().toString()); save(state);
            }
            return operation.apply(state);
        } catch (IOException e) { throw new UncheckedIOException("Shared worker registry unavailable: " + root, e); }
        finally { mutex.unlock(); }
    }
    private void save(Status state) { durable(root.resolve("registry.json"), state); }
    static void durable(Path file, Object value) {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            try (var channel = FileChannel.open(temporary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                var bytes = java.nio.ByteBuffer.wrap(AutoLlmFileUtil.JSON.writeValueAsBytes(value));
                while (bytes.hasRemaining()) channel.write(bytes); channel.force(true);
            }
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    /** Verify exclusion in another JVM, rather than relying on the JVM's overlapping-lock table. */
    public void verifyLocks() {
        if (VERIFIED_LOCKS.contains(root)) return;
        transaction(false, state -> {
            if (VERIFIED_LOCKS.contains(root)) return null;
            String resource = WorkerLockProbe.class.getName().replace('.', '/') + ".class";
            Path classes = root.resolve("tools/lock-probe"), target = classes.resolve(resource);
            Process process = null;
            try {
                Files.createDirectories(target.getParent());
                try (var input = WorkerLockProbe.class.getClassLoader().getResourceAsStream(resource)) {
                    Files.copy(Objects.requireNonNull(input), target, StandardCopyOption.REPLACE_EXISTING);
                }
                process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-cp", classes.toString(),
                        WorkerLockProbe.class.getName(), root.resolve("registry.lock").toString())
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
                process.getOutputStream().close();
                if (!process.waitFor(20, java.util.concurrent.TimeUnit.SECONDS) || process.exitValue() != 0)
                    throw new IllegalStateException("Shared worker registry failed cross-process filesystem lock verification: " + root);
                VERIFIED_LOCKS.add(root); return null;
            } catch (IOException e) { throw new UncheckedIOException(e); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new java.util.concurrent.CancellationException("Lock verification interrupted"); }
            finally { if (process != null && process.isAlive()) process.destroyForcibly(); }
        });
    }
    public Status status() { return transaction(false, s -> new Status(s.workerId(), s.generation(), s.lease(), s.jobs(), Instant.now().toString())); }
    public Lease acquire(Lease request) {
        verifyLocks();
        return transaction(false, s -> {
            if (s.lease() != null) {
                if (s.lease().token().equals(request.token()) && s.lease().controller().equals(request.controller())
                        && s.lease().operation().equals(request.operation()) && Objects.equals(s.lease().assignment(), request.assignment())) return s.lease();
                throw new Busy(s);
            }
            if (Files.exists(root.resolve("released-" + UUID.fromString(request.token()))) || request.generation() != 0) throw new IllegalStateException("Released lease cannot be reacquired");
            var lease = new Lease(s.workerId(), request.controller(), request.operation(), request.assignment(), request.token(), s.generation() + 1);
            save(new Status(s.workerId(), lease.generation(), lease, Map.of(), Instant.now().toString())); return lease;
        });
    }
    private static void require(Status s, Lease lease) {
        if (lease == null || !lease.equals(s.lease())) throw new IllegalStateException("Stale or released worker lease");
    }
    public void validate(Lease lease) { transaction(false, s -> { require(s, lease); return null; }); }
    /** Hold the registry transaction across spawn to prevent release racing submission. */
    public ProcessIdentity submit(Lease lease, String root, String id, Function<Activity,ProcessIdentity> spawn) {
        return transaction(false, s -> {
            require(s, lease);
            var jobs = new LinkedHashMap<>(s.jobs());
            String key = root + ":" + id;
            Activity previous = jobs.get(key);
            if (previous == null) {
                jobs.put(key, new Activity(root, id, "INTENT", null));
                save(new Status(s.workerId(), s.generation(), s.lease(), jobs, Instant.now().toString()));
            }
            var process = spawn.apply(previous);
            if (process != null) {
                jobs.put(key, new Activity(root, id, "RUNNING", process));
                save(new Status(s.workerId(), s.generation(), s.lease(), jobs, Instant.now().toString()));
            }
            return process;
        });
    }
    public void started(Lease lease, String executionRoot, String id, ProcessIdentity process) {
        update(lease, executionRoot, id, "RUNNING", process);
    }
    public void finishPreflight(Lease lease, String executionRoot, String id) {
        es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmProcessRunner.requireTerminated(Path.of(executionRoot).resolve(".preflight"));
        AutoLlmFileUtil.write(Path.of(executionRoot).resolve(".preflight/" + id + ".completed.json"), lease);
        complete(lease, executionRoot, id);
    }
    public void complete(Lease lease, String executionRoot, String id) { update(lease, executionRoot, id, "COMPLETE", null); }
    private void update(Lease lease, String executionRoot, String id, String phase, ProcessIdentity process) {
        transaction(false, s -> {
            require(s, lease); var jobs = new LinkedHashMap<>(s.jobs()); String key = executionRoot + ":" + id;
            if (!jobs.containsKey(key)) throw new IllegalStateException("Job has no durable submission intent");
            jobs.put(key, new Activity(executionRoot, id, phase, process));
            save(new Status(s.workerId(), s.generation(), s.lease(), jobs, Instant.now().toString())); return null;
        });
    }
    public boolean released(Lease lease) {
        return transaction(false, s -> s.workerId().equals(lease.workerId()) && Files.isRegularFile(root.resolve("released-" + UUID.fromString(lease.token()))));
    }
    public void release(Lease lease) {
        transaction(false, s -> {
            Path released = root.resolve("released-" + UUID.fromString(lease.token()));
            if (Files.isRegularFile(released)) return null;
            if (s.lease() == null && s.generation() == lease.generation()) return null;
            require(s, lease);
            for (var job : s.jobs().values()) if (!job.state().equals("COMPLETE")) {
                Path proof = Path.of(job.root()).resolve(".preflight/" + job.id() + ".completed.json");
                if (!job.id().startsWith("preflight-") || !Files.isRegularFile(proof) || !lease.equals(AutoLlmFileUtil.read(proof, Lease.class)))
                    throw new IllegalStateException("Unreconciled job blocks release (heartbeat expiry is insufficient): " + job);
                es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmProcessRunner.requireTerminated(Path.of(job.root()).resolve(".preflight"));
            }
            durable(root.resolve("released-" + UUID.fromString(lease.token())), Map.of("generation", lease.generation()));
            save(new Status(s.workerId(), s.generation(), null, Map.of(), Instant.now().toString())); return null;
        });
    }
}
