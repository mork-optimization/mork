package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmConfig;
import java.nio.file.Path;

public final class WorkloadPathUtil {
    private WorkloadPathUtil() {}
    public static String remoteRoot(AutoLlmConfig config, Path root) {
        Path context = root.resolve("execution-context.json");
        if (java.nio.file.Files.exists(context)) {
            var identity = es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil.read(context, java.util.Map.class);
            Path remote = Path.of(config.execution().ssh().onlyServer().workspace(), "campaigns", (String) identity.get("campaignId"), "targets", (String) identity.get("targetId"));
            if (identity.containsKey("trial")) remote = remote.resolve("trials/" + identity.get("trial"));
            if (identity.containsKey("assignment")) remote = remote.resolve("assignments/" + identity.get("assignment"));
            return remote.normalize().toString();
        }
        return Path.of(config.execution().ssh().onlyServer().workspace(), "campaigns", root.getFileName().toString()).normalize().toString();
    }
    public static Path remoteCampaign(Path root) {
        Path workspace = workspace(root);
        Path campaigns = workspace.resolve("campaigns");
        if (!root.startsWith(campaigns) || root.getNameCount() <= campaigns.getNameCount()) throw new IllegalArgumentException("Invalid campaign namespace");
        return campaigns.resolve(campaigns.relativize(root).getName(0));
    }
    /** Both v3 screening roots and target roots share the server workspace's measurement lock. */
    public static Path workspace(Path root) {
        if (root.getParent() != null && root.getParent().getFileName().toString().equals("assignments")) root = root.getParent().getParent();
        if (root.getParent() != null && root.getParent().getFileName().toString().equals("trials")) root = root.getParent().getParent();
        Path campaigns = root.getParent();
        if (campaigns != null && campaigns.getFileName().toString().equals("targets")) campaigns = campaigns.getParent().getParent();
        if (campaigns == null || !campaigns.getFileName().toString().equals("campaigns") || campaigns.getParent() == null)
            throw new IllegalArgumentException("Invalid remote workload namespace: " + root);
        return campaigns.getParent();
    }
    public static String translate(String token, String from, String to) {
        int split = token.indexOf('!');
        String container = split < 0 ? token : token.substring(0, split);
        String suffix = split < 0 ? "" : token.substring(split);
        if (container.equals(from)) return to + suffix;
        if (container.startsWith(from + "/")) return to + container.substring(from.length()) + suffix;
        if (java.io.File.separatorChar == '\\' && container.startsWith(from + "\\")) return to + container.substring(from.length()).replace('\\', '/') + suffix;
        return token;
    }
    public static String argument(String argument, Path local, String remote) {
        int equals = argument.indexOf('=');
        if (equals >= 0) return argument.substring(0, equals + 1) + translate(argument.substring(equals + 1), local.toString(), remote);
        return translate(argument, local.toString(), remote);
    }
    public static String quote(String argument) {
        if (argument.indexOf('\0') >= 0 || argument.indexOf('\n') >= 0 || argument.indexOf('\r') >= 0) throw new IllegalArgumentException("Invalid SSH argument");
        return "'" + argument.replace("'", "'\\''") + "'";
    }
    public static Path inside(Path root, String relative) {
        Path path = root.resolve(relative).normalize();
        if (Path.of(relative).isAbsolute() || !path.startsWith(root.normalize())) throw new IllegalArgumentException("Path escapes workload: " + relative);
        for (Path parent = path.getParent(); parent != null && parent.startsWith(root); parent = parent.getParent()) {
            if (java.nio.file.Files.isSymbolicLink(parent)) throw new IllegalArgumentException("Symlink parent: " + parent);
        }
        return path;
    }
}
