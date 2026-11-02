package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class CommittedBaselineTest {
    @TempDir Path temp;
    void command(List<String> args, Path cwd, Path logs) {
        try {
            Files.createDirectories(logs);
            var p = new ProcessBuilder(args).directory(cwd.toFile()).redirectOutput(logs.resolve("stdout.log").toFile()).redirectError(logs.resolve("stderr.log").toFile()).start();
            assertTrue(p.waitFor(30, TimeUnit.SECONDS));
            assertEquals(0, p.exitValue(), Files.readString(logs.resolve("stderr.log")));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    AutoLlmConfig config() throws Exception {
        Path repo = temp.resolve("repo"); Files.createDirectories(repo.resolve("src/test/resources"));
        Files.writeString(repo.resolve("src/test/resources/tracked.txt"), "committed");
        command(List.of("git", "init"), repo, temp.resolve("logs"));
        command(List.of("git", "add", "."), repo, temp.resolve("logs"));
        command(List.of("git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-m", "baseline"), repo, temp.resolve("logs"));
        return new AutoLlmConfig(new AutoLlmConfig.Project(repo.toString(), "HEAD", List.of("mvn", "verify"), "target/app.jar", null), temp.resolve("workspace").toString(), null, null, null, null, null, null, null, null, null, null, null, null);
    }
    @Test void freshAttemptsIgnoreCachedFixturesAndNeverFollowAMovingRef() throws Exception {
        var config = config(); Path root = temp.resolve("screening"); Files.createDirectories(root);
        Path repo = Path.of(config.project().repository());
        Files.createDirectories(repo.resolve("target/test-classes")); Files.writeString(repo.resolve("target/test-classes/missing.json"), "cached only");
        var first = CommittedBaseline.prepare(root, config, this::command);
        Path source = root.resolve(first.directory()).resolve("source");
        assertFalse(Files.exists(source.resolve("target")));
        Files.writeString(source.resolve("src/test/resources/tracked.txt"), "uncommitted correction");
        AutoLlmFileUtil.write(root.resolve(first.directory()).resolve("build/baseline-result.json"), new CommittedBaseline.Built(new AutoLlmProcessRunner.Result(1, false, 1), null));
        Files.writeString(repo.resolve("src/test/resources/tracked.txt"), "new HEAD");
        command(List.of("git", "add", "."), repo, temp.resolve("logs"));
        command(List.of("git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-m", "advance"), repo, temp.resolve("logs"));
        var second = CommittedBaseline.prepare(root, config, this::command);
        assertNotEquals(first.directory(), second.directory()); assertEquals(first.commit(), second.commit());
        assertEquals("committed", Files.readString(root.resolve(second.directory()).resolve("source/src/test/resources/tracked.txt")));
        assertEquals("uncommitted correction", Files.readString(source.resolve("src/test/resources/tracked.txt")));
    }
    @Test void uncollectedRemoteBuildRetainsItsAttemptUntilCollection() throws Exception {
        var config = config(); Path root = temp.resolve("screening"); Files.createDirectories(root);
        var first = CommittedBaseline.prepare(root, config, this::command); Path build = root.resolve(first.directory()).resolve("build");
        AutoLlmFileUtil.write(build.resolve("remote-job.json"), Map.of("id", "original"));
        assertEquals(first, CommittedBaseline.prepare(root, config, this::command));
        AutoLlmFileUtil.write(build.resolve("collection-completed.json"), Map.of("completed", true));
        AutoLlmFileUtil.write(build.resolve("baseline-result.json"), new CommittedBaseline.Built(new AutoLlmProcessRunner.Result(1, false, 1), null));
        assertNotEquals(first.directory(), CommittedBaseline.prepare(root, config, this::command).directory());
    }
    @Test void uncommittedFixtureCannotBeInsertedBeforeSubmission() throws Exception {
        var config = config(); Path root = temp.resolve("screening"); Files.createDirectories(root);
        var attempt = CommittedBaseline.prepare(root, config, this::command);
        assertDoesNotThrow(() -> CommittedBaseline.verifyBeforeSubmission(root, attempt));
        Files.writeString(root.resolve(attempt.directory()).resolve("source/src/test/resources/missing.json"), "uncommitted correction");
        assertThrows(IllegalStateException.class, () -> CommittedBaseline.verifyBeforeSubmission(root, attempt));
    }
    @Test void collectedButUnsealedSuccessRequiresFreshBuild() throws Exception {
        var config = config(); Path root = temp.resolve("screening"); Files.createDirectories(root);
        var first = CommittedBaseline.prepare(root, config, this::command);
        Path build = root.resolve(first.directory()).resolve("build");
        AutoLlmFileUtil.write(build.resolve("remote-job.json"), Map.of("id", "original"));
        AutoLlmFileUtil.write(build.resolve("remote-status.json"), Map.of("status", "SUCCEEDED"));
        AutoLlmFileUtil.write(build.resolve("collection-completed.json"), Map.of("completed", true));
        assertNotEquals(first.directory(), CommittedBaseline.prepare(root, config, this::command).directory());
    }
    @Test void receiptDetectsChangedEvidenceAndExecutable() throws Exception {
        var config = config(); Path root = temp.resolve("screening"); Files.createDirectories(root);
        var attempt = CommittedBaseline.prepare(root, config, this::command); Path build = root.resolve(attempt.directory()).resolve("build");
        AutoLlmFileUtil.write(build.resolve("collection-completed.json"), Map.of("completed", true));
        AutoLlmFileUtil.write(root.resolve("target-preflight.json"), Map.of("identity", Map.of("hostname", "test")));
        Path jar = root.resolve(attempt.directory()).resolve("source/target/app.jar"); Files.createDirectories(jar.getParent()); Files.writeString(jar, "executable");
        AutoLlmFileUtil.write(build.resolve("baseline-result.json"), new CommittedBaseline.Built(new AutoLlmProcessRunner.Result(0, false, 1), AutoLlmFileUtil.hash(jar)));
        var receipt = CommittedBaseline.completed(root, attempt, config, jar);
        var preparation = new RemoteScreeningService.Preparation(attempt.commit(), root.relativize(jar).toString(), AutoLlmFileUtil.hash(jar), "sota", config, null, List.of(), 1, "default", .15, "pool", receipt);
        assertDoesNotThrow(() -> CommittedBaseline.verify(root, preparation));
        Files.writeString(jar, "changed"); assertThrows(IllegalStateException.class, () -> CommittedBaseline.verify(root, preparation));
        Files.writeString(jar, "executable"); AutoLlmFileUtil.write(build.resolve("baseline-result.json"), new CommittedBaseline.Built(new AutoLlmProcessRunner.Result(0, false, 1), "changed"));
        assertThrows(IllegalStateException.class, () -> CommittedBaseline.verify(root, preparation));
    }
}
