package es.urjc.etsii.grafo.autoconfig.autollm;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/** One owned process tree per stage; no asynchronous Java-thread termination. */
@Service
public class AutoLlmProcessRunner {
    public record Result(int exitCode, boolean plannedDeadline, long elapsedMillis) {}
    public Result run(List<String> command, Path cwd, Path output, long timeoutMillis, long plannedMillis,
                      long graceMillis, BooleanSupplier cancelled, boolean watchEvaluations, long startupMillis) {
        try {
            Files.createDirectories(output);
            requireTerminated(output);
            Files.deleteIfExists(output.resolve("owner.json"));
            Files.deleteIfExists(output.resolve("process-terminated.json"));
            Files.deleteIfExists(output.resolve("process-children.json"));
            String submission = UUID.randomUUID().toString();
            AutoLlmFileUtil.write(output.resolve("process-intent.json"), Map.of("submission", submission));
            Process process;
            try {
                process = new ProcessBuilder(command).directory(cwd.toFile())
                        .redirectOutput(output.resolve("stdout.log").toFile()).redirectError(output.resolve("stderr.log").toFile()).start();
            } catch (IOException failure) {
                AutoLlmFileUtil.write(output.resolve("process-terminated.json"), Map.of("submission", submission, "notStarted", true));
                throw failure;
            }
            long begin = System.currentTimeMillis();
            boolean planned = false;
            var children = new LinkedHashMap<Long,ProcessHandle>();
            try {
                process.getOutputStream().close();
                var start = process.info().startInstant().map(Instant::toString).orElse("");
                // A fast child can lose its OS metadata before the process reaper reports its exit.
                if (start.isEmpty() && !process.waitFor(100, TimeUnit.MILLISECONDS)) throw new IllegalStateException("Cannot establish child process identity");
                AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", process.pid(), "start", start, "command", command,
                        "executionTarget", Boolean.getBoolean("mork.execution.remote") ? "SSH" : "LOCAL"));
                while (!process.waitFor(100, TimeUnit.MILLISECONDS)) {
                    boolean discovered = false;
                    for (var child : process.descendants().toList()) if (children.putIfAbsent(child.pid(), child) == null) discovered = true;
                    if (discovered) {
                        var identities = new ArrayList<Map<String,Object>>();
                        for (var child : children.values()) identities.add(Map.of("pid", child.pid(), "start", child.info().startInstant().map(Instant::toString).orElse("")));
                        AutoLlmFileUtil.write(output.resolve("process-children.json"), Map.of("submission", submission, "children", identities));
                    }
                    if (Files.exists(output.resolve("codex-launch.json"))) CodexProgressUtil.refresh(output);
                    if (cancelled.getAsBoolean() || AutoLlmCleanup.requested(output) || Thread.currentThread().isInterrupted()) throw new CancellationException("Campaign cancelled");
                    long now = System.currentTimeMillis();
                    if (now - begin > timeoutMillis) throw new IllegalStateException("Stage exceeded its hard timeout");
                    if (watchEvaluations) checkDeadlines(output.resolve("execution/active.json"), now, graceMillis);
                    Path ready = output.resolve("execution/readiness.json");
                    if (startupMillis > 0 && !Files.exists(ready) && now - begin > startupMillis) throw AutoLlmFailureUtil.error(
                            AutoLlmFailureUtil.Category.INFRASTRUCTURE, "STARTUP_TIMEOUT", "Child did not become ready before startup timeout", output);
                    if (plannedMillis > 0 && Files.exists(ready)) {
                        long readyAt = AutoLlmFileUtil.JSON.readTree(Files.readString(ready)).path("startedAtMillis").longValue();
                        if (!planned && now - readyAt >= plannedMillis) {
                            Files.writeString(output.resolve("execution/stop"), "pilot deadline");
                            planned = true;
                        }
                        if (planned && now - readyAt >= plannedMillis + graceMillis && !hasActive(output)) {
                            stop(process.toHandle());
                            return new Result(-1, true, now - begin);
                        }
                    }
                }
                if (Files.exists(output.resolve("codex-launch.json"))) CodexProgressUtil.refresh(output);
                AutoLlmFileUtil.unfinished(output, "FAILED", "Child exited with unfinished evaluations");
                if (process.exitValue() != 0) BuildDiagnostics.capture(cwd, output);
                return new Result(process.exitValue(), planned, System.currentTimeMillis() - begin);
            } catch (RuntimeException | InterruptedException failure) {
                try { AutoLlmFileUtil.unfinished(output, failure instanceof CancellationException || failure instanceof InterruptedException ? "INTERRUPTED" : "FAILED", failure.toString()); }
                catch (RuntimeException recordingFailure) { failure.addSuppressed(recordingFailure); }
                throw failure;
            } finally {
                boolean interrupted = Thread.interrupted();
                try {
                    stopTree(process.toHandle(), children.values());
                    if (process.isAlive()) throw new IllegalStateException("Process termination is unconfirmed");
                    for (var child : children.values()) if (child.isAlive()) throw new IllegalStateException("Child termination is unconfirmed");
                    AutoLlmFileUtil.write(output.resolve("process-terminated.json"), Map.of("pid", process.pid(), "submission", submission, "confirmedAt", Instant.now().toString()));
                } finally { if (interrupted) Thread.currentThread().interrupt(); }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Campaign interrupted"); }
    }
    private boolean hasActive(Path output) {
        Path file = output.resolve("execution/active.json");
        try { return Files.exists(file) && !AutoLlmFileUtil.JSON.readTree(Files.readString(file)).isEmpty(); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void checkDeadlines(Path file, long now, long grace) throws IOException {
        if (!Files.exists(file)) return;
        for (var entry : AutoLlmFileUtil.JSON.readTree(Files.readString(file)).properties()) {
            if (now > entry.getValue().path("deadline").longValue() + grace) {
                String phase = entry.getValue().path("phase").asString();
                throw AutoLlmFailureUtil.error(phase.equals("RUNNING") ? AutoLlmFailureUtil.Category.COMPONENT : AutoLlmFailureUtil.Category.UNKNOWN,
                        "EVALUATION_TIMEOUT", "Hung evaluation " + entry.getKey() + " in phase " + phase, file);
            }
        }
    }
    /** A dead parent alone does not establish that its children stopped. */
    public static void requireTerminated(Path root) {
        boolean interrupted = Thread.interrupted();
        try { verifyTerminated(root); }
        finally { if (interrupted) Thread.currentThread().interrupt(); }
    }
    private static void verifyTerminated(Path root) {
        for (Path intent : AutoLlmFileUtil.ownedFiles(root, "process-intent.json")) {
            if (AutoLlmLocalRecovery.operatorConfirmed(intent.getParent())) continue;
            Path proof = intent.resolveSibling("process-terminated.json");
            String submission = Objects.toString(AutoLlmFileUtil.read(intent, Map.class).get("submission"), "");
            if (submission.isEmpty() || !Files.isRegularFile(proof)
                    || !submission.equals(AutoLlmFileUtil.read(proof, Map.class).get("submission")))
                throw new IllegalStateException("Process submission is unreconciled: " + intent);
        }
        for (Path file : AutoLlmFileUtil.ownedFiles(root, "owner.json")) {
            if (AutoLlmLocalRecovery.operatorConfirmed(file.getParent())) continue;
            var owner = AutoLlmFileUtil.read(file, Map.class);
            if (!(owner.get("pid") instanceof Number pid)) continue;
            if (ProcessHandle.of(pid.longValue()).filter(ProcessHandle::isAlive).isPresent())
                throw new IllegalStateException("Live or reused process blocks completion: " + file);
            Path proof = file.resolveSibling("process-terminated.json");
            if (!Files.isRegularFile(proof) || AutoLlmFileUtil.read(proof, Map.class).get("pid") == null
                    || ((Number) AutoLlmFileUtil.read(proof, Map.class).get("pid")).longValue() != pid.longValue())
                throw new IllegalStateException("Process-tree termination is unconfirmed: " + file);
        }
    }
    public void reconcile(Path campaign) {
        if (!Files.isDirectory(campaign)) return;
        try (var files = Files.walk(campaign)) {
            for (Path file : files.toList()) if (file.getFileName().toString().equals("owner.json") && !campaign.relativize(file).toString().replace('\\', '/').contains("/source/")) {
                var record = AutoLlmFileUtil.JSON.readTree(Files.readString(file));
                if ("SSH".equals(record.path("executionTarget").asString()) && !Boolean.getBoolean("mork.execution.remote")) continue;
                ProcessHandle.of(record.path("pid").longValue()).ifPresent(process -> {
                    String start = record.path("start").stringValue();
                    if (process.isAlive() && !start.isBlank() && process.info().startInstant().map(Instant::toString).orElse("").equals(start)) stop(process);
                });
                AutoLlmFileUtil.unfinished(file.getParent(), "INTERRUPTED", "Parent interrupted before stage completion");
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void stop(ProcessHandle process) {
        stopTree(process, List.of());
    }
    private void stopTree(ProcessHandle process, Collection<ProcessHandle> known) {
        boolean interrupted = Thread.interrupted();
        var tree = new LinkedHashMap<Long,ProcessHandle>();
        for (var child : known) tree.put(child.pid(), child);
        try { for (var child : process.descendants().toList()) tree.put(child.pid(), child); }
        catch (RuntimeException ignored) { /* Restricted hosts can deny process-tree enumeration. Still stop the owned process. */ }
        tree.put(process.pid(), process);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        try {
            for (var child : tree.values()) if (child.isAlive()) child.destroy();
            awaitTree(tree.values(), Math.min(deadline, System.nanoTime() + TimeUnit.SECONDS.toNanos(2)));
            for (var child : tree.values()) if (child.isAlive()) child.destroyForcibly();
            awaitTree(tree.values(), deadline);
            for (var child : tree.values()) if (child.isAlive()) throw new IllegalStateException("Process-tree termination is unconfirmed: " + child.pid());
        } finally { if (interrupted) Thread.currentThread().interrupt(); }
    }
    private void awaitTree(Collection<ProcessHandle> tree, long deadline) {
        boolean interrupted = false;
        try {
            for (var child : tree) while (child.isAlive() && System.nanoTime() < deadline) {
                try { child.onExit().get(Math.max(1, deadline - System.nanoTime()), TimeUnit.NANOSECONDS); }
                catch (InterruptedException e) { interrupted = true; }
                catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) { break; }
            }
        } finally { if (interrupted) Thread.currentThread().interrupt(); }
    }
}
