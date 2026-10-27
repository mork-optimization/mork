package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Transport failures concern the SSH connection, never the component being optimized. */
public class SshTransport {
    public static class ConnectionException extends RuntimeException {
        public ConnectionException(String message) { super(message); }
        public ConnectionException(String message, Throwable cause) { super(message, cause); }
    }
    private final AutoLlmConfig config;
    private final Path scratch;
    private String runner;
    public SshTransport(AutoLlmConfig config, Path scratch) { this.config = config; this.scratch = scratch; }
    public static Path runnerArtifact(AutoLlmConfig config) {
        String configured = config.execution().ssh().runnerArtifact();
        Path repository = Path.of(config.project().repository()).toAbsolutePath();
        if (configured != null && !configured.isBlank()) return repository.resolve(configured).normalize();
        Path target = repository.resolve("autoconfig/target");
        try (var files = Files.newDirectoryStream(target, "*-remote-runner.jar")) {
            var candidates = new ArrayList<Path>(); for (Path file : files) candidates.add(file);
            if (candidates.size() == 1) return candidates.getFirst();
        } catch (IOException ignored) { /* Show an actionable prerequisite message below. */ }
        throw new IllegalArgumentException("Set execution.ssh.runnerArtifact to the attached autoconfig *-remote-runner.jar (mvn -pl autoconfig -am package)");
    }
    public void bootstrap() {
        Path jar = scratch.resolve("runner.jar"), identityFile = scratch.resolve("runner.json");
        try {
            Files.createDirectories(scratch);
            if (!Files.exists(identityFile)) {
                Path source = runnerArtifact(config);
                Files.copy(source, jar, StandardCopyOption.REPLACE_EXISTING);
                AutoLlmFileUtil.write(identityFile, Map.of("sha256", AutoLlmFileUtil.hash(jar), "source", source.toString()));
            }
            if (!AutoLlmFileUtil.hash(jar).equals(AutoLlmFileUtil.JSON.readTree(Files.readString(identityFile)).path("sha256").asString())) throw new IllegalStateException("Frozen remote runner changed");
        } catch (IOException e) { throw new UncheckedIOException(e); }
        requireJava();
        String hash = AutoLlmFileUtil.hash(jar);
        String directory = config.execution().ssh().onlyServer().workspace().replaceAll("/+$", "") + "/.runner";
        runner = directory + "/" + hash + ".jar";
        try {
            Path exists = Files.createTempFile(scratchDirectory(), "runner-", ".check");
            execute("if test -f " + WorkloadPathUtil.quote(runner) + "; then printf 1; fi", null, exists);
            if (Files.readString(exists).equals("1")) {
                try {
                    var identity = AutoLlmFileUtil.JSON.readTree(call("identify", config.execution().ssh().onlyServer().workspace(), null, Map.of()));
                    if (identity.path("version").asInt() == WorkloadProtocol.VERSION && hash.equals(identity.path("sha256").asString())) return;
                } catch (IllegalStateException ignored) { /* Replace an incomplete or incompatible cached runner. */ }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        String temporary = runner + "." + UUID.randomUUID() + ".tmp";
        String command = "umask 077; mkdir -p -- " + WorkloadPathUtil.quote(directory)
                + " && cat > " + WorkloadPathUtil.quote(temporary) + " && mv -- " + WorkloadPathUtil.quote(temporary) + " " + WorkloadPathUtil.quote(runner);
        execute(command, jar, scratch.resolve(UUID.randomUUID() + ".bootstrap"));
        var identity = AutoLlmFileUtil.JSON.readTree(call("identify", config.execution().ssh().onlyServer().workspace(), null, Map.of()));
        if (identity.path("version").asInt() != WorkloadProtocol.VERSION || !hash.equals(identity.path("sha256").asString())) throw new IllegalStateException("Remote runner checksum or protocol mismatch");
    }
    private void requireJava() {
        var command = new StringBuilder("exec env");
        for (var entry : config.execution().ssh().onlyServer().environment().entrySet()) command.append(' ').append(WorkloadPathUtil.quote(entry.getKey() + "=" + entry.getValue()));
        command.append(' ').append(WorkloadPathUtil.quote(config.execution().ssh().onlyServer().javaExecutable())).append(" -version");
        Path output = scratch.resolve("java-version.log");
        try {
            execute(command.toString(), null, output);
            String text = Files.readString(output) + Files.readString(output.resolveSibling(output.getFileName() + ".stderr"));
            var version = java.util.regex.Pattern.compile("(?m)^(?:openjdk|java|GraalVM)\\s+(?:version\\s+)?\"?([0-9]+)").matcher(text);
            if (!version.find() || Integer.parseInt(version.group(1)) < TargetPreflightUtil.minimumJavaFeature()) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE,
                    "REMOTE_JAVA", "Configured remote Java must support Java " + TargetPreflightUtil.minimumJavaFeature() + "; inspect " + output + " and set javaExecutable/JAVA_HOME/PATH", output);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public byte[] call(String action, String root, String id, Object input) {
        try {
            Path data = Files.createTempFile(scratchDirectory(), "request-", ".json");
            Files.write(data, AutoLlmFileUtil.JSON.writeValueAsBytes(input));
            Path response = Files.createTempFile(scratchDirectory(), "response-", ".json");
            try { execute(command(action, root, id), data, response, action.equals("probe") ? TargetPreflightUtil.timeoutMillis(config, id) : 60_000); return Files.readAllBytes(response); }
            finally { Files.deleteIfExists(data); Files.deleteIfExists(response); Files.deleteIfExists(response.resolveSibling(response.getFileName() + ".stderr")); }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public void upload(String root, String relative, Path file) {
        try {
            Path response = Files.createTempFile(scratchDirectory(), "upload-", ".json");
            execute(command("put", root, relative), file, response);
            var result = AutoLlmFileUtil.JSON.readTree(Files.readString(response));
            if (!AutoLlmFileUtil.hash(file).equals(result.path("sha256").asString())) throw new IllegalStateException("Remote upload checksum mismatch");
            Files.deleteIfExists(response);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public void download(String root, String id, Path target) { execute(command("collect", root, id), null, target); }
    private Path scratchDirectory() throws IOException { Files.createDirectories(scratch); return scratch; }
    private String command(String action, String root, String id) {
        if (runner == null) throw new IllegalStateException("Remote runner has not been staged");
        var arguments = new ArrayList<String>(); arguments.add("env");
        for (var entry : config.execution().ssh().onlyServer().environment().entrySet()) arguments.add(entry.getKey() + "=" + entry.getValue());
        arguments.addAll(List.of(config.execution().ssh().onlyServer().javaExecutable(), "-Xmx" + config.execution().heap(), "-jar", runner, action, root));
        if (id != null) arguments.add(id);
        var result = new StringBuilder("exec");
        for (String argument : arguments) result.append(' ').append(WorkloadPathUtil.quote(argument));
        return result.toString();
    }
    private void execute(String remoteCommand, Path input, Path output) {
        execute(remoteCommand, input, output, config.execution().phaseTimeoutMillis());
    }
    private void execute(String remoteCommand, Path input, Path output, long timeoutMillis) {
        try {
            Files.createDirectories(output.getParent());
            Path errors = output.resolveSibling(output.getFileName() + ".stderr");
            var arguments = new ArrayList<>(List.of("ssh", "-oBatchMode=yes", "-oConnectTimeout=10", "-oServerAliveInterval=15", "-oServerAliveCountMax=2"));
            if (config.execution().ssh().onlyServer().configFile() != null) arguments.addAll(List.of("-F", config.execution().ssh().onlyServer().configFile()));
            arguments.addAll(List.of("--", config.execution().ssh().onlyServer().host(), remoteCommand));
            var builder = new ProcessBuilder(arguments)
                    .redirectOutput(output.toFile()).redirectError(errors.toFile());
            builder.redirectInput(input == null ? ProcessBuilder.Redirect.from(new File("/dev/null")) : ProcessBuilder.Redirect.from(input.toFile()));
            var process = builder.start();
            try {
                if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly(); throw new ConnectionException("SSH transport timed out; remote job status is unknown");
                }
                if (process.exitValue() != 0) {
                    String message = Files.readString(errors); if (message.length() > 4000) message = message.substring(message.length() - 4000);
                    if (process.exitValue() == 255) throw new ConnectionException("SSH unavailable: " + message);
                    throw new IllegalStateException("Remote runner failed (" + process.exitValue() + "): " + message);
                }
            } catch (InterruptedException e) { process.destroyForcibly(); Thread.currentThread().interrupt(); throw new java.util.concurrent.CancellationException("SSH detached; reconcile the saved remote job before resuming"); }
        } catch (IOException e) { throw new ConnectionException("Cannot invoke SSH", e); }
    }
}
