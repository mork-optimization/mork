package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class IOUtilTests {

    @Test
    public void createAndCheckRecursively(@TempDir Path tempDir){
        String path = "parent1/parent2/parent3";
        String test = tempDir.resolve(path).toAbsolutePath().toString();

        IOUtil.createFolder(test);
        Assertions.assertDoesNotThrow(() -> tempDir.resolve("parent1").toAbsolutePath().toString());
        Assertions.assertTrue(tempDir.resolve("parent1").toFile().exists());
        Assertions.assertDoesNotThrow(() -> tempDir.resolve("parent1/parent2").toAbsolutePath().toString());
        Assertions.assertTrue(tempDir.resolve("parent1/parent2").toFile().exists());
        Assertions.assertDoesNotThrow(() -> tempDir.resolve("parent1/parent2/parent3").toAbsolutePath().toString());
        Assertions.assertTrue(tempDir.resolve("parent1/parent2/parent3").toFile().exists());

        // Should not fail if folder already exists, just no-op
        Assertions.assertDoesNotThrow(() -> IOUtil.createFolder(test));
        Assertions.assertDoesNotThrow(() -> tempDir.resolve("parent1").toAbsolutePath().toString());
        Assertions.assertTrue(tempDir.resolve("parent1").toFile().exists());
        Assertions.assertDoesNotThrow(() -> tempDir.resolve("parent1/parent2").toAbsolutePath().toString());
        Assertions.assertTrue(tempDir.resolve("parent1/parent2").toFile().exists());
        Assertions.assertDoesNotThrow(() -> tempDir.resolve("parent1/parent2/parent3").toAbsolutePath().toString());
        Assertions.assertTrue(tempDir.resolve("parent1/parent2/parent3").toFile().exists());
    }

    @Test
    public void checkFolder(@TempDir Path tempDir) {

    }

    @Test
    public void isNormalFile(@TempDir Path tempDir) {

    }

    @Test
    public void iterate(@TempDir Path tempDir) {
        // check iterate using a string

    }
}
