package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import java.nio.file.*;
import java.util.concurrent.CancellationException;
import static org.junit.jupiter.api.Assertions.*;

class CodexTaskRunnerTest {
    @TempDir Path temp;
    @ParameterizedTest @ValueSource(strings = {"success", "failure", "timeout", "cancel"})
    void archivesAndAccountsAfterEveryProcessOutcome(String outcome) throws Exception {
        Path home = temp.resolve("codex-home"), checkout = temp.resolve("checkout"), output = temp.resolve("attempt");
        Files.createDirectories(checkout);
        Path cli = temp.resolve("codex-fixture");
        Files.writeString(cli, "#!/bin/sh\nexport CODEX_HOME='" + home + "'\nexec '" + Path.of(System.getProperty("java.home"), "bin/java") + "' -cp '" + System.getProperty("java.class.path").replace("'", "'\\''") + "' " + AutoLlmCodexFixture.class.getName() + " \"$@\"\n");
        assertTrue(cli.toFile().setExecutable(true));
        var config = new AutoLlmConfig(null, null, null, null, null, null, null, null, null,
                new AutoLlmConfig.Codex(cli.toString(), null, null, outcome.equals("failure") ? "fail-fixture" : outcome.equals("success") ? null : "hang-fixture", null), null, null, null,
                new AutoLlmConfig.Execution(1, 50L, 2000L, 5000L, 5000L, 5000L, 5000L, 5000L, "128m"));
        var runner = new CodexTaskRunner(new AutoLlmProcessRunner(), new MockEnvironment().withProperty("CODEX_HOME", home.toString()));
        Runnable task = () -> runner.run(config, checkout, output, "fixture prompt", null, () -> {
            if (!outcome.equals("cancel")) return false;
            try { return Files.readString(output.resolve("stdout.log")).contains("fixture.ready"); } catch (Exception e) { return false; }
        });
        if (outcome.equals("success")) assertDoesNotThrow(task::run);
        else if (outcome.equals("cancel")) assertThrows(CancellationException.class, task::run);
        else assertThrows(IllegalStateException.class, task::run);
        var evidence = AutoLlmFileUtil.read(output.resolve("evidence.json"), CodexEvidenceUtil.Evidence.class);
        assertEquals("ARCHIVED", evidence.status()); assertEquals("COMPLETE", evidence.usage().status()); assertEquals(20L, evidence.usage().tokens().totalTokens());
        assertTrue(Files.readString(output.resolve(evidence.conversations().getFirst().path())).contains("fixture prompt"));
        var owner = AutoLlmFileUtil.read(output.resolve("owner.json"), java.util.Map.class);
        assertFalse(ProcessHandle.of(((Number) owner.get("pid")).longValue()).map(ProcessHandle::isAlive).orElse(false));
    }
}
