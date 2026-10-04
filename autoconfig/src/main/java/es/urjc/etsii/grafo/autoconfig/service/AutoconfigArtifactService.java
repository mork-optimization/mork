package es.urjc.etsii.grafo.autoconfig.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Manages the fixed set of downloadable files produced by the current IRACE run. */
@Service
public class AutoconfigArtifactService {

    private final Path workingDirectory;
    private String runId;

    public AutoconfigArtifactService() {
        this(Path.of("").toAbsolutePath().normalize());
    }

    AutoconfigArtifactService(Path workingDirectory) {
        this.workingDirectory = workingDirectory.toAbsolutePath().normalize();
    }

    public synchronized void prepareRun(String runId) {
        Objects.requireNonNull(runId, "Run ID cannot be null");
        try {
            for (var artifact : ArtifactDefinition.values()) {
                Files.deleteIfExists(path(artifact));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot remove artifacts from the previous autoconfig run", e);
        }
        this.runId = runId;
    }

    public synchronized ArtifactManifest manifest(String requestedRunId) {
        if (!isCurrentRun(requestedRunId)) {
            return null;
        }
        var artifacts = new ArrayList<ArtifactView>();
        for (var artifact : ArtifactDefinition.values()) {
            var artifactPath = path(artifact);
            if (!isRegularFile(artifactPath)) {
                continue;
            }
            try {
                artifacts.add(new ArtifactView(
                        artifact.id,
                        artifact.filename,
                        artifact.mediaType,
                        Files.size(artifactPath),
                        Files.getLastModifiedTime(artifactPath, LinkOption.NOFOLLOW_LINKS).toInstant()
                ));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot inspect autoconfig artifact " + artifact.filename, e);
            }
        }
        return new ArtifactManifest(runId, artifacts);
    }

    public synchronized ArtifactDownload download(String requestedRunId, String artifactId) {
        if (!isCurrentRun(requestedRunId)) {
            return null;
        }
        var artifact = ArtifactDefinition.find(artifactId);
        if (artifact == null) {
            return null;
        }
        var artifactPath = path(artifact);
        if (!isRegularFile(artifactPath)) {
            return null;
        }
        return new ArtifactDownload(artifact.filename, artifact.mediaType, artifactPath);
    }

    private boolean isCurrentRun(String requestedRunId) {
        return runId != null && runId.equals(requestedRunId);
    }

    private Path path(ArtifactDefinition artifact) {
        return workingDirectory.resolve(artifact.filename);
    }

    private static boolean isRegularFile(Path path) {
        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
    }

    public record ArtifactManifest(String runId, List<ArtifactView> artifacts) {
        public ArtifactManifest {
            artifacts = List.copyOf(artifacts);
        }
    }

    public record ArtifactView(
            String id,
            String filename,
            String mediaType,
            long size,
            Instant lastModified
    ) {
    }

    public record ArtifactDownload(String filename, String mediaType, Path path) {
    }

    private enum ArtifactDefinition {
        FINAL_ELITES("final-elites", "autoconfig-final-elites.json", "application/json"),
        PLOTS("plots", "plots.pdf", "application/pdf"),
        IRACE_DATA("irace-data", "irace.Rdata", "application/octet-stream"),
        STDOUT("stdout", "runner.R.stdout.log", "text/plain;charset=UTF-8"),
        STDERR("stderr", "runner.R.stderr.log", "text/plain;charset=UTF-8");

        private final String id;
        private final String filename;
        private final String mediaType;

        ArtifactDefinition(String id, String filename, String mediaType) {
            this.id = id;
            this.filename = filename;
            this.mediaType = mediaType;
        }

        private static ArtifactDefinition find(String id) {
            for (var artifact : values()) {
                if (artifact.id.equals(id)) {
                    return artifact;
                }
            }
            return null;
        }
    }
}
