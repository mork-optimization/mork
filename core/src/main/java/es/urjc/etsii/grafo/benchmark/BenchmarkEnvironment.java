package es.urjc.etsii.grafo.benchmark;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

/** Environment of the VM which actually executes the benchmark and solver. */
final class BenchmarkEnvironment {
    private BenchmarkEnvironment() {}

    static Map<String, String> runtime() {
        var info = new LinkedHashMap<String, String>();
        for (String key : new String[]{"os.name", "os.version", "os.arch", "java.vendor", "java.runtime.version", "java.vm.name", "java.vm.version"}) {
            info.put(key, System.getProperty(key, ""));
        }
        info.put("processors", Integer.toString(Runtime.getRuntime().availableProcessors()));
        info.put("maxHeapBytes", Long.toString(Runtime.getRuntime().maxMemory()));
        // JVM arguments may contain credentials: retain only their fingerprint.
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                digest.update(argument.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
            }
            info.put("jvmArgumentsSha256", HexFormat.of().formatHex(digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return info;
    }

    static Map<String, String> current() {
        var info = runtime();
        String hostname = "";
        try { hostname = InetAddress.getLocalHost().getHostName(); }
        catch (IOException ignored) { /* Unknown identity disables reuse. */ }
        info.put("hostname", hostname);
        info.put("cpu", cpu());
        return Map.copyOf(info);
    }

    static boolean identifiable(Map<String, String> environment) {
        if (environment == null) return false;
        for (String key : new String[]{"hostname", "cpu", "processors", "maxHeapBytes", "jvmArgumentsSha256",
                "os.name", "os.version", "os.arch", "java.vendor", "java.runtime.version", "java.vm.name", "java.vm.version"}) {
            String value = environment.get(key);
            if (value == null || value.isBlank()) return false;
        }
        return true;
    }

    private static String cpu() {
        String os = System.getProperty("os.name", "");
        try {
            if (os.startsWith("Linux")) {
                try (var reader = Files.newBufferedReader(Path.of("/proc/cpuinfo"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int separator = line.indexOf(':');
                        if (separator < 0) continue;
                        String key = line.substring(0, separator).trim();
                        if (key.equals("model name") || key.equals("Hardware")) return line.substring(separator + 1).trim();
                    }
                }
            } else if (os.startsWith("Mac")) {
                Process process = new ProcessBuilder("/usr/sbin/sysctl", "-n", "machdep.cpu.brand_string")
                        .redirectError(ProcessBuilder.Redirect.DISCARD).start();
                try {
                    if (process.waitFor(1, TimeUnit.SECONDS) && process.exitValue() == 0) {
                        return new String(process.getInputStream().readNBytes(4096), StandardCharsets.UTF_8).trim();
                    }
                } finally {
                    process.destroyForcibly();
                    process.getInputStream().close();
                }
            } else if (os.startsWith("Windows")) {
                return System.getenv().getOrDefault("PROCESSOR_IDENTIFIER", "");
            }
        } catch (IOException ignored) {
            // Hardware discovery is optional; never reuse measurements with an unknown CPU.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Interrupted while identifying benchmark CPU");
        }
        return "";
    }
}
