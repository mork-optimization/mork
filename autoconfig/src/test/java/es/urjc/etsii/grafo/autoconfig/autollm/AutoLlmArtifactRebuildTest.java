package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmArtifactRebuildTest {
    @TempDir Path root;
    @Test void fingerprintChecksApplicationDependenciesAndResourcesButIgnoresZipLayout() throws Exception {
        var entries = new LinkedHashMap<String,byte[]>();
        entries.put("BOOT-INF/classes/App.class", "code".getBytes(UTF_8));
        entries.put("META-INF/MANIFEST.MF", "Main-Class: App\n".getBytes(UTF_8));
        entries.put("BOOT-INF/classes/settings.yml", "resource".getBytes(UTF_8));
        entries.put("BOOT-INF/lib/dependency.jar", zip(Map.of("Dependency.class", "library".getBytes(UTF_8)), 0));
        Path first = root.resolve("first.jar"), second = root.resolve("second.jar");
        Files.write(first, zip(entries, 0)); var expected = AutoLlmExecutableFingerprint.read(first);
        var reversed = new LinkedHashMap<String,byte[]>();
        for (String name : new ArrayList<>(entries.keySet()).reversed()) reversed.put(name, entries.get(name));
        Files.write(second, zip(reversed, 2000000000000L));
        assertTrue(expected.matches(AutoLlmExecutableFingerprint.read(second)));
        for (String name : entries.keySet()) {
            var changed = new LinkedHashMap<>(entries);
            changed.put(name, name.endsWith(".jar") ? zip(Map.of("Dependency.class", "changed".getBytes(UTF_8)), 0) : "changed".getBytes(UTF_8));
            Files.write(second, zip(changed, 0));
            assertFalse(expected.matches(AutoLlmExecutableFingerprint.read(second)), name);
        }
        Files.write(second, zip(Map.of("invalid.jar", "not a jar".getBytes(UTF_8)), 0));
        assertThrows(IOException.class, () -> AutoLlmExecutableFingerprint.read(second));
    }
    @Test void ignoresOnlyRecognizedBuildMetadataAndChecksNestedEntryContents() throws Exception {
        Path first = root.resolve("first.jar"), second = root.resolve("second.jar");
        var dependency = new LinkedHashMap<String,byte[]>();
        dependency.put("Library.class", "code".getBytes(UTF_8)); dependency.put("builddef.lst", "original path".getBytes(UTF_8));
        var reordered = new LinkedHashMap<String,byte[]>();
        reordered.put("builddef.lst", "relocated path".getBytes(UTF_8)); reordered.put("Library.class", "code".getBytes(UTF_8));
        for (String name : List.of("mork-1.jar", "mork-common-1.jar", "other-1.jar")) {
            Files.write(first, zip(Map.of("BOOT-INF/lib/" + name, zip(dependency, 0)), 0));
            Files.write(second, zip(Map.of("BOOT-INF/lib/" + name, zip(reordered, 2000000000000L)), 0));
            var original = AutoLlmExecutableFingerprint.read(first); var rebuilt = AutoLlmExecutableFingerprint.read(second);
            assertEquals(name.startsWith("mork-"), original.matches(rebuilt), name);
            if (name.startsWith("mork-")) assertNotEquals(original.buildMetadata(), rebuilt.buildMetadata());
        }
    }
    private static byte[] zip(Map<String,byte[]> entries, long time) throws IOException {
        var buffer = new ByteArrayOutputStream();
        try (var output = new ZipOutputStream(buffer)) {
            for (var item : entries.entrySet()) {
                var entry = new ZipEntry(item.getKey()); entry.setTime(time); output.putNextEntry(entry); output.write(item.getValue()); output.closeEntry();
            }
        }
        return buffer.toByteArray();
    }
}
