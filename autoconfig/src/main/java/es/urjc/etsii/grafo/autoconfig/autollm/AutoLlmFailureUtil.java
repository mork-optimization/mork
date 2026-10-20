package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.ExecutionTargetException;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.SshTransport;
import es.urjc.etsii.grafo.autoconfig.exception.ComponentDefinitionException;
import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/** Positive attribution is required before asking an LLM to repair a candidate. */
public final class AutoLlmFailureUtil {
    public static final int MAX_RETRIES = 2;
    public enum Category { COMPONENT, TRANSIENT_INFRASTRUCTURE, INFRASTRUCTURE, INTEGRITY, UNKNOWN }
    public record Failure(Category category, String code, String message, List<String> evidence, boolean retryable, String recovery) {}
    public static final class StageException extends IllegalStateException {
        private final Failure failure;
        public StageException(Failure failure, Throwable cause) { super(failure.message(), cause); this.failure = failure; }
        public Failure failure() { return failure; }
    }
    private AutoLlmFailureUtil() {}
    public static StageException error(Category category, String code, String message, Path evidence) {
        return new StageException(new Failure(category, code, message, evidence == null ? List.of() : List.of(evidence.toString()),
                category == Category.TRANSIENT_INFRASTRUCTURE, category == Category.COMPONENT ? "Repair the generated components." : "Inspect the evidence, correct the cause, then resume the saved operation."), null);
    }
    public static Failure describe(Throwable error, Path evidence) {
        return describe(error, evidence, null);
    }

    /** Only a declaration owned by a new concrete component can be attributed using the baseline inventory. */
    public static Failure describe(Throwable error, Path evidence, Set<String> baselineComponents) {
        var causes = new ArrayList<Throwable>();
        var visited = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        for (Throwable cause = error; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof StageException typed) return typed.failure();
            causes.add(cause);
        }
        Category category = Category.UNKNOWN;
        Throwable detail = causes.getLast();
        for (var cause : causes) {
            if (cause instanceof ExecutionTargetException || cause instanceof CodexEvidenceUtil.ArchiveException) {
                category = Category.INTEGRITY; detail = cause; break;
            }
            if (cause instanceof SshTransport.ConnectionException) {
                category = Category.TRANSIENT_INFRASTRUCTURE; detail = cause;
            }
        }
        if (category == Category.UNKNOWN) {
            for (var cause : causes) if (cause instanceof ComponentDefinitionException definition) {
                detail = definition;
                var owner = definition.componentType();
                if (baselineComponents != null && !baselineComponents.contains(owner.getName())
                        && owner.isAnnotationPresent(AlgorithmComponent.class)
                        && Modifier.isPublic(owner.getModifiers()) && !Modifier.isAbstract(owner.getModifiers())) {
                    category = Category.COMPONENT;
                }
                break;
            }
        }
        String code = detail instanceof ComponentDefinitionException ? "INVALID_COMPONENT_DEFINITION" : detail.getClass().getSimpleName();
        return new Failure(category, code, Objects.toString(detail.getMessage(), detail.toString()),
                evidence == null ? List.of() : List.of(evidence.toString()), category == Category.TRANSIENT_INFRASTRUCTURE,
                category == Category.COMPONENT ? "Repair the generated component declaration."
                        : "Inspect the evidence and correct the cause before resuming. No component repair was charged.");
    }
    public static StageException buildFailure(Path source, Path baseline, Path output, String message, java.util.function.UnaryOperator<String> localPath) {
        // Compiler diagnostics must name an added/changed project source. Test-suite and
        // dependency failures without that evidence are deliberately left unclassified.
        var pattern = Pattern.compile("^\\s*(?:\\[ERROR]\\s*)?(.+?\\.java):\\[?[0-9]+", Pattern.MULTILINE);
        for (String log : List.of("stdout.log", "stderr.log")) {
            Path file = output.resolve(log);
            if (!Files.exists(file)) continue;
            var matcher = pattern.matcher(tail(file, 262144));
            while (matcher.find()) {
                Path changed = source.resolve(localPath.apply(matcher.group(1).trim())).normalize();
                if (!changed.startsWith(source) || !Files.isRegularFile(changed)) continue;
                Path original = baseline.resolve(source.relativize(changed));
                if (!Files.isRegularFile(original) || !AutoLlmFileUtil.hash(original).equals(AutoLlmFileUtil.hash(changed)))
                    return error(Category.COMPONENT, "COMPILATION", message, output);
            }
        }
        boolean candidateTests = false, unknownTests = false;
        try (var paths = Files.walk(source)) {
            for (Path report : paths.toList()) if (Files.isRegularFile(report) && report.getParent().getFileName().toString().equals("surefire-reports") && report.getFileName().toString().endsWith(".txt")) {
                String contents = tail(report, 262144);
                if (!contents.contains("<<< FAILURE!") && !contents.contains("<<< ERROR!")) continue;
                String name = report.getFileName().toString().replaceFirst("\\.txt$", "").split("\\$", 2)[0].replace('.', '/');
                Path test = report.getParent().getParent().getParent().resolve("src/test/java/" + name + ".java");
                if (Files.isRegularFile(test) && !Files.exists(baseline.resolve(source.relativize(test)))) candidateTests = true;
                else unknownTests = true;
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        if (candidateTests && !unknownTests) return error(Category.COMPONENT, "GENERATED_TEST", message, output);
        return error(Category.UNKNOWN, "BUILD_FAILED", message + "; failure was not attributable to a changed component", output);
    }
    public static String tail(Path file, int maximumBytes) {
        try (var input = new RandomAccessFile(file.toFile(), "r")) {
            input.seek(Math.max(0, input.length() - maximumBytes));
            byte[] bytes = new byte[(int)Math.min(maximumBytes, input.length())]; input.readFully(bytes);
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
