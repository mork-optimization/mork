package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import es.urjc.etsii.grafo.util.IOUtil;
import org.springframework.boot.system.ApplicationHome;
import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import static es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.*;

public final class ScreeningFileUtil {
    private ScreeningFileUtil() {}
    public static String digest(Object value) {
        try { return AutoLlmFileUtil.hash(new ByteArrayInputStream(AutoLlmFileUtil.JSON.writeValueAsBytes(value))); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static String inputHash(String token) {
        try (var input = IOUtil.getInputStream(token)) { return AutoLlmFileUtil.hash(input); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static Map<String,String> host() {
        var host = new TreeMap<String,String>();
        for (String key : List.of("os.name", "os.version", "os.arch", "java.runtime.version", "java.vm.name")) host.put(key, System.getProperty(key));
        host.put("processors", Integer.toString(Runtime.getRuntime().availableProcessors()));
        try { host.put("hostname", InetAddress.getLocalHost().getHostName()); }
        catch (IOException e) { throw new UncheckedIOException("Cannot identify screening host", e); }
        Path cpu = Path.of("/proc/cpuinfo");
        if (Files.isRegularFile(cpu)) {
            try {
                for (String line : Files.readAllLines(cpu)) if (line.startsWith("model name")) {
                    host.put("cpu", line.substring(line.indexOf(':') + 1).trim()); break;
                }
            } catch (IOException e) { throw new UncheckedIOException(e); }
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            try {
                var process = new ProcessBuilder("sysctl", "-n", "machdep.cpu.brand_string").start();
                String model = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                if (process.waitFor() != 0) throw new IllegalStateException("Cannot identify screening CPU");
                host.put("cpu", model);
            } catch (IOException e) { throw new UncheckedIOException(e); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
        }
        return host;
    }
    public static List<String> javaCommand(Class<?> applicationType) {
        var args = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-da"));
        File source = new ApplicationHome(applicationType).getSource();
        if (source != null && source.isFile()) {
            args.addAll(List.of("-jar", source.getAbsolutePath()));
        } else {
            var entries = new ArrayList<String>();
            for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
                entries.add(Path.of(entry).toAbsolutePath().normalize().toString());
            }
            args.addAll(List.of("-cp", String.join(File.pathSeparator, entries), System.getProperty("sun.java.command").split("\\s+", 2)[0]));
        }
        return args;
    }
    public static String artifactHash(Class<?> applicationType) {
        File source = new ApplicationHome(applicationType).getSource();
        if (source != null && source.isFile()) return AutoLlmFileUtil.hash(source.toPath());
        var hashes = new TreeMap<String,String>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            Path path = Path.of(entry).toAbsolutePath().normalize();
            if (Files.isRegularFile(path)) hashes.put(path.toString(), AutoLlmFileUtil.hash(path));
            else if (Files.isDirectory(path)) try (var files = Files.walk(path)) {
                for (Path file : files.toList()) if (Files.isRegularFile(file)) hashes.put(file.toString(), AutoLlmFileUtil.hash(file));
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        return digest(hashes);
    }
    public static Path runPath(Path output, String instance, long seed) {
        return output.resolve("runs").resolve(digest(List.of(instance, seed)) + ".json");
    }
    public static Run readRun(Path output, Manifest manifest, Input input, long seed) {
        Path file = runPath(output, input.path(), seed);
        if (!Files.exists(file)) return null;
        RunEvidence evidence = AutoLlmFileUtil.read(file, RunEvidence.class);
        Run run = evidence.run();
        if (run == null || !digest(run).equals(evidence.sha256())) throw new IllegalStateException("Cached screening evidence checksum failed: " + file);
        if (!run.fingerprint().equals(manifest.fingerprint()) || !run.instance().equals(input.path()) || run.seed() != seed) {
            throw new IllegalStateException("Cached screening run has different provenance: " + file);
        }
        return run;
    }
    public static void writeRun(Path output, Run run) { AutoLlmFileUtil.write(runPath(output, run.instance(), run.seed()), new RunEvidence(digest(run), run)); }
    public static void writeText(Path path, String value) {
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temporary, value);
            try { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static void index(Path file, List<String> tokens) {
        var contents = new StringBuilder();
        for (String token : tokens) {
            int split = token.indexOf('!');
            String container = split < 0 ? token : token.substring(0, split);
            String suffix = split < 0 ? "" : token.substring(split);
            String relative = file.toAbsolutePath().getParent().relativize(Path.of(container).toAbsolutePath()).toString();
            String entry = relative + suffix;
            if (entry.contains("\n") || entry.contains("\r") || entry.contains("#")) throw new IllegalArgumentException("Unsupported index path: " + token);
            contents.append(relative).append(suffix).append('\n');
        }
        writeText(file, contents.toString());
    }
    public static void verifyInputs(Manifest manifest, String currentArtifact) {
        if (!manifest.host().equals(host()) || !manifest.artifactSha256().equals(currentArtifact)) {
            throw new IllegalStateException("Screening host or executable changed; use a new output directory");
        }
        if (!manifest.sotaSha256().equals(AutoLlmFileUtil.hash(Path.of(manifest.settings().sotaFile())))) {
            throw new IllegalStateException("SOTA changed since screening");
        }
        for (Input input : manifest.inputs()) if (!input.sha256().equals(inputHash(input.path()))) {
            throw new IllegalStateException("Screening input changed: " + input.path());
        }
    }
    public static void approve(Path output, String reviewSha256, String currentArtifact) {
        Path proposalFile = output.resolve("proposal.json");
        if (reviewSha256 == null || !reviewSha256.equals(AutoLlmFileUtil.hash(proposalFile))) {
            throw new IllegalArgumentException("Approval requires --instance-screening.review-sha256=<SHA-256 printed with the reviewed proposal>");
        }
        var proposal = AutoLlmFileUtil.read(proposalFile, Proposal.class);
        var manifest = AutoLlmFileUtil.read(output.resolve("manifest.json"), Manifest.class);
        verifyInputs(manifest, currentArtifact);
        if (!proposal.fingerprint().equals(manifest.fingerprint()) || proposal.splitFailure() != null
                || proposal.trainingCount() == 0 || proposal.validationCount() == 0) {
            throw new IllegalStateException("No complete nonempty training/validation split to approve");
        }
        for (var file : proposal.files().entrySet()) {
            if (!AutoLlmFileUtil.hash(AutoLlmFileUtil.inside(output, file.getKey())).equals(file.getValue())) {
                throw new IllegalStateException("Reviewed screening artifact changed: " + file.getKey());
            }
        }
        Map<String,String> runs = AutoLlmFileUtil.read(output.resolve("run-hashes.json"), new tools.jackson.core.type.TypeReference<>() {});
        for (var run : runs.entrySet()) {
            if (!AutoLlmFileUtil.hash(AutoLlmFileUtil.inside(output.resolve("runs"), run.getKey())).equals(run.getValue())) {
                throw new IllegalStateException("Reviewed run evidence changed: " + run.getKey());
            }
        }
        for (String name : List.of("retained", "train", "validation")) {
            Path target = output.resolve(name + ".index");
            Path proposed = output.resolve("proposed-" + name + ".index");
            if (Files.exists(target) && !AutoLlmFileUtil.hash(target).equals(AutoLlmFileUtil.hash(proposed))) {
                throw new IllegalStateException("Refusing to overwrite a different approved index: " + target);
            }
        }
        try {
            for (String name : List.of("retained", "train", "validation")) {
                writeText(output.resolve(name + ".index"), Files.readString(output.resolve("proposed-" + name + ".index")));
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        AutoLlmFileUtil.write(output.resolve("approved.json"), Map.of("state", "APPROVED", "approvedAt", Instant.now().toString(),
                "proposalSha256", reviewSha256, "fingerprint", manifest.fingerprint(), "files", proposal.files()));
    }
}
