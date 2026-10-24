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
            var process = new ProcessBuilder(command).directory(cwd.toFile())
                    .redirectOutput(output.resolve("stdout.log").toFile()).redirectError(output.resolve("stderr.log").toFile()).start();
            long begin = System.currentTimeMillis();
            boolean planned = false;
            try {
                process.getOutputStream().close();
                var start = process.info().startInstant().map(Instant::toString).orElse("");
                // A fast child can lose its OS metadata before the process reaper reports its exit.
                if (start.isEmpty() && !process.waitFor(100, TimeUnit.MILLISECONDS)) throw new IllegalStateException("Cannot establish child process identity");
                AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", process.pid(), "start", start, "command", command,
                        "executionTarget", Boolean.getBoolean("mork.execution.remote") ? "SSH" : "LOCAL"));
                while (!process.waitFor(100, TimeUnit.MILLISECONDS)) {
                    if (Files.exists(output.resolve("codex-launch.json"))) CodexProgressUtil.refresh(output);
                    if (cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) throw new CancellationException("Campaign cancelled");
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
                return new Result(process.exitValue(), planned, System.currentTimeMillis() - begin);
            } catch (RuntimeException | InterruptedException failure) {
                stop(process.toHandle());
                try { AutoLlmFileUtil.unfinished(output, failure instanceof CancellationException || failure instanceof InterruptedException ? "INTERRUPTED" : "FAILED", failure.toString()); }
                catch (RuntimeException recordingFailure) { failure.addSuppressed(recordingFailure); }
                throw failure;
            } finally { if (process.isAlive()) stop(process.toHandle()); }
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
        var children = new ArrayList<ProcessHandle>();
        try { children.addAll(process.descendants().toList()); }
        catch (RuntimeException ignored) { /* Restricted hosts can deny process-tree enumeration. Still stop the owned process. */ }
        children.sort(Comparator.comparingLong(ProcessHandle::pid).reversed());
        for (var child : children) child.destroy();
        process.destroy();
        try { process.onExit().get(2, TimeUnit.SECONDS); } catch (Exception ignored) { /* bounded grace */ }
        for (var child : children) if (child.isAlive()) child.destroyForcibly();
        if (process.isAlive()) process.destroyForcibly();
        try { process.onExit().get(2, TimeUnit.SECONDS); } catch (Exception ignored) { /* bounded cleanup */ }
    }
}
