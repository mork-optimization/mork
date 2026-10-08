package es.urjc.etsii.grafo.autoconfig.r;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class RScriptRunnerTest {

    @Test
    void capturesBothOutputStreamsAndReturnsExitCode(@TempDir Path temp) throws IOException {
        Path script = temp.resolve("test.R");
        Files.writeString(script, "success");
        var originalCommand = new AtomicReference<List<String>>();
        var runner = javaBackedRunner(originalCommand);

        var result = runner.execute(new RExecutionRequest(
                script,
                temp,
                Map.of("MORK_R_RUNNER_TEST", "available")
        ));

        assertTrue(result.successful());
        assertEquals(0, result.exitCode());
        assertEquals(List.of("Rscript", script.toString()), originalCommand.get());
        assertEquals("stdout:success:available" + System.lineSeparator(), Files.readString(result.stdoutLog()));
        assertEquals("stderr:success" + System.lineSeparator(), Files.readString(result.stderrLog()));
        assertFalse(result.elapsed().isNegative());
    }

    @Test
    void preservesNonZeroExitCode(@TempDir Path temp) throws IOException {
        Path script = temp.resolve("failure.R");
        Files.writeString(script, "failure");
        var runner = javaBackedRunner(new AtomicReference<>());

        var result = runner.execute(new RExecutionRequest(script, temp, Map.of()));

        assertFalse(result.successful());
        assertEquals(7, result.exitCode());
    }

    @Test
    void reportsMissingRscriptClearly(@TempDir Path temp) throws IOException {
        Path script = temp.resolve("test.R");
        Files.writeString(script, "success");
        var runner = new RScriptRunner(processBuilder -> {
            throw new IOException("not found");
        });

        var failure = assertThrows(
                IllegalStateException.class,
                () -> runner.execute(new RExecutionRequest(script, temp, Map.of()))
        );

        assertTrue(failure.getMessage().contains("Install GNU R"));
        assertTrue(failure.getMessage().contains("PATH"));
    }

    @Test
    void requestCopiesEnvironmentAndResolvesRelativeScript(@TempDir Path temp) {
        var environment = new java.util.HashMap<>(Map.of("A", "one"));

        var request = new RExecutionRequest(Path.of("script.R"), temp, environment);
        environment.put("A", "two");

        assertEquals(temp.resolve("script.R").toAbsolutePath().normalize(), request.script());
        assertEquals(Map.of("A", "one"), request.environment());
        assertThrows(UnsupportedOperationException.class, () -> request.environment().put("B", "three"));
    }

    @Test
    void captureFailureStopsTheChildInsteadOfBlocking(@TempDir Path temp) throws IOException {
        Path script = temp.resolve("hang.R");
        Files.writeString(script, "hang");
        Files.createDirectory(temp.resolve("hang.R.stdout.log"));
        var runner = javaBackedRunner(new AtomicReference<>());

        var failure = assertTimeoutPreemptively(
                java.time.Duration.ofSeconds(5),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> runner.execute(new RExecutionRequest(script, temp, Map.of()))
                )
        );

        assertTrue(failure.getMessage().contains("Failed capturing Rscript output"));
    }

    private RScriptRunner javaBackedRunner(AtomicReference<List<String>> originalCommand) {
        return new RScriptRunner(processBuilder -> {
            originalCommand.set(List.copyOf(processBuilder.command()));
            String script = processBuilder.command().getLast();
            processBuilder.command(
                    javaExecutable(),
                    "-cp",
                    System.getProperty("java.class.path"),
                    FakeRScript.class.getName(),
                    script
            );
            return processBuilder.start();
        });
    }

    private String javaExecutable() {
        return Path.of(
                System.getProperty("java.home"),
                "bin",
                System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java"
        ).toString();
    }

    public static final class FakeRScript {
        private FakeRScript() {
        }

        public static void main(String[] args) throws Exception {
            String content = Files.readString(Path.of(args[0]));
            if (content.equals("hang")) {
                Thread.sleep(Long.MAX_VALUE);
            }
            System.out.println("stdout:" + content + ":" + System.getenv().getOrDefault("MORK_R_RUNNER_TEST", "missing"));
            System.err.println("stderr:" + content);
            if (content.equals("failure")) {
                System.exit(7);
            }
        }
    }
}
