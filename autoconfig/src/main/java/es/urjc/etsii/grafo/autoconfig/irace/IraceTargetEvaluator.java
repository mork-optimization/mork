package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.exception.IllegalAlgorithmConfigException;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.metrics.MetricUtil;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.services.ReflectiveSolutionBuilder;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.ExecutionJournalUtil;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator.decideImplementation;
import static java.util.concurrent.TimeUnit.*;

/**
 * Executes target configurations requested by IRACE.
 */
@Service
public class IraceTargetEvaluator<S extends Solution<S, I>, I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(IraceTargetEvaluator.class);
    private static final String IRACE_INSTANCE_PATH_KEY = "irace";

    private final SolverConfig solverConfig;
    private final IraceConfig iraceConfig;
    private final InstanceManager<I> instanceManager;
    private final SolutionBuilder<S, I> solutionBuilder;
    private final AlgorithmBuilder<S, I> algorithmBuilder;
    private final Optional<SolutionValidator<S, I>> validator;
    private final Optional<TimeLimitCalculator<S, I>> timeLimitCalculator;
    private final AutoconfigRunState runState;
    private final ThreadLocal<String> journalId = new ThreadLocal<>();

    public IraceTargetEvaluator(
            SolverConfig solverConfig,
            IraceConfig iraceConfig,
            InstanceManager<I> instanceManager,
            List<SolutionBuilder<S, I>> solutionBuilders,
            List<AlgorithmBuilder<S, I>> algorithmBuilders,
            Optional<SolutionValidator<S, I>> validator,
            Optional<TimeLimitCalculator<S, I>> timeLimitCalculator,
            AutoconfigRunState runState
    ) {
        this.solverConfig = solverConfig;
        this.iraceConfig = iraceConfig;
        this.instanceManager = instanceManager;
        this.solutionBuilder = decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
        this.algorithmBuilder = decideImplementation(algorithmBuilders, AutomaticAlgorithmBuilder.class);
        this.validator = validator;
        this.timeLimitCalculator = timeLimitCalculator;
        this.runState = runState;
    }

    /**
     * Execute and record a batch of IRACE target evaluations.
     *
     * @param configurations configurations to evaluate
     * @return results in request order
     */
    public List<ExecuteResponse> evaluateBatch(List<IraceExecuteConfig> configurations) {
        boolean automaticMode = runState.isAutomaticMode();
        return executeBatch(
                configurations,
                config -> evaluateAndRecord(new IraceRuntimeConfiguration(config), automaticMode)
        );
    }

    /**
     * Execute an IRACE scenario-check batch without recording it as tuning work.
     *
     * @param configurations configurations to validate
     * @return results in request order
     */
    public List<ExecuteResponse> preflightBatch(List<IraceExecuteConfig> configurations) {
        boolean automaticMode = runState.isAutomaticMode();
        return executeBatch(
                configurations,
                config -> evaluateAndRecord(new IraceRuntimeConfiguration(config), automaticMode, false)
        );
    }

    private List<ExecuteResponse> executeBatch(
            List<IraceExecuteConfig> configurations,
            Function<IraceExecuteConfig, ExecuteResponse> evaluator
    ) {
        if (solverConfig.isParallelExecutor()) {
            try (var executor = Executors.newFixedThreadPool(solverConfig.getnWorkers())) {
                var futures = new ArrayList<Future<ExecuteResponse>>(configurations.size());
                for (var configuration : configurations) {
                    futures.add(executor.submit(() -> evaluator.apply(configuration)));
                }
                return ConcurrencyUtil.awaitAll(futures);
            }
        }

        var results = new ArrayList<ExecuteResponse>(configurations.size());
        for (var configuration : configurations) {
            results.add(evaluator.apply(configuration));
        }
        return results;
    }

    private ExecuteResponse evaluateAndRecord(IraceRuntimeConfiguration config, boolean automaticMode) {
        return evaluateAndRecord(config, automaticMode, true);
    }

    private ExecuteResponse evaluateAndRecord(IraceRuntimeConfiguration config, boolean automaticMode, boolean tuning) {
        long evaluationId = tuning ? runState.evaluationStarted(config) : -1;
        String id = java.util.UUID.randomUUID().toString();
        journalId.set(id);
        var metadata = new java.util.LinkedHashMap<String, Object>();
        metadata.put("id", id);
        metadata.put("configurationId", config.getCandidateConfiguration());
        metadata.put("parameters", config.getAlgorithmConfig().getConfig());
        metadata.put("instance", config.getInstanceName());
        metadata.put("seed", config.getSeed());
        metadata.put("preflight", !tuning);
        ExecutionJournalUtil.append("evaluation-start", metadata);
        phase("LOADING", Long.getLong("mork.execution.phase-timeout", 300_000L));
        try {
            var result = evaluate(config, automaticMode);
            if (tuning && result.rejectionCode() != null) {
                runState.evaluationRejected(
                        evaluationId,
                        result.rejectionCode(),
                        result.rejectionMessage(),
                        result.slowOverrunMillis()
                );
            } else if (tuning) {
                runState.evaluationSucceeded(
                        evaluationId,
                        result.response().getCost(),
                        result.response().getTime(),
                        result.slowOverrunMillis()
                );
            }
            metadata.put("state", result.rejectionCode() == null ? "SUCCEEDED" : "REJECTED");
            metadata.put("rejectionCode", result.rejectionCode());
            metadata.put("rejectionMessage", result.rejectionMessage());
            metadata.put("cost", Double.isFinite(result.response().getCost()) ? result.response().getCost() : null);
            metadata.put("seconds", Double.isFinite(result.response().getTime()) ? result.response().getTime() : null);
            metadata.put("slowOverrunMillis", result.slowOverrunMillis());
            if (ExecutionJournalUtil.enabled() && result.rejectionCode() == null && Metrics.areMetricsEnabled()) {
                var curves = new java.util.LinkedHashMap<String,Object>();
                for (var entry : Metrics.getCurrentThreadMetrics().getMetrics().entrySet()) curves.put(entry.getKey(), entry.getValue().getValues());
                metadata.put("curves", curves);
            }
            ExecutionJournalUtil.finished(id, metadata);
            return result.response();
        } catch (RuntimeException e) {
            if (tuning) runState.evaluationFailed(evaluationId, e);
            metadata.put("state", "FAILED");
            metadata.put("failure", ExceptionUtil.stacktrace(e));
            ExecutionJournalUtil.finished(id, metadata);
            throw e;
        } finally {
            journalId.remove();
        }
    }

    private void phase(String name, long millis) {
        ExecutionJournalUtil.phase(journalId.get(), name, millis, java.util.Map.of());
    }

    private ExecutionResult candidateFailure(Throwable failure) {
        if (!iraceConfig.isRejectExecutionErrors()) {
            if (failure instanceof RuntimeException runtime) throw runtime;
            throw (AssertionError) failure;
        }
        return new ExecutionResult(new ExecuteResponse(), 0, "CANDIDATE_EXCEPTION", ExceptionUtil.stacktrace(failure));
    }

    private ExecutionResult evaluate(IraceRuntimeConfiguration config, boolean automaticMode) {
        if (ExecutionJournalUtil.enabled() && java.nio.file.Files.exists(ExecutionJournalUtil.directory().resolve("stop"))) {
            return new ExecutionResult(new ExecuteResponse(), 0, "PILOT_STOPPED", "Pilot admission deadline reached");
        }
        var instancePath = instanceManager.requireConfiguredInstancePath(
                IRACE_INSTANCE_PATH_KEY,
                config.getInstanceName()
        );
        var instance = instanceManager.getInstance(instancePath);
        Algorithm<S, I> algorithm;
        try {
            algorithm = buildAlgorithm(config, automaticMode);
        } catch (IllegalAlgorithmConfigException e) {
            log.debug("Invalid config, reason {}, config: {}", e.getMessage(), config);
            return new ExecutionResult(
                    new ExecuteResponse(),
                    0,
                    "INVALID_CONFIGURATION",
                    e.getMessage()
            );
        } catch (RuntimeException | AssertionError e) {
            return candidateFailure(e);
        }

        log.debug("Config {}. Built algorithm: {}", config, algorithm);
        Context.Configurator.resetRandom(solverConfig.getRandomType(), config.getSeed());

        try {
            long maxExecTime = solverConfig.getIgnoreInitialMillis() + solverConfig.getIntervalDurationMillis();
            if (!automaticMode && iraceConfig.isTimecontrol()) {
                if (timeLimitCalculator.isEmpty()) {
                    throw new IllegalStateException("irace.timecontrol is true, but no time limit calculator has been found, time control will not be enabled");
                }
                var timelimit = timeLimitCalculator.get().timeLimitInMillis(instance, algorithm);
                TimeControl.setMaxExecutionTime(timelimit, MILLISECONDS);
                TimeControl.start();
            }

            if (automaticMode) {
                TimeControl.setMaxExecutionTime(maxExecTime, MILLISECONDS);
                TimeControl.start();
                Metrics.enableMetrics();
            }

            if (Metrics.areMetricsEnabled()) {
                Metrics.resetMetrics();
            }

            long startTime = System.nanoTime();
            long duration = TimeControl.isEnabled() ? Math.max(1, NANOSECONDS.toMillis(TimeControl.remaining())) : Long.getLong("mork.execution.phase-timeout", 300_000L);
            phase("RUNNING", duration);
            S solution;
            try { solution = algorithm.algorithm(instance); }
            catch (RuntimeException | AssertionError e) { return candidateFailure(e); }
            long endTime = System.nanoTime();
            phase("VALIDATING", Long.getLong("mork.execution.phase-timeout", 300_000L));

            double score;
            long slowOverrunMillis = 0;
            Objective<?, S, I> mainObj = Context.getMainObjective();
            if (automaticMode || iraceConfig.isTimecontrol()) {
                slowOverrunMillis = checkExecutionTime(algorithm, instance);
                if (iraceConfig.isRejectExecutionErrors() && NANOSECONDS.toMillis(-TimeControl.remaining()) > Long.getLong("mork.execution.watchdog-grace", 30_000L)) {
                    return new ExecutionResult(new ExecuteResponse(), slowOverrunMillis, "TIME_LIMIT_EXCEEDED", "Algorithm returned after its deadline and watchdog grace");
                }
            }
            if (validator.isPresent()) {
                es.urjc.etsii.grafo.solution.ValidationResult validation;
                try { validation = validator.get().validate(solution); }
                catch (RuntimeException | AssertionError e) { return candidateFailure(e); }
                if (!validation.isValid()) {
                    String reason = validation.getReasonFailed();
                    log.warn("Rejecting invalid solution for configuration {}, instance {}, seed {}: {}. Parameters: {}",
                            config.getCandidateConfiguration(), config.getInstanceName(), config.getSeed(),
                            reason, config.getAlgorithmConfig().getConfig());
                    return new ExecutionResult(
                            new ExecuteResponse(),
                            slowOverrunMillis,
                            "INVALID_SOLUTION",
                            reason
                    );
                }
            }
            if (automaticMode) {
                try {
                    score = MetricUtil.areaUnderCurve(
                            mainObj,
                            TimeUtil.convert(
                                    solverConfig.getIgnoreInitialMillis(),
                                    MILLISECONDS,
                                    NANOSECONDS
                            ),
                            TimeUtil.convert(
                                    solverConfig.getIntervalDurationMillis(),
                                    MILLISECONDS,
                                    NANOSECONDS
                            ),
                            solverConfig.isLogScaleArea()
                    );
                    if (!solverConfig.isLogScaleArea()) {
                        score /= TimeUtil.NANOS_IN_MILLISECOND;
                    }
                } catch (IllegalArgumentException e) {
                    log.debug("Error while calculating AUC: ", e);
                    return new ExecutionResult(
                            new ExecuteResponse(),
                            slowOverrunMillis,
                            "INVALID_AUC",
                            e.getMessage()
                    );
                }
            } else if (iraceConfig.isAuc()) {
                score = MetricUtil.areaUnderCurve(
                        mainObj,
                        TimeUtil.convert(
                                solverConfig.getIgnoreInitialMillis(),
                                MILLISECONDS,
                                NANOSECONDS
                        ),
                        TimeUtil.convert(
                                solverConfig.getIntervalDurationMillis(),
                                MILLISECONDS,
                                NANOSECONDS
                        ),
                        solverConfig.isLogScaleArea()
                );
            } else {
                score = mainObj.evalSol(solution);
            }

            if (mainObj.getFMode() == FMode.MAXIMIZE) {
                score *= -1;
            }

            double elapsedSeconds = TimeUtil.nanosToSecs(endTime - startTime);
            log.debug("IRACE Iteration: {} {}", score, elapsedSeconds);
            return new ExecutionResult(
                    new ExecuteResponse(score, elapsedSeconds),
                    slowOverrunMillis,
                    null,
                    null
            );
        } finally {
            TimeControl.remove();
        }
    }

    private Algorithm<S, I> buildAlgorithm(IraceRuntimeConfiguration config, boolean automaticMode) {
        Algorithm<S, I> algorithm = algorithmBuilder.buildFromConfig(config.getAlgorithmConfig());
        algorithm.setBuilder(solutionBuilder);
        if (automaticMode && solverConfig.isAutorestart()) {
            int iterations = Integer.MAX_VALUE / 2;
            algorithm = new MultiStartAlgorithm<>(
                    algorithm.getName(),
                    Context.getMainObjective(),
                    algorithm,
                    iterations,
                    iterations,
                    iterations
            );
            algorithm.setBuilder(solutionBuilder);
        }
        return algorithm;
    }

    private long checkExecutionTime(Algorithm<S, I> algorithm, I instance) {
        long remaining = TimeControl.remaining();
        if (remaining < -TimeUtil.secsToNanos(Executor.EXTRA_SECS_BEFORE_WARNING)) {
            log.warn(
                    "Algorithm takes too long to stop after time is up in instance {}. Algorithm::toString {}",
                    instance.getId(),
                    algorithm
            );
            return NANOSECONDS.toMillis(-remaining);
        }
        return 0;
    }

    private record ExecutionResult(
            ExecuteResponse response,
            long slowOverrunMillis,
            String rejectionCode,
            String rejectionMessage
    ) {
    }
}
