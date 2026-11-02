package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.zip.*;

/** Closed evidence keeps its logical filename; independently compressed chunks permit bounded byte reads. */
public final class AutoLlmEvidenceFiles {
    static final int CHUNK_BYTES = 1024 * 1024;
    private static final String DIRECTORY = ".evidence";
    public record Chunk(int bytes, String sha256) {}
    public record Index(int version, String name, long bytes, long modifiedMillis, String sha256, List<Chunk> chunks) {}
    public record Attributes(long size, FileTime modified, Object fileKey) {}
    private AutoLlmEvidenceFiles() {}

    public static Path archive(Path file) {
        String name;
        try { name = AutoLlmFileUtil.hash(new ByteArrayInputStream(file.getFileName().toString().getBytes(StandardCharsets.UTF_8))); }
        catch (IOException e) { throw new UncheckedIOException(e); }
        return file.resolveSibling(DIRECTORY).resolve(name + ".zip");
    }
    public static boolean exists(Path file) {
        return Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) || Files.isRegularFile(archive(file), LinkOption.NOFOLLOW_LINKS);
    }
    public static boolean archived(Path file) {
        return !Files.exists(file, LinkOption.NOFOLLOW_LINKS) && Files.isRegularFile(archive(file), LinkOption.NOFOLLOW_LINKS);
    }
    public static Attributes attributes(Path file) throws IOException {
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            try {
                var attributes = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (!attributes.isRegularFile()) throw new IOException("Evidence is not a regular file: " + file);
                return new Attributes(attributes.size(), attributes.lastModifiedTime(), attributes.fileKey());
            } catch (NoSuchFileException ignored) { /* Compaction published the archive before removing the loose copy. */ }
        }
        Path archive = archive(file);
        try (var zip = new ZipFile(archive.toFile())) {
            var index = index(zip, file.getFileName().toString());
            return new Attributes(index.bytes(), FileTime.fromMillis(index.modifiedMillis()),
                    archive.toAbsolutePath() + ":" + Files.getLastModifiedTime(archive).toMillis() + ":" + index.sha256());
        }
    }
    public static InputStream open(Path file) throws IOException {
        return open(file, 0);
    }
    public static InputStream open(Path file, long offset) throws IOException {
        if (offset < 0) throw new IllegalArgumentException("Negative evidence offset");
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            try {
                var channel = FileChannel.open(file, StandardOpenOption.READ);
                try {
                    if (offset > channel.size()) throw new EOFException("Evidence offset exceeds length");
                    channel.position(offset); return Channels.newInputStream(channel);
                } catch (Throwable error) { channel.close(); throw error; }
            } catch (NoSuchFileException ignored) { /* A concurrent reader can cross the atomic archive publication. */ }
        }
        var zip = new ZipFile(archive(file).toFile());
        try { return new ArchivedInput(zip, index(zip, file.getFileName().toString()), offset); }
        catch (Throwable error) { zip.close(); throw error; }
    }
    public static BufferedReader reader(Path file) throws IOException {
        return new BufferedReader(new InputStreamReader(open(file), StandardCharsets.UTF_8));
    }
    public static byte[] range(Path file, long offset, int length) throws IOException {
        if (length < 0) throw new IllegalArgumentException("Negative evidence length");
        try (var input = open(file, offset)) { return input.readNBytes(length); }
    }
    public static String readString(Path file) throws IOException {
        try (var input = open(file)) { return new String(input.readAllBytes(), StandardCharsets.UTF_8); }
    }

    /** Includes virtual archived files, hiding physical containers. Loose replacements take precedence. */
    public static List<Path> files(Path root) throws IOException {
        var result = new TreeSet<Path>();
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) return new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) throws IOException {
                if (!directory.getFileName().toString().equals(DIRECTORY)) return FileVisitResult.CONTINUE;
                try (var paths = Files.newDirectoryStream(directory, "*.zip")) {
                    for (Path archive : paths) {
                        if (!Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS)) continue;
                        try (var zip = new ZipFile(archive.toFile())) {
                            var index = index(zip, null);
                            Path logical = directory.getParent().resolve(index.name());
                            if (!archive(logical).equals(archive)) throw new IOException("Evidence archive identity mismatch: " + archive);
                            result.add(logical);
                        }
                    }
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                if (attributes.isRegularFile()) result.add(file);
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFileFailed(Path file, IOException error) throws IOException {
                if (error instanceof NoSuchFileException) return FileVisitResult.CONTINUE;
                throw error;
            }
        });
        return new ArrayList<>(result);
    }

    /** Only scheduler-owned terminal trials in the new layout may discard verified loose evidence. */
    public static void compact(Path campaign, Path trial, AutoLlmCampaign.Trial state) throws IOException {
        if (!state.status.terminal() || !Files.isRegularFile(campaign.resolve("control/storage-layout.json"))
                || AutoLlmCampaignStore.hasUncollectedJobs(trial)) return;
        // Finish the derived activity stream before freezing its input and output logs.
        for (var attempt : state.attempts) if (attempt.stage.generation()) {
            Path directory = AutoLlmFileUtil.inside(campaign, attempt.directory);
            long previous = -1;
            while (true) {
                var progress = CodexProgressUtil.refresh(directory);
                if (progress.cursor == previous) break;
                previous = progress.cursor;
            }
        }
        for (String section : List.of("attempts", "metadata-requests")) {
            Path directory = trial.resolve(section);
            if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) continue;
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) {
                    String name = path.getFileName().toString();
                    return Set.of(DIRECTORY, "source", ".git", "control", "storage", ".store").contains(name)
                            || name.startsWith("source-snapshot-") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    String name = file.getFileName().toString();
                    if (attributes.isRegularFile() && attributes.size() >= 4096
                            && (name.endsWith(".log") || name.endsWith(".jsonl") || name.endsWith(".csv") || name.endsWith(".txt"))) compact(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /** Publish and verify every chunk and the whole original before unlinking; retries are idempotent. */
    static synchronized void compact(Path file) throws IOException {
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) return;
        Path archive = archive(file); Files.createDirectories(archive.getParent());
        if (!Files.exists(archive, LinkOption.NOFOLLOW_LINKS)) {
            var attributes = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            String hash = AutoLlmFileUtil.hash(file);
            Path temporary = Files.createTempFile(archive.getParent(), "packing-", ".tmp");
            try {
                var chunks = new ArrayList<Chunk>(); long size = 0;
                try (var output = new ZipOutputStream(Files.newOutputStream(temporary)); var input = Files.newInputStream(file)) {
                    while (true) {
                        byte[] bytes = input.readNBytes(CHUNK_BYTES); if (bytes.length == 0) break;
                        chunks.add(new Chunk(bytes.length, AutoLlmFileUtil.hash(new ByteArrayInputStream(bytes))));
                        output.putNextEntry(new ZipEntry(chunkName(chunks.size() - 1))); output.write(bytes); output.closeEntry(); size += bytes.length;
                    }
                    var index = new Index(1, file.getFileName().toString(), size, attributes.lastModifiedTime().toMillis(), hash, chunks);
                    output.putNextEntry(new ZipEntry("index.json")); output.write(AutoLlmFileUtil.JSON.writeValueAsBytes(index)); output.closeEntry();
                }
                verify(temporary, file);
                try { Files.move(temporary, archive, StandardCopyOption.ATOMIC_MOVE); }
                catch (AtomicMoveNotSupportedException e) { Files.move(temporary, archive); }
            } finally { Files.deleteIfExists(temporary); }
        }
        verify(archive, file);
        try (var channel = FileChannel.open(archive, StandardOpenOption.WRITE)) { channel.force(true); }
        Files.delete(file);
    }
    private static void verify(Path archive, Path original) throws IOException {
        if (!Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Evidence archive is not regular: " + archive);
        try (var zip = new ZipFile(archive.toFile())) {
            var index = index(zip, original.getFileName().toString());
            try (var input = new ArchivedInput(zip, index, 0)) {
                if (!AutoLlmFileUtil.hash(input).equals(index.sha256())) throw new IOException("Evidence archive checksum mismatch: " + archive);
            }
            if (!Files.isRegularFile(original, LinkOption.NOFOLLOW_LINKS) || Files.size(original) != index.bytes()
                    || !AutoLlmFileUtil.hash(original).equals(index.sha256())) throw new IOException("Evidence changed during compression: " + original);
        }
    }
    private static String chunkName(int number) { return "chunks/" + number; }
    private static Index index(ZipFile zip, String expectedName) throws IOException {
        var entry = zip.getEntry("index.json");
        if (entry == null || entry.getSize() > 64L * 1024 * 1024) throw new IOException("Invalid evidence index");
        Index index;
        try (var input = zip.getInputStream(entry)) { index = AutoLlmFileUtil.JSON.readValue(input, Index.class); }
        if (index.version() != 1 || index.name() == null || index.name().isBlank() || index.name().equals(".") || index.name().equals("..")
                || index.name().contains("/") || index.name().contains("\\") || (expectedName != null && !expectedName.equals(index.name()))
                || index.bytes() < 0 || index.chunks() == null || index.sha256() == null) throw new IOException("Invalid evidence index identity");
        long size = 0;
        for (int i = 0; i < index.chunks().size(); i++) {
            var chunk = index.chunks().get(i);
            if (chunk.bytes() <= 0 || chunk.bytes() > CHUNK_BYTES || i < index.chunks().size() - 1 && chunk.bytes() != CHUNK_BYTES
                    || chunk.sha256() == null || zip.getEntry(chunkName(i)) == null) throw new IOException("Invalid evidence chunk index");
            size += chunk.bytes();
        }
        if (size != index.bytes()) throw new IOException("Evidence index length mismatch");
        return index;
    }
    private static final class ArchivedInput extends InputStream {
        private final ZipFile zip;
        private final Index index;
        private long position;
        private int loaded = -1;
        private byte[] bytes;
        ArchivedInput(ZipFile zip, Index index, long position) throws IOException {
            if (position > index.bytes()) throw new EOFException("Evidence offset exceeds length");
            this.zip = zip; this.index = index; this.position = position;
        }
        private void load() throws IOException {
            int number = (int)(position / CHUNK_BYTES);
            if (number == loaded) return;
            var chunk = index.chunks().get(number);
            try (var input = zip.getInputStream(zip.getEntry(chunkName(number)))) {
                bytes = input.readNBytes(chunk.bytes() + 1);
                if (bytes.length != chunk.bytes() || !AutoLlmFileUtil.hash(new ByteArrayInputStream(bytes)).equals(chunk.sha256())) throw new IOException("Evidence chunk checksum mismatch: " + index.name());
            }
            loaded = number;
        }
        @Override public int read() throws IOException {
            if (position == index.bytes()) return -1;
            load(); return bytes[(int)(position++ % CHUNK_BYTES)] & 255;
        }
        @Override public int read(byte[] target, int offset, int length) throws IOException {
            Objects.checkFromIndexSize(offset, length, target.length);
            if (length == 0) return 0;
            if (position == index.bytes()) return -1;
            load(); int start = (int)(position % CHUNK_BYTES), count = Math.min(length, bytes.length - start);
            System.arraycopy(bytes, start, target, offset, count); position += count; return count;
        }
        @Override public void close() throws IOException { zip.close(); }
    }
}
