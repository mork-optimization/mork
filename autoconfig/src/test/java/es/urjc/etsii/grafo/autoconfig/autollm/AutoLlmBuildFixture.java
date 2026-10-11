package es.urjc.etsii.grafo.autoconfig.autollm;
import java.nio.file.*;
public class AutoLlmBuildFixture {
    public static void main(String[] args) throws Exception { Files.createDirectories(Path.of("target")); Files.copy(Path.of("template.jar"), Path.of("target/project.jar"), StandardCopyOption.REPLACE_EXISTING); }
}
