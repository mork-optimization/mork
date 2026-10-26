package es.urjc.etsii.grafo.util;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class IOUtilTest {

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

        var list = new HashSet<>(IOUtil.iterate(tempDir.toString()));
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

    void assertEntries(List<String> entries){
        Set<String> expected = Set.of("instances/f1/bye-world.txt", "instances/f2/test1.txt", "instances/f2/test2.txt", "instances/hello-world.txt", "instances/empty-world.txt");
        Set<String> actual = new HashSet<>();
        for(var e: entries){
            Assertions.assertTrue(e.contains(Compression.SEP));
            actual.add(e.split(Compression.SEP)[1]);
        }
        Assertions.assertEquals(expected, actual);
    }

    void assertContent(String path, String content) {
        content = content.strip();
        if(path.endsWith("test1.txt")){
            Assertions.assertEquals("1", content);
        } else if(path.endsWith("test2.txt")){
            Assertions.assertEquals("2", content);
        } else if(path.endsWith("hello-world.txt")){
            Assertions.assertEquals("Hello world!", content);
        } else if(path.endsWith("bye-world.txt")){
            Assertions.assertEquals("Bye world!", content);
        } else if(path.endsWith("empty-world.txt")){
            Assertions.assertEquals("", content);
        } else {
            Assertions.fail("Unexpected file: " + path);
        }
    }

    @Test
    void compressedLoadPathParts() {
        String token = "folder/archive.zip!instances/f1/hello-world.txt";

        Assertions.assertEquals("folder/archive.zip", IOUtil.containerPath(token));
        Assertions.assertEquals("instances/f1/hello-world.txt", IOUtil.entryPath(token));
    }

    @Test
    void relativizePathOnlyForWorkingDirectoryDescendants() throws IOException {
        Path workingDirectory = Path.of(".").toRealPath();
        Path descendant = workingDirectory.resolve("instances").resolve("a.txt");
        Path sibling = workingDirectory.resolveSibling(workingDirectory.getFileName() + "-instances").resolve("a.txt");

        Assertions.assertEquals(Path.of("instances", "a.txt").toString(), IOUtil.relativizePath(descendant.toString()));
        Assertions.assertEquals(sibling.toString(), IOUtil.relativizePath(sibling.toString()));
        Assertions.assertEquals("", IOUtil.relativizePath(workingDirectory.toString()));
        Assertions.assertEquals(Path.of("instances", "a.txt").toString(), IOUtil.relativizePath(Path.of("instances", "a.txt").toString()));
    }

    @Test
    void testZip() throws IOException {
        var entries = IOUtil.iterate("src/test/resources/instances.zip");
        assertEntries(entries);
        for(var e: entries){
            try (var in = IOUtil.getInputStream(e)) {
                var content = IOUtils.toString(in, StandardCharsets.UTF_8);
                assertContent(e, content);
            }
        }
    }


    @Test
    void test7z() throws IOException {
        var entries = IOUtil.iterate("src/test/resources/instances.7z");
        assertEntries(entries);
        for(var e: entries){
            try (var in = IOUtil.getInputStream(e)) {
                var content = IOUtils.toString(in, StandardCharsets.UTF_8);
                assertContent(e, content);
            }
        }
    }

    @Test
    void testTarGz() throws IOException {
        var entries = IOUtil.iterate("src/test/resources/instances.tar.gz");
        assertEntries(entries);
        for(var e: entries){
            try (var in = IOUtil.getInputStream(e)) {
                var content = IOUtils.toString(in, StandardCharsets.UTF_8);
                assertContent(e, content);
            }
        }
    }

    @Test
    void testTar() throws IOException {
        var entries = IOUtil.iterate("src/test/resources/instances.tar");
        assertEntries(entries);
        for(var e: entries){
            try (var in = IOUtil.getInputStream(e)) {
                var content = IOUtils.toString(in, StandardCharsets.UTF_8);
                assertContent(e, content);
            }
        }
    }

    @Test void closesArchiveEvenWhenTheMemberFailsToClose() {
        var memberFailure = new IOException("member failed");
        var archiveFailure = new IOException("archive failed");
        var closed = new AtomicBoolean();
        var member = new ByteArrayInputStream(new byte[0]) {
            @Override public void close() throws IOException { throw memberFailure; }
        };
        Closeable owner = () -> { closed.set(true); throw archiveFailure; };
        var input = IOUtil.closeWithOwner(member, owner);
        var error = Assertions.assertThrows(IOException.class, input::close);
        Assertions.assertSame(memberFailure, error);
        Assertions.assertTrue(closed.get());
        Assertions.assertArrayEquals(new Throwable[]{archiveFailure}, error.getSuppressed());
    }

    @ParameterizedTest @ValueSource(strings = {"zip", "7z"})
    void repeatedMemberReadsAndMissingEntriesDoNotLeakFileDescriptors(String extension) throws IOException {
        var operatingSystem = ManagementFactory.getOperatingSystemMXBean();
        org.junit.jupiter.api.Assumptions.assumeTrue(operatingSystem instanceof com.sun.management.UnixOperatingSystemMXBean);
        var unix = (com.sun.management.UnixOperatingSystemMXBean) operatingSystem;
        String container = "src/test/resources/instances." + extension;
        String token = container + "!instances/hello-world.txt";
        try (var input = IOUtil.getInputStream(token)) { input.readAllBytes(); }
        long before = unix.getOpenFileDescriptorCount();
        for (int i = 0; i < 100; i++) {
            try (var input = IOUtil.getInputStream(token)) { assertContent(token, IOUtils.toString(input, StandardCharsets.UTF_8)); }
            Assertions.assertThrows(IOException.class, () -> IOUtil.getInputStream(container + "!missing.txt"));
        }
        Assertions.assertTrue(unix.getOpenFileDescriptorCount() <= before + 5, "Archive file descriptors accumulated");
    }

}
