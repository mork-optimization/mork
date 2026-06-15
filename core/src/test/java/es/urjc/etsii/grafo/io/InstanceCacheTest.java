package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.testutil.TestInstance;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class InstanceCacheTest {

    @Test
    void relativeAndAbsolutePathsShareCacheEntry() throws IOException {
        Path targetDirectory = Path.of("target");
        Files.createDirectories(targetDirectory);
        Path instancePath = Files.createTempFile(targetDirectory, "instance-cache", ".txt");
        var instance = new TestInstance("logical-id");
        var cache = new InstanceCache<TestInstance>();

        cache.put(instancePath.toAbsolutePath().toString(), instance);

        assertSame(instance, cache.get(instancePath.toString()));
    }

    @Test
    void relativeAndAbsoluteCompressedLoadTokensShareCacheEntry() throws IOException {
        Path targetDirectory = Path.of("target");
        Files.createDirectories(targetDirectory);
        Path archivePath = Files.createTempFile(targetDirectory, "instance-cache", ".zip");
        String relativeToken = archivePath + "!folder/instance.txt";
        String absoluteToken = archivePath.toAbsolutePath() + "!folder/instance.txt";
        var instance = new TestInstance("compressed-id");
        var cache = new InstanceCache<TestInstance>();

        cache.put(absoluteToken, instance);

        assertSame(instance, cache.get(relativeToken));
    }

    @Test
    void logicalInstanceIdIsNotACacheKey() throws IOException {
        Path targetDirectory = Path.of("target");
        Files.createDirectories(targetDirectory);
        Path instancePath = Files.createTempFile(targetDirectory, "instance-cache", ".txt");
        var instance = new TestInstance("logical-id");
        var cache = new InstanceCache<TestInstance>();

        cache.put(instancePath.toString(), instance);

        assertNull(cache.get(instance.getId()));
    }
}
