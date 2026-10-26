package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;

/** Verified cold storage for completed evidence trees, with optional files retained for execution. */
public final class DirectoryArchiveUtil {
    private DirectoryArchiveUtil() {}

    public static Path archivePath(Path directory) {
        return directory.resolveSibling(directory.getFileName() + ".zip");
    }

    public static void archive(Path directory) throws IOException { archive(directory, Set.of()); }

    /** Retained names are relative to directory; the ZIP still contains every original file. */
    public static void archive(Path directory, Set<String> retained) throws IOException {
        directory = directory.toAbsolutePath().normalize();
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) return;
        var files = new TreeMap<String,Path>();
        WorkloadArchiveUtil.addTree(files, directory, directory, false);
        if (files.isEmpty()) return;

        Path archive = archivePath(directory);
        if (!Files.exists(archive, LinkOption.NOFOLLOW_LINKS)) {
            Path temporary = Files.createTempFile(directory.getParent(), directory.getFileName() + "-", ".zip.tmp");
            try {
                WorkloadArchiveUtil.pack(temporary, files);
                WorkloadArchiveUtil.verify(temporary, directory);
                try { Files.move(temporary, archive, StandardCopyOption.ATOMIC_MOVE); }
                catch (AtomicMoveNotSupportedException e) { Files.move(temporary, archive); }
            } finally { Files.deleteIfExists(temporary); }
        }
        if (!Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Archive is not a regular file: " + archive);
        var entries = WorkloadArchiveUtil.verify(archive, directory);
        // A retry may find a verified archive and only part of the original tree left to remove.
        verifyRemaining(files, entries);
        var keep = new HashSet<Path>();
        for (String name : retained) {
            Path file = WorkloadPathUtil.inside(directory, name);
            for (Path parent = file; parent != null && parent.startsWith(directory); parent = parent.getParent()) keep.add(parent);
        }
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) if (!keep.contains(path)) Files.delete(path);
        }
    }

    private static void verifyRemaining(Map<String,Path> files, Map<String,WorkloadArchiveUtil.Entry> entries) throws IOException {
        for (var item : files.entrySet()) {
            var entry = entries.get(item.getKey());
            if (entry == null) throw new IOException("Unarchived file: " + item.getKey());
            Path file = item.getValue();
            if (Files.isSymbolicLink(file)) {
                // Path.of removes redundant separators, including the trailing slash on directory links.
                // Match the representation that NIO uses when restoring, while retaining the raw target in the ZIP.
                if (entry.link() == null || !Path.of(Files.readSymbolicLink(file).toString()).equals(Path.of(entry.link()))) throw new IOException("Archived link changed: " + file);
            } else if (entry.link() != null || !AutoLlmFileUtil.hash(file).equals(entry.sha256())) {
                throw new IOException("File changed after archiving: " + file);
            }
        }
    }

    /** Restore before reusing a cold checkout. Refuse to overwrite changed or additional files. */
    public static void restore(Path directory) throws IOException {
        directory = directory.toAbsolutePath().normalize();
        Path archive = archivePath(directory);
        if (!Files.exists(archive, LinkOption.NOFOLLOW_LINKS)) return;
        var entries = WorkloadArchiveUtil.verify(archive, directory);
        var files = new TreeMap<String,Path>();
        WorkloadArchiveUtil.addTree(files, directory, directory, false);
        verifyRemaining(files, entries);
        WorkloadArchiveUtil.unpack(archive, directory);
    }

    /** Read a loose file, falling back to its archived copy. Missing files return null. */
    public static byte[] read(Path directory, String name) throws IOException {
        directory = directory.toAbsolutePath().normalize();
        Path file = WorkloadPathUtil.inside(directory, name);
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) return Files.readAllBytes(file);
        Path archive = archivePath(directory);
        if (!Files.exists(archive, LinkOption.NOFOLLOW_LINKS)) return null;
        try (var zip = new ZipFile(archive.toFile())) {
            var entry = zip.getEntry(name);
            if (entry == null) return null;
            try (var input = zip.getInputStream(entry)) { return input.readAllBytes(); }
        }
    }

    /** Hash all evidence once, including verified archived members and any newer loose files. */
    public static Map<String,String> hashes(Path directory) throws IOException {
        directory = directory.toAbsolutePath().normalize();
        var hashes = new TreeMap<String,String>();
        Path archive = archivePath(directory);
        if (Files.exists(archive, LinkOption.NOFOLLOW_LINKS)) {
            for (var entry : WorkloadArchiveUtil.verify(archive, directory).entrySet()) {
                if (entry.getValue().link() != null) throw new IOException("Evidence cannot be a symbolic link: " + entry.getKey());
                hashes.put(entry.getKey(), entry.getValue().sha256());
            }
        }
        var files = new TreeMap<String,Path>();
        WorkloadArchiveUtil.addTree(files, directory, directory, false);
        for (var file : files.entrySet()) {
            if (Files.isSymbolicLink(file.getValue())) throw new IOException("Evidence cannot be a symbolic link: " + file.getKey());
            hashes.put(file.getKey(), AutoLlmFileUtil.hash(file.getValue()));
        }
        return hashes;
    }
}
