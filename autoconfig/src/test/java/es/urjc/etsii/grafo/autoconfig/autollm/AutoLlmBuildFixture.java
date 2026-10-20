package es.urjc.etsii.grafo.autoconfig.autollm;
import java.nio.file.*;
public class AutoLlmBuildFixture {
    public static void main(String[] args) throws Exception {
        if (java.util.Arrays.asList(args).contains("--fail-replay") && Path.of("").toAbsolutePath().toString().contains("baseline-replay")) {
            System.err.println("Unchanged framework test failed in relocated baseline"); System.exit(1);
        }
        Path marker = Path.of(".framework-failed");
        if (java.util.Arrays.asList(args).contains("--fail-once-trial") && Path.of("").toAbsolutePath().toString().contains("trials/001/source") && !Files.exists(marker)) {
            Files.writeString(marker, "fixture framework failure"); System.err.println("Unchanged framework test failed"); System.exit(1);
        }
        Files.createDirectories(Path.of("target"));
        Path generated = Path.of("src/main/java/FixtureUtil.java");
        if (Files.exists(generated) && javax.tools.ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", "target", generated.toString()) != 0) {
            throw new IllegalStateException("Generated fixture component failed compilation");
        }
        Files.copy(Path.of("template.jar"), Path.of("target/project.jar"), StandardCopyOption.REPLACE_EXISTING);
    }
}
