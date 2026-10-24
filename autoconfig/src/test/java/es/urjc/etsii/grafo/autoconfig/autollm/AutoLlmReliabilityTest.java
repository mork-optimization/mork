package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.boot.context.properties.bind.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmReliabilityTest {
    @Test void preservesWrappedTypedFailuresWithoutClassifyingArbitraryArgumentErrors() {
        var typed = AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.COMPONENT, "SEED_ENCODING", "required seed missing", null);
        assertEquals(typed.failure(), AutoLlmFailureUtil.describe(new IllegalStateException("Spring wrapper", typed), null));
        assertEquals(AutoLlmFailureUtil.Category.UNKNOWN,
                AutoLlmFailureUtil.describe(new IllegalStateException("Spring wrapper", new IllegalArgumentException("framework bug")), null).category());
        assertEquals(AutoLlmFailureUtil.Category.UNKNOWN, AutoLlmFailureUtil.describe(new AssertionError("framework invariant"), null).category());
    }
    @TempDir Path temp;
    @Test void byteCursorsPreserveUnicodeAndPartialWritesAndDetectTruncation() throws Exception {
        Path log = temp.resolve("stdout.log"); Files.write(log, new byte[]{'a', (byte)0xe2, (byte)0x82});
        var first = AutoLlmLogUtil.read(log, 0, 4); assertEquals("a", first.text()); assertEquals(1, first.nextCursor());
        assertFalse(AutoLlmLogUtil.read(log, first.nextCursor(), 4).more(), "An incomplete trailing character must not cause a busy polling loop");
        Files.write(log, new byte[]{(byte)0xac, 'b', '\n'}, StandardOpenOption.APPEND);
        var second = AutoLlmLogUtil.read(log, first.nextCursor(), 4); assertEquals("€b", second.text()); assertEquals(5, second.nextCursor()); assertTrue(second.more());
        assertEquals("\n", AutoLlmLogUtil.read(log, second.nextCursor(), 4).text());
        Files.writeString(log, "x"); var reset = AutoLlmLogUtil.read(log, 6, 4); assertTrue(reset.reset()); assertEquals("x", reset.text());
    }
    @Test void generationEventsAreIncrementalBoundedAndNotCountedTwice() throws Exception {
        Path log = temp.resolve("stdout.log");
        Files.writeString(log, "{\"type\":\"thread.started\",\"thread_id\":\"session\"}\n{\"type\":\"turn.started\"}\n{\"type\":\"item.star");
        var initial = CodexProgressUtil.refresh(temp); assertEquals("session", initial.threadId); assertEquals(1, initial.turns);
        assertTrue(Files.exists(temp.resolve("execution/readiness.json")));
        Files.writeString(log, "ted\",\"item\":{\"type\":\"command_execution\",\"command\":\"build\"}}\n", StandardOpenOption.APPEND);
        AutoLlmFileUtil.append(log, Map.of("type", "item.completed", "item", Map.of("type", "command_execution", "command", "build", "aggregated_output", "α".repeat(500_000))));
        AutoLlmFileUtil.append(log, Map.of("type", "item.completed", "item", Map.of("type", "agent_message", "text", "Build completed")));
        AutoLlmFileUtil.append(log, Map.of("type", "turn.completed", "usage", Map.of("output_tokens", 20)));
        var progress = CodexProgressUtil.refresh(temp); assertEquals(1, progress.commandsStarted); assertEquals(1, progress.commandsCompleted); assertEquals(1, progress.messages); assertEquals("COMPLETED", progress.state);
        long size = Files.size(temp.resolve("activity.jsonl")); assertTrue(size < 10000);
        CodexProgressUtil.refresh(temp); assertEquals(size, Files.size(temp.resolve("activity.jsonl")));
        assertEquals(Files.size(log), progress.cursor);
    }
    @Test void codexLaunchWithoutSessionHasInfrastructureStartupDeadline() throws Exception {
        AutoLlmFileUtil.write(temp.resolve("codex-launch.json"), Map.of("startedAt", "now"));
        String java = Path.of(System.getProperty("java.home"), "bin/java").toString();
        var command = List.of(java, "-cp", System.getProperty("java.class.path"), AutoLlmChildFixture.class.getName(), "no-session");
        var failure = assertThrows(AutoLlmFailureUtil.StageException.class, () -> new AutoLlmProcessRunner().run(command, temp, temp, 5000, 0, 100, () -> false, false, 300));
        assertEquals(AutoLlmFailureUtil.Category.INFRASTRUCTURE, failure.failure().category()); assertEquals("STARTUP_TIMEOUT", failure.failure().code());
        var owner = AutoLlmFileUtil.read(temp.resolve("owner.json"), Map.class);
        assertFalse(ProcessHandle.of(((Number)owner.get("pid")).longValue()).map(ProcessHandle::isAlive).orElse(false));
    }
    @Test void onlyDiagnosticsNamingChangedSourceTriggerComponentRepair() throws Exception {
        Path source = temp.resolve("trial"), baseline = temp.resolve("baseline"), evidence = temp.resolve("build");
        Files.createDirectories(source.resolve("src/main/java")); Files.createDirectories(evidence);
        Files.writeString(source.resolve("src/main/java/ConstructiveNew.java"), "invalid source");
        Files.writeString(evidence.resolve("stderr.log"), "src/main/java/ConstructiveNew.java:1: error: syntax\n");
        assertEquals(AutoLlmFailureUtil.Category.COMPONENT, AutoLlmFailureUtil.buildFailure(source, baseline, evidence, "Compilation failed", path -> path).failure().category());
        Files.writeString(evidence.resolve("stderr.log"), "ConcurrentMonitoringTest:85 NoInteractionsWanted\n");
        assertEquals(AutoLlmFailureUtil.Category.UNKNOWN, AutoLlmFailureUtil.buildFailure(source, baseline, evidence, "Tests failed", path -> path).failure().category());
        Files.writeString(evidence.resolve("stderr.log"), "[ERROR] /remote/work space/src/main/java/ConstructiveNew.java:[1,2] syntax error\n");
        assertEquals(AutoLlmFailureUtil.Category.COMPONENT, AutoLlmFailureUtil.buildFailure(source, baseline, evidence, "Remote compilation failed", path -> path.replace("/remote/work space", source.toString())).failure().category());
    }
    @Test void newTestFailuresAreRepairableButMixedFrameworkFailuresPause() throws Exception {
        Path source = temp.resolve("trial"), baseline = temp.resolve("baseline"), output = temp.resolve("build");
        Files.createDirectories(source.resolve("module/src/test/java/project")); Files.createDirectories(output);
        Files.writeString(source.resolve("module/src/test/java/project/ConstructiveNewTest.java"), "class ConstructiveNewTest {}");
        Path reports = source.resolve("module/target/surefire-reports"); Files.createDirectories(reports);
        Files.writeString(reports.resolve("project.ConstructiveNewTest.txt"), "Tests run: 1, Failures: 1 <<< FAILURE!");
        assertEquals(AutoLlmFailureUtil.Category.COMPONENT, AutoLlmFailureUtil.buildFailure(source, baseline, output, "Test failed", path -> path).failure().category());
        Files.writeString(reports.resolve("framework.MonitoringTest.txt"), "Tests run: 1, Failures: 1 <<< FAILURE!");
        assertEquals(AutoLlmFailureUtil.Category.UNKNOWN, AutoLlmFailureUtil.buildFailure(source, baseline, output, "Tests failed", path -> path).failure().category());
    }
    @Test void unresolvedJobsReserveWorkspaceUntilCollectionAndIgnoreEditableClone() throws Exception {
        var settings = new AutoLlmConfig(null, temp.toString(), null, null, null, null, null, null, null, null, null, null, null, null);
        var store = new AutoLlmCampaignStore(settings); String first = UUID.randomUUID().toString(), second = UUID.randomUUID().toString();
        Path output = temp.resolve(first).resolve("attempt/build"); AutoLlmFileUtil.write(output.resolve("remote-job.json"), Map.of("id", "saved"));
        store.reserve(first); store.releaseReservation(first, temp.resolve(first));
        assertThrows(IllegalStateException.class, () -> store.requireReservation(second));
        AutoLlmFileUtil.write(output.resolve("collection-completed.json"), Map.of("jobId", "saved"));
        AutoLlmFileUtil.write(temp.resolve(first).resolve("trials/001/source/remote-job.json"), Map.of("id", "editable"));
        store.releaseReservation(first, temp.resolve(first)); assertDoesNotThrow(() -> store.requireReservation(second));
    }
    @Test void springOptionalBlankSshPathsMatchAbsentJsonSettings() {
        var environment = new MockEnvironment().withProperty("autollm.execution.target", "SSH").withProperty("autollm.execution.ssh.servers[0].id", "server").withProperty("autollm.execution.ssh.servers[0].host", "server")
                .withProperty("autollm.execution.ssh.servers[0].workspace", "/remote").withProperty("autollm.execution.ssh.servers[0].config-file", "").withProperty("autollm.execution.ssh.runner-artifact", "");
        var config = Binder.get(environment).bind("autollm", Bindable.of(AutoLlmConfig.class)).get();
        assertNull(config.execution().ssh().screeningServer().configFile()); assertNull(config.execution().ssh().runnerArtifact());
    }
}
