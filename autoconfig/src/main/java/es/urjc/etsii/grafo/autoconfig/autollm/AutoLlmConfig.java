package es.urjc.etsii.grafo.autoconfig.autollm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

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
        codex = codex == null ? new Codex(null, null, null, null, null) : codex;
        preparation = preparation == null ? new Preparation(null, null) : preparation;
        tuning = tuning == null ? new Tuning(null, null, null, null) : tuning;
        comparison = comparison == null ? new Comparison(null, null) : comparison;
        execution = execution == null ? new Execution(null, null, null, null, null, null, null, null, null) : execution;
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
    public record Codex(String executable, String promptFile, String prompt, String model, String reasoningEffort) {
        public Codex { executable = executable == null ? "codex" : executable; }
    }
    public record Preparation(Long pilotDurationMillis, Integer maxRepairs) {
        public Preparation { pilotDurationMillis = pilotDurationMillis == null ? 600_000L : pilotDurationMillis; maxRepairs = maxRepairs == null ? 3 : maxRepairs; }
    }
    public record Tuning(Integer evaluations, Long ignoreInitialMillis, Long intervalDurationMillis, Boolean logScaleArea) {
        public Tuning { evaluations = evaluations == null ? 10_000 : evaluations; ignoreInitialMillis = ignoreInitialMillis == null ? 10_000L : ignoreInitialMillis;
            intervalDurationMillis = intervalDurationMillis == null ? 50_000L : intervalDurationMillis; logScaleArea = logScaleArea == null || logScaleArea; }
    }
    public record Comparison(Integer repetitions, Long timeLimitMillis) {
        public Comparison { repetitions = repetitions == null ? 30 : repetitions; timeLimitMillis = timeLimitMillis == null ? 0L : timeLimitMillis; }
    }
    public record Execution(Integer workers, Long watchdogGraceMillis, Long codexTimeoutMillis, Long buildTimeoutMillis,
                            Long startupTimeoutMillis, Long searchSpaceTimeoutMillis, Long phaseTimeoutMillis, Long stageTimeoutMillis, String heap) {
        public Execution {
            workers = workers == null ? 1 : workers; watchdogGraceMillis = watchdogGraceMillis == null ? 30_000L : watchdogGraceMillis;
            codexTimeoutMillis = codexTimeoutMillis == null ? 3_600_000L : codexTimeoutMillis;
            buildTimeoutMillis = buildTimeoutMillis == null ? 1_800_000L : buildTimeoutMillis;
            startupTimeoutMillis = startupTimeoutMillis == null ? 300_000L : startupTimeoutMillis;
            searchSpaceTimeoutMillis = searchSpaceTimeoutMillis == null ? 120_000L : searchSpaceTimeoutMillis;
            phaseTimeoutMillis = phaseTimeoutMillis == null ? 300_000L : phaseTimeoutMillis;
            stageTimeoutMillis = stageTimeoutMillis == null ? 604_800_000L : stageTimeoutMillis;
            heap = heap == null ? "2g" : heap;
        }
    }
    public AutoLlmConfig withPaths(String training, String comparison) {
        return new AutoLlmConfig(project, workspace, trials, seed, maxParameters, sotaFile, initialConfigurationsFile, training, comparison, codex, preparation, tuning, this.comparison, execution);
    }
    public static AutoLlmConfig defaults() { return new AutoLlmConfig(null, null, null, null, null, null, null, null, null, null, null, null, null, null); }
}
