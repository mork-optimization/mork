package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import tools.jackson.core.type.TypeReference;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.*;

/** Portable snapshots with content hashes, permissions and bounded path resolution. */
public final class WorkloadArchiveUtil {
    private static final String MANIFEST = ".mork-transfer.json";
    public record Entry(String sha256, int mode, String link) {}
    private WorkloadArchiveUtil() {}

    public static void addTree(Map<String,Path> files, Path root, Path directory, boolean source) throws IOException {
        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) return;
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) {
                if (source && !path.equals(directory) && excluded(path.getFileName().toString())) return FileVisitResult.SKIP_SUBTREE;
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) {
                String name = root.relativize(path).toString().replace('\\', '/');
                if (source && excluded(path.getFileName().toString())) return FileVisitResult.CONTINUE;
                if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) files.put(name, path);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    private static boolean excluded(String name) { return Set.of(".git", "target", "node_modules", ".codex", ".mork-metadata").contains(name); }

    public static void pack(Path archive, Map<String,Path> files) throws IOException {
        var manifest = new TreeMap<String,Entry>();
        Files.createDirectories(archive.getParent());
        try (var output = new ZipOutputStream(Files.newOutputStream(archive))) {
            for (var item : new TreeMap<>(files).entrySet()) {
                if (item.getKey().equals(MANIFEST)) throw new IllegalArgumentException("Reserved transfer name");
                Path path = item.getValue();
                String link = Files.isSymbolicLink(path) ? Files.readSymbolicLink(path).toString() : null;
                manifest.put(item.getKey(), new Entry(link == null ? AutoLlmFileUtil.hash(path) : null, mode(path), link));
                var entry = new ZipEntry(item.getKey()); entry.setTime(0); output.putNextEntry(entry);
                if (link == null) Files.copy(path, output);
                output.closeEntry();
            }
            var entry = new ZipEntry(MANIFEST); entry.setTime(0); output.putNextEntry(entry);
            output.write(AutoLlmFileUtil.JSON.writeValueAsBytes(manifest)); output.closeEntry();
        }
    }

    public static void unpack(Path archive, Path root) throws IOException {
        Files.createDirectories(root);
        try (var zip = new ZipFile(archive.toFile())) {
            var manifest = verifiedEntries(zip, root);
            for (var item : manifest.entrySet()) {
                Path target = WorkloadPathUtil.inside(root, item.getKey());
                Files.createDirectories(target.getParent());
                if (Files.isSymbolicLink(target)) Files.delete(target);
                if (item.getValue().link() == null) {
                    Path temporary = target.resolveSibling(target.getFileName() + ".transfer-tmp");
                    try (var input = zip.getInputStream(zip.getEntry(item.getKey()))) { Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING); }
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                    permissions(target, item.getValue().mode());
                }
            }
            for (var item : manifest.entrySet()) if (item.getValue().link() != null) {
                Path target = WorkloadPathUtil.inside(root, item.getKey());
                Files.deleteIfExists(target); Files.createSymbolicLink(target, Path.of(item.getValue().link()));
            }
        }
    }

    /** Verify every member without expanding the archive. */
    public static Map<String,Entry> verify(Path archive, Path root) throws IOException {
        try (var zip = new ZipFile(archive.toFile())) { return Map.copyOf(verifiedEntries(zip, root)); }
    }

    private static Map<String,Entry> verifiedEntries(ZipFile zip, Path root) throws IOException {
        var descriptor = zip.getEntry(MANIFEST);
        if (descriptor == null) throw new IOException("Transfer manifest missing");
        Map<String,Entry> manifest;
        try (var input = zip.getInputStream(descriptor)) { manifest = AutoLlmFileUtil.JSON.readValue(input.readAllBytes(), new TypeReference<>() {}); }
        var seen = new HashSet<String>();
        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            var entry = entries.nextElement();
            if (entry.getName().equals(MANIFEST)) continue;
            Entry expected = manifest.get(entry.getName());
            if (expected == null || !seen.add(entry.getName())) throw new IOException("Unexpected or duplicate transfer entry");
            WorkloadPathUtil.inside(root, entry.getName());
            if (expected.link() == null) try (var input = zip.getInputStream(entry)) {
                if (!expected.sha256().equals(AutoLlmFileUtil.hash(input))) throw new IOException("Transfer checksum failed: " + entry.getName());
            } else {
                Path path = WorkloadPathUtil.inside(root, entry.getName());
                if (Path.of(expected.link()).isAbsolute() || !path.getParent().resolve(expected.link()).normalize().startsWith(root)) throw new IOException("Transfer symlink escapes workspace");
            }
        }
        if (!seen.equals(manifest.keySet())) throw new IOException("Incomplete transfer archive");
        return manifest;
    }
    public static void deleteTree(Path path) throws IOException {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return;
        try (var paths = Files.walk(path)) {
            for (Path item : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(item);
        }
    }
    private static int mode(Path path) throws IOException {
        try {
            int value = 0;
            for (var permission : Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS)) value |= 1 << (8 - permission.ordinal());
            return value;
        } catch (UnsupportedOperationException e) { return Files.isExecutable(path) ? 0755 : 0644; }
    }
    private static void permissions(Path path, int mode) throws IOException {
        try {
            var permissions = EnumSet.noneOf(PosixFilePermission.class);
            for (var permission : PosixFilePermission.values()) if ((mode & (1 << (8 - permission.ordinal()))) != 0) permissions.add(permission);
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException e) { if ((mode & 0111) != 0) path.toFile().setExecutable(true); }
    }
}
