package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkloadArchiveTest {
    @TempDir Path temp;
    @Test void preservesContentsExecutableModesAndSafeSymlinks() throws Exception {
        Path source = temp.resolve("source"), target = temp.resolve("target"), archive = temp.resolve("source.zip"); Files.createDirectories(source);
        Files.writeString(source.resolve("build script"), "#!/bin/sh\nprintf ok\n"); source.resolve("build script").toFile().setExecutable(true);
        Files.createSymbolicLink(source.resolve("alias"), Path.of("build script"));
        WorkloadArchiveUtil.pack(archive, Map.of("build script", source.resolve("build script"), "alias", source.resolve("alias")));
        WorkloadArchiveUtil.unpack(archive, target);
        assertEquals(Files.readString(source.resolve("build script")), Files.readString(target.resolve("alias")));
        assertTrue(Files.isExecutable(target.resolve("build script"))); assertTrue(Files.isSymbolicLink(target.resolve("alias")));
    }
    @Test void rejectsTraversalChecksumChangesAndSymlinkParentsBeforeWriting() throws Exception {
        Path source = temp.resolve("source"); Files.writeString(source, "content");
        Path archive = temp.resolve("bad.zip"), target = temp.resolve("target");
        WorkloadArchiveUtil.pack(archive, Map.of("../escaped", source));
        assertThrows(IllegalArgumentException.class, () -> WorkloadArchiveUtil.unpack(archive, target)); assertFalse(Files.exists(temp.resolve("escaped")));
        WorkloadArchiveUtil.pack(archive, Map.of("safe/file", source)); Files.createDirectories(target); Files.createSymbolicLink(target.resolve("safe"), temp);
        assertThrows(IllegalArgumentException.class, () -> WorkloadArchiveUtil.unpack(archive, target));
        Path corrupt = temp.resolve("corrupt.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(corrupt))) {
            zip.putNextEntry(new ZipEntry("file")); zip.write("changed".getBytes()); zip.closeEntry();
            zip.putNextEntry(new ZipEntry(".mork-transfer.json")); zip.write(AutoLlmFileUtil.JSON.writeValueAsBytes(Map.of("file", new WorkloadArchiveUtil.Entry("wrong", 0644, null)))); zip.closeEntry();
        }
        assertThrows(java.io.IOException.class, () -> WorkloadArchiveUtil.unpack(corrupt, temp.resolve("clean")));
    }
    @Test void mapsOnlyPathBoundariesAndKeepsArchiveEntriesLiteral() {
        assertEquals("/remote/data/a.zip!dir/file", WorkloadPathUtil.translate("/local/data/a.zip!dir/file", "/local", "/remote"));
        assertEquals("/local-other/a", WorkloadPathUtil.translate("/local-other/a", "/local", "/remote"));
        assertEquals("--file=/remote/a b", WorkloadPathUtil.argument("--file=/local/a b", Path.of("/local"), "/remote"));
        assertEquals("'a'\\''b;$(x)'", WorkloadPathUtil.quote("a'b;$(x)"));
    }
    @Test void defaultsAndExplicitWorkersUseTheSameMorkRule() {
        var defaults = AutoLlmConfig.defaults(); assertEquals(-1, defaults.execution().workers()); assertEquals(AutoLlmConfig.Target.LOCAL, defaults.execution().target());
        assertEquals(1, ConcurrencyUtil.resolveWorkerCount(-1, 1)); assertEquals(2, ConcurrencyUtil.resolveWorkerCount(-1, 5));
        assertEquals(16, ConcurrencyUtil.resolveWorkerCount(-1, 32)); assertEquals(32, ConcurrencyUtil.resolveWorkerCount(32, 32));
        assertEquals(defaults, AutoLlmFileUtil.JSON.readValue(AutoLlmFileUtil.JSON.writeValueAsBytes(defaults), AutoLlmConfig.class));
    }
    @Test void localReconciliationCannotKillARemotePid() throws Exception {
        Path output = temp.resolve("remote"); Files.createDirectories(output);
        var process = new ProcessBuilder("sleep", "20").start();
        try {
            AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", process.pid(), "start", process.info().startInstant().orElseThrow().toString(), "executionTarget", "SSH"));
            new AutoLlmProcessRunner().reconcile(temp); assertTrue(process.isAlive());
        } finally { process.destroyForcibly(); }
    }
}
