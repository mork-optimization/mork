package es.urjc.etsii.grafo.autoconfig.autollm;
import java.nio.file.*;
public class AutoLlmBuildFixture {
    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("target"));
        Path generated = Path.of("src/main/java/FixtureUtil.java");
        if (Files.exists(generated) && javax.tools.ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", "target", generated.toString()) != 0) {
            throw new IllegalStateException("Generated fixture component failed compilation");
        }
        Files.copy(Path.of("template.jar"), Path.of("target/project.jar"), StandardCopyOption.REPLACE_EXISTING);
    }
}
