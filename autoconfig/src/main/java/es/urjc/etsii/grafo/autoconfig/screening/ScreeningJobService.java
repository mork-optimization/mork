package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.AutoLlmExecutionService;
import es.urjc.etsii.grafo.config.*;
import jakarta.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/** Preparation jobs share campaign ownership, transport, immutable evidence and approval checks. */
@Service
@ConditionalOnProperty("autollm")
public class ScreeningJobService {
    public record Request(AutoLlmConfig config, InstanceScreeningConfig settings, String inputPath, Double trainingFraction) {}
    public static class Job {
        public int version = 2;
        public String id, createdAt, status = "DRAFT", mirror, reviewedHash;
        public Request request;
        public Map<String,Object> activity;
        public Object progress;
        public AutoLlmFailureUtil.Failure problem;
    }
    private final AutoLlmCampaignStore campaigns;
    private final AutoLlmProcessRunner processes;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("screening-job").factory());
    private volatile String active;
    private volatile boolean cancelled;
    private Future<?> task;
    public ScreeningJobService(AutoLlmCampaignStore campaigns, AutoLlmProcessRunner processes) { this.campaigns = campaigns; this.processes = processes; }
    public String active() { return active; }
    private Path directory(String id) {
        if (!id.matches("[a-f0-9-]{36}")) throw new IllegalArgumentException("Invalid screening id");
        return campaigns.root().resolve("screenings").resolve(id);
    }
    private synchronized void save(Job job) {
        AutoLlmFileUtil.write(directory(job.id).resolve("job.json"), job);
        AutoLlmFileUtil.write(campaigns.root().resolve("screening-session.json"), Map.of("screeningId", job.id, "status", job.status, "directory", directory(job.id).toString()));
    }
    public Job load(String id) {
        Path file = directory(id).resolve("job.json"); if (!Files.exists(file)) throw new NoSuchElementException("Screening job unavailable");
        Job job = AutoLlmFileUtil.read(file, Job.class);
        if (job.version != 2) throw new IllegalStateException("Use a fresh screening workspace");
        if (job.mirror != null && Files.exists(Path.of(job.mirror).resolve("proposal/screening-progress.json"))) job.progress = AutoLlmFileUtil.read(Path.of(job.mirror).resolve("proposal/screening-progress.json"), Object.class);
        return job;
    }
    public List<Job> list() {
        var jobs = new ArrayList<Job>(); Path root = campaigns.root().resolve("screenings"); if (!Files.isDirectory(root)) return jobs;
        try (var entries = Files.newDirectoryStream(root)) { for (Path entry : entries) if (Files.isRegularFile(entry.resolve("job.json"))) jobs.add(load(entry.getFileName().toString())); }
        catch (IOException e) { throw new UncheckedIOException(e); }
        jobs.sort(Comparator.comparing((Job job) -> job.createdAt).reversed()); return jobs;
    }
    @PostConstruct public void recover() {
        try (var lock = campaigns.lock()) {
            for (var job : list()) if (List.of("RUNNING", "WAITING_FOR_REMOTE", "CANCELLATION_PENDING").contains(job.status)) { job.status = "INTERRUPTED"; save(job); }
        } catch (IllegalStateException ignored) { /* Another owned controller is active. */ }
    }
    public synchronized Job create(Request request) {
        if (request == null || request.config() == null || request.settings() == null) throw new IllegalArgumentException("Screening configuration and settings are required");
        var errors = AutoLlmExecutionService.validate(request.config()); errors.addAll(AutoLlmConfigUtil.projectErrors(request.config()));
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        var config = request.config(); Path repo = Path.of(config.project().repository()).toAbsolutePath().normalize();
        if (!Path.of(config.workspace()).toAbsolutePath().normalize().equals(campaigns.root())) throw new IllegalArgumentException("Workspace is fixed by backend configuration");
        if (request.inputPath() == null || !Files.exists(repo.resolve(request.inputPath()))) throw new IllegalArgumentException("Original input pool does not exist");
        String sota = request.settings().sotaFile() == null ? config.sotaFile() : request.settings().sotaFile();
        if (sota == null || !Files.isRegularFile(repo.resolve(sota))) throw new IllegalArgumentException("Fixed SOTA file does not exist");
        double fraction = request.trainingFraction() == null ? 0.15 : request.trainingFraction();
        if (!Double.isFinite(fraction) || fraction <= 0 || fraction >= 1) throw new IllegalArgumentException("Training fraction must be between zero and one");
        var job = new Job(); job.id = UUID.randomUUID().toString(); job.createdAt = Instant.now().toString();
        var selected = request.settings();
        var settings = new InstanceScreeningConfig(repo.resolve(sota).toString(), directory(job.id).resolve("evidence").toString(), selected.thresholdMillis(), selected.candidateMultiplier(), selected.horizonMillis(), selected.confirmationRuns(), selected.fastFraction(), selected.python(), selected.warmupRepetitions(), selected.warmupMillis(), null);
        job.request = new Request(config, settings, repo.resolve(request.inputPath()).toString(), fraction); save(job); return job;
    }
    public synchronized Job resume(String id) { return start(id, null); }
    public synchronized Job approve(String id, String reviewedHash) {
        var job = load(id);
        if (!job.status.equals("AWAITING_APPROVAL")) throw new IllegalStateException("Review a completed proposal before approval");
        if (reviewedHash == null || !reviewedHash.equals(AutoLlmFileUtil.hash(Path.of(job.mirror).resolve("proposal/proposal.json")))) throw new IllegalArgumentException("Approval requires the exact reviewed proposal SHA-256");
        return start(id, reviewedHash);
    }
    private Job start(String id, String approval) {
        if (active != null) throw new IllegalStateException("A screening job is active");
        var job = load(id);
        if (job.status.equals("APPROVED") || approval == null && job.status.equals("AWAITING_APPROVAL")) throw new IllegalStateException("Selection is already complete; review or use its published indexes");
        campaigns.requireReservation(id); var lock = campaigns.lock();
        try {
            campaigns.reserve(id);
            active = id; cancelled = false; if (approval != null) job.reviewedHash = approval;
            job.status = "RUNNING"; job.problem = null; save(job);
            task = executor.submit(() -> {
                try {
                    var inputs = new InstanceConfiguration(); inputs.setForSelection(job.request.inputPath()); inputs.setPreliminarPercentage(job.request.trainingFraction());
                    var solver = new SolverConfig(); solver.setSeed(job.request.config().seed()); solver.setRandomType(es.urjc.etsii.grafo.util.random.RandomType.DEFAULT);
                    var service = new RemoteScreeningService(processes, state -> {
                        job.activity = state; if (state.containsKey("mirror")) job.mirror = (String)state.get("mirror");
                        if (state.get("remote") instanceof Map<?,?> remote) job.status = Set.of("WAITING_FOR_REMOTE", "CANCELLATION_PENDING").contains(remote.get("status")) ? (String)remote.get("status") : "RUNNING";
                        save(job);
                    }, () -> cancelled || Thread.currentThread().isInterrupted());
                    service.run(job.request.config(), job.request.settings(), inputs, solver, job.reviewedHash != null, job.reviewedHash, List.of("api-screening", id));
                    job.status = job.reviewedHash == null ? "AWAITING_APPROVAL" : "APPROVED";
                } catch (CancellationException e) { job.status = cancelled ? "CANCELLED" : "INTERRUPTED"; }
                catch (IOException | RuntimeException e) {
                    job.status = "NEEDS_ATTENTION"; job.problem = AutoLlmFailureUtil.describe(e instanceof RuntimeException runtime ? runtime : new UncheckedIOException((IOException)e), directory(id));
                } finally {
                    try { save(job); }
                    finally { try { campaigns.releaseReservation(id, directory(id)); } finally { lock.close(); synchronized (this) { active = null; } } }
                }
            });
            return job;
        } catch (RuntimeException e) { active = null; try { campaigns.releaseReservation(id, directory(id)); } finally { lock.close(); } throw e; }
    }
    public synchronized Job cancel(String id) {
        if (active == null && AutoLlmCampaignStore.hasUncollectedJobs(directory(id))) {
            var job = load(id); campaigns.requireReservation(id); var lock = campaigns.lock(); campaigns.reserve(id); active = id; cancelled = true;
            job.status = "CANCELLATION_PENDING"; save(job);
            task = executor.submit(() -> {
                try { new AutoLlmExecutionService(processes).cancelOutstanding(job.request.config(), directory(id), state -> { job.activity = Map.of("phase", "CANCELLATION", "remote", state); save(job); }); job.status = "CANCELLED"; job.problem = null; }
                catch (RuntimeException e) { job.status = "NEEDS_ATTENTION"; job.problem = AutoLlmFailureUtil.describe(e, directory(id)); }
                finally { save(job); try { campaigns.releaseReservation(id, directory(id)); } finally { lock.close(); synchronized (this) { active = null; } } }
            });
            return job;
        }
        if (active == null && List.of("DRAFT", "NEEDS_ATTENTION", "INTERRUPTED").contains(load(id).status)) { var job = load(id); job.status = "CANCELLED"; save(job); return job; }
        if (!id.equals(active)) throw new IllegalStateException("Screening is not active");
        cancelled = true; return load(id);
    }
    public Path artifact(String id, String relative) {
        Path root = directory(id), path = AutoLlmFileUtil.inside(root, relative);
        if (relative.contains(".git/") || relative.contains(".transport/") || relative.contains(".transfer/") || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw new NoSuchElementException("Artifact unavailable");
        try { if (!path.toRealPath().startsWith(root.toRealPath())) throw new IllegalArgumentException("Artifact escapes screening directory"); }
        catch (IOException e) { throw new UncheckedIOException(e); }
        return path;
    }
    public Object review(String id) {
        var job = load(id); if (job.mirror == null) throw new IllegalStateException("Proposal has not completed");
        Path proposal = Path.of(job.mirror).resolve("proposal"), file = proposal.resolve("proposal.json");
        if (!Files.isRegularFile(file)) throw new IllegalStateException("Proposal has not completed");
        try { return Map.of("proposal", AutoLlmFileUtil.read(file, ScreeningData.Proposal.class), "proposalSha256", AutoLlmFileUtil.hash(file), "report", Files.readString(proposal.resolve("report.md")),
                "proposalDirectory", proposal.toString(), "artifactPrefix", directory(id).relativize(proposal).toString()); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    @PreDestroy public void close() { if (task != null) task.cancel(true); executor.shutdownNow(); }
}
