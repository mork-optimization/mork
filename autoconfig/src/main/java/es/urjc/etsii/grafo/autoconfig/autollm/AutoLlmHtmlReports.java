package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.FMode;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Atomic portable report publication and durable generation receipts. */
public final class AutoLlmHtmlReports {
    private AutoLlmHtmlReports() {}
    public record Status(String requestId, String state, String snapshotAt, String finishedAt,
                         AutoLlmPortableReport.Selection selection, Map<String,FMode> objectives,
                         String path, String failure, String exportFailure) {}
    public static Status status(AutoLlmCampaign c, Path root, String requestId) {
        if (requestId != null && !requestId.matches("[a-f0-9-]{36}")) throw new IllegalArgumentException("Invalid report request ID");
        Path file = root.resolve(requestId == null ? "reports/report-status.json" : "reports/report-jobs/" + requestId + ".json");
        if (Files.isRegularFile(file)) return AutoLlmFileUtil.read(file, Status.class);
        if (requestId != null) throw new NoSuchElementException("Report request unavailable");
        return new Status(null, "NOT_STARTED", null, null, AutoLlmPortableReport.selection(c, root, null),
                AutoLlmPortableReport.objectives(c, root), existing(root), null, null);
    }
    public static Status queued(AutoLlmCampaign c, Path root, AutoLlmPortableReport.Selection requested) {
        requireAvailable(root);
        var selection = AutoLlmPortableReport.selection(c, root, requested);
        var objectives = AutoLlmPortableReport.objectives(c, root);
        if (selection.objective() != null && !objectives.isEmpty() && objectives.get(selection.objective()) != selection.mode())
            throw new IllegalArgumentException("Objective or direction differs from saved campaign metadata");
        if (requested != null && requested.objective() != null) AutoLlmFileUtil.write(root.resolve("reports/report-settings.json"), selection);
        var status = new Status(UUID.randomUUID().toString(), "QUEUED", Instant.now().toString(), null, selection, objectives, existing(root), null, null);
        save(root, status); return status;
    }
    public static Status generate(AutoLlmCampaign c, Path root, Status queued, String exportFailure) {
        if (!available(root)) return queued;
        save(root, new Status(queued.requestId(), "RUNNING", queued.snapshotAt(), null, queued.selection(), queued.objectives(), existing(root), null, exportFailure));
        Path temporary = null;
        Status result;
        try {
            var snapshot = AutoLlmPortableReport.build(c, root, queued.selection(), Instant.parse(queued.snapshotAt()));
            String html = AutoLlmReportHtml.render(snapshot);
            requireAvailable(root);
            temporary = Files.createTempFile(root.resolve("reports"), ".report-", ".html");
            Files.writeString(temporary, html);
            requireAvailable(root);
            String immutable = "reports/report-jobs/" + queued.requestId() + ".html";
            Files.copy(temporary, root.resolve(immutable));
            try { Files.move(temporary, root.resolve("reports/report.html"), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, root.resolve("reports/report.html"), StandardCopyOption.REPLACE_EXISTING); }
            result = new Status(queued.requestId(), "SUCCEEDED", queued.snapshotAt(), Instant.now().toString(), queued.selection(), queued.objectives(), immutable, null, exportFailure);
        } catch (IOException | RuntimeException e) {
            result = new Status(queued.requestId(), "FAILED", queued.snapshotAt(), Instant.now().toString(), queued.selection(), queued.objectives(), existing(root), e.toString(), exportFailure);
        } finally {
            if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* Failed temporary output is disposable. */ }
        }
        if (available(root)) save(root, result);
        return result;
    }
    static boolean available(Path root) {
        return Files.isRegularFile(root.resolve("campaign.json")) && !AutoLlmCleanup.requested(root) && !AutoLlmCleanup.requested(root.getParent());
    }
    private static void requireAvailable(Path root) { if (!available(root)) throw new IllegalStateException("Campaign is unavailable or cleanup is pending"); }
    private static String existing(Path root) { return Files.isRegularFile(root.resolve("reports/report.html")) ? "reports/report.html" : null; }
    private static void save(Path root, Status status) {
        requireAvailable(root);
        AutoLlmFileUtil.write(root.resolve("reports/report-jobs/" + status.requestId() + ".json"), status);
        AutoLlmFileUtil.write(root.resolve("reports/report-status.json"), status);
    }
}
