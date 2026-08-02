package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.IraceConfig;
import es.urjc.etsii.grafo.autoconfig.irace.IraceRuntimeConfiguration;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 500;

    private final AutomaticAlgorithmBuilder<?, ?> algorithmBuilder;
    private final int evaluationHistoryLimit;
    private final LinkedHashMap<Long, MutableEvaluation> evaluations = new LinkedHashMap<>();
    private final Map<String, MutableCandidate> candidates = new LinkedHashMap<>();

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
    private Integer iteration;
    private Instant elitesUpdatedAt;
    private boolean finalElites;
    private List<EliteView> elites = List.of();

    public AutoconfigRunState(
            AutomaticAlgorithmBuilder<?, ?> algorithmBuilder,
            IraceConfig iraceConfig
    ) {
        this.algorithmBuilder = algorithmBuilder;
        this.evaluationHistoryLimit = iraceConfig.getApiEvaluationHistoryLimit();
    }

    public synchronized String prepareCoordinator(int parameterCount) {
        reset();
        this.role = Role.COORDINATOR;
        this.state = RunStatus.PREPARING;
        this.runId = UUID.randomUUID().toString();
        this.preparedAt = Instant.now();
        this.generatedParameterCount = parameterCount;
        return runId;
    }

    public synchronized void prepareWorker() {
        reset();
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
        candidate.total++;
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

    public synchronized void publishElites(
            String reportedRunId,
            Integer reportedIteration,
            List<EliteConfiguration> reportedElites,
            boolean finalSnapshot
    ) {
        if (runId == null || !runId.equals(reportedRunId)) {
            throw new IllegalStateException("Elite snapshot belongs to a different autoconfig run");
        }
        if (role != Role.COORDINATOR || state != RunStatus.RUNNING) {
            throw new IllegalStateException("Autoconfig run is not accepting elite snapshots");
        }
        if (finalElites) {
            throw new IllegalStateException("The final elite snapshot has already been published");
        }
        Objects.requireNonNull(reportedElites, "Elites cannot be null");
        if (reportedIteration != null && reportedIteration < 1) {
            throw new IllegalArgumentException("IRACE iteration must be positive");
        }
        if (!finalSnapshot && iteration != null && reportedIteration != null && reportedIteration < iteration) {
            throw new IllegalStateException("IRACE progress snapshot is older than the current snapshot");
        }

        for (var candidate : candidates.values()) {
            candidate.elitePosition = null;
        }

        var views = new ArrayList<EliteView>(reportedElites.size());
        int position = 1;
        for (var reported : reportedElites) {
            var candidate = candidate(reported.configurationId(), reported.parameters());
            if (candidate.algorithm == null) {
                throw new IllegalArgumentException(
                        "Cannot decode elite configuration %s: %s"
                                .formatted(candidate.configurationId, candidate.decodeError)
                );
            }
            candidate.elitePosition = position;
            views.add(new EliteView(
                    candidate.configurationId,
                    position,
                    candidate.parameters,
                    candidate.algorithm
            ));
            position++;
        }

        if (reportedIteration != null) {
            this.iteration = reportedIteration;
        }
        this.elitesUpdatedAt = Instant.now();
        this.finalElites = finalSnapshot;
        this.elites = List.copyOf(views);
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
                new EvaluationCounts(used, running, succeeded, rejected, failed, slow),
                generatedParameterCount,
                new IraceProgress(iteration, elites.size(), elitesUpdatedAt, finalElites),
                failure
        );
    }

    public synchronized EliteSnapshot eliteSnapshot() {
        return new EliteSnapshot(
                runId,
                iteration,
                elitesUpdatedAt,
                finalElites,
                elites
        );
    }

    public synchronized EvaluationPage evaluations(
            Long after,
            Integer requestedLimit,
            EvaluationState stateFilter,
            String configurationId,
            Boolean slowFilter
    ) {
        long cursor = after == null ? 0 : after;
        if (cursor < 0) {
            throw new IllegalArgumentException("Evaluation cursor cannot be negative");
        }
        int limit = requestedLimit == null ? DEFAULT_PAGE_SIZE : requestedLimit;
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Evaluation limit must be between 1 and " + MAX_PAGE_SIZE);
        }

        var items = new ArrayList<EvaluationSummary>(limit);
        long nextCursor = cursor;
        boolean pageFull = false;
        for (var evaluation : evaluations.values()) {
            if (evaluation.id <= cursor) {
                continue;
            }
            if (!matches(evaluation, stateFilter, configurationId, slowFilter)) {
                nextCursor = evaluation.id;
                continue;
            }
            items.add(evaluation.summary());
            nextCursor = evaluation.id;
            if (items.size() == limit) {
                pageFull = true;
                break;
            }
        }
        if (!pageFull) {
            nextCursor = nextEvaluationId;
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

    public synchronized EvaluationDetail evaluation(long evaluationId) {
        var evaluation = evaluations.get(evaluationId);
        return evaluation == null ? null : evaluation.detail();
    }

    public synchronized CandidateView candidate(String configurationId) {
        var candidate = candidates.get(configurationId);
        return candidate == null ? null : candidate.view();
    }

    private void reset() {
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
        this.iteration = null;
        this.elitesUpdatedAt = null;
        this.finalElites = false;
        this.elites = List.of();
        this.evaluations.clear();
        this.candidates.clear();
    }

    private MutableCandidate candidate(String configurationId, Map<String, String> parameters) {
        var normalizedParameters = immutableParameters(parameters);
        var existing = candidates.get(configurationId);
        if (existing != null) {
            if (!existing.parameters.equals(normalizedParameters)) {
                throw new IllegalArgumentException(
                        "IRACE reused configuration ID %s with different parameters".formatted(configurationId)
                );
            }
            return existing;
        }

        JsonNode algorithm = null;
        String decodeError = null;
        try {
            algorithm = algorithmBuilder.asJsonTree(new AlgorithmConfiguration(normalizedParameters));
        } catch (RuntimeException e) {
            decodeError = safeMessage(e);
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

    private static boolean matches(
            MutableEvaluation evaluation,
            EvaluationState state,
            String configurationId,
            Boolean slow
    ) {
        if (state != null && evaluation.state != state) {
            return false;
        }
        if (configurationId != null && !configurationId.equals(evaluation.configurationId)) {
            return false;
        }
        return slow == null || evaluation.slow == slow;
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
            long total,
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
            boolean finalSnapshot
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
            List<EvaluationSummary> evaluations
    ) {
    }

    public record EvaluationSummary(
            long id,
            String configurationId,
            String instanceId,
            String instanceName,
            EvaluationState state,
            Double cost,
            Double timeSeconds,
            Instant startedAt,
            Instant finishedAt,
            boolean slow
    ) {
    }

    public record EvaluationDetail(
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
            CandidateEvaluationCounts evaluations,
            Integer elitePosition
    ) {
    }

    public record CandidateEvaluationCounts(
            long total,
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
        private long total;
        private long running;
        private long succeeded;
        private long rejected;
        private long failed;
        private long slow;
        private Integer elitePosition;

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
                    new CandidateEvaluationCounts(total, running, succeeded, rejected, failed, slow),
                    elitePosition
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

        private EvaluationSummary summary() {
            return new EvaluationSummary(
                    id,
                    configurationId,
                    instanceId,
                    instanceName,
                    state,
                    cost,
                    timeSeconds,
                    startedAt,
                    finishedAt,
                    slow
            );
        }

        private EvaluationDetail detail() {
            return new EvaluationDetail(
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
}
