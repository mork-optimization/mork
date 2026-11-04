package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import es.urjc.etsii.grafo.config.*;
import es.urjc.etsii.grafo.util.*;
import tools.jackson.core.type.TypeReference;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** The remote host prepares and approves; the local mirror retains the original reviewed bytes. */
public final class RemoteScreeningService {
    public record Preparation(String commit, String artifact, String artifactSha256, String sotaSha256,
                              AutoLlmConfig config, InstanceScreeningConfig settings, List<String> arguments,
                              long seed, String randomType, double trainingFraction, String originalPool, CommittedBaseline.Receipt baseline) {}
    private final AutoLlmProcessRunner processes;
    private final AutoLlmExecutionService workloads;
    private final java.util.function.Consumer<Map<String,Object>> observer;
    private final java.util.function.BooleanSupplier cancelled;
    public RemoteScreeningService(AutoLlmProcessRunner processes, java.util.function.Consumer<Map<String,Object>> observer, java.util.function.BooleanSupplier cancelled) {
        this.processes = processes; this.workloads = new AutoLlmExecutionService(processes); this.observer = observer; this.cancelled = cancelled;
    }

    public void run(AutoLlmConfig config, InstanceScreeningConfig settings, InstanceConfiguration inputs, SolverConfig solver,
                    boolean approve, String reviewedHash, List<String> runArguments) throws IOException {
        if (config.execution().target() == AutoLlmConfig.Target.SSH) config = config.forServer(config.execution().ssh().screeningServer());
        Path output = Path.of(settings.output()).toAbsolutePath().normalize(); Files.createDirectories(output);
        String key = AutoLlmFileUtil.hash(new ByteArrayInputStream(output.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8))).substring(0, 24);
        Path root = output.resolve(config.execution().target().name().toLowerCase(Locale.ROOT) + "-" + key); Files.createDirectories(root);

