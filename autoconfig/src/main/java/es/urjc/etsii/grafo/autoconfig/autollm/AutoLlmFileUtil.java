package es.urjc.etsii.grafo.autoconfig.autollm;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public final class AutoLlmFileUtil {
    public static final JsonMapper JSON = new JsonMapper();
    private static final TypeReference<Map<String, Object>> OBJECT_TYPE = new TypeReference<>() {};
    private AutoLlmFileUtil() {}
    public static void write(Path path, Object value) {
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            Path temp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temp, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(value));
            try { Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static <T> T read(Path path, Class<T> type) {
        try { return JSON.readValue(Files.readString(path), type); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static <T> T read(Path path, TypeReference<T> type) {
        try { return JSON.readValue(Files.readString(path), type); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static Map<String, Object> parseObject(String json) {
        return JSON.readValue(json, OBJECT_TYPE);
    }
    /** Copy a JSON object while checking its string keys and retaining its value types. */
    public static Map<String, Object> objectMap(Object value) {
        var source = (Map<?, ?>) value;
        var result = new LinkedHashMap<String, Object>();
        for (var entry : source.entrySet()) {
            result.put(String.class.cast(entry.getKey()), entry.getValue());
        }
        return result;
    }
    public static void append(Path file, Map<String,?> event) {
        try {
            Files.createDirectories(file.getParent());
            byte[] data = (JSON.writeValueAsString(event) + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            try (var channel = java.nio.channels.FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                var bytes = java.nio.ByteBuffer.wrap(data); while (bytes.hasRemaining()) channel.write(bytes); channel.force(false);
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    /** Call only after the owner process has stopped, so its journal cannot race this update. */
    public static void unfinished(Path output, String state, String failure) {
        Path active = output.resolve("execution/active.json"), journal = output.resolve("execution/events.jsonl");
        if (!Files.isRegularFile(active)) return;
        Map<String,Map<String,Object>> remaining = read(active, new TypeReference<>() {});
        if (remaining.isEmpty()) return;
        var started = new HashMap<String,Map<String,Object>>();
        for (var event : events(journal)) if ("evaluation-start".equals(event.get("kind"))) started.put((String) event.get("id"), event);
        for (var entry : remaining.entrySet()) {
            var row = new LinkedHashMap<String,Object>(started.getOrDefault(entry.getKey(), entry.getValue()));
            row.put("id", entry.getKey()); row.put("kind", "evaluation-result"); row.put("state", state); row.put("failure", failure); row.put("timestamp", java.time.Instant.now().toString());
            append(journal, row);
        }
        write(output.resolve("execution/unfinished.json"), remaining); write(active, Map.of());
    }
    public static String hash(Path path) {
        try (var input = Files.newInputStream(path)) {
            return hash(input);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static String hash(InputStream input) throws IOException {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65536];
            for (int count; (count = input.read(buffer)) != -1;) digest.update(buffer, 0, count);
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public record Dependencies(Map<String,String> sha256, Map<String,String> contentSha256) {}
    /** Compare actual dependency contents, independently of ZIP timestamps and entry order. */
    public static Dependencies dependencyHashes(Path jar) {
        var raw = new TreeMap<String,String>(); var contents = new TreeMap<String,String>();
        try (var zip = new java.util.zip.ZipFile(jar.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.getName().startsWith("BOOT-INF/lib/") || entry.isDirectory()) continue;
                try (var input = zip.getInputStream(entry)) { raw.put(entry.getName(), hash(input)); }
                var files = new TreeMap<String,String>();
                try (var input = new java.util.zip.ZipInputStream(zip.getInputStream(entry))) {
                    for (var file = input.getNextEntry(); file != null; file = input.getNextEntry()) {
                        if (file.isDirectory()) continue;
                        if (files.put(file.getName(), hash(input)) != null) throw new IOException("Duplicate dependency entry: " + file.getName());
                    }
                }
                contents.put(entry.getName(), hash(new ByteArrayInputStream(JSON.writeValueAsBytes(files))));
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        return new Dependencies(raw, contents);
    }
    public static Map<String, String> sourceHashes(Path root) {
        var hashes = new TreeMap<String, String>();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.toList()) {
                String name = root.relativize(path).toString().replace('\\', '/');
                if (Files.isRegularFile(path) && !excluded(name) && (name.contains("src/") || name.endsWith("pom.xml") || name.endsWith(".gradle")
                        || name.endsWith(".gradle.kts") || name.endsWith("AGENTS.md") || name.startsWith(".mvn/") || name.contains("/.mvn/") || name.startsWith("gradle/") || name.contains("/gradle/")
                        || name.startsWith("scripts/") || name.contains("/scripts/") || path.getFileName().toString().matches("mvnw(?:\\.cmd)?|gradlew(?:\\.bat)?"))) {
                    hashes.put(name, hash(path));
                }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        return hashes;
    }
    public static void verifySources(Path root, Map<String, String> original) {
        for (var entry : original.entrySet()) {
            Path path = root.resolve(entry.getKey());
            if (!Files.isRegularFile(path) || !entry.getValue().equals(hash(path))) throw new IllegalStateException("Preserved baseline file changed: " + entry.getKey());
        }
    }
    public static void archiveSources(Path root, Path archive, Map<String,String> hashes) {
        verifySources(root, hashes);
        try (var zip = new java.util.zip.ZipOutputStream(Files.newOutputStream(archive))) {
            for (String file : hashes.keySet()) { zip.putNextEntry(new java.util.zip.ZipEntry(file)); Files.copy(root.resolve(file), zip); zip.closeEntry(); }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static boolean excluded(String name) {
        return name.startsWith(".git/") || name.contains("/.git/") || name.contains("/target/") || name.startsWith("target/") || name.contains("node_modules/");
    }
    public static Path inside(Path root, String relative) {
        Path result = root.resolve(relative).normalize();
        if (!result.startsWith(root.normalize())) throw new IllegalArgumentException("Path escapes artifact directory");
        return result;
    }
    public static List<Map<String, Object>> events(Path file) {
        var result = new ArrayList<Map<String, Object>>();
        if (!Files.exists(file)) return result;
        try {
            var lines = Files.readAllLines(file);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).isBlank()) continue;
                try { result.add(parseObject(lines.get(i))); }
                catch (RuntimeException e) { if (i != lines.size() - 1) throw e; }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        return result;
    }
}
