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
                new AutoLlmConfig.Execution(1, 50L, 2000L, 5000L, 5000L, 5000L, 5000L, 5000L, "128m", null, null));
        var runner = new CodexTaskRunner(new AutoLlmProcessRunner(), new MockEnvironment().withProperty("CODEX_HOME", home.toString()));
        Runnable task = () -> runner.run(config, checkout, output, "custom fixture prompt", null, () -> {
            if (!outcome.equals("cancel")) return false;
            try { return Files.readString(output.resolve("stdout.log")).contains("fixture.ready"); } catch (Exception e) { return false; }
        });
        if (outcome.equals("success")) assertDoesNotThrow(task::run);
        else if (outcome.equals("cancel")) assertThrows(CancellationException.class, task::run);
        else assertThrows(IllegalStateException.class, task::run);
        var evidence = AutoLlmFileUtil.read(output.resolve("evidence.json"), CodexEvidenceUtil.Evidence.class);
        assertEquals("ARCHIVED", evidence.status()); assertEquals("COMPLETE", evidence.usage().status()); assertEquals(20L, evidence.usage().tokens().totalTokens());
        String sourceInstructions;
        try (var input = getClass().getResourceAsStream("/autollm/source-instructions.txt")) {
            sourceInstructions = new String(java.util.Objects.requireNonNull(input).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        String expectedPrompt = "custom fixture prompt\n\n" + sourceInstructions;
        assertEquals(expectedPrompt, Files.readString(output.resolve("prompt.txt")));
        assertTrue(Files.readString(output.resolve(evidence.conversations().getFirst().path())).contains("custom fixture prompt"));
        var owner = AutoLlmFileUtil.read(output.resolve("owner.json"), java.util.Map.class);
        var command = assertInstanceOf(java.util.List.class, owner.get("command"));
        assertEquals(expectedPrompt, command.getLast());
        assertFalse(ProcessHandle.of(((Number) owner.get("pid")).longValue()).map(ProcessHandle::isAlive).orElse(false));
        if (outcome.equals("success")) {
            Path resumed = temp.resolve("resumed-attempt");
            var result = runner.run(config, checkout, resumed, "custom continuation", evidence.threadId(), () -> false);
            assertEquals(evidence.threadId(), result.threadId());
            String resumedPrompt = "custom continuation\n\n" + sourceInstructions;
            assertEquals(resumedPrompt, Files.readString(resumed.resolve("prompt.txt")));
            var resumedOwner = AutoLlmFileUtil.read(resumed.resolve("owner.json"), java.util.Map.class);
            var resumedCommand = assertInstanceOf(java.util.List.class, resumedOwner.get("command"));
            assertEquals(evidence.threadId(), resumedCommand.get(resumedCommand.indexOf("resume") + 1));
            assertEquals(resumedPrompt, resumedCommand.getLast());
        }
    }
}