        Path preparedFile = root.resolve("remote-screening.json");
        Path artifact;
        Preparation preparation;
        observer.accept(Map.of("phase", "PREFLIGHT", "mirror", root.toString()));
        workloads.probe(config, root, settings.python() == null ? "" : settings.python());
        if (approve) {
            if (!Files.exists(preparedFile)) throw new IllegalStateException("Remote preparation has not completed");
            preparation = AutoLlmFileUtil.read(preparedFile, Preparation.class);
            if (!preparation.config().execution().equals(config.execution())) throw new IllegalStateException("Remote execution settings changed since preparation");
            if (reviewedHash == null || !reviewedHash.equals(AutoLlmFileUtil.hash(root.resolve("proposal/proposal.json")))) throw new IllegalArgumentException("Approval requires the exact reviewed remote proposal hash");
            ScreeningFileUtil.verifyProposalArtifacts(root.resolve("proposal"), AutoLlmFileUtil.read(root.resolve("proposal/proposal.json"), ScreeningData.Proposal.class));
            if (!AutoLlmFileUtil.hash(preparedFile).equals(AutoLlmFileUtil.hash(root.resolve("proposal/source-metadata.json")))) throw new IllegalStateException("Reviewed source metadata changed");

        } else {
            if (Files.exists(preparedFile)) {
                preparation = AutoLlmFileUtil.read(preparedFile, Preparation.class);
                if (!preparation.settings().equals(settings) || !preparation.arguments().equals(runArguments) || !preparation.config().equals(config)) throw new IllegalStateException("Screening settings changed; choose a new output directory");
                if (preparation.seed() != solver.getSeed() || !preparation.randomType().equals(solver.getRandomType().name()) || preparation.trainingFraction() != inputs.getPreliminarPercentage()) throw new IllegalStateException("Screening seeds or selection settings changed; choose a new output directory");
                if (Files.exists(root.resolve("proposal/approved.json"))) throw new IllegalStateException("Selection is already approved; choose a new output directory to rescreen");
            } else {
                observer.accept(Map.of("phase", "SOURCE", "mirror", root.toString()));
                var attempt = CommittedBaseline.prepare(root, config, this::command);
                String commit = attempt.commit();
                Path source = root.resolve(attempt.directory()).resolve("source");
                artifact = source.resolve(config.project().artifact());
                Path build = root.resolve(attempt.directory()).resolve("build");
                Files.createDirectories(root.resolve("control"));
                Files.copy(Path.of(settings.sotaFile()), root.resolve("control/sota.json"), StandardCopyOption.REPLACE_EXISTING);
                observer.accept(Map.of("phase", "INPUTS", "mirror", root.toString()));
                var pool = new ArrayList<String>(); var original = new LinkedHashMap<String,Map<String,String>>();
                String path = inputs.getForSelection() == null ? inputs.getPath("default") : inputs.getForSelection();
                for (String token : InstancePathUtil.list(path)) {
                    Path container = Path.of(IOUtil.containerPath(token)); String hash = AutoLlmFileUtil.hash(container);
                    Path copy = root.resolve("data/original/" + hash + "/" + container.getFileName()); Files.createDirectories(copy.getParent());
                    if (!Files.exists(copy)) Files.copy(container, copy);
                    String suffix = token.contains("!") ? token.substring(token.indexOf('!')) : "";
                    String staged = copy + suffix; pool.add(staged);
                    original.put(token, Map.of("path", staged, "sha256", AutoLlmFileUtil.inputHash(token)));
                }
                Files.write(root.resolve("control/original.index"), pool); AutoLlmFileUtil.write(root.resolve("data/original-inputs.json"), original);
                AutoLlmFileUtil.write(root.resolve("control/config.json"), config);
                observer.accept(Map.of("phase", "BUILD", "mirror", root.toString()));
                CommittedBaseline.verifyBeforeSubmission(root, attempt);
                var result = workloads.run(config, root, config.project().buildCommand(), source, build, source, List.of(artifact),
                        config.execution().buildTimeoutMillis(), 0, false, 0, cancelled, state -> observer.accept(Map.of("phase", "BUILD", "remote", state, "mirror", root.toString())));
                AutoLlmFileUtil.write(build.resolve("baseline-result.json"), new CommittedBaseline.Built(result, result.exitCode() == 0 ? AutoLlmFileUtil.hash(artifact) : null));
                if (result.exitCode() != 0) throw new IllegalStateException("Committed screening baseline build failed; inspect " + build);
                preparation = new Preparation(commit, root.relativize(artifact).toString(), AutoLlmFileUtil.hash(artifact), AutoLlmFileUtil.hash(root.resolve("control/sota.json")), config, settings, runArguments,
                        solver.getSeed(), solver.getRandomType().name(), inputs.getPreliminarPercentage(), path, CommittedBaseline.completed(root, attempt, config, artifact));
                AutoLlmFileUtil.write(preparedFile, preparation);
            }
        }
        CommittedBaseline.verify(root, preparation);
        artifact = root.resolve(preparation.artifact());
        if (!preparation.sotaSha256().equals(AutoLlmFileUtil.hash(Path.of(preparation.settings().sotaFile())))) throw new IllegalStateException("Original SOTA changed since remote preparation");
        Map<String,Map<String,String>> original = AutoLlmFileUtil.read(root.resolve("data/original-inputs.json"), new TypeReference<>() {});
        if (!new HashSet<>(InstancePathUtil.list(preparation.originalPool())).equals(original.keySet())) throw new IllegalStateException("Original pool membership changed since remote preparation");
        for (var entry : original.entrySet()) if (!entry.getValue().get("sha256").equals(AutoLlmFileUtil.inputHash(entry.getKey()))) throw new IllegalStateException("Original input changed since remote preparation: " + entry.getKey());
        var arguments = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-da", "-Xmx" + config.execution().heap(),
                "-Dmork.execution.directory=" + root.resolve((approve ? "approval" : "screening") + "/execution"), "-jar", artifact.toString(),
                approve ? "--instance-screening-approve" : "--instance-screening", "--instance-screening.output=" + root.resolve("proposal"),
                "--server.address=127.0.0.1", "--server.port=0", "--event.webserver.stop-on-execution-end=true", "--autollm.execution.target=LOCAL",
                "--autollm.execution.heap=" + config.execution().heap(), "--autollm.execution.watchdog-grace-millis=" + config.execution().watchdogGraceMillis(),
                "--autollm.execution.startup-timeout-millis=" + config.execution().startupTimeoutMillis(), "--autollm.execution.phase-timeout-millis=" + config.execution().phaseTimeoutMillis(),
                "--autollm.execution.build-timeout-millis=" + config.execution().buildTimeoutMillis()));
        arguments.add("--instance-screening.provenance-file=" + preparedFile);
        arguments.addAll(config.project().runArguments());
        if (approve) arguments.add("--instance-screening.review-sha256=" + reviewedHash);
        else {
            arguments.addAll(List.of("--instance-screening.sota-file=" + root.resolve("control/sota.json"), "--instances.for-selection=" + root.resolve("control/original.index"),
                    "--instance-screening.threshold-millis=" + settings.thresholdMillis(), "--instance-screening.candidate-multiplier=" + settings.candidateMultiplier(),
                    "--instance-screening.horizon-millis=" + settings.horizonMillis(), "--instance-screening.confirmation-runs=" + settings.confirmationRuns(),
                    "--instance-screening.fast-fraction=" + settings.fastFraction(), "--instance-screening.warmup-repetitions=" + settings.warmupRepetitions(),
                    "--instance-screening.warmup-millis=" + settings.warmupMillis(), "--instance-screening.python=" + settings.python(),
                    "--instances.preliminar-percentage=" + inputs.getPreliminarPercentage(), "--solver.seed=" + solver.getSeed(), "--solver.random-type=" + solver.getRandomType()));
        }
        Path operation = root.resolve(approve ? "approval-operation.json" : "screening-operation.json");
        String operationDirectory = approve ? "approval" : "screening";
        if (Files.exists(operation)) {
            operationDirectory = AutoLlmFileUtil.JSON.readTree(Files.readString(operation)).path("directory").asString();
            Path status = root.resolve(operationDirectory).resolve("remote-status.json");
            if (Files.exists(status)) {
                String state = AutoLlmFileUtil.JSON.readTree(Files.readString(status)).path("status").asString();
                if (Set.of("FAILED", "INTERRUPTED", "CANCELLED").contains(state)) operationDirectory = (approve ? "approval-" : "screening-") + UUID.randomUUID();
            }
        }
        AutoLlmFileUtil.write(operation, Map.of("directory", operationDirectory));
        Path jobOutput = root.resolve(operationDirectory);
        observer.accept(Map.of("phase", approve ? "APPROVAL" : "SCREENING", "mirror", root.toString()));
        // Observation settings and proposal paths remain fixed across worker restarts.
        arguments.set(3, "-Dmork.execution.directory=" + jobOutput.resolve("execution"));
        var result = workloads.run(config, root, arguments, jobOutput, jobOutput, null, List.of(root.resolve("proposal")), config.execution().stageTimeoutMillis(),
                0, false, 0, cancelled, state -> observer.accept(Map.of("phase", approve ? "APPROVAL" : "SCREENING", "remote", state, "mirror", root.toString())));
        if (result.exitCode() != 0) throw new IllegalStateException("Remote screening operation failed; inspect " + jobOutput);
        System.out.println("Remote screening evidence: " + root.resolve("proposal"));
        if (!approve) {
            System.out.println(Files.readString(root.resolve("proposal/report.md")));
            System.out.println("Reviewed proposal SHA-256: " + AutoLlmFileUtil.hash(root.resolve("proposal/proposal.json")));
        }

    }
    private void command(List<String> command, Path cwd, Path output) {
        var result = processes.run(command, cwd, output, 300000, 0, 1000, cancelled, false, 0);
        if (result.exitCode() != 0) throw new IllegalStateException("Source discovery failed; inspect " + output);
    }

}
