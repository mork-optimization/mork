package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceProgressDetails;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.IraceConfig;
import es.urjc.etsii.grafo.autoconfig.irace.IraceRuntimeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Thread-safe, in-memory state for the autoconfig REST API.
 */
@Service
public class AutoconfigRunState {

    private static final Logger log = LoggerFactory.getLogger(AutoconfigRunState.class);
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 500;

    private final AutomaticAlgorithmBuilder<?, ?> algorithmBuilder;
    private final int evaluationHistoryLimit;
    private final LinkedHashMap<Long, MutableEvaluation> evaluations = new LinkedHashMap<>();
    private final Map<String, MutableCandidate> candidates = new LinkedHashMap<>();

    private boolean decodeAlgorithms;
    private Role role = Role.DISABLED;
    private RunStatus state = RunStatus.NOT_STARTED;
    private String runId;
    private Instant preparedAt;
    private Instant startedAt;
    private Instant finishedAt;
    private FailureSnapshot failure;
    private int maximumBudget;
    private int generatedParameterCount;
    private long nextEvaluationId;
    private long used;
    private long running;
    private long succeeded;
    private long rejected;
    private long failed;
    private long slow;
    private boolean historyTruncated;
    private StoredIraceSnapshot irace = StoredIraceSnapshot.empty();

    public AutoconfigRunState(
            AutomaticAlgorithmBuilder<?, ?> algorithmBuilder,
            IraceConfig iraceConfig
    ) {
        this.algorithmBuilder = algorithmBuilder;
        this.evaluationHistoryLimit = iraceConfig.getApiEvaluationHistoryLimit();
    }

    public synchronized String prepareCoordinator(int parameterCount, boolean decodeAlgorithms) {
        reset();
        this.decodeAlgorithms = decodeAlgorithms;
        this.role = Role.COORDINATOR;
        this.state = RunStatus.PREPARING;
        this.runId = UUID.randomUUID().toString();
        this.preparedAt = Instant.now();
        this.generatedParameterCount = parameterCount;
        return runId;
    }

    public synchronized void prepareWorker(boolean decodeAlgorithms) {
        reset();
        this.decodeAlgorithms = decodeAlgorithms;
        this.role = Role.WORKER;
    }

    public synchronized void markRunning(int maximumBudget) {
        if (maximumBudget < 1) {
            throw new IllegalArgumentException("Maximum budget must be positive");
        }
        if (role != Role.COORDINATOR || state != RunStatus.PREPARING) {
            throw new IllegalStateException("Autoconfig run is not preparing");
        }
        this.maximumBudget = maximumBudget;
        this.startedAt = Instant.now();
        this.state = RunStatus.RUNNING;
    }

    public synchronized void setGeneratedParameterCount(int generatedParameterCount) {
        if (generatedParameterCount < 0) {
            throw new IllegalArgumentException("Generated parameter count cannot be negative");
        }
        if (role != Role.COORDINATOR || state != RunStatus.PREPARING) {
            throw new IllegalStateException("Autoconfig run is not preparing");
        }
        this.generatedParameterCount = generatedParameterCount;
    }

    public synchronized void markCompleted() {
        if (role != Role.COORDINATOR || state != RunStatus.RUNNING) {
            throw new IllegalStateException("Autoconfig run is not running");
        }
        this.finishedAt = Instant.now();
        this.state = RunStatus.COMPLETED;
    }

    public synchronized void markFailed(Throwable throwable) {
        Objects.requireNonNull(throwable, "Failure cannot be null");
        this.finishedAt = Instant.now();
        this.failure = new FailureSnapshot(
                throwable.getClass().getSimpleName(),
                safeMessage(throwable)
        );
        this.state = RunStatus.FAILED;
    }

    public synchronized String getRunId() {
        return runId;
    }

    public synchronized long evaluationStarted(IraceRuntimeConfiguration configuration) {
        Objects.requireNonNull(configuration, "IRACE configuration cannot be null");
        var candidate = candidate(
                configuration.getCandidateConfiguration(),
                configuration.getAlgorithmConfig().getConfig()
        );
        long evaluationId = ++nextEvaluationId;
        var evaluation = new MutableEvaluation(
                evaluationId,
                candidate.configurationId,
                configuration.getInstanceId(),
                configuration.getInstanceName(),
                configuration.getSeed(),
                Instant.now()
        );
        evaluations.put(evaluationId, evaluation);
        used++;
        running++;
        candidate.running++;
        evictCompletedEvaluations();
        return evaluationId;
    }

