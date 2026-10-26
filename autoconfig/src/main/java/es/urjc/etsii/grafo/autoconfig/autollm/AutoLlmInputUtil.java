package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.util.InstancePathUtil;
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
