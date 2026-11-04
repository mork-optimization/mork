package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.util.InstancePathUtil;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** User-approved dataset membership, independent of campaign workspaces and baseline builds. */
public final class AutoLlmDatasetLibrary {
    static final String MARKER = "dataset-library.json";
    public record Request(AutoLlmConfig configuration, String name, String digest) {}
    public record Review(String digest, String name, String repository, String artifact, List<String> training,
                         List<String> comparison, Map<String,String> hashes, Map<String,String> aliases, Object provenance) {}
    public record Entry(String id, String name, String approvedAt, String artifact, String trainingPath,
                        String comparisonPath, int trainingCount, int comparisonCount, int overlap, Review review,
                        Map<String,String> files) {}
    private final AutoLlmConfig config;
    private final Path repository;
    private final Path root;

    public AutoLlmDatasetLibrary(AutoLlmConfig config) {
        this.config = AutoLlmNormalization.normalize(Objects.requireNonNull(config, "Dataset configuration is required"));
        repository = AutoLlmNormalization.localPath(Path.of(this.config.project().repository()));
        root = repository.resolve(".mork/datasets");
    }

    static List<String> members(Path repository, String input, String label) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException(label + " path is required; select existing datasets or explicitly prepare a screened dataset");
        var values = InstancePathUtil.list(repository.resolve(input).toString());
        if (values.isEmpty()) throw new IllegalArgumentException(label + " dataset is empty");
        if (new HashSet<>(values).size() != values.size()) throw new IllegalArgumentException(label + " dataset contains duplicate instances");
        return values;
    }

    public Review preview(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Give the approved selection a name");
        var training = members(repository, config.trainingPath(), "Training");
        var comparison = members(repository, config.comparisonPath(), "Comparison");
        Path train = repository.resolve(config.trainingPath()).normalize(), test = repository.resolve(config.comparisonPath()).normalize();
        verifySelection(train, test);
        var hashes = new TreeMap<String,String>();
        var selected = new LinkedHashSet<>(training); selected.addAll(comparison);
        for (var input : selected) hashes.put(input, AutoLlmFileUtil.inputHash(input));
        var aliases = AutoLlmInputUtil.aliases(train, test);
        Object provenance = provenance(train, test);
        var unsigned = new Review(null, name.strip(), repository.toString(), config.project().artifact(), training, comparison, hashes, aliases, provenance);
        return new Review(AutoLlmFileUtil.canonicalHash(unsigned), unsigned.name(), unsigned.repository(), unsigned.artifact(), training, comparison, hashes, aliases, provenance);
    }

    public Entry publish(String name, String digest) {
        Review review = preview(name);
        if (!review.digest().equals(digest)) throw new IllegalArgumentException("Dataset selection changed or was not reviewed; preview and approve its current digest");
        if (AutoLlmCleanup.requested(root)) throw new IllegalStateException("Dataset library is inside a pending cleanup scope");
        Path destination = root.resolve(digest);
        if (Files.exists(destination)) return get(digest);
        Path staging = null;
        try {
            Files.createDirectories(root);
            AutoLlmFileUtil.write(root.resolve(MARKER), Map.of("kind", "approved-dataset-library", "version", 1));
            staging = Files.createTempDirectory(root, ".publishing-");
            var copied = new LinkedHashMap<String,String>();
            var files = new TreeMap<String,String>();
            for (String input : review.hashes().keySet()) {
                int split = input.indexOf('!');
                Path container = Path.of(split < 0 ? input : input.substring(0, split));
                String relative = "data/" + AutoLlmFileUtil.canonicalHash(container.toString()) + "/" + container.getFileName();
                Path target = staging.resolve(relative);
                if (!Files.exists(target)) {
                    String hash = AutoLlmFileUtil.hash(container);
                    Files.createDirectories(target.getParent()); Files.copy(container, target);
                    if (!hash.equals(AutoLlmFileUtil.hash(target))) throw new IllegalStateException("Instance container changed while copying: " + container);
                    files.put(relative, hash);
                }
                String token = relative + (split < 0 ? "" : input.substring(split));
                if (!review.hashes().get(input).equals(AutoLlmFileUtil.inputHash(InstancePathUtil.resolve(staging, token))))
                    throw new IllegalStateException("Instance changed after review: " + input);
                copied.put(input, token);
            }
            writeIndex(staging.resolve("training.index"), review.training(), copied);
            writeIndex(staging.resolve("comparison.index"), review.comparison(), copied);
            var aliases = new LinkedHashMap<>(copied);
            for (var alias : review.aliases().entrySet()) if (copied.containsKey(alias.getValue())) aliases.put(alias.getKey(), copied.get(alias.getValue()));
            AutoLlmFileUtil.write(staging.resolve("aliases.json"), aliases);
            AutoLlmFileUtil.write(staging.resolve("provenance.json"), Map.of("selectionId", digest, "name", review.name(), "historical", review.provenance()));
            for (String file : List.of("training.index", "comparison.index", "aliases.json", "provenance.json")) files.put(file, AutoLlmFileUtil.hash(staging.resolve(file)));
            var overlap = new HashSet<>(review.training()); overlap.retainAll(review.comparison());
            var entry = new Entry(digest, review.name(), Instant.now().toString(), review.artifact(), destination.resolve("training.index").toString(),
                    destination.resolve("comparison.index").toString(), review.training().size(), review.comparison().size(), overlap.size(), review, files);
            AutoLlmFileUtil.write(staging.resolve("selection.json"), entry);
            // Recheck membership and provenance too, not just the bytes copied above.
            if (!digest.equals(preview(name).digest())) throw new IllegalStateException("Dataset selection changed during publication; review again");
            try { Files.move(staging, destination, StandardCopyOption.ATOMIC_MOVE); }
            catch (FileAlreadyExistsException e) { return get(digest); }
            return get(digest);
        } catch (IOException e) { throw new UncheckedIOException(e); }
        finally {
            if (staging != null && Files.exists(staging)) try (var paths = Files.walk(staging)) {
                var ordered = new ArrayList<>(paths.toList()); ordered.sort(Comparator.reverseOrder());
                for (Path path : ordered) Files.deleteIfExists(path);
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
    }

    private static void writeIndex(Path file, List<String> members, Map<String,String> copied) throws IOException {
        var tokens = new ArrayList<String>(); for (String member : members) tokens.add(copied.get(member)); Files.write(file, tokens);
    }

    public List<Entry> list() {
        var entries = new ArrayList<Entry>();
        if (!Files.isDirectory(root)) return entries;
        try (var children = Files.newDirectoryStream(root)) {
            for (Path child : children) if (child.getFileName().toString().matches("[a-f0-9]{64}") && Files.isRegularFile(child.resolve("selection.json"))) {
                var entry = AutoLlmFileUtil.read(child.resolve("selection.json"), Entry.class);
                if (config.project().artifact().equals(entry.artifact())) entries.add(entry);
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        entries.sort(Comparator.comparing(Entry::approvedAt).reversed()); return entries;
    }

    public Entry get(String id) {
        if (id == null || !id.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Invalid dataset selection ID");
        var entry = verify(root.resolve(id));
        if (!config.project().artifact().equals(entry.artifact())) throw new IllegalArgumentException("Dataset belongs to another project module");
        return entry;
    }

    private static Entry verify(Path directory) {
        var entry = AutoLlmFileUtil.read(directory.resolve("selection.json"), Entry.class);
        var r = entry.review();
        var unsigned = new Review(null, r.name(), r.repository(), r.artifact(), r.training(), r.comparison(), r.hashes(), r.aliases(), r.provenance());
        if (!entry.id().equals(AutoLlmFileUtil.canonicalHash(unsigned)) || !entry.id().equals(directory.getFileName().toString()))
            throw new IllegalStateException("Approved dataset review changed: " + directory);
        for (var file : entry.files().entrySet()) if (!file.getValue().equals(AutoLlmFileUtil.hash(AutoLlmFileUtil.inside(directory, file.getKey()))))
            throw new IllegalStateException("Approved dataset file changed: " + file.getKey());
        return entry;
    }

    static void verifySelection(Path training, Path comparison) {
        var parents = new LinkedHashSet<Path>();
        for (Path input : List.of(training, comparison)) if (input.getParent() != null) parents.add(input.getParent());
        for (Path parent : parents) if (Files.isRegularFile(parent.resolve("selection.json"))) verify(parent);
    }

    static Object provenance(Path training, Path comparison) {
        var sources = new LinkedHashMap<String,Object>();
        for (Path input : List.of(training, comparison)) {
            Path parent = input.getParent();
            if (Files.isRegularFile(parent.resolve("selection.json"))) {
                sources.put(parent.toString(), AutoLlmFileUtil.read(parent.resolve("provenance.json"), Object.class));
            } else if (Files.isRegularFile(parent.resolve("proposal.json")) && Files.isRegularFile(parent.getParent().resolve("remote-screening.json"))) {
                var historical = new LinkedHashMap<String,Object>();
                for (String file : List.of("proposal.json", "manifest.json", "source-metadata.json")) if (Files.isRegularFile(parent.resolve(file)))
                    historical.put(file, AutoLlmFileUtil.read(parent.resolve(file), Object.class));
                historical.put("basis", "Historical screening; its timings and baseline are not current campaign measurements");
                sources.put(parent.toString(), historical);
            } else {
                if (parent.getParent() == null) continue;
                Path controls = parent.getParent().resolve("control");
                var evidence = new LinkedHashMap<String,Object>();
                for (String file : List.of("dataset-origin.json", "dataset-decisions.json", "original-pool.json"))
                    if (Files.isRegularFile(controls.resolve(file))) evidence.put(file, AutoLlmFileUtil.read(controls.resolve(file), Object.class));
                if (!evidence.isEmpty()) sources.put(controls.toString(), evidence);
            }
        }
        return sources;
    }
}
