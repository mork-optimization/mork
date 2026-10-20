package es.urjc.etsii.grafo.autoconfig.autollm;

import mork.startupfixture.StartupFailureFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AutoLlmStartupFailureTest {
    @TempDir Path temp;

    @Test
    void newComponentDeclarationFailureSurvivesEarlySpringStartupFailure() throws Exception {
        var failure = launch("component", List.of(), null);
        assertEquals(AutoLlmFailureUtil.Category.COMPONENT, failure.category());
        assertEquals("INVALID_COMPONENT_DEFINITION", failure.code());
        assertTrue(failure.message().contains("min 2 > max 1"));
    }

    @Test
    void existingComponentDefinitionIsNotAttributedToTheCandidate() throws Exception {
        var failure = launch("component", List.of(StartupFailureFixture.InvalidComponentNew.class.getName()), null);
        assertEquals(AutoLlmFailureUtil.Category.UNKNOWN, failure.category());
        assertEquals("INVALID_COMPONENT_DEFINITION", failure.code());
    }

    @Test
    void baselineAndUnknownFailuresStayUnattributed() throws Exception {
        assertEquals(AutoLlmFailureUtil.Category.UNKNOWN, launch("component", null, null).category());
        assertEquals(AutoLlmFailureUtil.Category.UNKNOWN, launch("unknown", List.of(), null).category());
    }

    @Test
    void startupListenerPreservesAnExistingSpecificFailure() throws Exception {
        var original = AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.COMPONENT, "SEED_ENCODING", "saved seed diagnostic", temp).failure();
        assertEquals(original, launch("unknown", List.of(), original));
    }

    private AutoLlmFailureUtil.Failure launch(String mode, List<String> baseline, AutoLlmFailureUtil.Failure existing) throws Exception {
        Path work = Files.createTempDirectory(temp, "child-");
        Path execution = work.resolve("execution");
        var command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString(),
                "-Dmork.execution.directory=" + execution, "-cp", System.getProperty("java.class.path"), StartupFailureFixture.class.getName(),
                "--advanced.scan-pkgs=mork.startupfixture", "--spring.main.web-application-type=none", "--spring.main.banner-mode=off",
                "--fixture.failure=" + mode));
        if (baseline != null) {
            Path file = work.resolve("baseline.json");
            AutoLlmFileUtil.write(file, baseline);
            command.add("--autollm.child.baseline-components-file=" + file);
        }
        if (existing != null) AutoLlmFileUtil.write(execution.resolve("failure.json"), existing);
        Path log = work.resolve("child.log");
        var process = new ProcessBuilder(command).directory(work.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Startup child timed out");
            assertNotEquals(0, process.exitValue(), () -> AutoLlmFailureUtil.tail(log, 65536));
            assertTrue(Files.exists(execution.resolve("failure.json")), () -> AutoLlmFailureUtil.tail(log, 65536));
            assertFalse(Files.exists(execution.resolve("readiness.json")));
            assertEquals("FAILED", AutoLlmFileUtil.JSON.readTree(Files.readString(execution.resolve("terminal.json"))).path("outcome").asString());
            return AutoLlmFileUtil.read(execution.resolve("failure.json"), AutoLlmFailureUtil.Failure.class);
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }
}
