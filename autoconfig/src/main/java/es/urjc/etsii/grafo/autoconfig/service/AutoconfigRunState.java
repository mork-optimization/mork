package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceProgressDetails;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.IraceRuntimeConfiguration;
import es.urjc.etsii.grafo.algorithms.FMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
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
    private final NavigableMap<Long, MutableEvaluation> evaluations = new TreeMap<>();
    private final NavigableMap<Long, EvaluationChange> evaluationChanges = new TreeMap<>();
    private final NavigableMap<Integer, EliteIterationSnapshot> eliteHistory = new TreeMap<>();
    private final Map<String, MutableCandidate> candidates = new LinkedHashMap<>();
    private ComponentUsageAggregator componentUsage = new ComponentUsageAggregator();

    private RunMode mode = RunMode.DISABLED;
    private Role role = Role.DISABLED;
    private RunPhase phase = RunPhase.NOT_STARTED;
    private CostMetricSnapshot metric;
    private String runId;
    private Instant preparedAt;
    private Instant startedAt;
    private Instant finishedAt;
    private FailureSnapshot failure;
    private int maximumBudget;
    private int generatedParameterCount;
    private Integer trainingInstanceCount;
    private long nextEvaluationId;
    private long latestEvaluationRevision;
    private long used;
    private long running;
    private long succeeded;
    private long rejected;
    private long failed;
    private long slow;
    private StoredIraceSnapshot irace = StoredIraceSnapshot.empty();

    public AutoconfigRunState(AutomaticAlgorithmBuilder<?, ?> algorithmBuilder) {
        this.algorithmBuilder = algorithmBuilder;
    }

    public synchronized String prepareCoordinator(RunMode mode, CostMetricSnapshot metric) {
        reset();
        requireTuningMode(mode);
        this.mode = mode;
        this.role = Role.COORDINATOR;
        this.phase = RunPhase.PREPARING;
        this.metric = Objects.requireNonNull(metric, "Cost metric cannot be null");
        this.runId = UUID.randomUUID().toString();
        this.preparedAt = Instant.now();
        return runId;
    }

    public synchronized void prepareWorker(RunMode mode, CostMetricSnapshot metric) {
        reset();
        requireTuningMode(mode);
        this.mode = mode;
        this.role = Role.WORKER;
        this.phase = RunPhase.WAITING_FOR_WORK;
        this.metric = Objects.requireNonNull(metric, "Cost metric cannot be null");
    }

    private static void requireTuningMode(RunMode mode) {
        Objects.requireNonNull(mode, "Run mode cannot be null");
        if (mode == RunMode.DISABLED) {
            throw new IllegalArgumentException("A tuning run cannot use DISABLED mode");
        }
    }

    /**
     * Whether the current IRACE process is evaluating the generated automatic search space.
     *
     * @return true in automatic configuration mode
     */
    public synchronized boolean isAutomaticMode() {
        return mode == RunMode.AUTOCONFIG;
    }

    public synchronized void markRunning(int maximumBudget) {
        if (maximumBudget < 1) {
            throw new IllegalArgumentException("Maximum budget must be positive");
        }
        if (role != Role.COORDINATOR || phase != RunPhase.PREPARING) {
            throw new IllegalStateException("Autoconfig run is not preparing");
        }
        this.maximumBudget = maximumBudget;
        this.startedAt = Instant.now();
        this.phase = RunPhase.CHECKING_SCENARIO;
    }

    public synchronized void publishGeneratedSearchSpace(int generatedParameterCount) {
        if (generatedParameterCount < 1) {
            throw new IllegalArgumentException("Generated parameter count must be positive");
        }
        if (role != Role.COORDINATOR || phase != RunPhase.PREPARING) {
            throw new IllegalStateException("Autoconfig run is not preparing");
        }
        if (mode != RunMode.AUTOCONFIG) {
            throw new IllegalStateException("The current run does not use the automatic search space");
        }
        this.generatedParameterCount = generatedParameterCount;
    }

    public synchronized void publishTrainingInstanceCount(int trainingInstanceCount) {
        if (trainingInstanceCount < 0) {
            throw new IllegalArgumentException("Training instance count cannot be negative");
        }
        if (role != Role.COORDINATOR || phase != RunPhase.PREPARING) {
            throw new IllegalStateException("Autoconfig run is not preparing");
        }
        this.trainingInstanceCount = trainingInstanceCount;
    }

    public synchronized boolean hasGeneratedSearchSpace() {
        return generatedParameterCount > 0;
    }

    public synchronized void markCompleted() {
        if (role != Role.COORDINATOR || !phase.isActiveCoordinatorPhase()) {
            throw new IllegalStateException("Autoconfig run is not running");
        }
        this.finishedAt = Instant.now();
        this.phase = RunPhase.COMPLETED;
    }

    public synchronized void markFailed(Throwable throwable) {
        Objects.requireNonNull(throwable, "Failure cannot be null");
        if (role != Role.COORDINATOR || phase == RunPhase.COMPLETED || phase == RunPhase.FAILED) {
            throw new IllegalStateException("Autoconfig run cannot transition to failed from " + phase);
        }
        this.finishedAt = Instant.now();
        this.failure = new FailureSnapshot(
                throwable.getClass().getSimpleName(),
                safeMessage(throwable)
        );
        this.phase = RunPhase.FAILED;
    }

    public synchronized void reportPhase(String reportedRunId, RunPhase reportedPhase) {
        requireCurrentCoordinatorRun(reportedRunId);
        Objects.requireNonNull(reportedPhase, "Run phase cannot be null");
        if (reportedPhase != RunPhase.RACING && reportedPhase != RunPhase.POSTPROCESSING) {
            throw new IllegalArgumentException("IRACE may only report RACING or POSTPROCESSING");
        }
        if (reportedPhase == phase) {
            return;
        }
        boolean validTransition = switch (reportedPhase) {
            case RACING -> phase == RunPhase.CHECKING_SCENARIO;
            case POSTPROCESSING -> phase == RunPhase.CHECKING_SCENARIO || phase == RunPhase.RACING;
            default -> false;
        };
        if (!validTransition) {
            throw new IllegalStateException("IRACE phase cannot move from %s to %s".formatted(phase, reportedPhase));
        }
        this.phase = reportedPhase;
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
        if (role == Role.COORDINATOR && phase == RunPhase.CHECKING_SCENARIO) {
            phase = RunPhase.RACING;
        }
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
        if (mode == RunMode.AUTOCONFIG && role == Role.COORDINATOR) {
            componentUsage.add(candidate.footprint, candidate.totalEvaluations() == 0, 1);
        }
        candidate.running++;
        recordEvaluationChange(evaluation);
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
        recordEvaluationChange(evaluation);
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
        recordEvaluationChange(evaluation);
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
        recordEvaluationChange(evaluation);
    }

    private void recordEvaluationChange(MutableEvaluation evaluation) {
        long revision = ++latestEvaluationRevision;
        evaluationChanges.put(revision, new EvaluationChange(revision, evaluation.view()));
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
        if (phase == RunPhase.CHECKING_SCENARIO) {
            phase = RunPhase.RACING;
        }
        var updatedAt = Instant.now();
        this.irace = new StoredIraceSnapshot(
                reportedIteration,
                updatedAt,
                false,
                progress,
                views
        );
        this.eliteHistory.put(
                reportedIteration,
                new EliteIterationSnapshot(reportedIteration, updatedAt, progress, views)
        );
    }

    public synchronized void publishFinalElites(
            String reportedRunId,
            List<EliteConfiguration> reportedElites
    ) {
        requireProgressRun(reportedRunId);
        var views = eliteViews(reportedElites);
        if (phase == RunPhase.CHECKING_SCENARIO || phase == RunPhase.RACING) {
            phase = RunPhase.POSTPROCESSING;
        }
        this.irace = new StoredIraceSnapshot(
                irace.iteration(),
                Instant.now(),
                true,
                irace.progress(),
                views
        );
    }

    private void requireProgressRun(String reportedRunId) {
        requireCurrentCoordinatorRun(reportedRunId);
        if (!phase.isActiveCoordinatorPhase()) {
            throw new IllegalStateException("Autoconfig run is not accepting elite snapshots");
        }
        if (irace.finalSnapshot()) {
            throw new IllegalStateException("The final elite snapshot has already been published");
        }
    }

    private void requireCurrentCoordinatorRun(String reportedRunId) {
        if (runId == null || !runId.equals(reportedRunId)) {
            throw new IllegalStateException("Update belongs to a different autoconfig run");
        }
        if (role != Role.COORDINATOR) {
            throw new IllegalStateException("The current process is not the autoconfig coordinator");
        }
    }

    private List<EliteView> eliteViews(List<EliteConfiguration> reportedElites) {
        Objects.requireNonNull(reportedElites, "Elites cannot be null");
        var definitions = new ArrayList<CandidateDefinition>(reportedElites.size());
        var reportedConfigurationIds = new HashSet<String>();
        for (var reported : reportedElites) {
            Objects.requireNonNull(reported, "Elite configuration cannot be null");
            if (!reportedConfigurationIds.add(reported.configurationId())) {
                throw new IllegalArgumentException(
                        "Duplicated elite configuration " + reported.configurationId()
                );
            }
            var parameters = immutableParameters(reported.parameters());
            validateCandidate(candidates.get(reported.configurationId()), reported.configurationId(), parameters);
            definitions.add(new CandidateDefinition(reported.configurationId(), parameters));
        }

        var reportedCandidates = new ArrayList<MutableCandidate>(definitions.size());
        for (var definition : definitions) {
            var candidate = candidates.get(definition.configurationId());
            if (candidate == null) {
                candidate = createCandidate(definition.configurationId(), definition.parameters());
            }
            if (mode == RunMode.AUTOCONFIG && candidate.algorithm == null) {
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

        for (var candidate : reportedCandidates) {
            candidates.putIfAbsent(candidate.configurationId, candidate);
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
                mode,
                role,
                phase.status(),
                phase,
                preparedAt,
                startedAt,
                finishedAt,
                elapsedMillis,
                new BudgetSnapshot(maximumBudget, used, remaining),
                new EvaluationCounts(running, succeeded, rejected, failed, slow),
                latestEvaluationRevision,
                generatedParameterCount,
                trainingInstanceCount,
                metric,
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

    public synchronized EliteHistorySnapshot eliteHistory() {
        return new EliteHistorySnapshot(runId, List.copyOf(eliteHistory.values()));
    }

    public synchronized EvaluationPage evaluations(
            Long after,
            Integer requestedLimit,
            EvaluationState state
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
        for (var evaluation : evaluations.tailMap(cursor, false).values()) {
            if (state != null && evaluation.state != state) {
                continue;
            }
            items.add(evaluation.view());
            nextCursor = evaluation.id;
            if (items.size() == limit) {
                break;
            }
        }

        return new EvaluationPage(
                nextEvaluationId,
                nextCursor,
                List.copyOf(items)
        );
    }

    public synchronized EvaluationChangePage evaluationChanges(Long after, Integer requestedLimit) {
        long cursor = after == null ? 0 : after;
        if (cursor < 0) {
            throw new IllegalArgumentException("Evaluation revision cursor cannot be negative");
        }
        int limit = requestedLimit == null ? DEFAULT_PAGE_SIZE : requestedLimit;
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Evaluation change limit must be between 1 and " + MAX_PAGE_SIZE);
        }

        var changes = new ArrayList<EvaluationChange>(limit);
        long nextRevision = cursor;
        for (var change : evaluationChanges.tailMap(cursor, false).values()) {
            changes.add(change);
            nextRevision = change.revision();
            if (changes.size() == limit) {
                break;
            }
        }

        return new EvaluationChangePage(
                runId,
                latestEvaluationRevision,
                nextRevision,
                List.copyOf(changes)
        );
    }

    public synchronized CandidateView candidate(String configurationId) {
        var candidate = candidates.get(configurationId);
        return candidate == null ? null : candidate.view();
    }

    public synchronized boolean hasComponentUsage() {
        return mode == RunMode.AUTOCONFIG && role == Role.COORDINATOR;
    }

    public synchronized ComponentUsage.Snapshot componentUsage(ComponentUsage.Scope scope) {
        var aggregate = componentUsage;
        if (scope == ComponentUsage.Scope.CURRENT_ELITES) {
            aggregate = new ComponentUsageAggregator();
            for (var elite : irace.elites()) {
                var candidate = candidates.get(elite.configurationId());
                aggregate.add(candidate.footprint, true, candidate.totalEvaluations());
            }
        }
        return aggregate.snapshot(runId, scope, latestEvaluationRevision, irace.updatedAt());
    }

    public synchronized ComponentUsage.CandidatePage componentCandidates(
            ComponentUsage.Scope scope, ComponentUsage.Basis basis,
            String component, String parent, String role, String child, int offset, int limit
    ) {
        if (offset < 0 || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Offset must be nonnegative and limit must be between 1 and 100");
        }
        boolean componentSelector = component != null;
        boolean anyRelationship = parent != null || role != null || child != null;
        if (componentSelector == anyRelationship || (componentSelector && component.isBlank())
                || (anyRelationship && (parent == null || parent.isBlank() || role == null || role.isBlank()
                || child == null || child.isBlank()))) {
            throw new IllegalArgumentException("Select either a nonblank component or a complete parent, role and child relationship");
        }
        var elitePositions = new LinkedHashMap<String, Integer>();
        for (var elite : irace.elites()) {
            elitePositions.put(elite.configurationId(), elite.position());
        }
        var key = componentSelector ? null : new ComponentUsage.RelationshipKey(parent, role, child);
        var matches = new ArrayList<ComponentUsage.Candidate>();
        for (var candidate : candidates.values()) {
            if (candidate.footprint == null
                    || (scope == ComponentUsage.Scope.ALL && candidate.totalEvaluations() == 0)
                    || (scope == ComponentUsage.Scope.CURRENT_ELITES && !elitePositions.containsKey(candidate.configurationId))) {
                continue;
            }
            long occurrences = componentSelector
                    ? candidate.footprint.components().getOrDefault(component, 0L)
                    : candidate.footprint.relationships().getOrDefault(key, 0L);
            if (occurrences > 0) {
                matches.add(new ComponentUsage.Candidate(candidate.configurationId, occurrences,
                        occurrences * candidate.totalEvaluations(), elitePositions.get(candidate.configurationId),
                        candidate.evaluationCounts()));
            }
        }
        Comparator<ComponentUsage.Candidate> ordering = Comparator.comparingLong(item ->
                basis == ComponentUsage.Basis.CANDIDATE ? item.occurrences() : item.evaluationPlacements());
        matches.sort(ordering.reversed()
                .thenComparing(ComponentUsage.Candidate::elitePosition, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ComponentUsage.Candidate::configurationId));
        int end = (int) Math.min(matches.size(), (long) offset + limit);
        var page = offset >= matches.size() ? List.<ComponentUsage.Candidate>of() : matches.subList(offset, end);
        return new ComponentUsage.CandidatePage(runId, scope, matches.size(), offset,
                end < matches.size() ? end : null, page);
    }

    private void reset() {
        this.mode = RunMode.DISABLED;
        this.role = Role.DISABLED;
        this.phase = RunPhase.NOT_STARTED;
        this.metric = null;
        this.runId = null;
        this.preparedAt = null;
        this.startedAt = null;
        this.finishedAt = null;
        this.failure = null;
        this.maximumBudget = 0;
        this.generatedParameterCount = 0;
        this.trainingInstanceCount = null;
        this.nextEvaluationId = 0;
        this.latestEvaluationRevision = 0;
        this.used = 0;
        this.running = 0;
        this.succeeded = 0;
        this.rejected = 0;
        this.failed = 0;
        this.slow = 0;
        this.irace = StoredIraceSnapshot.empty();
        this.evaluations.clear();
        this.evaluationChanges.clear();
        this.eliteHistory.clear();
        this.candidates.clear();
        this.componentUsage = new ComponentUsageAggregator();
    }

    private MutableCandidate candidate(String configurationId, Map<String, String> parameters) {
        var normalizedParameters = immutableParameters(parameters);
        var existing = candidates.get(configurationId);
        validateCandidate(existing, configurationId, normalizedParameters);
        if (existing != null) {
            return existing;
        }

        var candidate = createCandidate(configurationId, normalizedParameters);
        candidates.put(configurationId, candidate);
        return candidate;
    }

    private static void validateCandidate(
            MutableCandidate candidate,
            String configurationId,
            Map<String, String> parameters
    ) {
        if (candidate != null && !candidate.parameters.equals(parameters)) {
            throw new IllegalArgumentException(
                    "IRACE reused configuration ID %s with different parameters: %s != %s"
                            .formatted(configurationId, candidate.parameters, parameters)
            );
        }
    }

    private MutableCandidate createCandidate(
            String configurationId,
            Map<String, String> normalizedParameters
    ) {
        JsonNode algorithm = null;
        String decodeError = null;
        ComponentUsage.Footprint footprint = null;
        if (mode == RunMode.AUTOCONFIG) {
            try {
                algorithm = algorithmBuilder.asJsonTree(new AlgorithmConfiguration(normalizedParameters));
            } catch (RuntimeException e) {
                decodeError = safeMessage(e);
            }
            if (algorithm != null && role == Role.COORDINATOR) {
                try {
                    footprint = ComponentUsageUtil.analyze(algorithm);
                } catch (RuntimeException e) {
                    log.warn("Component analytics unavailable for configuration {}", configurationId, e);
                }
            }
        }
        var candidate = new MutableCandidate(
                configurationId,
                normalizedParameters,
                algorithm,
                decodeError,
                footprint
        );
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

    private static String safeMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        String message = throwable.getMessage();
        return message == null || message.isBlank()
                ? throwable.getClass().getSimpleName()
                : message;
    }

    public enum RunMode {
        DISABLED,
        IRACE,
        AUTOCONFIG
    }

    public enum RunPhase {
        NOT_STARTED,
        PREPARING,
        CHECKING_SCENARIO,
        RACING,
        POSTPROCESSING,
        WAITING_FOR_WORK,
        COMPLETED,
        FAILED;

        private boolean isActiveCoordinatorPhase() {
            return this == CHECKING_SCENARIO || this == RACING || this == POSTPROCESSING;
        }

        private RunStatus status() {
            return switch (this) {
                case NOT_STARTED -> RunStatus.NOT_STARTED;
                case PREPARING -> RunStatus.PREPARING;
                case CHECKING_SCENARIO, RACING, POSTPROCESSING, WAITING_FOR_WORK -> RunStatus.RUNNING;
                case COMPLETED -> RunStatus.COMPLETED;
                case FAILED -> RunStatus.FAILED;
            };
        }
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

    public enum CostMetricKind {
        OBJECTIVE,
        AREA_UNDER_CURVE
    }

    public record StatusSnapshot(
            String runId,
            RunMode mode,
            Role role,
            RunStatus state,
            RunPhase phase,
            Instant preparedAt,
            Instant startedAt,
            Instant finishedAt,
            Long elapsedMillis,
            BudgetSnapshot budget,
            EvaluationCounts evaluations,
            long latestEvaluationRevision,
            int generatedParameterCount,
            Integer trainingInstanceCount,
            CostMetricSnapshot metric,
            IraceProgress irace,
            FailureSnapshot failure
    ) {
    }

    public record CostMetricSnapshot(
            String objectiveName,
            FMode objectiveMode,
            CostMetricKind kind,
            FMode costMode,
            boolean negated,
            AucSettings auc
    ) {
        public CostMetricSnapshot {
            Objects.requireNonNull(objectiveName, "Objective name cannot be null");
            Objects.requireNonNull(objectiveMode, "Objective mode cannot be null");
            Objects.requireNonNull(kind, "Cost metric kind cannot be null");
            Objects.requireNonNull(costMode, "Cost mode cannot be null");
            if (costMode != FMode.MINIMIZE) {
                throw new IllegalArgumentException("IRACE cost mode must be MINIMIZE");
            }
            if ((kind == CostMetricKind.AREA_UNDER_CURVE) != (auc != null)) {
                throw new IllegalArgumentException("AUC settings must be present exactly for AREA_UNDER_CURVE metrics");
            }
        }
    }

    public record AucSettings(
            long ignoreInitialMillis,
            long intervalDurationMillis,
            boolean logScale
    ) {
        public AucSettings {
            if (ignoreInitialMillis < 0) {
                throw new IllegalArgumentException("Ignored initial duration cannot be negative");
            }
            if (intervalDurationMillis <= 0) {
                throw new IllegalArgumentException("AUC interval duration must be positive");
            }
        }
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

    public record EliteHistorySnapshot(
            String runId,
            List<EliteIterationSnapshot> iterations
    ) {
        public EliteHistorySnapshot {
            iterations = List.copyOf(iterations);
        }
    }

    public record EliteIterationSnapshot(
            int iteration,
            Instant updatedAt,
            IraceProgressDetails progress,
            List<EliteView> elites
    ) {
        public EliteIterationSnapshot {
            elites = List.copyOf(elites);
        }
    }

    public record EliteView(
            String configurationId,
            int position,
            Map<String, String> parameters,
            JsonNode algorithm
    ) {
    }

    public record EvaluationPage(
            long latestId,
            long nextCursor,
            List<EvaluationView> evaluations
    ) {
    }

    public record EvaluationChangePage(
            String runId,
            long latestRevision,
            long nextRevision,
            List<EvaluationChange> changes
    ) {
        public EvaluationChangePage {
            changes = List.copyOf(changes);
        }
    }

    public record EvaluationChange(long revision, EvaluationView evaluation) {
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
        private final ComponentUsage.Footprint footprint;
        private long running;
        private long succeeded;
        private long rejected;
        private long failed;
        private long slow;

        private MutableCandidate(
                String configurationId,
                Map<String, String> parameters,
                JsonNode algorithm,
                String decodeError,
                ComponentUsage.Footprint footprint
        ) {
            this.configurationId = configurationId;
            this.parameters = parameters;
            this.algorithm = algorithm;
            this.decodeError = decodeError;
            this.footprint = footprint;
        }

        private long totalEvaluations() {
            return running + succeeded + rejected + failed;
        }

        private CandidateEvaluationCounts evaluationCounts() {
            return new CandidateEvaluationCounts(running, succeeded, rejected, failed, slow);
        }

        private CandidateView view() {
            return new CandidateView(
                    configurationId,
                    parameters,
                    algorithm,
                    decodeError,
                    evaluationCounts()
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

    private record CandidateDefinition(
            String configurationId,
            Map<String, String> parameters
    ) {
    }
}