    public synchronized void evaluationSucceeded(
            long evaluationId,
            double cost,
            double timeSeconds,
            long slowOverrunMillis
    ) {
        var evaluation = runningEvaluation(evaluationId);
        evaluation.state = EvaluationState.SUCCEEDED;
        evaluation.cost = cost;
        evaluation.timeSeconds = timeSeconds;
        evaluation.finishedAt = Instant.now();
        recordSlow(evaluation, slowOverrunMillis);

        running--;
        succeeded++;
        var candidate = candidates.get(evaluation.configurationId);
        candidate.running--;
        candidate.succeeded++;
        if (evaluation.slow) {
            candidate.slow++;
        }
        evictCompletedEvaluations();
    }

    public synchronized void evaluationRejected(
            long evaluationId,
            String code,
            String message,
            long slowOverrunMillis
    ) {
        var evaluation = runningEvaluation(evaluationId);
        evaluation.state = EvaluationState.REJECTED;
        evaluation.reasonCode = code;
        evaluation.message = message;
        evaluation.finishedAt = Instant.now();
        recordSlow(evaluation, slowOverrunMillis);

        running--;
        rejected++;
        var candidate = candidates.get(evaluation.configurationId);
        candidate.running--;
        candidate.rejected++;
        if (evaluation.slow) {
            candidate.slow++;
        }
        evictCompletedEvaluations();
    }

    public synchronized void evaluationFailed(long evaluationId, Throwable throwable) {
        var evaluation = runningEvaluation(evaluationId);
        evaluation.state = EvaluationState.FAILED;
        evaluation.reasonCode = "EXECUTION_ERROR";
        evaluation.message = safeMessage(throwable);
        evaluation.finishedAt = Instant.now();

        running--;
        failed++;
        var candidate = candidates.get(evaluation.configurationId);
        candidate.running--;
        candidate.failed++;
        evictCompletedEvaluations();
    }

    public synchronized void publishProgress(
            String reportedRunId,
            int reportedIteration,
            List<EliteConfiguration> reportedElites,
            IraceProgressDetails progress
    ) {
        requireProgressRun(reportedRunId);
        Objects.requireNonNull(progress, "IRACE progress cannot be null");
        if (reportedIteration < 1) {
            throw new IllegalArgumentException("IRACE iteration must be positive");
        }
        if (irace.iteration() != null && reportedIteration < irace.iteration()) {
            throw new IllegalStateException("IRACE progress snapshot is older than the current snapshot");
        }

        var views = eliteViews(reportedElites);
        warnIfInconsistent(reportedIteration, progress);
        this.irace = new StoredIraceSnapshot(
                reportedIteration,
                Instant.now(),
                false,
                progress,
                views
        );
    }

    public synchronized void publishFinalElites(
            String reportedRunId,
            List<EliteConfiguration> reportedElites
    ) {
        requireProgressRun(reportedRunId);
        var views = eliteViews(reportedElites);
        this.irace = new StoredIraceSnapshot(
                irace.iteration(),
                Instant.now(),
                true,
                irace.progress(),
                views
        );
    }

    private void requireProgressRun(String reportedRunId) {
        if (runId == null || !runId.equals(reportedRunId)) {
            throw new IllegalStateException("Elite snapshot belongs to a different autoconfig run");
        }
        if (role != Role.COORDINATOR || state != RunStatus.RUNNING) {
            throw new IllegalStateException("Autoconfig run is not accepting elite snapshots");
        }
        if (irace.finalSnapshot()) {
            throw new IllegalStateException("The final elite snapshot has already been published");
        }
    }

