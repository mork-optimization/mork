package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.AutoLlmExecutionService.LocalJob;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Audited operator assertions are distinct from process-runner termination receipts. */
public final class AutoLlmLocalRecovery {
    private AutoLlmLocalRecovery() {}
    public record Job(String id, String submission, String directory, String state, Object process,
                      Object children, boolean confirmationAllowed, String reason) {}
    public record Confirmation(String submission, boolean confirmStopped, String reason) {}
    public record Receipt(String jobId, String submission, Map<String,String> evidence, String confirmedAt,
                          String reason, String basis) {}

    public static List<Job> inspect(Path trial) {
        var result = new ArrayList<Job>();
        for (Path descriptor : AutoLlmFileUtil.ownedFiles(trial, "local-job.json")) {
            Path output = descriptor.getParent();
            var job = AutoLlmFileUtil.read(descriptor, LocalJob.class);
            String submission = submission(output);
            String state = operatorConfirmed(output) ? "OPERATOR_CONFIRMED" : unconfirmed(output) ? "TERMINATION_UNKNOWN"
                    : Files.exists(output.resolve("collection-completed.json")) || Files.exists(output.resolve("collection-discarded.json")) ? "COLLECTED"
                    : Files.exists(output.resolve("workload-result.json")) ? "PENDING_COLLECTION" : "INTERRUPTED";
            String reason = scopeReason(trial, output, job);
            if (reason == null && !state.equals("TERMINATION_UNKNOWN") && !state.equals("OPERATOR_CONFIRMED")) reason = "This job does not need an operator termination assertion";
            if (reason == null && knownProcessAlive(output)) reason = "A recorded process PID is live or has been reused";
            result.add(new Job(job.id(), submission, trial.relativize(output).toString(), state,
                    readOptional(output.resolve("owner.json")), readOptional(output.resolve("process-children.json")), reason == null, reason));
        }
        return result;
    }
    public static boolean hasUnconfirmed(Path root) {
        for (Path file : AutoLlmFileUtil.ownedFiles(root, "local-job.json")) if (unconfirmed(file.getParent())) return true;
        return false;
    }
    public static boolean unconfirmed(Path output) {
        // Collection is recorded only after the process runner returned. A later
        // reuse of an old PID must not reopen already collected historical work.
        if (Files.isRegularFile(output.resolve("collection-completed.json")) || Files.isRegularFile(output.resolve("collection-discarded.json"))) return false;
        if (!Files.exists(output.resolve("process-intent.json"))) return !operatorConfirmed(output);
        try { AutoLlmProcessRunner.requireTerminated(output); return false; }
        catch (IllegalStateException e) { return true; }
    }
    public static Receipt confirm(Path trial, String jobId, Confirmation request) {
        if (!request.confirmStopped() || request.reason() == null || request.reason().isBlank())
            throw new IllegalArgumentException("Explicit confirmation that the process tree stopped and an audit reason are required");
        for (var inspection : inspect(trial)) if (inspection.id().equals(jobId)) {
            if (!Objects.equals(request.submission(), inspection.submission())) throw new IllegalStateException("Local submission identity changed");
            if (!inspection.confirmationAllowed()) throw new IllegalStateException(inspection.reason());
            Path output = trial.resolve(inspection.directory());
            Path receiptFile = output.resolve("local-recovery.json");
            Receipt receipt;
            if (operatorConfirmed(output)) receipt = AutoLlmFileUtil.read(receiptFile, Receipt.class);
            else {
                receipt = new Receipt(jobId, request.submission(), evidence(output), Instant.now().toString(), request.reason().strip(), "OPERATOR_ASSERTION_AFTER_HOST_INSPECTION");
                AutoLlmFileUtil.write(receiptFile, receipt);
            }
            retireRequest(trial, output.getParent().getFileName().toString(), receipt);
            return receipt;
        }
        throw new NoSuchElementException("Unknown local job " + jobId);
    }
    private static void retireRequest(Path trial, String requestId, Receipt receipt) {
        Path durable = trial.resolve("metadata-requests"), saved = durable.resolve(requestId + ".result.json");
        try {
            Path previous = durable.resolve(requestId + ".before-recovery.result.json");
            if (Files.isRegularFile(saved) && !Files.exists(previous)) Files.copy(saved, previous);
        } catch (IOException e) { throw new UncheckedIOException(e); }
        var response = Map.of("error", "Metadata request interrupted. Its local job was reconciled by an operator; run the metadata helper again to create a fresh request ID.",
                "status", "INTERRUPTED", "recovery", receipt);
        AutoLlmFileUtil.write(saved, response);
        AutoLlmFileUtil.write(trial.resolve("source/.mork-metadata/" + requestId + ".result.json"), response);
    }
    public static boolean operatorConfirmed(Path output) {
        Path file = output.resolve("local-recovery.json");
        if (!Files.isRegularFile(file)) return false;
        var receipt = AutoLlmFileUtil.read(file, Receipt.class);
        var job = AutoLlmFileUtil.read(output.resolve("local-job.json"), LocalJob.class);
        Path trial = Path.of(job.owner().localRoot());
        return "OPERATOR_ASSERTION_AFTER_HOST_INSPECTION".equals(receipt.basis()) && scopeReason(trial, output, job) == null
                && job.id().equals(receipt.jobId()) && Objects.equals(submission(output), receipt.submission())
                && evidence(output).equals(receipt.evidence());
    }
    private static String scopeReason(Path trial, Path output, LocalJob job) {
        if (job.owner().remote() || job.owner().assignment() != null || !Path.of(job.owner().localRoot()).equals(trial.toAbsolutePath().normalize())) return "Only local trial metadata jobs can be confirmed";
        if (!output.startsWith(trial) || !trial.relativize(output).toString().replace('\\', '/').matches("metadata/[a-f0-9]{32}/(build|check)")) return "Only metadata build/check jobs can be confirmed";
        try {
            if (!output.toRealPath().equals(output.toAbsolutePath().normalize())) return "Symbolic links cannot be reconciled";
            for (String name : List.of("local-job.json", "process-intent.json", "owner.json")) if (Files.isSymbolicLink(output.resolve(name))) return "Symbolic evidence cannot be reconciled";
        } catch (IOException e) { throw new UncheckedIOException(e); }
        if (submission(output).isBlank()) return "A saved submission identity is required";
        if (Files.exists(output.resolve("workload-result.json"))) return "Collect the recorded workload result instead";
        return null;
    }
    private static boolean knownProcessAlive(Path output) {
        var owner = readOptional(output.resolve("owner.json"));
        if (owner instanceof Map<?,?> record && alive(record.get("pid"))) return true;
        var children = readOptional(output.resolve("process-children.json"));
        if (children instanceof Map<?,?> record && record.get("children") instanceof List<?> entries)
            for (var entry : entries) if (entry instanceof Map<?,?> child && alive(child.get("pid"))) return true;
        return false;
    }
    private static boolean alive(Object pid) { return pid instanceof Number n && ProcessHandle.of(n.longValue()).filter(ProcessHandle::isAlive).isPresent(); }
    private static String submission(Path output) {
        var intent = readOptional(output.resolve("process-intent.json"));
        return intent instanceof Map<?,?> map ? Objects.toString(map.get("submission"), "") : "";
    }
    private static Object readOptional(Path file) { return Files.isRegularFile(file) ? AutoLlmFileUtil.read(file, Object.class) : null; }
    private static Map<String,String> evidence(Path output) {
        var hashes = new TreeMap<String,String>();
        for (String name : List.of("local-job.json", "process-intent.json", "owner.json", "process-children.json"))
            if (Files.isRegularFile(output.resolve(name))) hashes.put(name, AutoLlmFileUtil.hash(output.resolve(name)));
        return hashes;
    }
}
