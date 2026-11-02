package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Frozen trial sources reference a campaign-owned baseline and a verified binary patch snapshot. */
public final class AutoLlmSourceService {
    private AutoLlmSourceService() {}
    public record Source(int version, String commit, String snapshot, String manifestSha256,
                         List<String> buildCommand, String artifact, Map<String,String> toolVersions) {}

    public static Source freeze(Path campaign, Path trial, Path source, Path attempt, String commit,
                                AutoLlmConfig config, Map<String,String> versions, GitSourceStore.Command command) throws IOException {
        var record = capture(campaign, source, attempt, commit, config, versions, command);
        AutoLlmFileUtil.write(trial.resolve("frozen/source-snapshot.json"), record);
        return record;
    }

    /** Capture another retained source state without replacing the measured trial's frozen source pointer. */
    public static Source capture(Path campaign, Path source, Path attempt, String commit,
                                 AutoLlmConfig config, Map<String,String> versions, GitSourceStore.Command command) throws IOException {
        Path bundle = GitSourceStore.bundle(campaign.resolve(".store/git"), source, commit, attempt.resolve("source-bundle"), command);
        Path snapshot = attempt.resolve("source-snapshot-" + UUID.randomUUID());
        GitPatchSnapshot.capture(bundle, commit, source, snapshot, command);
        Path verification = attempt.resolve("source-verification-" + UUID.randomUUID());
        try { GitPatchSnapshot.restore(bundle, snapshot, verification, command); }
        finally { WorkloadArchiveUtil.deleteTree(verification); }
        AutoLlmFileUtil.write(snapshot.resolve("verified.json"), Map.of("verified", true, "manifestSha256", AutoLlmFileUtil.hash(snapshot.resolve("snapshot.json"))));
        var record = new Source(1, commit, campaign.relativize(snapshot).toString().replace('\\', '/'),
                AutoLlmFileUtil.hash(snapshot.resolve("snapshot.json")), config.project().buildCommand(), config.project().artifact(), Map.copyOf(versions));
        return record;
    }

    public static Source restore(Path campaign, Path trial, Path destination, GitSourceStore.Command command) throws IOException {
        var record = AutoLlmFileUtil.read(trial.resolve("frozen/source-snapshot.json"), Source.class);
        return restore(campaign, record, destination, command);
    }

    public static Source restore(Path campaign, Source record, Path destination, GitSourceStore.Command command) throws IOException {
        if (record.version() != 1 || !record.commit().matches("[a-f0-9]{40,64}")) throw new IOException("Unsupported source snapshot");
        Path snapshot = WorkloadPathUtil.inside(campaign, record.snapshot());
        if (!AutoLlmFileUtil.hash(snapshot.resolve("snapshot.json")).equals(record.manifestSha256())) throw new IOException("Frozen source manifest changed");
        Path normalized = destination.toAbsolutePath().normalize();
        if (normalized.startsWith(campaign.toAbsolutePath().normalize())) throw new IOException("Restore sources outside the campaign evidence directory");
        GitPatchSnapshot.restore(campaign.resolve(".store/git/" + record.commit() + ".bundle"), snapshot, normalized, command);
        return record;
    }
}
