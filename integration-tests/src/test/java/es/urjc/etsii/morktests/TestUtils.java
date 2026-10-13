package es.urjc.etsii.morktests;

import es.urjc.etsii.grafo.autoconfigtests.Main;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestUtils {

    private static final String JAVA_BIN = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    private static final String CLASSPATH = System.getProperty("java.class.path");
    private static final String MAIN_CLASS = Main.class.getName();

    static int runJavaProcess(Duration timeout, String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(JAVA_BIN);
        command.add("-cp");
        command.add(CLASSPATH);
        command.add(MAIN_CLASS);
        for (var a : args) command.add(a);
        return runProcess(timeout, command);
    }

    static int runProcess(Duration timeout, List<String> command) throws IOException, InterruptedException {
        Path logs = Files.createDirectories(Path.of("target", "process-logs"));
        Path output = Files.createTempFile(logs, "process-", ".log");
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(output.toFile())
                .start();
        try {
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("Subprocess did not finish within timeout: " + timeout + "; output: " + output);
            }
            return process.exitValue();
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor();
            }
            // Forward through Surefire's captured stream instead of the fork's native stdout.
            Files.copy(output, System.out);
        }
    }

    static void deleteGeneratedFiles(Path... paths) throws IOException {
        for (Path path : paths) {
            if (Files.exists(path)) {
                FileUtils.forceDelete(path.toFile());
            }
        }
    }
}
