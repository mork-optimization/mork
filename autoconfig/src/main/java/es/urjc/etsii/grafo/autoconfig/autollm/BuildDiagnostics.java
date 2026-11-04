package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** Diagnostic classification describes the failing tool; it never assigns component blame. */
public final class BuildDiagnostics {
    public record Diagnostic(String kind, String summary, String location, String excerpt, String attribution, String confidence) {}
    // Maven and javac error locations have a diagnostic after the line number;
    // a Java stack frame ends its location with ')' instead.
    static final Pattern COMPILER_ERROR = Pattern.compile(
            "(?m)^\\h*(?:\\[ERROR]\\h*)?([^\\s\\[\\r\\n][^\\r\\n]*?\\.java):(?:\\[\\d+(?:,\\d+)?]\\h+|\\d+(?::\\d+)?:\\h+error:\\h+)[^\\r\\n]+");
    private BuildDiagnostics() {}
    public static Diagnostic classify(String text, String location) {
        String kind = "UNKNOWN", summary = "Unrecognized build failure", confidence = "UNKNOWN";
        var compiler = COMPILER_ERROR.matcher(text);
        if (compiler.find()) { kind = "COMPILER"; summary = compiler.group().strip(); confidence = "HIGH"; }
        else if (hasTestFailure(text)) {
            kind = "TEST"; summary = testFailureSummary(text, location); confidence = "HIGH";
        } else if (text.contains("Could not resolve dependencies") || text.contains("Could not transfer artifact") || text.contains("Could not resolve all files") || text.contains("Non-resolvable parent POM")) {
            kind = "DEPENDENCY"; summary = "Build dependency resolution failed"; confidence = "HIGH";
        }
        return new Diagnostic(kind, bounded(summary, 1024), location, bounded(text, 4096), "UNKNOWN", confidence);
    }
    static boolean isTestReport(Path file) {
        return file.getParent() != null && file.getParent().getFileName() != null
                && Set.of("surefire-reports", "failsafe-reports").contains(file.getParent().getFileName().toString())
                && file.getFileName().toString().endsWith(".txt");
    }
    static boolean hasTestFailure(String text) {
        return text.contains("<<< FAILURE!") || text.contains("<<< ERROR!") || text.contains("There are test failures")
                || text.contains("FAILURE: Build failed") && text.contains("test");
    }
    static String testFailureSummary(String text, String location) {
        var names = new LinkedHashSet<String>();
        Path report = Path.of(location);
        if (isTestReport(report)) names.add(report.getFileName().toString().replaceFirst("\\.txt$", ""));
        for (String line : text.split("\\R")) {
            int failure = line.indexOf("<<< FAILURE!"), error = line.indexOf("<<< ERROR!");
            int marker = failure >= 0 ? failure : error;
            if (marker < 0) continue;
            String name = line.substring(0, marker).strip().replaceFirst("^\\[ERROR]\\h*", "");
            if (name.startsWith("Tests run:")) continue;
            int elapsed = name.indexOf("Time elapsed:");
            if (elapsed >= 0) name = name.substring(0, elapsed).strip().replaceFirst("\\h*--$", "");
            if (!name.isBlank()) names.add(name);
            if (names.size() >= 4) break;
        }
        return names.isEmpty() ? "Test execution failed; inspect the named test report"
                : "Test execution failed: " + bounded(String.join(", ", names), 900);
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
                if (Files.isRegularFile(file) && isTestReport(file)) {
                    String text = AutoLlmFailureUtil.tail(file, 16384);
                    if (hasTestFailure(text)) diagnostics.add(classify(text, file.toString()));
                }
            }
        } catch (java.io.IOException e) { diagnostics.add(new Diagnostic("UNKNOWN", e.toString(), source.toString(), "", "UNKNOWN", "UNKNOWN")); }
        AutoLlmFileUtil.write(output.resolve("build-diagnostics.json"), diagnostics);
    }
}
