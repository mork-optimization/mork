package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmCleanupIT {
    @TempDir Path temporary;
    public static class CleanupControllerProcess {
        public static void main(String[] args) throws Exception {
            Path workspace = Path.of(args[0]);
            var config = AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class);
            new OwnershipManifest(workspace).directory(workspace, "workspace", workspace.toString());
            try (var channel = java.nio.channels.FileChannel.open(workspace.resolve("execution.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 var lock = channel.lock()) {
                var plan = new AutoLlmCleanup(new AutoLlmCampaignStore(config)).plan(new AutoLlmCleanup.Selection("workspace", null, null, true, true));
                AutoLlmFileUtil.write(Path.of(args[2]), plan);
                Thread.sleep(60_000);
            }
        }
    }
    @Test void externalFinalizerStopsExactControllerAndDeletesAfterItsFileLocksClose() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace"), pointer = temporary.resolve("reviewed.json");
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin/java").toString();
        String cp = System.getProperty("java.class.path"); Path registry = temporary.resolve("helper-tools");
        var controller = new ProcessBuilder(javaExecutable, "-cp", cp, CleanupControllerProcess.class.getName(), workspace.toString(), registry.toString(), pointer.toString())
                .redirectErrorStream(true).redirectOutput(temporary.resolve("controller.log").toFile()).start();
        Process helper = null;
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
            while (!Files.isRegularFile(pointer) && controller.isAlive() && System.nanoTime() < deadline) Thread.sleep(20);
            assertTrue(Files.isRegularFile(pointer), Files.readString(temporary.resolve("controller.log")));
            var plan = AutoLlmFileUtil.read(pointer, AutoLlmCleanup.Plan.class); assertTrue(plan.blockers().isEmpty());
            Path receipts = workspace.getParent().resolve(".mork-cleanup/" + AutoLlmFileUtil.canonicalHash(workspace.toString()));
            Path planFile = receipts.resolve(plan.digest() + ".plan.json"), runner = registry.resolve("tools/finalizer-test.jar"); Files.createDirectories(runner.getParent());
            var manifest = new java.util.jar.Manifest(); manifest.getMainAttributes().put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(java.util.jar.Attributes.Name.MAIN_CLASS, RemoteWorkloadMain.class.getName());
            var entries = new ArrayList<String>(); for (String entry : cp.split(System.getProperty("path.separator"))) entries.add(Path.of(entry).toAbsolutePath().toUri().toString());
            manifest.getMainAttributes().put(java.util.jar.Attributes.Name.CLASS_PATH, String.join(" ", entries));
            try (var jar = new java.util.jar.JarOutputStream(Files.newOutputStream(runner), manifest)) { }
            AutoLlmFileUtil.write(receipts.resolve(plan.digest() + ".finalizer.json"), new AutoLlmCleanup.Finalizer(runner.toString(), AutoLlmFileUtil.hash(runner), null));
            helper = new ProcessBuilder(javaExecutable, "-jar", runner.toString(), "cleanup-finish", workspace.toString(), planFile.toString())
                    .redirectErrorStream(true).redirectOutput(temporary.resolve("helper.log").toFile()).start();
            assertTrue(helper.waitFor(30, TimeUnit.SECONDS)); assertEquals(0, helper.exitValue(), Files.readString(temporary.resolve("helper.log")));
            assertTrue(controller.waitFor(5, TimeUnit.SECONDS)); assertFalse(Files.exists(workspace));
            assertEquals("SUCCEEDED", AutoLlmFileUtil.read(receipts.resolve(plan.digest() + ".receipt.json"), AutoLlmCleanup.Receipt.class).status());
            assertFalse(Files.exists(planFile));
        } finally { controller.destroyForcibly(); if (helper != null && helper.isAlive()) helper.destroyForcibly(); }
    }
}
