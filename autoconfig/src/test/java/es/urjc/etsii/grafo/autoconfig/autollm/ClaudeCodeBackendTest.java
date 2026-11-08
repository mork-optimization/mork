package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ClaudeCodeBackendTest {
    @TempDir Path temp;
    @ParameterizedTest @ValueSource(strings = {"success", "missing", "mismatch", "quota", "capacity", "hang"})
    void ownsArchivesAndRecoversClaudeSessions(String mode) throws Exception {
        Path home = temp.resolve("home"), checkout = temp.resolve("checkout"), output = temp.resolve("attempt");
        Files.createDirectories(checkout);
        Path cli = temp.resolve("claude-fixture");
        Files.writeString(cli, "#!/bin/sh\nexec '" + Path.of(System.getProperty("java.home"), "bin/java") + "' -cp '" + System.getProperty("java.class.path").replace("'", "'\\''") + "' " + AutoLlmClaudeFixture.class.getName() + " \"$@\"\n");
        assertTrue(cli.toFile().setExecutable(true));
        var config = new AutoLlmConfig(null, null, null, null, null, null, null, null, null,
                new AutoLlmConfig.Generation(AutoLlmConfig.Agent.CLAUDE_CODE, cli.toString(), null, null, mode, null, 1), null, null, null,
                new AutoLlmConfig.Execution(1, 100L, 2000L, 5000L, 5000L, 5000L, 5000L, 5000L, "128m", null, null));
        var runner = new GenerationTaskRunner(new AutoLlmProcessRunner(), new MockEnvironment().withProperty("CLAUDE_CONFIG_DIR", home.toString()));
        if (mode.equals("success")) {
            var first = runner.run(config, checkout, output, "fixture", null, () -> false);
            assertEquals(first, runner.recoveredResult(output, first.sessionId()));
            Path resumed = temp.resolve("resumed");
            assertEquals(first.sessionId(), runner.run(config, checkout, resumed, "repair", first.sessionId(), () -> false).sessionId());
            assertEquals(20L, AutoLlmFileUtil.read(resumed.resolve("usage.json"), GenerationEvidence.Usage.class).summary().tokens().totalTokens());
            assertEquals(20L, AutoLlmFileUtil.read(output.resolve("usage.json"), GenerationEvidence.Usage.class).summary().tokens().totalTokens());
            assertEquals("ARCHIVED", runner.archive(checkout, output, first.sessionId()).status());
        } else {
            Class<? extends RuntimeException> expected = switch (mode) {
                case "quota" -> GenerationQuotaUtil.QuotaLimitException.class;
                case "capacity" -> GenerationCapacityUtil.CapacityException.class;
                default -> IllegalStateException.class;
            };
            assertThrows(expected, () -> runner.run(config, checkout, output, "fixture", null, () -> false));
        }
        assertTrue(Files.isRegularFile(output.resolve("process-terminated.json")));
        if (!mode.equals("mismatch")) assertEquals("ARCHIVED", AutoLlmFileUtil.read(output.resolve("evidence.json"), GenerationEvidence.Evidence.class).status());
    }
}
