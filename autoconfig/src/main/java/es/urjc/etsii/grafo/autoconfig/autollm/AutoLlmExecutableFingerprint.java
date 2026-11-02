package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Exact entry-content identity, excluding ZIP layout and explicitly recognized AspectJ build metadata. */
public record AutoLlmExecutableFingerprint(int version, String sha256, Map<String,String> content, Map<String,String> buildMetadata) {
    public static AutoLlmExecutableFingerprint read(Path jar) throws IOException {
        var content = new TreeMap<String,String>(); var metadata = new TreeMap<String,String>();
        try (var zip = new ZipFile(jar.toFile())) {
            var seen = new HashSet<String>(); var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement(); if (entry.isDirectory()) continue;
                if (!seen.add(entry.getName())) throw new IOException("Duplicate executable entry: " + entry.getName());
                try (var input = zip.getInputStream(entry)) { add(entry.getName(), input, content, metadata, 0); }
            }
        }
        return new AutoLlmExecutableFingerprint(1, AutoLlmFileUtil.hash(jar), content, metadata);
    }
    private static void add(String name, InputStream input, Map<String,String> content, Map<String,String> metadata, int depth) throws IOException {
        if (depth > 8) throw new IOException("Executable archive nesting is too deep");
        if (name.endsWith(".jar")) {
            var buffered = new BufferedInputStream(input);
            buffered.mark(4); byte[] signature = buffered.readNBytes(4); buffered.reset();
            if (signature.length != 4 || signature[0] != 'P' || signature[1] != 'K'
                    || !(signature[2] == 3 && signature[3] == 4 || signature[2] == 5 && signature[3] == 6)) throw new IOException("Invalid nested JAR: " + name);
            // Mark even an empty nested archive; entry order and container compression do not affect identity.
            if (content.putIfAbsent(name + "!/", "archive") != null) throw new IOException("Duplicate executable entry: " + name);
            var zip = new ZipInputStream(new FilterInputStream(buffered) { @Override public void close() {} });
            var seen = new HashSet<String>();
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.isDirectory()) continue;
                if (!seen.add(entry.getName())) throw new IOException("Duplicate nested entry: " + name + "!/" + entry.getName());
                add(name + "!/" + entry.getName(), zip, content, metadata, depth + 1);
            }
            zip.close();
        } else {
            boolean generated = name.equals("builddef.lst") || name.equals("BOOT-INF/classes/builddef.lst")
                    || name.matches("BOOT-INF/lib/mork(?:-common)?-[^/]+\\.jar!/builddef\\.lst");
            var destination = generated ? metadata : content;
            if (destination.putIfAbsent(name, AutoLlmFileUtil.hash(input)) != null) throw new IOException("Duplicate executable entry: " + name);
        }
    }
    public boolean matches(AutoLlmExecutableFingerprint other) {
        return version == other.version && content.equals(other.content);
    }
}
