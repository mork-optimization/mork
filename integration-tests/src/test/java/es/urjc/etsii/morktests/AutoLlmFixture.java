package es.urjc.etsii.morktests;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import javax.tools.ToolProvider;
import tools.jackson.databind.json.JsonMapper;

/** Deterministic Codex/build replacements; the child Mork and R processes are real. */
public class AutoLlmFixture {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("login")) { System.out.println("Authenticated fixture"); return; }
        if (args[0].equals("build")) { build(); return; }
        if (List.of(args).contains("--version")) { System.out.println("fixture-codex 1"); return; }
        String thread = args.length > 2 && args[1].equals("resume") ? args[2] : UUID.randomUUID().toString();
        Path source = Path.of("src/main/java/es/urjc/etsii/grafo/autoconfigtests/components/FasterInvertedConstructiveNew.java");
        Files.createDirectories(source.getParent()); Files.writeString(source, """
                package es.urjc.etsii.grafo.autoconfigtests.components;
                import es.urjc.etsii.grafo.annotations.*;
                public class FasterInvertedConstructiveNew extends FasterInvertedConstructive {
                    @AutoconfigConstructor
                    public FasterInvertedConstructiveNew(@RealParam(min=-50, max=50) double sumThis) { super(sumThis); }
                }
                """);
        int output = List.of(args).indexOf("--output-last-message");
        var json = new JsonMapper();
        Files.writeString(Path.of(args[output + 1]), json.writeValueAsString(Map.of("inventory", List.of("SimpleAlgorithm"), "metaheuristics", List.of("SimpleAlgorithm"), "components", List.of("FasterInvertedConstructiveNew"), "blacklist", "", "tests", List.of("fixture build"), "limitations", List.of("Deterministic fixture"))));
        System.out.println(json.writeValueAsString(Map.of("type", "thread.started", "thread_id", thread)));
        System.out.println("{\"type\":\"turn.completed\"}");
    }
    private static void build() throws Exception {
        Path classes = Path.of("target/classes"); Files.createDirectories(classes);
        var command = new ArrayList<>(List.of("-classpath", System.getProperty("java.class.path"), "-d", classes.toString()));
        try (var files = Files.walk(Path.of("src/main/java"))) { for (Path file : files.toList()) if (file.toString().endsWith(".java")) command.add(file.toString()); }
        if (command.size() > 4 && ToolProvider.getSystemJavaCompiler().run(null, null, null, command.toArray(String[]::new)) != 0) throw new IllegalStateException("Fixture compilation failed");
        var manifest = new Manifest(); manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "es.urjc.etsii.grafo.autoconfigtests.Main");
        var classpath = new ArrayList<String>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) classpath.add(Path.of(entry).toAbsolutePath().toUri().toString());
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, String.join(" ", classpath));
        try (var jar = new JarOutputStream(Files.newOutputStream(Path.of("target/project.jar")), manifest); var files = Files.walk(classes)) {
            for (Path file : files.toList()) if (!file.equals(classes)) {
                boolean directory = Files.isDirectory(file);
                jar.putNextEntry(new JarEntry(classes.relativize(file).toString() + (directory ? "/" : "")));
                if (!directory) Files.copy(file, jar); jar.closeEntry();
            }
        }
    }
}