    private List<EliteView> eliteViews(List<EliteConfiguration> reportedElites) {
        Objects.requireNonNull(reportedElites, "Elites cannot be null");
        var reportedCandidates = new ArrayList<MutableCandidate>(reportedElites.size());
        var reportedConfigurationIds = new HashSet<String>();
        for (var reported : reportedElites) {
            Objects.requireNonNull(reported, "Elite configuration cannot be null");
            if (!reportedConfigurationIds.add(reported.configurationId())) {
                throw new IllegalArgumentException(
                        "Duplicated elite configuration " + reported.configurationId()
                );
            }
            var candidate = candidate(reported.configurationId(), reported.parameters());
            if (decodeAlgorithms && candidate.algorithm == null) {
                throw new IllegalArgumentException(
                        "Cannot decode elite configuration %s: %s"
                                .formatted(candidate.configurationId, candidate.decodeError)
                );
            }
            reportedCandidates.add(candidate);
        }

        var views = new ArrayList<EliteView>(reportedCandidates.size());
        int position = 1;
        for (var candidate : reportedCandidates) {
            views.add(new EliteView(
                    candidate.configurationId,
                    position,
                    candidate.parameters,
                    candidate.algorithm
            ));
            position++;
        }
        return List.copyOf(views);
    }

    private void warnIfInconsistent(int reportedIteration, IraceProgressDetails progress) {
        if (irace.iteration() != null) {
            if (reportedIteration == irace.iteration()) {
                log.warn("IRACE repeated progress snapshot for iteration {}", reportedIteration);
            } else if (reportedIteration > irace.iteration() + 1) {
                log.warn(
                        "IRACE progress skipped from iteration {} to {}",
                        irace.iteration(),
                        reportedIteration
                );
            }
        }
        if (reportedIteration > progress.nbIterations()) {
            log.warn(
                    "IRACE reported iteration {} but only {} planned iterations",
                    reportedIteration,
                    progress.nbIterations()
            );
        }
        if (used != progress.experimentsUsed()) {
            log.warn(
                    "Mork and IRACE disagree on used budget: Mork={}, IRACE={}",
                    used,
                    progress.experimentsUsed()
            );
        }
        if (progress.maxExperiments() > 0 && !progress.remainingBudgetEstimated()) {
            if (maximumBudget != progress.maxExperiments()) {
                log.warn(
                        "Mork and IRACE disagree on maximum budget: Mork={}, IRACE={}",
                        maximumBudget,
                        progress.maxExperiments()
                );
            }
            long remaining = Math.max(0, maximumBudget - used);
            if (remaining != progress.remainingBudget()) {
                log.warn(
                        "Mork and IRACE disagree on remaining budget: Mork={}, IRACE={}",
                        remaining,
                        progress.remainingBudget()
                );
            }
        }
    }

    public synchronized StatusSnapshot status() {
        Instant end = finishedAt == null ? Instant.now() : finishedAt;
        Long elapsedMillis = startedAt == null ? null : Duration.between(startedAt, end).toMillis();
        long remaining = maximumBudget == 0 ? 0 : Math.max(0, maximumBudget - used);
        return new StatusSnapshot(
                runId,
                role,
                state,
                preparedAt,
                startedAt,
                finishedAt,
                elapsedMillis,
                new BudgetSnapshot(maximumBudget, used, remaining),
                new EvaluationCounts(running, succeeded, rejected, failed, slow),
                generatedParameterCount,
                new IraceProgress(
                        irace.iteration(),
                        irace.elites().size(),
                        irace.updatedAt(),
                        irace.finalSnapshot(),
                        irace.progress()
                ),
                failure
        );
    }

    public synchronized EliteSnapshot eliteSnapshot() {
        return new EliteSnapshot(
                runId,
                irace.iteration(),
                irace.updatedAt(),
                irace.finalSnapshot(),
                irace.elites()
        );
    }

    public synchronized EvaluationPage evaluations(
            Long after,
            Integer requestedLimit
    ) {
        long cursor = after == null ? 0 : after;
        if (cursor < 0) {
            throw new IllegalArgumentException("Evaluation cursor cannot be negative");
        }
        int limit = requestedLimit == null ? DEFAULT_PAGE_SIZE : requestedLimit;
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Evaluation limit must be between 1 and " + MAX_PAGE_SIZE);
        }

