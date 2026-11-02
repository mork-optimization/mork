package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import static es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol.*;

/** Remote cleanup only releases bytes covered by a controller's durable collection acknowledgement. */
public final class RemoteCampaignStorage {
    private RemoteCampaignStorage() {}
    public record Acknowledgement(String jobId, String archiveSha256, String manifestSha256) {}
    public record Collected(int version, String jobId, String archiveSha256, long archiveBytes, long downloadMillis,
                            long collectionMillis, Map<String,WorkloadArchiveUtil.Entry> entries, State state) {}
    public record Released(Acknowledgement receipt, Map<String,WorkloadArchiveUtil.Entry> evidence,
                           Map<String,WorkloadArchiveUtil.Entry> inputs) {}

    public static boolean shared(String name) { return name.matches("\\.campaign-layers/[a-f0-9]{64}\\.zip"); }
    public static Path layer(Path root, String name) {
        if (shared(name)) return WorkloadPathUtil.inside(WorkloadPathUtil.remoteCampaign(root), ".store/layers/" + Path.of(name).getFileName());
        if (!name.matches("\\.layers/[a-f0-9]{64}\\.zip")) throw new IllegalArgumentException("Invalid layer path");
        return WorkloadPathUtil.inside(root, name);
    }
    public static void retainLayers(Path root, List<String> layers) throws IOException {
        Path campaign = WorkloadPathUtil.remoteCampaign(root), store = campaign.resolve(".store");
        Files.createDirectories(store);
        try (var channel = FileChannel.open(store.resolve("assets.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.lock()) {
            if (Files.exists(root.resolve("storage-retired.json"))) throw new IOException("Remote owner has been retired");
            Path file = reference(campaign, root);
            var names = new TreeSet<String>();
            if (Files.exists(file)) names.addAll(AutoLlmFileUtil.read(file, CampaignAssetStore.Reference.class).assets());
            for (String name : layers) {
                if (!shared(name)) throw new IOException("Expected campaign layer");
                names.add(".store/layers/" + Path.of(name).getFileName());
            }
            AutoLlmFileUtil.write(file, new CampaignAssetStore.Reference(campaign.relativize(root).toString(), names));
            AutoLlmFileUtil.write(root.resolve(".managed-storage.json"), Map.of("version", 1));
        }
    }
    public static void install(Path root, String name) throws IOException {
        Path archive = layer(root, name);
        if (!shared(name)) { WorkloadArchiveUtil.installLayer(archive, root); return; }
        String hash = archive.getFileName().toString().replace(".zip", "");
        Path receipt = root.resolve(".installed-layers/" + hash + ".json");
        if (Files.isRegularFile(receipt) && WorkloadArchiveUtil.matches(root, WorkloadArchiveUtil.verify(archive, root))) return;
        if (!hash.equals(AutoLlmFileUtil.hash(archive))) throw new IOException("Shared layer changed");
        WorkloadArchiveUtil.unpack(archive, root);
        AutoLlmFileUtil.write(receipt, Map.of("sha256", hash));
    }
    public static Object acknowledge(Path root, String id, Acknowledgement acknowledgement) throws IOException {
        Path directory = root.resolve(".jobs/" + id);
        if (!id.matches("[a-f0-9]{64}") || !id.equals(acknowledgement.jobId())) throw new IOException("Invalid collection acknowledgement");
        try (var channel = FileChannel.open(directory.resolve("run.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.lock()) {
            Job job = AutoLlmFileUtil.read(directory.resolve("job.json"), Job.class);
            if (!job.managedStorage()) throw new IOException("Legacy workloads do not support automatic release");
            State state = AutoLlmFileUtil.read(directory.resolve("state.json"), State.class);
            if (state.status().inProgress() || state.status() == JobStatus.NOT_FOUND || !Objects.equals(state.archiveSha256(), acknowledgement.archiveSha256())) throw new IOException("Collection acknowledgement does not match completed evidence");
            Path record = directory.resolve("acknowledged.json");
            if (Files.exists(record)) {
                if (!AutoLlmFileUtil.read(record, Released.class).receipt().equals(acknowledgement)) throw new IOException("Conflicting collection acknowledgement");
            } else {
                Path archive = directory.resolve("result.zip");
                if (!acknowledgement.archiveSha256().equals(AutoLlmFileUtil.hash(archive))) throw new IOException("Remote evidence changed");
                var evidence = WorkloadArchiveUtil.verify(archive, root);
                if (!AutoLlmFileUtil.canonicalHash(evidence).equals(acknowledgement.manifestSha256())) throw new IOException("Collected manifest does not match remote evidence");
                var inputs = WorkloadArchiveUtil.verify(directory.resolve("input.zip"), root);
                AutoLlmFileUtil.write(record, new Released(acknowledgement, evidence, inputs));
                force(record);
            }
            Files.deleteIfExists(directory.resolve("result.zip")); Files.deleteIfExists(directory.resolve("input.zip"));
            return Map.of("jobId", id, "acknowledged", true);
        }
    }
    /** Root is a terminal trial/target. Preserve unknown or changed files, including unarchived Git metadata. */
    public static Object retire(Path root) throws IOException {
        if (!Files.exists(root.resolve(".managed-storage.json"))) throw new IOException("Not a managed remote owner");
        try (var owner = FileChannel.open(root.resolve(".storage-owner.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var ownerLock = owner.lock();
             var workspace = FileChannel.open(WorkloadPathUtil.workspace(root).resolve("execution.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = workspace.tryLock()) {
            if (lock == null) throw new IOException("Remote workload is still active");
            var known = new TreeMap<String,Set<WorkloadArchiveUtil.Entry>>();
            Path jobs = root.resolve(".jobs");
            if (Files.isDirectory(jobs)) try (var directories = Files.newDirectoryStream(jobs)) {
                for (Path directory : directories) if (Files.isRegularFile(directory.resolve("job.json"))) {
                    var job = AutoLlmFileUtil.read(directory.resolve("job.json"), Job.class);
                    var state = AutoLlmFileUtil.read(directory.resolve("state.json"), State.class);
                    if (!job.managedStorage() || state.status().inProgress() || !Files.exists(directory.resolve("acknowledged.json"))) throw new IOException("Unacknowledged remote job: " + job.id());
                    var released = AutoLlmFileUtil.read(directory.resolve("acknowledged.json"), Released.class);
                    add(known, released.inputs()); add(known, released.evidence());
                }
            }
            Path campaign = WorkloadPathUtil.remoteCampaign(root), store = campaign.resolve(".store"); Files.createDirectories(store);
            try (var channel = FileChannel.open(store.resolve("assets.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var assets = channel.lock()) {
                Path reference = reference(campaign, root);
                if (Files.exists(reference)) for (String name : AutoLlmFileUtil.read(reference, CampaignAssetStore.Reference.class).assets()) {
                    Path archive = WorkloadPathUtil.inside(campaign, name);
                    if (Files.exists(archive)) add(known, WorkloadArchiveUtil.verify(archive, root));
                }
                AutoLlmFileUtil.write(root.resolve("storage-retired.json"), Map.of("version", 1, "retiredAt", Instant.now().toString()));
                force(root.resolve("storage-retired.json"));
                long releasedBytes = 0; int releasedFiles = 0;
                for (var entry : known.entrySet()) {
                    if (entry.getKey().startsWith(".jobs/") || entry.getKey().startsWith(".store/")) continue;
                    Path file = WorkloadPathUtil.inside(root, entry.getKey());
                    if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) continue;
                    boolean matches = false;
                    for (var expected : entry.getValue()) {
                        if (Files.isSymbolicLink(file)) matches |= expected.link() != null && Files.readSymbolicLink(file).toString().equals(expected.link());
                        else if (Files.isRegularFile(file) && expected.link() == null) matches |= AutoLlmFileUtil.hash(file).equals(expected.sha256());
                    }
                    if (matches) { releasedBytes += Files.isSymbolicLink(file) ? 0 : Files.size(file); Files.delete(file); releasedFiles++; }
                }
                Files.deleteIfExists(reference);
                var retained = new HashSet<String>();
                Path references = store.resolve("references");
                if (Files.isDirectory(references)) try (var files = Files.newDirectoryStream(references, "*.json")) {
                    for (Path file : files) retained.addAll(AutoLlmFileUtil.read(file, CampaignAssetStore.Reference.class).assets());
                }
                Path layers = store.resolve("layers");
                if (Files.isDirectory(layers)) try (var files = Files.newDirectoryStream(layers, "*.zip")) {
                    for (Path file : files) if (file.getFileName().toString().matches("[a-f0-9]{64}\\.zip") && !retained.contains(".store/layers/" + file.getFileName())) Files.delete(file);
                }
                WorkloadArchiveUtil.deleteTree(root.resolve(".installed-layers"));
                var result = Map.of("releasedFiles", releasedFiles, "releasedBytes", releasedBytes, "retired", true);
                AutoLlmFileUtil.write(root.resolve("storage-release.json"), result);
                return result;
            }
        }
    }
    private static void add(Map<String,Set<WorkloadArchiveUtil.Entry>> known, Map<String,WorkloadArchiveUtil.Entry> entries) {
        for (var entry : entries.entrySet()) known.computeIfAbsent(entry.getKey(), _ -> new HashSet<>()).add(entry.getValue());
    }
    private static Path reference(Path campaign, Path root) throws IOException {
        String key = AutoLlmFileUtil.hash(new ByteArrayInputStream(root.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return campaign.resolve(".store/references/" + key + ".json");
    }
    public static void force(Path path) throws IOException {
        try (var channel = FileChannel.open(path, StandardOpenOption.READ)) { channel.force(true); }
    }
    /** Revalidate retained bytes before retrying an acknowledgement after a controller restart. */
    public static void verifyCollection(Path root, Collected receipt) throws IOException {
        var archives = new HashMap<Path,Map<String,WorkloadArchiveUtil.Entry>>();
        for (var item : receipt.entries().entrySet()) {
            Path file = WorkloadPathUtil.inside(root, item.getKey()); var expected = item.getValue();
            if (expected.link() != null && Files.isSymbolicLink(file) && Files.readSymbolicLink(file).toString().equals(expected.link())) continue;
            if (expected.link() == null && es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmEvidenceFiles.exists(file)) {
                if (!expected.sha256().equals(AutoLlmFileUtil.hash(file))) throw new IOException("Collected evidence changed: " + file);
                force(Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) ? file : es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmEvidenceFiles.archive(file));
                continue;
            }
            boolean verified = false;
            for (Path parent = file.getParent(); parent != null && parent.startsWith(root); parent = parent.getParent()) {
                Path archive = DirectoryArchiveUtil.archivePath(parent);
                if (!Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS)) continue;
                var entries = archives.get(archive);
                if (entries == null) { entries = WorkloadArchiveUtil.verify(archive, parent); force(archive); archives.put(archive, entries); }
                var retained = entries.get(parent.relativize(file).toString().replace('\\', '/'));
                if (expected.equals(retained)) { verified = true; break; }
            }
            if (!verified) throw new IOException("Collected evidence is missing or changed: " + file);
        }
    }
}
