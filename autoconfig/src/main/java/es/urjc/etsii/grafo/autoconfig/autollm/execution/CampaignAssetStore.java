package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Immutable campaign assets with durable references. Ordinary files work on every supported filesystem. */
public final class CampaignAssetStore {
    private CampaignAssetStore() {}
    public record Reference(String owner, SortedSet<String> assets) {}

    public static Path campaign(Path executionRoot) {
        for (Path path = executionRoot.toAbsolutePath().normalize(); path != null; path = path.getParent()) {
            if (Files.isRegularFile(path.resolve("control/storage-layout.json"))) return path;
        }
        return null;
    }
    public static synchronized Path putArtifact(Path campaign, Path owner, Path jar) throws IOException {
        String hash = AutoLlmFileUtil.hash(jar);
        Path destination = campaign.resolve(".store/artifacts/" + hash + ".jar");
        Files.createDirectories(destination.getParent());
        if (!Files.exists(destination)) {
            Path temporary = Files.createTempFile(destination.getParent(), "artifact-", ".tmp");
            try {
                Files.copy(jar, temporary, StandardCopyOption.REPLACE_EXISTING);
                if (!hash.equals(AutoLlmFileUtil.hash(temporary))) throw new IOException("Executable changed while freezing");
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            } finally { Files.deleteIfExists(temporary); }
        }
        if (!hash.equals(AutoLlmFileUtil.hash(destination))) throw new IOException("Shared executable changed: " + destination);
        retain(campaign, owner, destination);
        return destination;
    }
    public static synchronized void retain(Path campaign, Path owner, Path asset) throws IOException {
        String relative = relative(campaign, asset);
        if (!relative.matches("\\.store/(?:artifacts/[a-f0-9]{64}\\.jar|layers/[a-f0-9]{64}\\.zip)")) throw new IOException("Not a managed asset: " + relative);
        Path file = reference(campaign, owner);
        var assets = Files.exists(file) ? new TreeSet<>(AutoLlmFileUtil.read(file, Reference.class).assets()) : new TreeSet<String>();
        assets.add(relative);
        AutoLlmFileUtil.write(file, new Reference(relative(campaign, owner), assets));
    }
    public static synchronized String layer(Path campaign, Path owner, Map<String,Path> files) throws IOException {
        Path cache = campaign.resolve(".store/layers");
        String hash = WorkloadArchiveUtil.prepareLayer(cache, files);
        retain(campaign, owner, cache.resolve(hash + ".zip"));
        return hash;
    }
    public static Path artifact(Path campaign, Path trial) {
        Path metadata = trial.resolve("frozen/artifact.json");
        if (Files.isRegularFile(metadata)) {
            Object asset = AutoLlmFileUtil.read(metadata, Map.class).get("asset");
            if (asset instanceof String path) {
                if (!path.matches("\\.store/artifacts/[a-f0-9]{64}\\.jar")) throw new IllegalArgumentException("Invalid shared artifact identity");
                return WorkloadPathUtil.inside(campaign, path);
            }
        }
        return trial.resolve("frozen/project.jar");
    }
    public static synchronized void releaseArtifact(Path campaign, Path owner) throws IOException {
        Path reference = reference(campaign, owner);
        if (!Files.exists(reference)) return;
        var previous = AutoLlmFileUtil.read(reference, Reference.class);
        var assets = new TreeSet<>(previous.assets());
        assets.removeIf(path -> path.startsWith(".store/artifacts/"));
        AutoLlmFileUtil.write(reference, new Reference(previous.owner(), assets));
        collect(campaign);
    }
    /** Release only transfer copies for a closed owner subtree, never its retained measured executables. */
    public static synchronized void releaseLayers(Path campaign, Path owner) throws IOException {
        Path directory = campaign.resolve(".store/references");
        if (!Files.isDirectory(directory)) return;
        String prefix = relative(campaign, owner);
        try (var files = Files.newDirectoryStream(directory, "*.json")) {
            for (Path file : files) {
                var ref = AutoLlmFileUtil.read(file, Reference.class);
                if (!ref.owner().equals(prefix) && !ref.owner().startsWith(prefix + "/")) continue;
                var assets = new TreeSet<>(ref.assets()); assets.removeIf(path -> path.startsWith(".store/layers/"));
                AutoLlmFileUtil.write(file, new Reference(ref.owner(), assets));
            }
        }
        collect(campaign);
    }
    private static void collect(Path campaign) throws IOException {
        var retained = new HashSet<String>();
        Path references = campaign.resolve(".store/references");
        if (Files.isDirectory(references)) try (var files = Files.newDirectoryStream(references, "*.json")) {
            for (Path file : files) retained.addAll(AutoLlmFileUtil.read(file, Reference.class).assets());
        }
        for (String category : List.of("artifacts", "layers")) {
            Path directory = campaign.resolve(".store/" + category);
            if (!Files.isDirectory(directory)) continue;
            try (var files = Files.newDirectoryStream(directory)) {
                for (Path file : files) if (file.getFileName().toString().matches("[a-f0-9]{64}\\.(?:jar|zip)")
                        && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) && !retained.contains(relative(campaign, file))) Files.delete(file);
            }
        }
    }
    private static Path reference(Path campaign, Path owner) throws IOException {
        String name = relative(campaign, owner);
        String key = AutoLlmFileUtil.hash(new ByteArrayInputStream(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return campaign.resolve(".store/references/" + key + ".json");
    }
    private static String relative(Path root, Path path) throws IOException {
        root = root.toAbsolutePath().normalize(); path = path.toAbsolutePath().normalize();
        if (!path.startsWith(root)) throw new IOException("Asset/owner outside campaign: " + path);
        return root.relativize(path).toString().replace('\\', '/');
    }
}
