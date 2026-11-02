package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.*;
import java.util.*;
import tools.jackson.databind.JsonNode;

/** The server's single semantic configuration representation. Record constructors supply defaults. */
public final class AutoLlmNormalization {
    private AutoLlmNormalization() {}
    public static String optional(String value) { return value == null || value.isBlank() ? null : value; }
    public static Path localPath(Path path) {
        path = path.toAbsolutePath().normalize();
        Path existing = path;
        while (existing != null && !Files.exists(existing)) existing = existing.getParent();
        try { return existing == null ? path : existing.toRealPath().resolve(existing.relativize(path)); }
        catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
    }
    private static String path(Path base, String value) {
        value = optional(value);
        if (value == null) return null;
        int member = value.indexOf('!');
        return localPath(base.resolve(member < 0 ? value : value.substring(0, member)))
                + (member < 0 ? "" : value.substring(member));
    }
    public static String heap(String value) {
        if (value == null || !value.matches("[1-9][0-9]*[kKmMgG]")) return value;
        long size = Long.parseLong(value.substring(0, value.length() - 1));
        return Math.multiplyExact(size, switch (Character.toLowerCase(value.charAt(value.length() - 1))) {
            case 'g' -> 1048576; case 'm' -> 1024; default -> 1;
        }) + "k";
    }
    public static AutoLlmConfig normalize(AutoLlmConfig requested) {
        // Turn optional blanks into null before invoking the records, without duplicating defaults.
        var tree = AutoLlmFileUtil.JSON.valueToTree(requested);
        blanks(tree);
        var c = AutoLlmFileUtil.JSON.treeToValue(tree, AutoLlmConfig.class);
        Path cwd = Path.of("").toAbsolutePath(), repo = localPath(Path.of(c.project().repository()));
        var e = c.execution(); var p = c.preparation(); var x = c.codex();
        AutoLlmConfig.Ssh ssh = null;
        if (e.ssh() != null) {
            var servers = new ArrayList<AutoLlmConfig.Server>();
            for (var s : e.ssh().servers()) servers.add(new AutoLlmConfig.Server(s.id(), s.host(), s.workspace(), s.javaExecutable(),
                    s.environment(), path(cwd, s.configFile()), s.workers() == null ? e.workers() : s.workers(), heap(s.heap() == null ? e.heap() : s.heap())));
            // Remote workspace/Java paths are opaque and must never use the controller filesystem.
            ssh = new AutoLlmConfig.Ssh(e.ssh().screeningServerId(), path(repo, e.ssh().runnerArtifact()), servers, e.ssh().reconnect());
        }
        return new AutoLlmConfig(new AutoLlmConfig.Project(repo.toString(), c.project().ref(), c.project().buildCommand(), c.project().artifact(), c.project().runArguments()),
                path(cwd, c.workspace()), c.trials(), c.seed(), c.maxParameters(), path(repo, c.sotaFile()), path(repo, c.initialConfigurationsFile()),
                path(repo, c.trainingPath()), path(repo, c.comparisonPath()),
                new AutoLlmConfig.Codex(x.executable(), path(repo, x.promptFile()), x.prompt(), x.model(), x.reasoningEffort(), x.maxConcurrentSessions()),
                new AutoLlmConfig.Preparation(p.probeDurationMillis(), p.pilotDurationMillis(), p.maxRepairs(), p.localBuilds(), p.readyQueueSize(), p.localWorkers(), heap(p.localHeap())),
                c.tuning(), c.comparison(), new AutoLlmConfig.Execution(e.workers(), e.watchdogGraceMillis(), e.codexTimeoutMillis(), e.buildTimeoutMillis(), e.startupTimeoutMillis(),
                e.searchSpaceTimeoutMillis(), e.phaseTimeoutMillis(), e.stageTimeoutMillis(), heap(e.heap()), e.target(), ssh));
    }
    private static void blanks(JsonNode node) {
        if (node instanceof tools.jackson.databind.node.ObjectNode object) {
            for (var entry : new ArrayList<>(object.properties())) {
                if (entry.getValue().isString() && entry.getValue().asString().isBlank()) object.putNull(entry.getKey());
                else if (!Set.of("environment", "buildCommand", "runArguments").contains(entry.getKey())) blanks(entry.getValue());
            }
        } else if (node.isArray()) for (var child : node) blanks(child);
    }
    public record Difference(String path, JsonNode reviewed, JsonNode current) {}
    public static List<Difference> diff(Object reviewed, Object current) {
        var differences = new ArrayList<Difference>();
        diff("", AutoLlmFileUtil.JSON.valueToTree(reviewed), AutoLlmFileUtil.JSON.valueToTree(current), differences); return differences;
    }
    private static void diff(String path, JsonNode a, JsonNode b, List<Difference> result) {
        if (a.equals(b)) return;
        if (a.isObject() && b.isObject()) {
            var keys = new TreeSet<String>(); for (var e : a.properties()) keys.add(e.getKey()); for (var e : b.properties()) keys.add(e.getKey());
            for (String key : keys) diff(path.isEmpty() ? key : path + "." + key, a.path(key), b.path(key), result);
        } else if (a.isArray() && b.isArray()) {
            for (int i = 0; i < Math.max(a.size(), b.size()); i++) diff(path + "[" + i + "]", a.path(i), b.path(i), result);
        } else result.add(new Difference(path, a, b));
    }
    public static final class ApprovalMismatch extends IllegalStateException {
        public final String code;
        public final List<Difference> differences;
        public ApprovalMismatch(String code, List<Difference> differences) { super(code); this.code = code; this.differences = differences; }
    }
}
