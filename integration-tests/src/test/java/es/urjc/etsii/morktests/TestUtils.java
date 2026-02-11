package es.urjc.etsii.morktests;

import es.urjc.etsii.grafo.autoconfigtests.Main;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process p = pb.start();
        boolean finished = p.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new RuntimeException("Subprocess did not finish within timeout: " + timeout);
        }
        return p.exitValue();
    }
}
