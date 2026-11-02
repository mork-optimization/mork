package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Reviewed, resumable deletion. Receipts live beside the workspace, never inside a deletion unit. */
public final class AutoLlmCleanup {
    public record Selection(String kind, String id, String export, boolean shutdown, boolean retainReceipt) {}
    public record OwnedProcess(String evidence, WorkerRegistry.ProcessIdentity identity) {}
    public record Remote(ExecutionOwner owner, OwnershipManifest.Resource resource, List<WorkloadProtocol.Job> jobs) {}
    public record Plan(String digest, String workspace, Selection selection, Set<String> references,
                       List<OwnershipManifest.Resource> delete, Map<String,String> retain, List<Remote> remote,
                       List<String> blockers, List<OwnedProcess> processes, WorkerRegistry.ProcessIdentity controller) {
        public Plan { references = Collections.unmodifiableSortedSet(new TreeSet<>(references)); }
    }
    public record Finalizer(String runner, String sha256, WorkerRegistry.ProcessIdentity process) {}
    public record Receipt(String digest, String status, Set<String> completed, Map<String,String> pending) {}
    private final AutoLlmCampaignStore store;
    private final Path receipts;
    public AutoLlmCleanup(AutoLlmCampaignStore store) {
        this.store = store;
        receipts = store.root().getParent().resolve(".mork-cleanup/" + AutoLlmFileUtil.canonicalHash(store.root().toString()));
    }
    public Plan plan(Selection selection) {
        if (!Set.of("campaign", "screening", "probe", "workspace").contains(selection.kind())) throw new IllegalArgumentException("Select campaign, screening, probe or workspace");
        Path selected = switch (selection.kind()) {
            case "workspace" -> store.root();
            case "campaign" -> store.directory(selection.id());
            case "screening" -> store.root().resolve("screenings").resolve(validId(selection.id()));
            default -> store.root().resolve("target-probes").resolve(validId(selection.id()));
        };
        var resources = OwnershipManifest.resources(); var refs = new LinkedHashSet<String>(); var blockers = new ArrayList<String>();
        refs.add(selected.toString());
        for (var resource : resources) if (Path.of(resource.path()).startsWith(selected)) refs.add(resource.path());
        // Include exclusively associated preparation/screening roots, retaining shared dependencies.
        boolean changed;
        do {
            changed = false;
            for (var resource : resources) if (!resource.kind().equals("workspace") && !resource.kind().equals("transcript") && !Collections.disjoint(resource.references(), refs)) {
                var external = new HashSet<>(resource.references()); external.remove(resource.path()); external.removeAll(refs);
                if (external.isEmpty()) changed |= refs.add(resource.path());
            }
        } while (changed);
        var delete = new ArrayList<OwnershipManifest.Resource>(); var retain = new LinkedHashMap<String,String>();
        for (var resource : resources) if (!Collections.disjoint(resource.references(), refs)) {
            if (refs.containsAll(resource.references())) { OwnershipManifest.verify(resource); delete.add(resource); }
            else retain.put(resource.path(), "Shared references: " + resource.references());
        }
        for (var resource : new ArrayList<>(delete)) {
            for (String kept : new ArrayList<>(retain.keySet())) if (Path.of(kept).startsWith(Path.of(resource.path()))) {
                delete.remove(resource); retain.put(resource.path(), "Contains shared resource " + kept); break;
            }
        }
        if (selection.kind().equals("workspace") && Files.isDirectory(selected)) {
            try (var entries = Files.newDirectoryStream(selected)) {
                for (Path entry : entries) {
                    String name = entry.getFileName().toString();
                    if (name.equals("screenings") || name.equals("target-probes")) {
                        try (var operations = Files.newDirectoryStream(entry)) {
                            for (Path operation : operations) {
                                boolean registered = false;
                                for (var resource : resources) if (Path.of(resource.path()).equals(operation)) registered = true;
                                if (!registered) blockers.add("Unregistered workspace operation: " + operation);
                            }
                        }
                        continue;
                    }
                    if (Set.of(".mork-owned-v7.json", "session.json", "backend-launch.json", "backend.log", "campaign-session.json", "screening-session.json", "operation-reservation.json", "execution.lock", "cleanup-requested.json", "screenings", "target-probes").contains(name)) continue;
                    boolean registered = false;
                    for (var resource : resources) if (!resource.kind().equals("workspace") && Path.of(resource.path()).equals(entry)) registered = true;
                    if (!registered) blockers.add("Unregistered workspace resource: " + entry);
                }
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        if (delete.isEmpty()) blockers.add("No version-7 ownership manifest for selected scope");
        var remote = new ArrayList<Remote>(); var seen = new HashSet<String>(); var remoteUnits = new HashSet<String>();
        for (var resource : delete) {
            if (resource.sessionId() != null || resource.kind().equals("workspace")) continue;
            for (Path file : AutoLlmFileUtil.ownedFiles(Path.of(resource.path()), "execution-owner.json")) {
                var owner = AutoLlmFileUtil.read(file, ExecutionOwner.class);
                if (!owner.remote() || !seen.add(owner.localRoot())) continue;
                var server = owner.config().execution().ssh().onlyServer();
                retain.put(server.host() + ":" + server.workspace().replaceAll("/+$", "") + "/.runner", "Shared content-addressed runner installation; outside operation ownership");
                var jobs = new ArrayList<WorkloadProtocol.Job>();
                for (Path job : AutoLlmFileUtil.ownedFiles(Path.of(owner.localRoot()), "remote-job.json")) jobs.add(AutoLlmFileUtil.read(job, WorkloadProtocol.Job.class));
                try {
                    if (owner.lease().workerId() == null) {
                        var status = AutoLlmFileUtil.JSON.readValue(owner.call("worker-status", Map.of()), WorkerRegistry.Status.class);
                        var lease = owner.lease();
                        owner = new ExecutionOwner(owner.version(), owner.operation(), owner.assignment(), owner.localRoot(), owner.executionRoot(), owner.config(),
                                new WorkerRegistry.Lease(status.workerId(), lease.controller(), lease.operation(), lease.assignment(), lease.token(), lease.generation()));
                    }
                    var units = AutoLlmFileUtil.JSON.readValue(owner.call("ownership-plan", Map.of()), new tools.jackson.core.type.TypeReference<List<OwnershipManifest.Resource>>() {});
                    for (var unit : units) if (remoteUnits.add(owner.lease().workerId() + ":" + unit.path())) remote.add(new Remote(owner, unit, unit.path().equals(owner.executionRoot()) ? jobs : List.of()));
                    remote.add(new Remote(owner, null, units.isEmpty() ? jobs : List.of()));
                } catch (RuntimeException e) {
                    Path cached = Path.of(owner.localRoot()).resolve("remote-assets.json");
                    if (!Files.isRegularFile(cached)) blockers.add("Missing remote asset ownership: " + owner.localRoot());
                    else {
                        var units = AutoLlmFileUtil.read(cached, new tools.jackson.core.type.TypeReference<List<OwnershipManifest.Resource>>() {});
                        for (var unit : units) if (remoteUnits.add(owner.lease().workerId() + ":" + unit.path())) remote.add(new Remote(owner, unit, unit.path().equals(owner.executionRoot()) ? jobs : List.of()));
                        remote.add(new Remote(owner, null, List.of()));
                    }
                    blockers.add("Unreachable worker ownership: " + owner.lease().workerId() + ": " + e.getMessage());
                }
                refs.add(owner.reference());
            }
        }
        remote.sort(Comparator.comparingInt((Remote unit) -> unit.resource() == null ? -1 : Path.of(unit.resource().path()).getNameCount()).reversed());
        for (var unit : new ArrayList<>(remote)) if (unit.resource() != null && !refs.containsAll(unit.resource().references())) {
            remote.remove(unit); retain.put(unit.owner().lease().workerId() + ":" + unit.resource().path(), "Shared worker references: " + unit.resource().references());
        }
        if (selection.export() != null) {
            Path export = AutoLlmNormalization.localPath(Path.of(selection.export()));
            for (var resource : delete) if (export.startsWith(Path.of(resource.path()))) throw new IllegalArgumentException("Diagnostic export must be outside every deletion boundary");
        }
        if (selection.shutdown() && !selection.kind().equals("workspace")) blockers.add("Controller shutdown requires complete workspace scope");
        // Unknown native threads cannot be silently omitted from complete deletion.
        for (var resource : delete) if (resource.kind().equals("campaign")) {
            for (Path thread : AutoLlmFileUtil.ownedFiles(Path.of(resource.path()), "thread-id.txt")) {
                try {
                    String id = Files.readString(thread).strip(); boolean found = false;
                    for (var nativeResource : resources) if (id.equals(nativeResource.sessionId())) found = true;
                    if (!found) blockers.add("Native transcript ownership unresolved for session " + id);
                } catch (IOException e) { throw new UncheckedIOException(e); }
            }
        }
        var processes = new ArrayList<OwnedProcess>(); var processFiles = new HashSet<Path>();
        for (var resource : delete) if (resource.sessionId() == null)
            for (Path evidence : AutoLlmFileUtil.ownedFiles(Path.of(resource.path()), "owner.json")) if (processFiles.add(evidence)) {
                var saved = AutoLlmFileUtil.read(evidence, Map.class);
                if (!"SSH".equals(saved.get("executionTarget")) && saved.get("pid") instanceof Number pid && saved.get("start") instanceof String start)
                    processes.add(new OwnedProcess(evidence.toString(), new WorkerRegistry.ProcessIdentity(pid.longValue(), start)));
            }
        var controller = WorkerRegistry.ProcessIdentity.of(ProcessHandle.current());
        var unsigned = new Plan(null, store.root().toString(), selection, refs, delete, retain, remote, blockers, processes, controller);
        var plan = new Plan(AutoLlmFileUtil.canonicalHash(unsigned), unsigned.workspace(), selection, refs, delete, retain, remote, blockers, processes, controller);
        AutoLlmFileUtil.write(receipts.resolve(plan.digest() + ".plan.json"), plan); return plan;
    }
    private static String validId(String id) { if (id == null || !id.matches("[a-f0-9-]{36}")) throw new IllegalArgumentException("Invalid operation ID"); return id; }
    private Path file(String digest, String suffix) {
        if (digest == null || !digest.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Supply the reviewed cleanup digest");
        return receipts.resolve(digest + suffix);
    }
    public void requestCancellation(String digest) {
        var plan = reviewed(digest);
        var scopeProblems = scopeProblems(plan, status(digest).completed());
        if (!scopeProblems.isEmpty()) throw new IllegalStateException("Cleanup scope changed before cancellation: " + scopeProblems);
        for (var resource : plan.delete()) if (resource.sessionId() == null && OwnershipManifest.deletionPhase(resource).equals("OWNED")
                && !status(digest).completed().contains(resource.path())) {
            OwnershipManifest.verify(resource);
            AutoLlmFileUtil.write(Path.of(resource.path()).resolve("cleanup-requested.json"), Map.of("digest", digest));
        }
    }
    public static boolean requested(Path root) {
        for (Path p = root; p != null; p = p.getParent()) if (Files.isRegularFile(p.resolve("cleanup-requested.json"))) return true;
        return false;
    }
    public Selection selected(String digest) { return reviewed(digest).selection(); }
    public Receipt status(String digest) {
        Path file = file(digest, ".receipt.json");
        return Files.isRegularFile(file) ? AutoLlmFileUtil.read(file, Receipt.class) : new Receipt(digest, Files.isRegularFile(file(digest, ".plan.json")) ? "PLANNED" : "UNKNOWN", Set.of(), Map.of());
    }
    private Receipt save(String digest, Set<String> completed, Map<String,String> pending) {
        var receipt = new Receipt(digest, pending.isEmpty() ? "SUCCEEDED" : "PENDING", new LinkedHashSet<>(completed), new LinkedHashMap<>(pending));
        AutoLlmFileUtil.write(file(digest, ".receipt.json"), receipt); return receipt;
    }
    private Plan reviewed(String digest) {
        var plan = AutoLlmFileUtil.read(file(digest, ".plan.json"), Plan.class);
        if (!plan.workspace().equals(store.root().toString())) throw new IllegalArgumentException("Cleanup belongs to another workspace");
        var signed = new Plan(null, plan.workspace(), plan.selection(), plan.references(), plan.delete(), plan.retain(), plan.remote(), plan.blockers(), plan.processes(), plan.controller());
        if (!AutoLlmFileUtil.canonicalHash(signed).equals(digest)) throw new IllegalStateException("Cleanup plan was changed");
        return plan;
    }
    private Map<String,String> scopeProblems(Plan plan, Set<String> completed) {
        var pending = new LinkedHashMap<String,String>();
        for (var resource : OwnershipManifest.resources()) {
            boolean insideBoundary = false;
            for (var boundary : plan.delete()) if (boundary.sessionId() == null && Path.of(resource.path()).startsWith(Path.of(boundary.path()))) insideBoundary = true;
            if (!insideBoundary && Collections.disjoint(resource.references(), plan.references())) continue;
            if (plan.retain().containsKey(resource.path())) continue;
            boolean remoteResource = false;
            for (var remote : plan.remote()) if (remote.resource() != null && remote.resource().equals(resource)) remoteResource = true;
            for (String kept : plan.retain().keySet()) if (kept.endsWith(":" + resource.path())) remoteResource = true;
            if (remoteResource) continue;
            if (!plan.delete().contains(resource)) pending.put("scope", "Ownership or references changed; create a new cleanup plan");
        }
        for (var resource : plan.delete()) if (!completed.contains(resource.path()) && OwnershipManifest.deletionPhase(resource).equals("OWNED")) {
            try { OwnershipManifest.verify(resource); }
            catch (RuntimeException e) { pending.put(resource.path(), "Ownership boundary changed; create a new plan: " + e.getMessage()); }
        }
        return pending;
    }
    public synchronized Receipt apply(String digest, boolean controllerIdle) {
        if (status(digest).status().equals("SUCCEEDED")) return status(digest);
        var plan = reviewed(digest);
        var previous = status(digest); var completed = new LinkedHashSet<>(previous.completed()); var pending = new LinkedHashMap<String,String>();
        for (String blocker : plan.blockers()) if (!blocker.startsWith("Unreachable worker ownership:")) pending.put(blocker, blocker);
        pending.putAll(scopeProblems(plan, completed));
        if (!controllerIdle) pending.put("controller", "Cancellation requested; wait for scoped coordinator work to stop");
        if (!pending.isEmpty()) return save(digest, completed, pending);
        if (plan.selection().shutdown() && plan.controller().alive()) return save(digest, completed, Map.of("controller", "Waiting for external finalizer"));
        return delete(plan, completed, pending);
    }
    public synchronized Receipt launchFinalizer(String digest, AutoLlmConfig config) {
        var plan = reviewed(digest);
        if (!plan.selection().shutdown() || !plan.selection().kind().equals("workspace") || !plan.blockers().isEmpty()) throw new IllegalStateException("Shutdown requires an unblocked complete-workspace plan");
        if (!plan.controller().equals(WorkerRegistry.ProcessIdentity.of(ProcessHandle.current()))) throw new IllegalStateException("Controller process identity changed");
        var check = apply(digest, true);
        if (!check.pending().equals(Map.of("controller", "Waiting for external finalizer"))) return check;
        Path launch = file(digest, ".finalizer.json");
        if (Files.exists(launch)) {
            var saved = AutoLlmFileUtil.read(launch, Finalizer.class);
            if (saved.process() == null || saved.process().alive()) return status(digest);
        }
        try {
            Path source = SshTransport.runnerArtifact(config);
            Path runner = WorkerRegistry.system().directory().resolve("tools/" + AutoLlmFileUtil.hash(source) + ".jar");
            Files.createDirectories(runner.getParent()); if (!Files.exists(runner)) Files.copy(source, runner);
            var command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString()));
            if (System.getProperty("mork.test.workerRegistry") != null) command.add("-Dmork.test.workerRegistry=" + System.getProperty("mork.test.workerRegistry"));
            command.addAll(List.of("-jar", runner.toString(), "cleanup-finish", store.root().toString(), file(digest, ".plan.json").toString()));
            AutoLlmFileUtil.write(launch, new Finalizer(runner.toString(), AutoLlmFileUtil.hash(runner), null));
            var process = new ProcessBuilder(command).directory(receipts.toFile()).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            process.getOutputStream().close(); AutoLlmFileUtil.write(launch, new Finalizer(runner.toString(), AutoLlmFileUtil.hash(runner), WorkerRegistry.ProcessIdentity.of(process.toHandle())));
            return save(digest, status(digest).completed(), Map.of("controller", "External finalizer started"));
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static void finish(Path planFile) throws Exception {
        var plan = AutoLlmFileUtil.read(planFile, Plan.class);
        var config = new AutoLlmConfig(null, plan.workspace(), null, null, null, null, null, null, null, null, null, null, null, null);
        var cleanup = new AutoLlmCleanup(new AutoLlmCampaignStore(config));
        if (!plan.selection().shutdown() || !plan.selection().kind().equals("workspace")) throw new IllegalStateException("Finalizer scope does not authorize shutdown");
        Path lockPath = WorkerRegistry.system().directory().resolve("cleanup-locks/" + plan.digest() + ".lock");
        Files.createDirectories(lockPath.getParent());
        try (var channel = java.nio.channels.FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.tryLock()) {
        if (lock == null) return;
        var launch = cleanup.file(plan.digest(), ".finalizer.json");
        var saved = AutoLlmFileUtil.read(launch, Finalizer.class);
        if (!saved.sha256().equals(AutoLlmFileUtil.hash(Path.of(saved.runner())))) throw new IllegalStateException("Cleanup helper artifact changed");
        AutoLlmFileUtil.write(launch, new Finalizer(saved.runner(), saved.sha256(), WorkerRegistry.ProcessIdentity.of(ProcessHandle.current())));
        cleanup.reviewed(plan.digest());
        var recordedProcess = ProcessHandle.of(plan.controller().pid());
        if (recordedProcess.isPresent() && recordedProcess.get().isAlive()
                && !plan.controller().equals(WorkerRegistry.ProcessIdentity.of(recordedProcess.get()))) throw new IllegalStateException("Controller PID was reused");
        var check = cleanup.apply(plan.digest(), true);
        if (!check.pending().isEmpty() && !check.pending().equals(Map.of("controller", "Waiting for external finalizer")))
            throw new IllegalStateException("Cleanup scope changed before shutdown: " + check.pending());
        if (check.status().equals("SUCCEEDED")) return;
        var controller = ProcessHandle.of(plan.controller().pid());
        if (controller.isPresent() && controller.get().isAlive()) {
            if (!plan.controller().equals(WorkerRegistry.ProcessIdentity.of(controller.get()))) throw new IllegalStateException("Controller PID was reused");
            controller.get().destroy(); controller.get().onExit().get(30, java.util.concurrent.TimeUnit.SECONDS);
        }
        cleanup.apply(plan.digest(), true);
        }
    }
    private Receipt delete(Plan plan, Set<String> completed, Map<String,String> pending) {
        String digest = plan.digest();
        if (plan.selection().export() != null && !completed.contains("export")) {
            try {
                var files = new TreeMap<String,Path>();
                for (var resource : plan.delete()) if (resource.sessionId() == null && !resource.kind().equals("workspace"))
                    WorkloadArchiveUtil.addTree(files, store.root(), Path.of(resource.path()), false);
                WorkloadArchiveUtil.pack(Path.of(plan.selection().export()).toAbsolutePath(), files); completed.add("export");
                save(digest, completed, Map.of("deletion", "Pending"));
            } catch (Exception e) { pending.put("export", e.toString()); return save(digest, completed, pending); }
        }
        for (var remote : plan.remote()) {
            String key = remote.owner().lease().workerId() + ":" + (remote.resource() == null ? "lease:" + remote.owner().lease().token() : remote.resource().path());
            try {
                if (remote.resource() == null) {
                    if (!completed.contains(key)) {
                        var transport = remote.owner().transport(); transport.bootstrap();
                        var current = AutoLlmFileUtil.JSON.readValue(transport.call("ownership-plan", remote.owner().executionRoot(), null, Map.of()), new tools.jackson.core.type.TypeReference<List<OwnershipManifest.Resource>>() {});
                        for (var resource : current) {
                            boolean reviewed = plan.retain().containsKey(remote.owner().lease().workerId() + ":" + resource.path());
                            for (var unit : plan.remote()) if (resource.equals(unit.resource())) reviewed = true;
                            if (!reviewed) throw new IllegalStateException("Remote ownership changed; create a new cleanup plan");
                        }
                        for (var job : remote.jobs()) {
                            var state = AutoLlmFileUtil.JSON.readValue(transport.call("status", job.root(), job.id(), Map.of()), WorkloadProtocol.State.class);
                            if (state.status().inProgress()) { transport.call("cancel", job.root(), job.id(), Map.of()); throw new IllegalStateException("Cancellation pending: " + job.id()); }
                            transport.call("job-collected", job.root(), job.id(), Map.of());
                        }
                        remote.owner().release(); completed.add(key); save(digest, completed, Map.of("deletion", "Pending"));
                    }
                    continue;
                }
                if (completed.contains(key)) { var acknowledged = remote.owner().transport(); acknowledged.bootstrap(); acknowledged.call("cleanup-ack", remote.resource().path(), null, remote.resource()); continue; }
                var owner = remote.owner(); var transport = owner.transport(); transport.bootstrap();
                var progress = AutoLlmFileUtil.JSON.readTree(transport.call("cleanup-progress", remote.resource().path(), null, remote.resource()));
                if (!progress.path("phase").asString().equals("OWNED")) {
                    transport.call("cleanup-owned", remote.resource().path(), null, new RemoteWorkloadMain.DeleteRequest(remote.resource(), plan.references()));
                    completed.add(key); save(digest, completed, Map.of("deletion", "Pending"));
                    transport.call("cleanup-ack", remote.resource().path(), null, remote.resource()); continue;
                }
                for (var job : remote.jobs()) {
                    if (!ExecutionOwner.of(job).executionRoot().equals(owner.executionRoot())) throw new IllegalStateException("Job owner mismatch");
                    var state = AutoLlmFileUtil.JSON.readValue(transport.call("status", job.root(), job.id(), Map.of()), WorkloadProtocol.State.class);
                    if (state.status().inProgress()) {
                        transport.call("cancel", job.root(), job.id(), Map.of());
                        throw new IllegalStateException("Cancellation pending: " + job.id());
                    }
                    // Explicit discard acknowledges termination without downloading disposable archives.
                    transport.call("job-collected", job.root(), job.id(), Map.of());
                }
                owner.release();
                transport.call("cleanup-owned", remote.resource().path(), null, new RemoteWorkloadMain.DeleteRequest(remote.resource(), plan.references()));
                completed.add(key); save(digest, completed, Map.of("deletion", "Pending"));
                transport.call("cleanup-ack", remote.resource().path(), null, remote.resource());
            } catch (RuntimeException e) { pending.put(key, e.toString()); }
        }
        if (!pending.isEmpty()) return save(digest, completed, pending);
        var reconciledWorkers = new HashSet<String>();
        for (var remote : plan.remote()) if (!Objects.equals(remote.owner().lease().workerId(), WorkerRegistry.system().status().workerId()) && reconciledWorkers.add(remote.owner().lease().workerId())) {
            try { remote.owner().call("ownership-release", plan.references()); }
            catch (RuntimeException e) { pending.put(remote.owner().lease().workerId(), e.toString()); }
        }
        if (!pending.isEmpty()) return save(digest, completed, pending);
        // Delete deepest resources first. The workspace boundary, if selected, is last.
        var local = new ArrayList<>(plan.delete()); local.sort(Comparator.comparingInt((OwnershipManifest.Resource r) -> Path.of(r.path()).getNameCount()).reversed());
        for (var resource : local) {
            try {
                if (completed.contains(resource.path())) { OwnershipManifest.acknowledgeDeletion(resource); continue; }
                if (resource.sessionId() == null) {
                    new AutoLlmProcessRunner().reconcile(Path.of(resource.path()));
                    for (Path ownerFile : AutoLlmFileUtil.ownedFiles(Path.of(resource.path()), "owner.json")) {
                        var identity = AutoLlmFileUtil.JSON.readTree(Files.readString(ownerFile));
                        if ("SSH".equals(identity.path("executionTarget").asString())) continue;
                        var pid = identity.path("pid").asLong(); var start = identity.path("start").asString();
                        var process = ProcessHandle.of(pid);
                        if (process.isPresent() && process.get().isAlive()) throw new IllegalStateException("Live or reused PID blocks cleanup: " + pid + " " + start);
                    }
                    for (Path jobFile : AutoLlmFileUtil.ownedFiles(Path.of(resource.path()), "local-job.json")) {
                        var job = AutoLlmFileUtil.read(jobFile, AutoLlmExecutionService.LocalJob.class);
                        if (!WorkerRegistry.system().released(job.owner().lease())) {
                            AutoLlmProcessRunner.requireTerminated(jobFile.getParent());
                            WorkerRegistry.system().complete(job.owner().lease(), job.owner().localRoot(), job.id());
                        }
                    }
                    for (Path ownerFile : AutoLlmFileUtil.ownedFiles(Path.of(resource.path()), "execution-owner.json")) {
                        var owner = AutoLlmFileUtil.read(ownerFile, ExecutionOwner.class);
                        if (!owner.remote()) owner.release();
                    }
                }
                OwnershipManifest.delete(resource, plan.references()); completed.add(resource.path()); save(digest, completed, Map.of("deletion", "Pending"));
                OwnershipManifest.acknowledgeDeletion(resource);
            } catch (Exception e) { pending.put(resource.path(), e.toString()); break; }
        }
        if (pending.isEmpty()) {
            try {
                for (String name : List.of("operation-reservation.json", "campaign-session.json", "screening-session.json")) {
                    Path file = store.root().resolve(name);
                    if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) continue;
                    var journal = AutoLlmFileUtil.JSON.readTree(Files.readString(file));
                    String id = journal.path(name.equals("operation-reservation.json") ? "id" : name.equals("campaign-session.json") ? "campaignId" : "screeningId").asString();
                    if (id == null || !id.matches("[a-f0-9-]{36}")) continue;
                    if (completed.contains(store.root().resolve(id).toString()) || completed.contains(store.root().resolve("screenings").resolve(id).toString())) Files.delete(file);
                }
                OwnershipManifest.removeReferences(plan.references());
            } catch (IOException e) { pending.put("journals", e.toString()); }
        }
        var result = save(digest, completed, pending);
        if (pending.isEmpty()) {
            try (var files = Files.newDirectoryStream(receipts, "*.plan.json")) {
                for (Path file : files) {
                    var previous = AutoLlmFileUtil.read(file, Plan.class);
                    if (!previous.workspace().equals(plan.workspace()) || !plan.references().containsAll(previous.references())) continue;
                    Files.delete(file);
                    if (!previous.selection().retainReceipt()) Files.deleteIfExists(file(previous.digest(), ".receipt.json"));
                    Files.deleteIfExists(file(previous.digest(), ".finalizer.json"));
                }
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        return result;
    }
}
