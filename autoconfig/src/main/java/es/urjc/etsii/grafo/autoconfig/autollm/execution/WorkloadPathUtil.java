package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmConfig;
import java.nio.file.Path;

public final class WorkloadPathUtil {
    private WorkloadPathUtil() {}
    public static String remoteRoot(AutoLlmConfig config, Path root) {
        return Path.of(config.execution().ssh().workspace(), "campaigns", root.getFileName().toString()).normalize().toString();
    }
    public static String translate(String token, String from, String to) {
        int split = token.indexOf('!');
        String container = split < 0 ? token : token.substring(0, split);
        String suffix = split < 0 ? "" : token.substring(split);
        if (container.equals(from)) return to + suffix;
        if (container.startsWith(from + "/")) return to + container.substring(from.length()) + suffix;
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
