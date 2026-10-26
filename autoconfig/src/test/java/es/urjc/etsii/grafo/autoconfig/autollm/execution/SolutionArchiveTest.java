package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmProcessRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SolutionArchiveTest {
    @TempDir Path root;

    @Test void preservesAllExportsAndMetadataAndCanRestoreWithoutGeneratedClasses() throws Exception {
        Path output = root.resolve("comparison");
        Files.createDirectories(output.resolve("solutions/nested"));
        Files.createDirectories(output.resolve("execution"));
        Files.writeString(output.resolve("solutions/first.json"), "{\"tour\":[0,1,0]}");
        Files.writeString(output.resolve("solutions/nested/second.json"), "{\"cost\":42}");
        Files.writeString(output.resolve("execution/events.jsonl"), "journal\n");
        Files.writeString(output.resolve("owner.json"), "owner");
        DirectoryArchiveUtil.archive(output.resolve("solutions"));
        assertFalse(Files.exists(output.resolve("solutions")));
        assertEquals("journal\n", Files.readString(output.resolve("execution/events.jsonl")));
        assertEquals("owner", Files.readString(output.resolve("owner.json")));
        var entries = WorkloadArchiveUtil.verify(output.resolve("solutions.zip"), root.resolve("restore"));
        assertEquals(2, entries.size());
        WorkloadArchiveUtil.unpack(output.resolve("solutions.zip"), root.resolve("restore"));
        assertEquals("{\"tour\":[0,1,0]}", Files.readString(root.resolve("restore/first.json")));
        assertEquals("{\"cost\":42}", Files.readString(root.resolve("restore/nested/second.json")));
        byte[] archive = Files.readAllBytes(output.resolve("solutions.zip"));
        DirectoryArchiveUtil.archive(output.resolve("solutions"));
        assertArrayEquals(archive, Files.readAllBytes(output.resolve("solutions.zip")));
    }

    @Test void resumesDeletionWithoutReplacingTheCompleteArchiveWithAPartialTree() throws Exception {
        Path output = root.resolve("comparison"); Files.createDirectories(output.resolve("solutions"));
        Path first = output.resolve("solutions/first.json"), second = output.resolve("solutions/second.json");
        Files.writeString(first, "first"); Files.writeString(second, "second");
        WorkloadArchiveUtil.pack(output.resolve("solutions.zip"), Map.of("first.json", first, "second.json", second));
        byte[] complete = Files.readAllBytes(output.resolve("solutions.zip"));
        Files.delete(first);
        DirectoryArchiveUtil.archive(output.resolve("solutions"));
        assertFalse(Files.exists(output.resolve("solutions")));
        assertArrayEquals(complete, Files.readAllBytes(output.resolve("solutions.zip")));
        assertEquals(2, WorkloadArchiveUtil.verify(output.resolve("solutions.zip"), root.resolve("restore")).size());
    }

    @Test void retainsOriginalsWhenAnExistingArchiveIsCorruptOrDoesNotContainTheirCurrentBytes() throws Exception {
        Path output = root.resolve("comparison"); Files.createDirectories(output.resolve("solutions"));
        Path solution = output.resolve("solutions/first.json"); Files.writeString(solution, "first");
        Path archive = output.resolve("solutions.zip"); Files.writeString(archive, "corrupt");
        assertThrows(IOException.class, () -> DirectoryArchiveUtil.archive(output.resolve("solutions")));
        assertEquals("first", Files.readString(solution));
        WorkloadArchiveUtil.pack(archive, Map.of("first.json", solution));
        byte[] originalArchive = Files.readAllBytes(archive);
        Files.writeString(solution, "changed");
        assertThrows(IOException.class, () -> DirectoryArchiveUtil.archive(output.resolve("solutions")));
        assertEquals("changed", Files.readString(solution));
        assertArrayEquals(originalArchive, Files.readAllBytes(archive));
        Files.writeString(solution, "first");
        Files.writeString(output.resolve("solutions/new.json"), "new");
        assertThrows(IOException.class, () -> DirectoryArchiveUtil.archive(output.resolve("solutions")));
        assertTrue(Files.exists(output.resolve("solutions/new.json")));
    }

    @Test void localExecutorArchivesOnlyAfterTheChildReturnsAndKeepsItsResult() throws Exception {
        Path output = root.resolve("comparison");
        var process = mock(AutoLlmProcessRunner.class);
        var expected = mock(AutoLlmProcessRunner.Result.class);
        when(process.run(anyList(), any(Path.class), any(Path.class), anyLong(), anyLong(), anyLong(), any(), anyBoolean(), anyLong()))
                .thenAnswer(_ -> {
                    assertFalse(Files.exists(output.resolve("solutions.zip")));
                    Files.createDirectories(output.resolve("solutions"));
                    Files.writeString(output.resolve("solutions/tour.json"), "tour");
                    return expected;
                });
        var workload = new AutoLlmWorkloadExecutor.Workload(List.of("child"), output, output, Map.of(), List.of(), List.of(),
                1000, 0, 100, false, 1000);
        assertSame(expected, new LocalWorkloadExecutor(process).run(workload, () -> false));
        assertTrue(Files.isRegularFile(output.resolve("solutions.zip")));
        assertFalse(Files.exists(output.resolve("solutions")));
    }

    @Test void retainsRuntimeJarAndRestoresSourcesPermissionsAndLinks() throws Exception {
        Path source = root.resolve("baseline/source");
        Files.createDirectories(source.resolve("target"));
        Files.createDirectories(source.resolve(".git"));
        Files.writeString(source.resolve(".git/config"), "repository");
        Files.writeString(source.resolve("target/application.jar"), "executable");
        Files.writeString(source.resolve("build.sh"), "build");
        source.resolve("build.sh").toFile().setExecutable(true);
        Files.createSymbolicLink(source.resolve("build-link"), Path.of("build.sh"));
        DirectoryArchiveUtil.archive(source, Set.of("target/application.jar"));
        assertEquals("executable", Files.readString(source.resolve("target/application.jar")));
        assertFalse(Files.exists(source.resolve(".git")));
        assertFalse(Files.exists(source.resolve("build-link"), LinkOption.NOFOLLOW_LINKS));
        byte[] archive = Files.readAllBytes(DirectoryArchiveUtil.archivePath(source));
        DirectoryArchiveUtil.archive(source, Set.of("target/application.jar"));
        assertArrayEquals(archive, Files.readAllBytes(DirectoryArchiveUtil.archivePath(source)));
        Files.writeString(source.resolve("target/application.jar"), "changed");
        assertThrows(IOException.class, () -> DirectoryArchiveUtil.restore(source));
        assertFalse(Files.exists(source.resolve(".git")));
        Files.writeString(source.resolve("target/application.jar"), "executable");
        DirectoryArchiveUtil.restore(source);
        assertEquals("repository", Files.readString(source.resolve(".git/config")));
        assertEquals(Path.of("build.sh"), Files.readSymbolicLink(source.resolve("build-link")));
        assertTrue(Files.isExecutable(source.resolve("build.sh")));
    }

    @Test void restoredDirectoryLinksCanBeVerifiedAndArchivedAgain() throws Exception {
        Path source = root.resolve("source");
        Files.createDirectories(source.resolve("nested"));
        Files.writeString(source.resolve("nested/data"), "data");
        // NIO normalizes a trailing separator when constructing a link target from a string.
        assertEquals(0, new ProcessBuilder("ln", "-s", "nested/", source.resolve("link").toString()).start().waitFor());
        DirectoryArchiveUtil.archive(source);
        var entries = WorkloadArchiveUtil.verify(DirectoryArchiveUtil.archivePath(source), source);
        assertEquals("nested/", entries.get("link").link());
        DirectoryArchiveUtil.restore(source);
        assertEquals("data", Files.readString(source.resolve("link/data")));
        DirectoryArchiveUtil.restore(source);
        DirectoryArchiveUtil.archive(source);
        assertFalse(Files.exists(source));
    }
}
