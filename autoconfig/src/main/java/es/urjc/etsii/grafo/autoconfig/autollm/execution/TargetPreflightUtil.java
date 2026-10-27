package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.autoconfig.screening.ScreeningFileUtil;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Inspects the environment inherited by actual workloads, without installing anything. */
public final class TargetPreflightUtil {
    public record Prerequisite(String name, String executable, String status, String output, String diagnostic) {}
    private TargetPreflightUtil() {}
    public static int minimumJavaFeature() {
        try (var input = new DataInputStream(Objects.requireNonNull(AutoLlmConfig.class.getResourceAsStream("AutoLlmConfig.class")))) {
            input.readInt(); input.readUnsignedShort(); return input.readUnsignedShort() - 44;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static WorkloadProtocol.Host identity(AutoLlmConfig config, Path root, List<Prerequisite> checks) throws IOException {
        var os = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        long memory = os instanceof com.sun.management.OperatingSystemMXBean extended ? extended.getTotalMemorySize() : -1;
        return new WorkloadProtocol.Host(WorkloadProtocol.VERSION, ScreeningFileUtil.host(), config.execution().workers(),
                ConcurrencyUtil.resolveWorkerCount(config.execution().workers(), Runtime.getRuntime().availableProcessors()), Runtime.getRuntime().maxMemory(),
                memory, Files.getFileStore(root).getUsableSpace(), checks);
    }
    private static final long CHECK_TIMEOUT_MILLIS = 30_000;
    private static final long CHECK_GRACE_MILLIS = 1_000;
    private static LinkedHashMap<String,List<String>> commands(AutoLlmConfig config, String selectorPython) {
        var commands = new LinkedHashMap<String,List<String>>();
        commands.put("java", List.of(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-version"));
        commands.put("git", List.of("git", "--version"));
        commands.put("r-packages", List.of("Rscript", "-e", "for(p in c('irace','httr','jsonlite')) {if(!requireNamespace(p,quietly=TRUE)) stop(paste('Missing R package',p));cat(p,as.character(packageVersion(p)),'\\n')}"));
        String python = selectorPython != null && !selectorPython.isBlank() ? selectorPython : config.execution().target() == AutoLlmConfig.Target.SSH ? config.execution().ssh().onlyServer().environment().getOrDefault("MORK_SELECTOR_PYTHON", "python3") : System.getenv().getOrDefault("MORK_SELECTOR_PYTHON", "python3");
        if (selectorPython != null) commands.put("selector-python", List.of(python, "-c", "import sys,numpy,pandas,sklearn,yellowbrick,matplotlib,seaborn; print(sys.executable); print(sys.version)"));
        String build = config.project().buildCommand().getFirst();
        if (!build.contains("/")) commands.put("build-tool", List.of(build, "--version"));
        return commands;
    }
    /** Include every sequential child deadline and room for host inspection and SSH dispatch. */
    public static long timeoutMillis(AutoLlmConfig config, String selectorPython) {
        return commands(config, selectorPython).size() * (CHECK_TIMEOUT_MILLIS + CHECK_GRACE_MILLIS) + 30_000;
    }
    public static WorkloadProtocol.Host inspect(AutoLlmConfig config, Path root, AutoLlmProcessRunner processes, String selectorPython) throws IOException {
        Files.createDirectories(root);
        var commands = commands(config, selectorPython);
        String build = config.project().buildCommand().getFirst();
        var checks = new ArrayList<Prerequisite>();
        for (var entry : commands.entrySet()) {
            Path output = root.resolve(".preflight/" + entry.getKey());
            try {
                var result = processes.run(entry.getValue(), root, output, CHECK_TIMEOUT_MILLIS, 0, CHECK_GRACE_MILLIS, () -> Thread.currentThread().isInterrupted(), false, 0);
                String log = AutoLlmFailureUtil.tail(output.resolve("stdout.log"), 4000) + AutoLlmFailureUtil.tail(output.resolve("stderr.log"), 4000);
                checks.add(new Prerequisite(entry.getKey(), executable(entry.getValue().getFirst()), result.exitCode() == 0 ? "PASSED" : "FAILED", log,
                        result.exitCode() == 0 ? null : "Prepare this dependency in the configured noninteractive environment, then rerun preflight. Evidence: " + output));
            } catch (java.util.concurrent.CancellationException e) { throw e; }
            catch (RuntimeException e) { checks.add(new Prerequisite(entry.getKey(), executable(entry.getValue().getFirst()), "FAILED", "", e.toString())); }
        }
        if (build.contains("/")) checks.add(new Prerequisite("build-wrapper", build, "DEFERRED", "", "The committed wrapper is checked with --version after its source snapshot is staged."));
        var host = identity(config, root, checks); AutoLlmFileUtil.write(root.resolve("target-preflight.json"), host); return host;
    }
    public static void requireReady(WorkloadProtocol.Host host, Path evidence) {
        for (var check : host.prerequisites()) if (check.status().equals("FAILED")) throw AutoLlmFailureUtil.error(
                AutoLlmFailureUtil.Category.INFRASTRUCTURE, "PREREQUISITE_FAILED", check.name() + ": " + check.diagnostic(), evidence.resolve("target-preflight.json"));
    }
    private static String executable(String name) {
        if (name.contains("/")) return Path.of(name).toAbsolutePath().normalize().toString();
        for (String folder : System.getenv().getOrDefault("PATH", "").split(File.pathSeparator)) {
            Path path = Path.of(folder).resolve(name); if (Files.isExecutable(path)) return path.toAbsolutePath().toString();
        }
        return name;
    }
}
