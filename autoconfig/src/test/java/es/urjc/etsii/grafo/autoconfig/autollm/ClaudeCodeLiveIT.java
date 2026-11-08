package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.StandardEnvironment;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in, paid CLI qualification using the user's existing login. */
@EnabledIfSystemProperty(named = "mork.claude.liveModel", matches = ".+")
class ClaudeCodeLiveIT {
    @TempDir Path temp;
    @Test void generateRepairAndRecover() throws Exception {
        Path checkout = temp.resolve("source"); Files.createDirectories(checkout);
        var config = new AutoLlmConfig(null, null, null, null, null, null, null, null, null,
                new AutoLlmConfig.Generation(AutoLlmConfig.Agent.CLAUDE_CODE, null, null, null, System.getProperty("mork.claude.liveModel"), null, 1),
                null, null, null, new AutoLlmConfig.Execution(null, null, 180_000L, null, 90_000L, null, null, null, null, null, null));
        var runner = new GenerationTaskRunner(new AutoLlmProcessRunner(), new StandardEnvironment());
        Path first = temp.resolve("first");
        var generated = runner.run(config, checkout, first, "This is a CLI protocol smoke test in an empty directory. Create smoke.txt containing first. Do not build or search for projects. Return the required manifest with empty arrays and null blacklist; no optimization components are needed for this smoke test.", null, () -> false);
        assertEquals("first", Files.readString(checkout.resolve("smoke.txt")).strip());
        Path repair = temp.resolve("repair");
        var repaired = runner.run(config, checkout, repair, "Continue the smoke test: replace smoke.txt with repaired. Return the same empty manifest. Do not search for projects or run builds.", generated.sessionId(), () -> false);
        assertEquals(generated.sessionId(), repaired.sessionId());
        assertEquals("repaired", Files.readString(checkout.resolve("smoke.txt")).strip());
        assertEquals(repaired, runner.recoveredResult(repair, repaired.sessionId()));
        assertEquals("ARCHIVED", runner.archive(checkout, repair, repaired.sessionId()).status());
    }
}
