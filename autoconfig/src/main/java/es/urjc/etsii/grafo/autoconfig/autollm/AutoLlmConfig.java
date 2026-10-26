package es.urjc.etsii.grafo.autoconfig.autollm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;
import java.util.Map;

/** Immutable settings copied into every campaign manifest. Durations are expressed in milliseconds. */
@ConfigurationProperties("autollm")
public record AutoLlmConfig(Project project, String workspace, Integer trials, Long seed, Integer maxParameters,
                            String sotaFile, String initialConfigurationsFile, String trainingPath, String comparisonPath,
                            Codex codex, Preparation preparation, Tuning tuning, Comparison comparison, Execution execution) {
    public AutoLlmConfig {
        project = project == null ? new Project(null, null, null, null, null) : project;
        workspace = workspace == null ? "autollm-runs" : workspace;
        trials = trials == null ? 10 : trials;
        seed = seed == null ? 1234L : seed;
        maxParameters = maxParameters == null ? 299 : maxParameters;
        codex = codex == null ? new Codex(null, null, null, null, null, null) : codex;
        preparation = preparation == null ? new Preparation(null, null, null) : preparation;
        tuning = tuning == null ? new Tuning(null, null, null, null) : tuning;
        comparison = comparison == null ? new Comparison(null, null) : comparison;
        execution = execution == null ? new Execution(null, null, null, null, null, null, null, null, null, null, null) : execution;
    }
    public record Project(String repository, String ref, List<String> buildCommand, String artifact, List<String> runArguments) {
        public Project {
            repository = repository == null ? "." : repository;
            ref = ref == null ? "HEAD" : ref;
            buildCommand = buildCommand == null ? List.of("./mvnw", "verify") : List.copyOf(buildCommand);
            artifact = artifact == null ? "target/project.jar" : artifact;
            runArguments = runArguments == null ? List.of() : List.copyOf(runArguments);
        }
    }
    public record Codex(String executable, String promptFile, String prompt, String model, String reasoningEffort, Integer maxConcurrentSessions) {
        public Codex { executable = executable == null ? "codex" : executable; maxConcurrentSessions = maxConcurrentSessions == null ? 1 : maxConcurrentSessions; }
    }
    public record Preparation(Long probeDurationMillis, Long pilotDurationMillis, Integer maxRepairs) {
        public Preparation {
            probeDurationMillis = probeDurationMillis == null ? 600_000L : probeDurationMillis;
            pilotDurationMillis = pilotDurationMillis == null ? 600_000L : pilotDurationMillis;
            maxRepairs = maxRepairs == null ? 3 : maxRepairs;
        }
    }
    public record Tuning(Integer evaluations, Long ignoreInitialMillis, Long intervalDurationMillis, Boolean logScaleArea) {
        public Tuning { evaluations = evaluations == null ? 10_000 : evaluations; ignoreInitialMillis = ignoreInitialMillis == null ? 10_000L : ignoreInitialMillis;
            intervalDurationMillis = intervalDurationMillis == null ? 50_000L : intervalDurationMillis; logScaleArea = logScaleArea == null || logScaleArea; }
    }
    public record Comparison(Integer repetitions, Long timeLimitMillis) {
        public Comparison { repetitions = repetitions == null ? 30 : repetitions; timeLimitMillis = timeLimitMillis == null ? 0L : timeLimitMillis; }
    }
    public record Execution(Integer workers, Long watchdogGraceMillis, Long codexTimeoutMillis, Long buildTimeoutMillis,
                            Long startupTimeoutMillis, Long searchSpaceTimeoutMillis, Long phaseTimeoutMillis, Long stageTimeoutMillis, String heap,
                            Target target, Ssh ssh) {
        public Execution {
            target = target == null ? Target.LOCAL : target;
            workers = workers == null ? -1 : workers; watchdogGraceMillis = watchdogGraceMillis == null ? 30_000L : watchdogGraceMillis;
            codexTimeoutMillis = codexTimeoutMillis == null ? 3_600_000L : codexTimeoutMillis;
            buildTimeoutMillis = buildTimeoutMillis == null ? 1_800_000L : buildTimeoutMillis;
            startupTimeoutMillis = startupTimeoutMillis == null ? 300_000L : startupTimeoutMillis;
            searchSpaceTimeoutMillis = searchSpaceTimeoutMillis == null ? 120_000L : searchSpaceTimeoutMillis;
            phaseTimeoutMillis = phaseTimeoutMillis == null ? 300_000L : phaseTimeoutMillis;
            stageTimeoutMillis = stageTimeoutMillis == null ? 604_800_000L : stageTimeoutMillis;
            heap = heap == null ? "2g" : heap;
        }
    }
    public enum Target { LOCAL, SSH }
    public record Ssh(String screeningServerId, String runnerArtifact, List<Server> servers) {
        public Ssh {
            servers = servers == null ? List.of() : List.copyOf(servers);
            screeningServerId = screeningServerId == null && !servers.isEmpty() ? servers.getFirst().id() : screeningServerId;
            runnerArtifact = runnerArtifact == null || runnerArtifact.isBlank() ? null : runnerArtifact;
        }
        public Server onlyServer() {
            if (servers.size() != 1) throw new IllegalStateException("Resolve a server before executing a workload");
            return servers.getFirst();
        }
        public Server screeningServer() {
            for (var server : servers) if (server.id().equals(screeningServerId)) return server;
            throw new IllegalArgumentException("Screening server must name a configured server");
        }
    }
    public record Server(String id, String host, String workspace, String javaExecutable, Map<String,String> environment, String configFile, Integer workers, String heap) {
        public Server {
            javaExecutable = javaExecutable == null ? "java" : javaExecutable;
            environment = environment == null ? Map.of() : Map.copyOf(environment);
            configFile = configFile == null || configFile.isBlank() ? null : configFile;
            heap = heap == null || heap.isBlank() ? null : heap;
        }
    }
    /** Resolve an immutable execution configuration for exactly one enrolled server. */
    public AutoLlmConfig forServer(Server server) {
        var e = execution;
        var resolved = new Execution(server.workers() == null ? e.workers() : server.workers(), e.watchdogGraceMillis(), e.codexTimeoutMillis(), e.buildTimeoutMillis(),
                e.startupTimeoutMillis(), e.searchSpaceTimeoutMillis(), e.phaseTimeoutMillis(), e.stageTimeoutMillis(), server.heap() == null ? e.heap() : server.heap(),
                Target.SSH, new Ssh(server.id(), e.ssh().runnerArtifact(), List.of(server)));
        return new AutoLlmConfig(project, workspace, trials, seed, maxParameters, sotaFile, initialConfigurationsFile, trainingPath, comparisonPath, codex, preparation, tuning, comparison, resolved);
    }
    public AutoLlmConfig withPaths(String training, String comparison) {
        return new AutoLlmConfig(project, workspace, trials, seed, maxParameters, sotaFile, initialConfigurationsFile, training, comparison, codex, preparation, tuning, this.comparison, execution);
    }
    public AutoLlmConfig withRef(String ref) {
        return new AutoLlmConfig(new Project(project.repository(), ref, project.buildCommand(), project.artifact(), project.runArguments()), workspace,
                trials, seed, maxParameters, sotaFile, initialConfigurationsFile, trainingPath, comparisonPath, codex, preparation, tuning, this.comparison, execution);
    }
    public static AutoLlmConfig defaults() { return new AutoLlmConfig(null, null, null, null, null, null, null, null, null, null, null, null, null, null); }
}
