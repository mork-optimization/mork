package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BuildDiagnosticsTest {
    @TempDir Path temporary;

    @ParameterizedTest
    @ValueSource(strings = {
            "[ERROR] /src/ConstructiveNew.java:[8,4] cannot find symbol",
            "/src/ConstructiveNew.java:8: error: cannot find symbol",
            "[ERROR] C:\\work space\\ConstructiveNew.java:[8,4] cannot find symbol"
    })
    void recognizesCompilerDiagnostics(String text) {
        var diagnostic = BuildDiagnostics.classify(text, "stderr.log");
        assertEquals("COMPILER", diagnostic.kind());
        assertEquals("UNKNOWN", diagnostic.attribution());
    }

    @Test void failingTestStackFramesAreNotCompilerErrors() {
        String text = """
                Tests run: 1, Failures: 1, Errors: 0 <<< FAILURE! -- in framework.LifecycleIT
                framework.LifecycleIT.capacityWait -- Time elapsed: 0.1 s <<< FAILURE!
                java.lang.AssertionError: expected ready
                    at framework.LifecycleIT.capacityWait(LifecycleIT.java:83)
                """;
        var diagnostic = BuildDiagnostics.classify(text, "/module/target/failsafe-reports/framework.LifecycleIT.txt");
        assertEquals("TEST", diagnostic.kind());
        assertTrue(diagnostic.summary().contains("framework.LifecycleIT.capacityWait"), diagnostic.summary());
        assertEquals("UNKNOWN", diagnostic.attribution());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "    at project.ConstructiveNew.run(ConstructiveNew.java:42)",
            "/src/A.java:8: warning: unchecked conversion",
            "[WARNING] /src/A.java:[8,4] unchecked conversion"
    })
    void stackFramesAndWarningsDoNotEstablishACompilationFailure(String text) {
        assertEquals("UNKNOWN", BuildDiagnostics.classify(text, "stderr.log").kind());
    }

    @Test void capturesSurefireAndFailsafeFailuresAndIgnoresPassingReports() throws Exception {
        Path source = temporary.resolve("source"), output = temporary.resolve("build");
        for (String directory : new String[]{"surefire-reports", "failsafe-reports"}) {
            Path reports = source.resolve("module/target/" + directory);
            Files.createDirectories(reports);
            Files.writeString(reports.resolve("project.BrokenTest.txt"), """
                    Tests run: 1, Failures: 0, Errors: 1 <<< FAILURE!
                    project.BrokenTest.check -- Time elapsed: 0.1 s <<< ERROR!
                        at project.BrokenTest.check(BrokenTest.java:25)
                    """);
            Files.writeString(reports.resolve("project.PassingTest.txt"), "Tests run: 1, Failures: 0, Errors: 0");
        }
        BuildDiagnostics.capture(source, output);
        var diagnostics = AutoLlmFileUtil.JSON.readTree(Files.readString(output.resolve("build-diagnostics.json")));
        assertEquals(2, diagnostics.size());
        for (var diagnostic : diagnostics) {
            assertEquals("TEST", diagnostic.path("kind").asString());
            assertEquals("UNKNOWN", diagnostic.path("attribution").asString());
            assertTrue(diagnostic.path("summary").asString().contains("project.BrokenTest.check"));
        }
    }
}
