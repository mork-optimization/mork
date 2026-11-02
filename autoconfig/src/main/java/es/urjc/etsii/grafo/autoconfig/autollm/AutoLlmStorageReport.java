package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;

/** Read-only portable accounting. Logical bytes are not unique physical allocation. */
public final class AutoLlmStorageReport {
    private AutoLlmStorageReport() {}
    public record Usage(long logicalBytes, long files) {}
    public record Report(String observedAt, long logicalBytes, long files, long availableBytes,
                         Long allocatedBytes, Map<String,Usage> categories, Map<String,Object> artifactRetention, Map<String,Object> maintenance, long disappearedFiles, String note) {}

    public static Report inspect(Path root) {
        var totals = new TreeMap<String,long[]>();
        var retention = new TreeMap<String,Object>();
        var maintenance = new TreeMap<String,Object>();
        long[] missing = {0};
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    if (attributes.isRegularFile()) {
                        var total = totals.computeIfAbsent(category(root.relativize(file)), _ -> new long[2]);
                        total[0] += attributes.size(); total[1]++;
                        if (file.getFileName().toString().equals("artifact-retention.json") && file.getParent().getFileName().toString().equals("storage")) {
                            try { retention.put(root.relativize(file.getParent().getParent()).toString(), AutoLlmFileUtil.read(file, Object.class)); }
                            catch (UncheckedIOException error) { if (error.getCause() instanceof NoSuchFileException) missing[0]++; else throw error; }
                        }
                        if (Set.of("retention.json", "remote-retention.json").contains(file.getFileName().toString())) {
                            boolean storage = false; for (Path part : root.relativize(file)) if (part.toString().equals("storage")) storage = true;
                            if (storage) try { maintenance.put(root.relativize(file).toString(), AutoLlmFileUtil.read(file, Object.class)); }
                            catch (UncheckedIOException error) { if (error.getCause() instanceof NoSuchFileException) missing[0]++; else throw error; }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult visitFileFailed(Path file, IOException error) throws IOException {
                    if (error instanceof NoSuchFileException) { missing[0]++; return FileVisitResult.CONTINUE; }
                    throw error;
                }
            });
            var categories = new TreeMap<String,Usage>(); long bytes = 0, files = 0;
            for (var entry : totals.entrySet()) {
                categories.put(entry.getKey(), new Usage(entry.getValue()[0], entry.getValue()[1]));
                bytes += entry.getValue()[0]; files += entry.getValue()[1];
            }
            return new Report(Instant.now().toString(), bytes, files, Files.getFileStore(root).getUsableSpace(), null,
                    categories, retention, maintenance, missing[0], "Logical file sizes; allocated/shared physical bytes unavailable through the portable API. Symlinks are not followed. Live files may change during inspection.");
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private static String category(Path path) {
        for (Path part : path) {
            String name = part.toString();
            if (name.equals(".store")) return "shared-assets";
            if (name.equals(".git")) return "git";
            if (name.equals(".transfer")) return "transfer-cache";
            if (name.equals(".source-bundles")) return "source-bundles";
            if (name.equals("target")) return "build-output";
        }
        if (path.getFileName().toString().endsWith(".jar")) return "executables";
        for (Path part : path) if (part.toString().equals("source")) return "source-checkouts";
        return "evidence-and-metadata";
    }
}
