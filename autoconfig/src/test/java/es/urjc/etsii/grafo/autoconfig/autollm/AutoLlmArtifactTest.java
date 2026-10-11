package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmArtifactTest {
    @TempDir Path temp;

    @Test void dependencyGuardIgnoresArchiveMetadataButDetectsContentChanges() throws Exception {
        var entries = new LinkedHashMap<String,String>();
        entries.put("library/Algorithm.class", "original bytecode");
        entries.put("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n");
        var reversed = new LinkedHashMap<String,String>();
        reversed.put("META-INF/MANIFEST.MF", entries.get("META-INF/MANIFEST.MF"));
        reversed.put("library/Algorithm.class", entries.get("library/Algorithm.class"));
        Path first = artifact("first.jar", entries, 1_700_000_000_000L);
        Path rebuilt = artifact("rebuilt.jar", reversed, 1_700_100_000_000L);
        var baseline = AutoLlmFileUtil.dependencyHashes(first);
        var fresh = AutoLlmFileUtil.dependencyHashes(rebuilt);
        assertNotEquals(baseline.sha256(), fresh.sha256());
        assertEquals(baseline.contentSha256(), fresh.contentSha256());
        AutoLlmFileUtil.write(temp.resolve("dependencies.json"), baseline);
        assertEquals(baseline, AutoLlmFileUtil.read(temp.resolve("dependencies.json"), AutoLlmFileUtil.Dependencies.class));
        reversed.put("library/Algorithm.class", "changed bytecode");
        var changed = AutoLlmFileUtil.dependencyHashes(artifact("changed.jar", reversed, 1_700_100_000_000L));
        assertNotEquals(baseline.contentSha256(), changed.contentSha256());
        reversed.put("library/Algorithm.class", entries.get("library/Algorithm.class"));
        reversed.put("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\nChanged: true\n");
        assertNotEquals(baseline.contentSha256(), AutoLlmFileUtil.dependencyHashes(artifact("manifest.jar", reversed, 1_700_100_000_000L)).contentSha256());
    }

    private Path artifact(String name, Map<String,String> entries, long timestamp) throws Exception {
        var library = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(library)) {
            for (var entry : entries.entrySet()) {
                var file = new ZipEntry(entry.getKey()); file.setTime(timestamp); zip.putNextEntry(file);
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
            }
        }
        Path path = temp.resolve(name);
        try (var zip = new ZipOutputStream(Files.newOutputStream(path))) {
            zip.putNextEntry(new ZipEntry("BOOT-INF/lib/library.jar")); zip.write(library.toByteArray()); zip.closeEntry();
        }
        return path;
    }
}
