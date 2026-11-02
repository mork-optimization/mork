package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** Diagnostic classification describes the failing tool; it never assigns component blame. */
public final class BuildDiagnostics {
    public record Diagnostic(String kind, String summary, String location, String excerpt, String attribution, String confidence) {}
    private BuildDiagnostics() {}
    public static Diagnostic classify(String text, String location) {
        String kind = "UNKNOWN", summary = "Unrecognized build failure", confidence = "UNKNOWN";
        var compiler = Pattern.compile("(?m)^.*\\.java:[\\[(]?[0-9]+.*$").matcher(text);
        if (compiler.find()) { kind = "COMPILER"; summary = compiler.group().strip(); confidence = "HIGH"; }
        else if (text.contains("<<< FAILURE!") || text.contains("<<< ERROR!") || text.contains("There are test failures") || text.contains("FAILURE: Build failed") && text.contains("test")) {
            kind = "TEST"; summary = "Test execution failed; inspect the named test report"; confidence = "HIGH";
        } else if (text.contains("Could not resolve dependencies") || text.contains("Could not transfer artifact") || text.contains("Could not resolve all files") || text.contains("Non-resolvable parent POM")) {
            kind = "DEPENDENCY"; summary = "Build dependency resolution failed"; confidence = "HIGH";
        }
        return new Diagnostic(kind, bounded(summary, 1024), location, bounded(text, 4096), "UNKNOWN", confidence);
    }
    private static String bounded(String value, int bytes) {
        byte[] encoded = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (encoded.length <= bytes) return value;
        return new String(encoded, 0, bytes - 32, java.nio.charset.StandardCharsets.UTF_8) + "\n[excerpt truncated]";
    }
    public static void capture(Path source, Path output) {
        var diagnostics = new ArrayList<Diagnostic>();
        for (String name : List.of("stdout.log", "stderr.log")) {
            Path file = output.resolve(name);
            if (Files.isRegularFile(file)) diagnostics.add(classify(AutoLlmFailureUtil.tail(file, 65536), file.toString()));
        }
        try (var files = Files.walk(source)) {
            for (var iterator = files.iterator(); iterator.hasNext();) {
                Path file = iterator.next();
                if (diagnostics.size() >= 32) break;
                if (Files.isRegularFile(file) && file.getParent().getFileName().toString().equals("surefire-reports") && file.toString().endsWith(".txt")) {
                    String text = AutoLlmFailureUtil.tail(file, 16384);
                    if (text.contains("<<< FAILURE!") || text.contains("<<< ERROR!")) diagnostics.add(classify(text, file.toString()));
                }
            }
        } catch (java.io.IOException e) { diagnostics.add(new Diagnostic("UNKNOWN", e.toString(), source.toString(), "", "UNKNOWN", "UNKNOWN")); }
        AutoLlmFileUtil.write(output.resolve("build-diagnostics.json"), diagnostics);
    }
}
