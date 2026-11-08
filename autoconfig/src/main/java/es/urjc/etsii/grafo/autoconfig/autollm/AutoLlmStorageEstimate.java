package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.SshTransport;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.InstancePathUtil;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/** Advisory additional peak usage. Logical-byte ranges are estimates, never reservations or hard bounds. */
public final class AutoLlmStorageEstimate {
    private static final long MIB = 1024 * 1024, GIB = 1024 * MIB;
    private AutoLlmStorageEstimate() {}
    public record Range(long lowBytes, long highBytes) {}
    public record Capacity(Long availableBytes, String observedAt, String source, Long lastKnownAvailableBytes, String warning) {}
    public record Location(String label, String path, Range additionalPeak, Capacity capacity, String assessment,
                           Map<String,Range> breakdown) {}
    public record Estimate(String observedAt, int remainingTrials, Map<String,Long> sizingBasis, Location controller,
                           Map<String,Location> workers, List<String> assumptions, String approvalLine) {}
    @FunctionalInterface interface CapacityReader { Capacity read(AutoLlmConfig config, Path targetRoot, AutoLlmCampaign.TargetState target); }

    public static Estimate estimate(AutoLlmCampaign campaign, Path root, AutoLlmConfig proposed) {
        return estimate(campaign, root, proposed, AutoLlmStorageEstimate::remoteCapacity);
    }
    static Estimate estimate(AutoLlmCampaign campaign, Path root, AutoLlmConfig proposed, CapacityReader capacities) {
        String observed = Instant.now().toString();
        var assumptions = new ArrayList<String>();
        assumptions.add("Additional logical bytes from the observed state; existing campaign bytes are already reflected in free space. No APFS/reflink/hard-link savings are assumed.");
        assumptions.add("Active trials are budgeted for a full remaining trial, conservatively allowing overlapping current and replacement copies. Baseline observations and explicit fallback allowances form the sizing basis.");
        assumptions.add("Heuristic planning range, not a hard upper bound. Generated code, logs, repair attempts and dependency downloads can exceed it; free space is not reserved.");
        assumptions.add("Low range assumes successful retirement/compression; high range allows retained checkouts/JARs and uncompressed evidence when verification or cleanup fails. Shared assets are counted once per campaign/worker.");
        assumptions.add("Worker high range allows all remaining trials to land on any selected worker; allocation is dynamic. Concurrent campaigns on the same filesystem compete for the displayed free space.");
        assumptions.add("Rebuild/source verification uses one temporary checkout per local build slot. Closed-file compression and diagnostic preservation require an additional copy before originals can be removed; no compression saving is assumed in the high range.");
        boolean managed = Files.isRegularFile(root.resolve("control/storage-layout.json"));
        if (!managed) assumptions.add("Legacy campaign: no automatic retirement/compression credit; both ends use the conservative retained-copy estimate.");
        var sample = measure(campaign, root, proposed, assumptions);
        int remaining = Math.max(0, proposed.trials());
        for (var trial : campaign.trials) if (trial.status.terminal()) remaining = Math.max(0, remaining - 1);
        var selected = new TreeMap<String,AutoLlmCampaign.TargetState>();
        if (proposed.execution().target() == AutoLlmConfig.Target.SSH) {
            for (var item : campaign.targets.entrySet()) if (campaign.reviewedTargets.contains(item.getKey()) && item.getValue().status != AutoLlmCampaign.TargetStatus.REMOVED) selected.put(item.getKey(), item.getValue());
        }
        int active = Math.min(remaining, Math.max(1, selected.size()));
        int builds = Math.min(remaining, proposed.preparation().localBuilds());
        int queued = Math.min(remaining, active + proposed.preparation().readyQueueSize() + proposed.generation().maxConcurrentSessions());
        long source = sample.get("sourceBytes"), git = sample.get("checkoutGitBytes"), build = sample.get("buildOutputBytes"), jar = sample.get("artifactBytes");
        long checkout = plus(source, git, build), inputs = sample.get("inputContainerBytes"), runner = sample.get("runnerArtifactBytes");
        long comparisons = times(sample.get("testingInstances"), times(proposed.comparison().repetitions(), 2));
        long evaluations = plus(proposed.tuning().evaluations(), comparisons, Math.max(1000, proposed.tuning().evaluations() / 10));
        long repairs = 1L + Math.max(0, proposed.preparation().maxRepairs());
        long eventLow = Math.max(8 * 1024, sample.get("observedBytesPerEvaluation"));
        long eventHigh = Math.max(256 * 1024, times(eventLow, 8));
        boolean timing = false;
        for (String argument : proposed.project().runArguments()) if (argument.replace("-", "").replace("_", "").equalsIgnoreCase("solver.timestats=true")) timing = true;
        if (timing) {
            eventHigh = Math.max(eventHigh, 16 * MIB);
            assumptions.add("Detailed component timing is enabled: timing volume is workload dependent; the high allowance is 16 MiB per evaluation or the larger observed rate and is not a bound.");
        }
        long evidenceLow = plus(times(evaluations, eventLow) / 4, times(repairs, 16 * MIB));
        long evidenceHigh = plus(times(evaluations, eventHigh), times(repairs, 256 * MIB));
        var local = new LinkedHashMap<String,Range>();
        local.put("retainedEvidenceAndSourcePatches", range(times(remaining, plus(evidenceLow, Math.max(MIB, source / 10))), times(remaining, plus(evidenceHigh, times(source, 3)))));
        local.put("checkoutsAndBuilds", range(times(Math.max(active, builds), checkout), times(Math.max(queued, remaining), checkout)));
        local.put("retainedExecutableCopies", range(times(active, jar), times(remaining, times(jar, 2))));
        local.put("sharedAssetsAndTransferStaging", range(times(active, jar), plus(runner, times(remaining, jar), times(active, plus(inputs, jar, evidenceHigh)))));
        local.put("rebuildAndCompressionScratch", range(times(builds, checkout), plus(times(builds, plus(checkout, jar)), times(Math.max(1, active), evidenceHigh))));
        local.put("dependencyCacheGrowth", remaining == 0 ? range(0, 0) : range(0, Math.max(2 * GIB, times(build, 2))));
        if (remaining == 0) local.replaceAll((_, _) -> range(0, 0));
        else if (!managed) local.replaceAll((_, value) -> range(value.highBytes(), value.highBytes()));
        Capacity localCapacity;
        try { localCapacity = new Capacity(Files.getFileStore(existing(root)).getUsableSpace(), observed, "LIVE_FILESTORE", null, null); }
        catch (IOException e) { localCapacity = new Capacity(null, observed, "UNAVAILABLE", null, e.toString()); }
        Location controller = location("controller", root.toAbsolutePath().toString(), local, localCapacity);
        var workers = new TreeMap<String,Location>();
        // Each probe is bounded to 15 seconds and uses an already staged runner, without remote bootstrap.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var pending = new TreeMap<String,Future<Capacity>>();
            for (var item : selected.entrySet()) {
                var target = item.getValue();
                pending.put(item.getKey(), executor.submit(() -> capacities.read(proposed.forServer(target.definition), root.resolve(target.directory), target)));
            }
            for (var item : selected.entrySet()) {
                var target = item.getValue(); Capacity capacity;
                try { capacity = pending.get(item.getKey()).get(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Storage observation interrupted"); }
                catch (ExecutionException e) { capacity = unavailable(root.resolve(target.directory), target, e.getCause().toString()); }
                var parts = new LinkedHashMap<String,Range>();
                long newBaseline = target.baselineArtifactSha256 == null ? plus(inputs, git, runner) : 0;
                parts.put("sharedInputsBundlesAndExecutables", range(plus(newBaseline, jar), plus(newBaseline, times(remaining, jar))));
                parts.put("checkoutsAndBuilds", range(checkout, times(Math.max(1, remaining), checkout)));
                parts.put("evidenceAndUnacknowledgedResults", range(evidenceLow, times(remaining, times(evidenceHigh, 2))));
                parts.put("transferAndCompressionScratch", range(plus(inputs, jar), plus(inputs, checkout, evidenceHigh)));
                parts.put("dependencyCacheGrowth", range(0, Math.max(2 * GIB, times(build, 2))));
                if (remaining == 0) parts.replaceAll((_, _) -> range(0, 0));
                else if (!managed) parts.replaceAll((_, value) -> range(value.highBytes(), value.highBytes()));
                workers.put(item.getKey(), location(target.definition.host(), target.definition.workspace(), parts, capacity));
            }
        }
        return new Estimate(observed, remaining, sample, controller, workers, assumptions, approvalLine(controller, workers));
    }

    private static Map<String,Long> measure(AutoLlmCampaign campaign, Path root, AutoLlmConfig config, List<String> assumptions) {
        long[] sizes = new long[4]; long events = 0, rate = 0;
        var targets = new ArrayList<AutoLlmCampaign.TargetState>(); targets.add(campaign.localPreparation); targets.addAll(campaign.targets.values());
        for (var target : targets) {
            if (target.directory == null) continue;
            Path targetRoot = root.resolve(target.directory), baseline = targetRoot.resolve("baseline/source");
            if (Files.isDirectory(baseline, LinkOption.NOFOLLOW_LINKS)) try {
                long[] observed = new long[4];
                Files.walkFileTree(baseline, new SimpleFileVisitor<>() {
                    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (!attrs.isRegularFile()) return FileVisitResult.CONTINUE;
                        int category = 0;
                        for (Path part : baseline.relativize(file)) { if (part.toString().equals(".git")) { category = 1; break; } if (part.toString().equals("target")) category = 2; }
                        observed[category] = plus(observed[category], attrs.size()); return FileVisitResult.CONTINUE;
                    }
                });
                Path artifact = baseline.resolve(config.project().artifact()).normalize();
                if (artifact.startsWith(baseline) && Files.isRegularFile(artifact, LinkOption.NOFOLLOW_LINKS)) observed[3] = Files.size(artifact);
                for (int i = 0; i < sizes.length; i++) sizes[i] = Math.max(sizes[i], observed[i]);
            } catch (IOException e) { assumptions.add("Incomplete baseline size observation: " + e.getMessage()); }
            List<Path> journals;
            try { journals = AutoLlmFileUtil.ownedFiles(targetRoot.resolve("baseline-preflight"), "events.jsonl"); }
            catch (RuntimeException e) { assumptions.add("Baseline journal discovery unavailable: " + e.getMessage()); journals = List.of(); }
            for (Path journal : journals) try (var input = Files.newInputStream(journal)) {
                byte[] prefix = input.readNBytes(1024 * 1024); String text = new String(prefix, StandardCharsets.UTF_8);
                long count = 0;
                int last = text.lastIndexOf('\n');
                if (last >= 0) for (String line : text.substring(0, last).split("\n")) {
                    try { if ("evaluation-result".equals(AutoLlmFileUtil.JSON.readTree(line).path("kind").asString())) count++; }
                    catch (RuntimeException ignored) { /* A partial or interrupted journal is not a reliable sample. */ }
                }
                if (count > 0) { events += count; rate = Math.max(rate, prefix.length / count); }
            } catch (IOException e) { assumptions.add("Unable to sample baseline journal: " + journal.getFileName()); }
        }
        if (sizes[0] == 0) { sizes[0] = 256 * MIB; assumptions.add("Source size unavailable: using a 256 MiB checkout-source allowance."); }
        if (sizes[1] == 0) { sizes[1] = 512 * MIB; assumptions.add("Git checkout size unavailable: using a 512 MiB allowance."); }
        if (sizes[3] == 0) { sizes[3] = 64 * MIB; assumptions.add("Executable size unavailable: using a 64 MiB allowance."); }
        sizes[2] = Math.max(sizes[2], times(sizes[3], 2));
        if (events == 0) assumptions.add("No completed baseline evaluation sample: evidence uses 8–256 KiB per evaluation plus 16–256 MiB of logs/transcripts per generation or repair attempt.");
        long inputBytes = 0, testing = 0;
        var containers = new HashSet<Path>();
        for (String index : List.of(config.trainingPath() == null ? "" : config.trainingPath(), config.comparisonPath() == null ? "" : config.comparisonPath())) {
            if (index.isBlank()) continue;
            try {
                var inputs = InstancePathUtil.list(index); if (index.equals(config.comparisonPath())) testing = inputs.size();
                for (String input : inputs) {
                    Path file = Path.of(IOUtil.containerPath(input)).toAbsolutePath().normalize();
                    if (containers.add(file)) inputBytes = plus(inputBytes, Files.size(file));
                }
            } catch (IOException | RuntimeException e) { assumptions.add("Input size observation incomplete: " + e.getMessage()); }
        }
        long runner = 0;
        if (config.execution().target() == AutoLlmConfig.Target.SSH) try { runner = Files.size(SshTransport.runnerArtifact(config)); }
        catch (IOException | RuntimeException e) { runner = 64 * MIB; assumptions.add("Runner executable size unavailable: using a 64 MiB shared-asset allowance."); }
        return Map.of("sourceBytes", sizes[0], "checkoutGitBytes", sizes[1], "buildOutputBytes", sizes[2], "artifactBytes", sizes[3], "runnerArtifactBytes", runner,
                "inputContainerBytes", inputBytes, "testingInstances", testing, "sampledEvaluations", events, "observedBytesPerEvaluation", rate);
    }
    private static Capacity remoteCapacity(AutoLlmConfig config, Path root, AutoLlmCampaign.TargetState target) {
        try {
            var result = AutoLlmFileUtil.JSON.readTree(new SshTransport(config, root.resolve(".transport")).storage());
            var usable = result.path("usableDiskBytes");
            if (!usable.isIntegralNumber() || usable.asLong() < 0) throw new IllegalStateException("Runner did not provide usable disk bytes");
            return new Capacity(usable.asLong(), result.path("observedAt").asString(Instant.now().toString()), "LIVE_REMOTE_FILESTORE", null, null);
        } catch (RuntimeException e) { return unavailable(root, target, e.getMessage()); }
    }
    private static Capacity unavailable(Path root, AutoLlmCampaign.TargetState target, String reason) {
        Long previous = target.host != null && target.host.usableDiskBytes() >= 0 ? target.host.usableDiskBytes() : null;
        String observed = null;
        try { if (Files.isRegularFile(root.resolve("execution-target.json"))) observed = Files.getLastModifiedTime(root.resolve("execution-target.json")).toInstant().toString(); }
        catch (IOException ignored) { /* Keep an unknown timestamp unknown. */ }
        return new Capacity(null, observed, "UNAVAILABLE", previous, reason);
    }
    private static Path existing(Path path) throws IOException {
        Path current = path.toAbsolutePath();
        while (current != null && !Files.exists(current)) current = current.getParent();
        if (current == null) throw new IOException("No existing filesystem ancestor");
        return current;
    }
    private static Location location(String label, String path, Map<String,Range> breakdown, Capacity capacity) {
        long low = 0, high = 0;
        for (var value : breakdown.values()) { low = plus(low, value.lowBytes()); high = plus(high, value.highBytes()); }
        String assessment = capacity.availableBytes() == null ? "UNKNOWN" : plus(high, high / 5) > capacity.availableBytes() ? "MAY_NOT_FIT" : "WITHIN_ESTIMATE";
        return new Location(label, path, range(low, high), capacity, assessment, breakdown);
    }
    static String approvalLine(Location controller, Map<String,Location> workers) {
        var text = new StringBuilder("Storage (additional peak estimate): ");
        var all = new LinkedHashMap<String,Location>(); all.put("controller", controller); all.putAll(workers);
        var risk = new ArrayList<String>(); var unknown = new ArrayList<String>();
        for (var entry : all.entrySet()) {
            if (text.charAt(text.length() - 1) != ' ') text.append("; ");
            var location = entry.getValue();
            text.append(entry.getKey()).append(' ').append(gib(location.additionalPeak().lowBytes())).append('–').append(gib(location.additionalPeak().highBytes())).append(" GiB / ");
            text.append(location.capacity().availableBytes() == null ? "unknown free" : gib(location.capacity().availableBytes()) + " GiB free");
            if (location.assessment().equals("MAY_NOT_FIT")) risk.add(entry.getKey());
            if (location.assessment().equals("UNKNOWN")) unknown.add(entry.getKey());
        }
        if (!risk.isEmpty()) text.append(". WARNING: may not fit with 20% headroom on ").append(String.join(", ", risk));
        if (!unknown.isEmpty()) text.append(". WARNING: capacity unavailable on ").append(String.join(", ", unknown));
        return text.append(". Heuristic range; actual usage may exceed it.").toString();
    }
    private static String gib(long bytes) { return String.format(Locale.ROOT, "%.2f", bytes / (double) GIB); }
    private static Range range(long low, long high) { return new Range(low, Math.max(low, high)); }
    private static long plus(long... values) {
        long sum = 0; for (long value : values) { if (value > Long.MAX_VALUE - sum) return Long.MAX_VALUE; sum += Math.max(0, value); } return sum;
    }
    private static long times(long value, long factor) {
        if (value <= 0 || factor <= 0) return 0;
        return value > Long.MAX_VALUE / factor ? Long.MAX_VALUE : value * factor;
    }
}
