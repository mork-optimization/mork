package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BenchmarkUtilTest {

    @Test
    void parsesValidCache(@TempDir Path tempDir) throws IOException {
        Path cache = tempDir.resolve(".benchmark");
        Files.writeString(cache, "4\nvm-version\njava-version\n12.5\n");

        var parsed = BenchmarkUtil.parseCache(cache.toFile());

        assertEquals(4, parsed.info().nProcessors());
        assertEquals("vm-version", parsed.info().vmVersion());
        assertEquals("java-version", parsed.info().javaVersion());
        assertEquals(12.5, parsed.score());
    }

    @Test
    void malformedCacheIsCacheMiss(@TempDir Path tempDir) throws IOException {
        Path cache = tempDir.resolve(".benchmark");

        Files.writeString(cache, "invalid\nvm-version\njava-version\n12.5\n");
        assertNull(BenchmarkUtil.parseCache(cache.toFile()));

        Files.writeString(cache, "4\nvm-version\njava-version\ninvalid\n");
        assertNull(BenchmarkUtil.parseCache(cache.toFile()));

        Files.writeString(cache, "4\nvm-version\njava-version\n");
        assertNull(BenchmarkUtil.parseCache(cache.toFile()));
    }
}