        var items = new ArrayList<EvaluationView>(limit);
        long nextCursor = cursor;
        for (var evaluation : evaluations.values()) {
            if (evaluation.id <= cursor) {
                continue;
            }
            items.add(evaluation.view());
            nextCursor = evaluation.id;
            if (items.size() == limit) {
                break;
            }
        }

        long oldest = evaluations.isEmpty() ? 0 : evaluations.keySet().iterator().next();
        return new EvaluationPage(
                historyTruncated,
                oldest,
                nextEvaluationId,
                nextCursor,
                List.copyOf(items)
        );
    }

    public synchronized CandidateView candidate(String configurationId) {
        var candidate = candidates.get(configurationId);
        return candidate == null ? null : candidate.view();
    }

    private void reset() {
        this.decodeAlgorithms = false;
        this.role = Role.DISABLED;
        this.state = RunStatus.NOT_STARTED;
        this.runId = null;
        this.preparedAt = null;
        this.startedAt = null;
        this.finishedAt = null;
        this.failure = null;
        this.maximumBudget = 0;
        this.generatedParameterCount = 0;
        this.nextEvaluationId = 0;
        this.used = 0;
        this.running = 0;
        this.succeeded = 0;
        this.rejected = 0;
        this.failed = 0;
        this.slow = 0;
        this.historyTruncated = false;
        this.irace = StoredIraceSnapshot.empty();
        this.evaluations.clear();
        this.candidates.clear();
    }

    private MutableCandidate candidate(String configurationId, Map<String, String> parameters) {
        var normalizedParameters = immutableParameters(parameters);
        var existing = candidates.get(configurationId);
        if (existing != null) {
            if (!existing.parameters.equals(normalizedParameters)) {
                throw new IllegalArgumentException(
                        "IRACE reused configuration ID %s with different parameters: %s != %s"
                                .formatted(configurationId, existing.parameters, normalizedParameters)
                );
            }
            return existing;
        }

        JsonNode algorithm = null;
        String decodeError = null;
        if (decodeAlgorithms) {
            try {
                algorithm = algorithmBuilder.asJsonTree(new AlgorithmConfiguration(normalizedParameters));
            } catch (RuntimeException e) {
                decodeError = safeMessage(e);
            }
        }
        var candidate = new MutableCandidate(
                configurationId,
                normalizedParameters,
                algorithm,
                decodeError
        );
        candidates.put(configurationId, candidate);
        return candidate;
    }

    private static Map<String, String> immutableParameters(Map<String, String> parameters) {
        Objects.requireNonNull(parameters, "IRACE parameters cannot be null");
        var copy = new TreeMap<String, String>();
        for (var entry : parameters.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank() && !"NA".equals(entry.getValue())) {
                copy.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    private MutableEvaluation runningEvaluation(long evaluationId) {
        var evaluation = evaluations.get(evaluationId);
        if (evaluation == null) {
            throw new IllegalArgumentException("Unknown evaluation ID " + evaluationId);
        }
        if (evaluation.state != EvaluationState.RUNNING) {
            throw new IllegalStateException("Evaluation %s is already complete".formatted(evaluationId));
        }
        return evaluation;
    }

    private void recordSlow(MutableEvaluation evaluation, long slowOverrunMillis) {
        if (slowOverrunMillis <= 0) {
            return;
        }
        evaluation.slow = true;
        evaluation.slowOverrunMillis = slowOverrunMillis;
        slow++;
    }

    private void evictCompletedEvaluations() {
        while (evaluations.size() > evaluationHistoryLimit) {
            boolean removed = false;
            Iterator<Map.Entry<Long, MutableEvaluation>> iterator = evaluations.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (entry.getValue().state != EvaluationState.RUNNING) {
                    iterator.remove();
                    historyTruncated = true;
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                return;
            }
        }
    }

    private static String safeMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        String message = throwable.getMessage();
        return message == null || message.isBlank()
                ? throwable.getClass().getSimpleName()
                : message;
    }

    public enum Role {
        COORDINATOR,
        WORKER,
        DISABLED
    }

    public enum RunStatus {
        NOT_STARTED,
        PREPARING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    public enum EvaluationState {
        RUNNING,
        SUCCEEDED,
        REJECTED,
        FAILED
    }

    public record StatusSnapshot(
            String runId,
            Role role,
            RunStatus state,
            Instant preparedAt,
            Instant startedAt,
            Instant finishedAt,
            Long elapsedMillis,
            BudgetSnapshot budget,
            EvaluationCounts evaluations,
            int generatedParameterCount,
            IraceProgress irace,
            FailureSnapshot failure
    ) {
    }

    public record BudgetSnapshot(int maximum, long used, long remaining) {
    }

    public record EvaluationCounts(
            long running,
            long succeeded,
            long rejected,
            long failed,
            long slow
    ) {
    }

    public record IraceProgress(
            Integer iteration,
            int eliteCount,
            Instant updatedAt,
            boolean finalSnapshot,
            IraceProgressDetails progress
    ) {
    }

    public record FailureSnapshot(String type, String message) {
    }

    public record EliteSnapshot(
            String runId,
            Integer iteration,
            Instant updatedAt,
            boolean finalSnapshot,
            List<EliteView> elites
    ) {
    }

    public record EliteView(
            String configurationId,
            int position,
            Map<String, String> parameters,
            JsonNode algorithm
    ) {
    }

    public record EvaluationPage(
            boolean historyTruncated,
            long oldestRetainedId,
            long latestId,
            long nextCursor,
            List<EvaluationView> evaluations
    ) {
    }

    public record EvaluationView(
            long id,
            String configurationId,
            String instanceId,
            String instanceName,
            long seed,
            EvaluationState state,
            Double cost,
            Double timeSeconds,
            Instant startedAt,
            Instant finishedAt,
            String reasonCode,
            String message,
            boolean slow,
            Long slowOverrunMillis
    ) {
    }

    public record CandidateView(
            String configurationId,
            Map<String, String> parameters,
            JsonNode algorithm,
            String decodeError,
            CandidateEvaluationCounts evaluations
    ) {
    }

    public record CandidateEvaluationCounts(
            long running,
            long succeeded,
            long rejected,
            long failed,
            long slow
    ) {
    }

    private static final class MutableCandidate {
        private final String configurationId;
        private final Map<String, String> parameters;
        private final JsonNode algorithm;
        private final String decodeError;
        private long running;
        private long succeeded;
        private long rejected;
        private long failed;
        private long slow;

        private MutableCandidate(
                String configurationId,
                Map<String, String> parameters,
                JsonNode algorithm,
                String decodeError
        ) {
            this.configurationId = configurationId;
            this.parameters = parameters;
            this.algorithm = algorithm;
            this.decodeError = decodeError;
        }

        private CandidateView view() {
            return new CandidateView(
                    configurationId,
                    parameters,
                    algorithm,
                    decodeError,
                    new CandidateEvaluationCounts(running, succeeded, rejected, failed, slow)
            );
        }
    }

    private static final class MutableEvaluation {
        private final long id;
        private final String configurationId;
        private final String instanceId;
        private final String instanceName;
        private final long seed;
        private final Instant startedAt;
        private EvaluationState state = EvaluationState.RUNNING;
        private Double cost;
        private Double timeSeconds;
        private Instant finishedAt;
        private String reasonCode;
        private String message;
        private boolean slow;
        private Long slowOverrunMillis;

        private MutableEvaluation(
                long id,
                String configurationId,
                String instanceId,
                String instanceName,
                long seed,
                Instant startedAt
        ) {
            this.id = id;
            this.configurationId = configurationId;
            this.instanceId = instanceId;
            this.instanceName = instanceName;
            this.seed = seed;
            this.startedAt = startedAt;
        }

        private EvaluationView view() {
            return new EvaluationView(
                    id,
                    configurationId,
                    instanceId,
                    instanceName,
                    seed,
                    state,
                    cost,
                    timeSeconds,
                    startedAt,
                    finishedAt,
                    reasonCode,
                    message,
                    slow,
                    slowOverrunMillis
            );
        }
    }

    private record StoredIraceSnapshot(
            Integer iteration,
            Instant updatedAt,
            boolean finalSnapshot,
            IraceProgressDetails progress,
            List<EliteView> elites
    ) {
        private static StoredIraceSnapshot empty() {
            return new StoredIraceSnapshot(null, null, false, null, List.of());
        }
    }
}
