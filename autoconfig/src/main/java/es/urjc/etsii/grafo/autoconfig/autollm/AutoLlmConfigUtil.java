package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.*;
import java.util.*;

/** Shared validation for campaigns and preparation jobs, before datasets exist. */
public final class AutoLlmConfigUtil {
    private AutoLlmConfigUtil() {}
    public static List<String> projectErrors(AutoLlmConfig config) {
        var errors = new ArrayList<String>();
        Path repo = Path.of(config.project().repository()).toAbsolutePath().normalize();
        if (!Files.exists(repo.resolve(".git"))) errors.add("Project must be a local Git repository");
        if (config.project().buildCommand().isEmpty() || config.project().buildCommand().getFirst().isBlank()) errors.add("Build command must be an argument array");
        if (Path.of(config.project().artifact()).isAbsolute() || config.project().artifact().contains("..")) errors.add("Artifact must be a relative project path");
        for (String argument : config.project().runArguments()) {
            String key = argument.split("=", 2)[0].replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
            if (!argument.startsWith("--") || !argument.contains("=") || key.startsWith("autollm") || key.startsWith("server.") || key.startsWith("instances.") || key.startsWith("irace.") || key.startsWith("event.webserver.")
                    || List.of("solver.seed", "solver.autorestart", "solver.experimentsperparameter", "solver.minimumnumberofexperiments", "solver.ignoreinitialmillis", "solver.intervaldurationmillis", "solver.logscalearea", "solver.parallelexecutor", "solver.nworkers", "solver.metrics", "solver.experiments", "solver.repetitions").contains(key)) errors.add("Run argument overrides campaign-owned setting: " + argument);
        }
        return errors;
    }
    public static List<String> executionErrors(AutoLlmConfig config) {
        var errors = new ArrayList<String>();
        for (long timeout : List.of(config.execution().watchdogGraceMillis(), config.execution().codexTimeoutMillis(), config.execution().buildTimeoutMillis(), config.execution().startupTimeoutMillis(), config.execution().searchSpaceTimeoutMillis(), config.execution().phaseTimeoutMillis(), config.execution().stageTimeoutMillis())) if (timeout <= 0) errors.add("All operational timeouts must be positive");
        if (!config.execution().heap().matches("[1-9][0-9]*[kKmMgG]")) errors.add("Heap must be a JVM size such as 2g");
        return errors;
    }
}
