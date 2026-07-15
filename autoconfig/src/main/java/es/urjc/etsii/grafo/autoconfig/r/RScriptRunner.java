package es.urjc.etsii.grafo.autoconfig.r;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Executes R scripts using the system {@code Rscript} command.
 */
public class RScriptRunner implements RLangRunner {
    private static final Logger log = LoggerFactory.getLogger(RScriptRunner.class);
    private static final String RSCRIPT_COMMAND = "Rscript";

    private final ProcessStarter processStarter;

    /**
     * Create a runner backed by {@link ProcessBuilder#start()}.
     */
    public RScriptRunner() {
        this(ProcessBuilder::start);
    }

    RScriptRunner(ProcessStarter processStarter) {
        this.processStarter = processStarter;
    }

    @Override
    public RExecutionResult execute(RExecutionRequest request) {
        validateRequest(request);
        Path stdoutLog = outputLog(request, "stdout");
        Path stderrLog = outputLog(request, "stderr");
        var processBuilder = new ProcessBuilder(List.of(RSCRIPT_COMMAND, request.script().toString()));
        processBuilder.directory(request.workingDirectory().toFile());
        processBuilder.environment().putAll(request.environment());

        long startedAt = System.nanoTime();
        final Process process;
        try {
            process = processStarter.start(processBuilder);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot start Rscript. Install GNU R and ensure Rscript is available on PATH.",
                    e
            );
        }

        var executor = Executors.newFixedThreadPool(2);
        try {
            var captureFailure = new CompletableFuture<Void>();
            Future<?> stdout = submitDrain(
                    executor,
                    process.getInputStream(),
                    stdoutLog,
                    false,
                    captureFailure
            );
            Future<?> stderr = submitDrain(
                    executor,
                    process.getErrorStream(),
                    stderrLog,
                    true,
                    captureFailure
            );

            int exitCode = waitForProcess(process, stdout, stderr, captureFailure);
            awaitDrain(stdout, "standard output");
            awaitDrain(stderr, "standard error");

            var elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
            log.info("Rscript process terminated with code {} after {}", exitCode, elapsed);
            return new RExecutionResult(exitCode, elapsed, stdoutLog, stderrLog);
        } finally {
            // Do not wait indefinitely for a reader whose underlying native
            // stream is being torn down after process failure.
            executor.shutdownNow();
        }
    }

    private Future<?> submitDrain(
            java.util.concurrent.ExecutorService executor,
            InputStream stream,
            Path destination,
            boolean errorStream,
            CompletableFuture<Void> captureFailure
    ) {
        return executor.submit(() -> {
            try {
                drain(stream, destination, errorStream);
            } catch (RuntimeException e) {
                captureFailure.completeExceptionally(e);
                throw e;
            }
        });
    }

    private int waitForProcess(
            Process process,
            Future<?> stdout,
            Future<?> stderr,
            CompletableFuture<Void> captureFailure
    ) {
        try {
            CompletableFuture.anyOf(process.onExit(), captureFailure).get();
            return process.waitFor();
        } catch (InterruptedException e) {
            stopProcess(process, stdout, stderr);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Rscript to finish", e);
        } catch (ExecutionException e) {
            stopProcess(process, stdout, stderr);
            throw new IllegalStateException("Failed capturing Rscript output", e.getCause());
        }
    }

    private void stopProcess(Process process, Future<?> stdout, Future<?> stderr) {
        stopProcessTree(process);
        stdout.cancel(true);
        stderr.cancel(true);
    }

    private void validateRequest(RExecutionRequest request) {
        if (!Files.isDirectory(request.workingDirectory())) {
            throw new IllegalArgumentException(
                    "R working directory does not exist: " + request.workingDirectory()
            );
        }
        if (!Files.isRegularFile(request.script())) {
            throw new IllegalArgumentException("R script does not exist: " + request.script());
        }
    }

    private Path outputLog(RExecutionRequest request, String streamName) {
        return request.workingDirectory().resolve(
                request.script().getFileName() + "." + streamName + ".log"
        );
    }

    private void drain(InputStream stream, Path destination, boolean errorStream) {
        try (
                var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                if (errorStream) {
                    log.warn("{}", line);
                } else {
                    log.info("{}", line);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot capture Rscript output in " + destination, e);
        }
    }

    private void awaitDrain(Future<?> drain, String streamName) {
        try {
            drain.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while capturing Rscript " + streamName, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed capturing Rscript " + streamName, e.getCause());
        }
    }

    private void stopProcessTree(Process process) {
        try {
            var descendants = new ArrayList<>(process.descendants().toList());
            descendants.sort(Comparator.comparingLong(ProcessHandle::pid).reversed());
            for (var descendant : descendants) {
                descendant.destroyForcibly();
            }
        } catch (RuntimeException e) {
            // Some restricted environments do not permit process-tree
            // discovery. The direct child must still be terminated.
            log.debug("Cannot enumerate Rscript descendants during termination", e);
        } finally {
            process.destroyForcibly();
        }
    }

    @FunctionalInterface
    interface ProcessStarter {
        Process start(ProcessBuilder processBuilder) throws IOException;
    }
}
