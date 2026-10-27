package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.util.InstancePathUtil;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Freeze input membership and payload hashes while retaining references to the original dataset. */
public final class AutoLlmInputUtil {
    private AutoLlmInputUtil() {}

    public static String snapshot(Path root, String name, List<String> source) throws IOException {
        var index = new ArrayList<String>();
        var manifest = new LinkedHashMap<String,Map<String,String>>();
        for (String token : source) {
            String path = InstancePathUtil.resolve(Path.of("."), token);
            index.add(path);
            manifest.put(path, Map.of("path", path, "sha256", AutoLlmFileUtil.inputHash(path)));
        }
        Path file = root.resolve("data/" + name + ".index");
        Files.createDirectories(file.getParent());
        Files.write(file, index);
        AutoLlmFileUtil.write(root.resolve("data/" + name + "-manifest.json"), manifest);
        return file.toString();
    }

    /** Preserve original dataset identities when approved screening indexes refer to owned copies. */
    public static Map<String,String> aliases(Path training, Path comparison) {
        Path proposal = training.getParent();
        if (proposal == null || !Files.isRegularFile(proposal.resolve("approved.json"))) return Map.of();
        Path original = proposal.getParent().resolve("data/original-inputs.json");
        if (!Files.isRegularFile(original)) return Map.of();
        var members = new HashSet<>(InstancePathUtil.list(training.toString()));
        members.addAll(InstancePathUtil.list(comparison.toString()));
        Map<String,Map<String,String>> manifest = AutoLlmFileUtil.read(original, new TypeReference<>() {});
        var aliases = new LinkedHashMap<String,String>();
        for (var entry : manifest.entrySet()) if (members.contains(entry.getValue().get("path"))) aliases.put(entry.getKey(), entry.getValue().get("path"));
        return aliases;
    }

    /** Apply the frozen parent override, then resolve the shared reference through input identities. */
    public static void configureReference(InstanceConfiguration instances, Path configFile) {
        var config = AutoLlmFileUtil.read(configFile, AutoLlmConfig.class);
        Path root = configFile.getParent().getParent();
        var paths = new LinkedHashMap<>(instances.getPaths() == null ? Map.<String,String>of() : instances.getPaths());
        boolean explicitArgument = false;
        for (String argument : config.project().runArguments()) if (argument.startsWith("--instances.path.reference=")) explicitArgument = true;
        Path overrides = root.resolve("control/instance-paths.json");
        if (!explicitArgument && Files.isRegularFile(overrides)) {
            Map<String,String> saved = AutoLlmFileUtil.read(overrides, new TypeReference<>() {});
            if (saved.containsKey("reference")) paths.put("reference", saved.get("reference"));
        }
        String reference = paths.get("reference");
        if (reference == null || reference.isBlank()) return;
        String identity = InstancePathUtil.resolve(Path.of(config.project().repository()), reference.trim());
        Path aliasesFile = root.resolve("control/input-aliases.json");
        if (Files.isRegularFile(aliasesFile)) {
            Map<String,String> aliases = AutoLlmFileUtil.read(aliasesFile, new TypeReference<>() {});
            identity = aliases.getOrDefault(identity, identity);
        }
        Path manifestFile = root.resolve("data/staged-inputs.json");
        if (Files.isRegularFile(manifestFile)) {
            Map<String,Map<String,String>> manifest = AutoLlmFileUtil.read(manifestFile, new TypeReference<>() {});
            var input = manifest.get(identity);
            if (input != null) identity = input.get("path");
        }
        paths.put("reference", identity);
        instances.setPath(paths);
    }

    private static final List<String> PATH_SETTINGS = List.of("control/instance-paths.json", "control/input-aliases.json");
    public static void addSettingsHashes(Map<String,String> hashes, Path root) {
        for (String file : PATH_SETTINGS) if (Files.isRegularFile(root.resolve(file))) hashes.put(file, AutoLlmFileUtil.hash(root.resolve(file)));
    }
    public static void copySettings(Path source, Path target) throws IOException {
        for (String file : PATH_SETTINGS) if (Files.isRegularFile(source.resolve(file))) Files.copy(source.resolve(file), target.resolve(file), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void addHashes(Map<String,String> hashes, Path root, String name) {
        for (String suffix : List.of(".index", "-manifest.json")) {
            String relative = "data/" + name + suffix;
            hashes.put(relative, AutoLlmFileUtil.hash(root.resolve(relative)));
        }
        Map<String,Map<String,String>> manifest = AutoLlmFileUtil.read(root.resolve("data/" + name + "-manifest.json"), new TypeReference<>() {});
        for (var input : manifest.values()) hashes.put(input.get("path"), input.get("sha256"));
        Path staged = root.resolve("data/staged-inputs.json");
        if (Files.exists(staged)) hashes.put("data/staged-inputs.json", AutoLlmFileUtil.hash(staged));
    }
}
