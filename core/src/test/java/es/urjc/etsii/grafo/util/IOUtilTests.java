package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

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
    public void checkFolder(@TempDir Path tempDir) throws IOException {
        String path = "parent1/parent2/parent3";
        String test = tempDir.resolve(path).toAbsolutePath().toString();
        IOUtil.createFolder(test);

        Assertions.assertDoesNotThrow(()-> IOUtil.checkIsFolder(tempDir.resolve(Path.of("parent1")).toString()));
        Assertions.assertDoesNotThrow(()-> IOUtil.checkIsFolder(tempDir.resolve(Path.of("parent1/parent2/parent3")).toString()));
        Assertions.assertThrows(IllegalArgumentException.class, ()-> IOUtil.checkIsFolder(tempDir.resolve(Path.of("doesnotexist")).toString()));

        tempDir.resolve("parent1/file.txt").toFile().createNewFile();
        Assertions.assertThrows(IllegalArgumentException.class, ()-> IOUtil.checkIsFolder(tempDir.resolve(Path.of("parent1/file.txt")).toString()));
    }

    @Test
    public void isNormalFile(@TempDir Path tempDir) throws IOException {
        String path = "parent1/parent2/parent3";
        String test = tempDir.resolve(path).toAbsolutePath().toString();
        IOUtil.createFolder(test);

        Assertions.assertFalse(IOUtil.isNormalFile(tempDir.resolve(Path.of("parent1"))));
        Assertions.assertFalse(IOUtil.isNormalFile(tempDir.resolve(Path.of("parent1/parent2/parent3"))));
        Assertions.assertFalse(IOUtil.isNormalFile(tempDir.resolve(Path.of("doesnotexist"))));

        tempDir.resolve("parent1/file.txt").toFile().createNewFile();
        Assertions.assertTrue(IOUtil.isNormalFile(tempDir.resolve(Path.of("parent1/file.txt"))));

        tempDir.resolve("parent1/.hidden").toFile().createNewFile();
        Assertions.assertFalse(()-> IOUtil.isNormalFile(tempDir.resolve(Path.of("parent1/.hidden"))));
    }

    @Test
    public void verifyExecutable(@TempDir Path tempDir) throws IOException {
        var f = tempDir.resolve("script.sh").toFile();
        f.createNewFile();
        IOUtil.markAsExecutable(f.getAbsolutePath());
        Assertions.assertTrue(f.canExecute());
    }

    @Test
    public void iterate(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("parent1/parent2/parent3");
        String test = path.toAbsolutePath().toString();
        IOUtil.createFolder(test);

        var invalidFiles = Set.of("parent1/parent2/.ignore_me");
        var validFiles = Set.of("parent1/file1.txt", "parent1/parent2/hello.png", "parent1/parent2/parent3/validfile");
        for(var s: invalidFiles){
            tempDir.resolve(s).toFile().createNewFile();
        }
        for(var s: validFiles){
            tempDir.resolve(s).toFile().createNewFile();
        }

        var list = IOUtil.iterate(tempDir.toString()).stream().map(p -> p.toAbsolutePath().toString()).collect(Collectors.toSet());
        for(var s: invalidFiles){
            for(var t: list){
                Assertions.assertFalse(t.endsWith(s));
            }
        }
        for(var s: validFiles){
            int count = 0;
            for(var t: list){
                if(Path.of(t).endsWith(s)){
                    count++;
                }
            }
            Assertions.assertEquals(1, count);
        }
    }
}
