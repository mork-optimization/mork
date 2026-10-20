package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScreeningJobServiceTest {
    @TempDir Path temp;
    private AutoLlmCampaignStore campaigns;
    private ScreeningJobService service;
    private ScreeningJobService.Request request;
    @BeforeEach void setup() throws Exception {
        Path repo = temp.resolve("repository"); Files.createDirectories(repo.resolve(".git"));
        Files.writeString(repo.resolve("sota.json"), "{}"); Files.writeString(repo.resolve("pool.index"), "input");
        var config = new AutoLlmConfig(new AutoLlmConfig.Project(repo.toString(), "HEAD", List.of("./mvnw", "verify"), "target/app.jar", null), temp.resolve("workspace").toString(), null, null, null,
                "sota.json", null, null, null, null, null, null, null, new AutoLlmConfig.Execution(32, null, null, null, null, null, null, null, null, null, null));
        campaigns = new AutoLlmCampaignStore(config); service = new ScreeningJobService(campaigns, mock(AutoLlmProcessRunner.class));
        request = new ScreeningJobService.Request(config, new InstanceScreeningConfig(null, null, null, null, null, null, null, null, null, null, null), "pool.index", null);
    }
    @AfterEach void close() { service.close(); }
    @Test void creationSavesDraftWithOwnedOutputAndPreservesWorkerOverride() {
        var job = service.create(request); assertEquals("DRAFT", job.status); assertNull(service.active());
        var saved = service.load(job.id); assertEquals(32, saved.request.config().execution().workers()); assertEquals(0.15, saved.request.trainingFraction());
        assertTrue(Path.of(saved.request.settings().output()).startsWith(campaigns.root().resolve("screenings").resolve(job.id)));
        assertEquals(job.id, service.list().getFirst().id);
    }
    @Test void approvalRequiresCompletedReviewAndExactHash() throws Exception {
        var job = service.create(request); assertThrows(IllegalStateException.class, () -> service.approve(job.id, "hash"));
        job.status = "AWAITING_APPROVAL"; job.mirror = campaigns.root().resolve("screenings").resolve(job.id).resolve("evidence/local-fixture").toString();
        AutoLlmFileUtil.write(Path.of(job.mirror).resolve("proposal/proposal.json"), Map.of("reviewed", true));
        AutoLlmFileUtil.write(campaigns.root().resolve("screenings").resolve(job.id).resolve("job.json"), job);
        assertThrows(IllegalArgumentException.class, () -> service.approve(job.id, "stale-hash"));
        assertEquals("AWAITING_APPROVAL", service.load(job.id).status); assertNull(service.active());
    }
    @Test void unresolvedCampaignPreventsStartingScreening() {
        var job = service.create(request); campaigns.reserve(UUID.randomUUID().toString());
        assertThrows(IllegalStateException.class, () -> service.resume(job.id)); assertEquals("DRAFT", service.load(job.id).status);
    }
    @Test void failedCheckpointWriteReleasesOwnershipWithoutStartingWork() throws Exception {
        var job = service.create(request);
        Files.createDirectory(campaigns.root().resolve("screenings").resolve(job.id).resolve("job.json.tmp"));
        assertThrows(java.io.UncheckedIOException.class, () -> service.resume(job.id)); assertNull(service.active());
        assertDoesNotThrow(() -> campaigns.requireReservation(UUID.randomUUID().toString()));
        try (var lock = campaigns.lock()) { assertTrue(lock.lock().isValid()); }
    }
    @Test void sharedValidationRejectsCampaignOwnedArgumentOverrides() {
        var c = request.config(); var altered = new AutoLlmConfig(new AutoLlmConfig.Project(c.project().repository(), "HEAD", c.project().buildCommand(), c.project().artifact(), List.of("--solver.n-workers=99")), c.workspace(), null, null, null, c.sotaFile(), null, null, null, null, null, null, null, c.execution());
        assertThrows(IllegalArgumentException.class, () -> service.create(new ScreeningJobService.Request(altered, request.settings(), request.inputPath(), null)));
    }
    @Test void artifactDownloadRejectsTraversalAndSymlinkEscape() throws Exception {
        var job = service.create(request); Path root = campaigns.root().resolve("screenings").resolve(job.id);
        Path outside = temp.resolve("outside.txt"); Files.writeString(outside, "outside"); Files.createSymbolicLink(root.resolve("link"), temp);
        assertThrows(IllegalArgumentException.class, () -> service.artifact(job.id, "../../outside.txt"));
        assertThrows(IllegalArgumentException.class, () -> service.artifact(job.id, "link/outside.txt"));
    }
}
