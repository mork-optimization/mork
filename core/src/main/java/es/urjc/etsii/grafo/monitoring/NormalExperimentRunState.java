package es.urjc.etsii.grafo.monitoring;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent.Outcome;
import es.urjc.etsii.grafo.executors.WorkUnit;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.results.ResultStore;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.StatisticsUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.util.*;

/** Authoritative current-process monitoring state. Mutations are bounded shared execution hooks, not event replay. */
@Service
public class NormalExperimentRunState<S extends Solution<S, I>, I extends Instance> {
    public enum Phase { IDLE, PLANNING, WARMING_UP, RUNNING, FINALIZING, SUCCEEDED, COMPLETED_WITH_ERRORS, FAILED, CANCELLED }
    public enum WorkState { PENDING, RUNNING, SUCCEEDED, FAILED, CANCELLED }
    public enum ResultOrder { PLANNED, INSTANCE, ALGORITHM, STATE, RUNTIME }
    public record Counts(long planned, long pending, long running, long succeeded, long failed, long cancelled) {
        public long completed() { return succeeded + failed; }
    }
    public record ExperimentSummary(String name, Counts counts, int instances, int completedInstances) {}
    public record Status(String runId, long revision, Phase phase, long startedAt, Long endedAt, long elapsedMillis,
                         String currentExperiment, Map<String, FMode> objectives, Counts counts,
                         int instances, int completedInstances, int runningInstances,
                         int algorithms, int completedAlgorithms, int runningAlgorithms,
                         List<ExperimentSummary> experiments, long issueCount) {}
    public record InstanceSummary(String id, String experimentName, String instanceName, Counts counts,
                                  Map<String, Double> references, int algorithms, int completedAlgorithms,
                                  boolean finalized, long revision) {}
    public record AlgorithmSummary(String name, Counts counts, StatisticsUtil.Summary objective,
                                   StatisticsUtil.Summary runtimeMillis, StatisticsUtil.Summary timeToBestMillis,
                                   UUID bestResultId) {}
    public record ResultView(UUID resultId, String instanceId, String experimentName, String instanceName,
                             String algorithmName, int ordinal, String iteration, long seed, WorkState state,
                             Map<String, Double> objectives, Map<String, Object> properties,
                             Long executionTime, Long timeToBest, Long startedAt, Long endedAt) {}
    public record Rendering(String state, String rendererId, Map<String, Object> data) {}
    public record ResultDetail(String runId, long revision, ResultView result, Rendering rendering, List<Issue> issues) {}
    public record InstanceDetail(String runId, long revision, InstanceSummary instance, String objective,
                                 List<AlgorithmSummary> algorithms, List<ResultView> results, int resultCount, boolean sampled) {}
    public record Page<T>(String runId, long revision, int total, int offset, int limit, List<T> items) {}
    public record Issue(long id, long timestamp, String severity, String scope, String exceptionType, String message,
                        UUID resultId, String experimentName, String instanceName, String algorithmName,
                        String iteration, Long seed) {}

    private final ResultStore<S, I> resultStore;
    private final ObjectProvider<SolutionViewProvider<S, I>> renderer;
    private final Map<String, FMode> objectives = new LinkedHashMap<>();
    private final Map<String, InstanceEntry> instances = new LinkedHashMap<>();
    private final Map<InstanceKey, InstanceEntry> instanceIndex = new HashMap<>();
    private final Map<UUID, WorkEntry> work = new LinkedHashMap<>();
    private final List<Issue> issues = new ArrayList<>();
    private final Map<UUID, Rendering> renderings = new LinkedHashMap<>();
    private final Counter totals = new Counter();
    private String runId;
    private long revision;
    private long startedAt;
    private Long endedAt;
    private Phase phase = Phase.IDLE;
    private String currentExperiment;

    public NormalExperimentRunState(ResultStore<S, I> resultStore, ObjectProvider<SolutionViewProvider<S, I>> renderer) {
        this.resultStore = resultStore;
        this.renderer = renderer;
    }

    public synchronized void prepare(Map<String, FMode> configuredObjectives) {
        if (runId != null) throw new IllegalStateException("Normal execution already prepared");
        runId = UUID.randomUUID().toString();
        objectives.putAll(configuredObjectives);
        startedAt = System.currentTimeMillis();
        phase = Phase.PLANNING;
        revision++;
    }

    public synchronized void plan(Experiment<S, I> experiment, List<String> paths, int repetitions, long seed) {
        for (var path : paths) {
            var entry = new InstanceEntry(UUID.randomUUID().toString(), experiment.name(), path);
            if (instanceIndex.putIfAbsent(new InstanceKey(experiment.name(), path), entry) != null)
                throw new IllegalArgumentException("Duplicate instance in experiment " + experiment.name());
            instances.put(entry.id, entry);
            for (var algorithm : experiment.algorithms()) {
                var alg = new AlgorithmEntry(algorithm.getName());
                if (entry.algorithms.putIfAbsent(alg.name, alg) != null)
                    throw new IllegalArgumentException("Duplicate algorithm name " + alg.name);
                for (int i = 0; i < repetitions; i++) {
                    var item = new WorkEntry(UUID.randomUUID(), entry, alg, i, seed + i);
                    work.put(item.id, item);
                    alg.work.add(item);
                    totals.add(WorkState.PENDING, 1); entry.counter.add(WorkState.PENDING, 1); alg.counter.add(WorkState.PENDING, 1);
                }
            }
            entry.revision = ++revision;
        }
    }

    public synchronized UUID workId(String experiment, String path, String algorithm, int iteration) {
        var instance = instanceIndex.get(new InstanceKey(experiment, path));
        if (instance == null) return UUID.randomUUID(); // Non-normal executor use (e.g. warm-up/custom tests).
        return instance.algorithms.get(algorithm).work.get(iteration).id;
    }

    public synchronized void phase(Phase value, String experiment) {
        if (runId == null || terminal()) return;
        phase = value; currentExperiment = experiment; revision++;
    }

    public synchronized void instanceStarted(String experiment, String path, String name, Map<String, Double> references) {
        var entry = instanceIndex.get(new InstanceKey(experiment, path));
        if (entry == null) return;
        entry.name = name; entry.references = Map.copyOf(references); entry.revision = ++revision;
        for (var algorithm : entry.algorithms.values()) algorithm.cached.clear();
    }

    public synchronized void started(WorkUnit<S, I> unit) {
        var entry = work.get(unit.resultId());
        if (entry == null || terminal()) return;
        if (entry.state != WorkState.PENDING) throw new IllegalStateException("Repetition started twice");
        transition(entry, WorkState.RUNNING);
        entry.startedAt = System.currentTimeMillis();
    }

    public synchronized void completed(WorkUnitResult<S, I> result) {
        var entry = work.get(result.resultId());
        if (entry == null || terminal()) return;
        if (entry.state != WorkState.RUNNING) throw new IllegalStateException("Repetition completed without start");
        transition(entry, result.success() ? WorkState.SUCCEEDED : WorkState.FAILED);
        entry.endedAt = System.currentTimeMillis();
        entry.instance.name = result.instanceId();
        if (result.success()) {
            var previousBest = new HashSet<>(entry.algorithm.best.values());
            for (var objective : objectives.entrySet()) {
                var value = result.objectives().get(objective.getKey());
                if (value == null || !Double.isFinite(value)) continue;
                entry.algorithm.scores.computeIfAbsent(objective.getKey(), ignored -> new StatisticsUtil.Accumulator()).add(value);
                var best = entry.algorithm.best.get(objective.getKey());
                if (best == null || better(result, best, objective.getKey(), objective.getValue())) {
                    entry.algorithm.best.put(objective.getKey(), entry.id);
                }
            }
            if (result.executionTime() >= 0) entry.algorithm.runtimes.add(result.executionTime() / 1_000_000.0);
            if (result.timeToTarget() >= 0) entry.algorithm.bestTimes.add(result.timeToTarget() / 1_000_000.0);
            previousBest.removeAll(entry.algorithm.best.values());
            for (var old : previousBest) renderings.remove(old);
            captureRendering(result);
        }
        entry.algorithm.cached.clear();
    }

    private boolean better(WorkUnitResult<S, I> candidate, UUID previous, String objective, FMode mode) {
        var old = resultStore.findResult(previous).orElseThrow();
        double value = candidate.objectives().get(objective), other = old.objectives().get(objective);
        if (value == other) return work.get(candidate.resultId()).iteration < work.get(previous).iteration;
        return mode == FMode.MAXIMIZE ? value > other : value < other;
    }

    private void captureRendering(WorkUnitResult<S, I> result) {
        boolean retained = false;
        var entry = work.get(result.resultId());
        for (var id : entry.algorithm.best.values()) if (id.equals(entry.id)) retained = true;
        if (retained) {
            try {
                var provider = renderer.getIfAvailable();
                if (provider != null) {
                    var mapper = new JsonMapper();
                    String json = mapper.writeValueAsString(provider.describe(result.solution()));
                    if (json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 256_000) throw new IllegalArgumentException("Solution view exceeds 256 KB");
                    @SuppressWarnings("unchecked") var data = (Map<String, Object>) mapper.readValue(json, Map.class);
                    renderings.put(result.resultId(), new Rendering("AVAILABLE", provider.rendererId(), Collections.unmodifiableMap(data)));
                    while (renderings.size() > 128) renderings.remove(renderings.keySet().iterator().next());
                }
            } catch (RuntimeException e) {
                issue("WARNING", "RENDERING", e, result.resultId(), result.experimentName());
            }
        }
    }

    public synchronized void instanceFinalized(String experiment, String path) {
        var entry = instanceIndex.get(new InstanceKey(experiment, path));
        if (entry != null) { entry.finalized = true; entry.revision = ++revision; }
    }

    public synchronized void issue(String severity, String scope, Throwable error, UUID resultId, String experiment) {
        if (runId == null) return;
        var entry = work.get(resultId);
        issues.add(new Issue(issues.size() + 1L, System.currentTimeMillis(), severity, scope,
                error.getClass().getSimpleName(), error.getMessage(), resultId, experiment,
                entry == null ? null : entry.instance.name, entry == null ? null : entry.algorithm.name,
                entry == null ? null : String.valueOf(entry.iteration), entry == null ? null : entry.seed));
        revision++;
    }

    public synchronized Outcome finish(Outcome requested) {
        if (runId == null) return requested;
        if (terminal()) return Outcome.valueOf(phase.name());
        for (var entry : work.values()) if (entry.state == WorkState.PENDING || entry.state == WorkState.RUNNING) {
            transition(entry, WorkState.CANCELLED);
            entry.endedAt = System.currentTimeMillis();
        }
        var outcome = requested;
        if ((outcome == Outcome.SUCCEEDED || outcome == Outcome.COMPLETED_WITH_ERRORS) && totals.cancelled > 0) outcome = Outcome.FAILED;
        if (outcome == Outcome.SUCCEEDED && (totals.failed > 0 || !issues.isEmpty())) outcome = Outcome.COMPLETED_WITH_ERRORS;
        phase = Phase.valueOf(outcome.name()); endedAt = System.currentTimeMillis(); revision++;
        return outcome;
    }

    private boolean terminal() { return phase == Phase.SUCCEEDED || phase == Phase.COMPLETED_WITH_ERRORS || phase == Phase.FAILED || phase == Phase.CANCELLED; }

    public synchronized Status status() {
        int completedInstances = 0, runningInstances = 0, algorithms = 0, completedAlgorithms = 0, runningAlgorithms = 0;
        var experiments = new LinkedHashMap<String, Counter>();
        var experimentInstances = new HashMap<String, int[]>();
        for (var entry : instances.values()) {
            if (entry.finalized) completedInstances++;
            if (entry.counter.running > 0) runningInstances++;
            var exp = experiments.computeIfAbsent(entry.experiment, ignored -> new Counter());
            exp.merge(entry.counter);
            var counts = experimentInstances.computeIfAbsent(entry.experiment, ignored -> new int[2]);
            counts[0]++; if (entry.finalized) counts[1]++;
            for (var alg : entry.algorithms.values()) {
                algorithms++; if (alg.finalized) completedAlgorithms++; if (alg.counter.running > 0) runningAlgorithms++;
            }
        }
        var summaries = new ArrayList<ExperimentSummary>();
        for (var entry : experiments.entrySet()) {
            var counts = experimentInstances.get(entry.getKey());
            summaries.add(new ExperimentSummary(entry.getKey(), entry.getValue().view(), counts[0], counts[1]));
        }
        return new Status(runId, revision, phase, startedAt, endedAt, runId == null ? 0 : (endedAt == null ? System.currentTimeMillis() : endedAt) - startedAt,
                currentExperiment, Collections.unmodifiableMap(new LinkedHashMap<>(objectives)), totals.view(), instances.size(), completedInstances,
                runningInstances, algorithms, completedAlgorithms, runningAlgorithms, List.copyOf(summaries), issues.size());
    }

    public synchronized List<WorkUnitResult<S, I>> orderedResults(String experiment) {
        if (runId == null) return resultStore.getResultsForExperiment(experiment);
        var result = new ArrayList<WorkUnitResult<S, I>>();
        for (var entry : work.values()) if (entry.instance.experiment.equals(experiment)) {
            resultStore.findResult(entry.id).ifPresent(result::add);
        }
        return List.copyOf(result);
    }

    public synchronized void algorithmFinalized(String experiment, String path, String algorithm) {
        var instance = instanceIndex.get(new InstanceKey(experiment, path));
        if (instance != null) { instance.algorithms.get(algorithm).finalized = true; instance.revision = ++revision; }
    }

    public synchronized Page<InstanceSummary> instances(String expectedRun, String experiment, String search, int offset, int limit) {
        requireRun(expectedRun);
        var items = new ArrayList<InstanceSummary>();
        for (var entry : instances.values()) {
            if (experiment != null && !experiment.isBlank() && !entry.experiment.equals(experiment)) continue;
            if (search != null && !search.isBlank() && !entry.name.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) continue;
            items.add(summary(entry));
        }
        return page(items, offset, limit);
    }

    public synchronized InstanceDetail instance(String expectedRun, String id, String objective) {
        requireRun(expectedRun);
        var instance = instances.get(id);
        if (instance == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown instance");
        if (!objectives.containsKey(objective)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown objective");
        var algorithms = new ArrayList<AlgorithmSummary>();
        var completed = new ArrayList<WorkEntry>();
        for (var alg : instance.algorithms.values()) {
            algorithms.add(alg.cached.computeIfAbsent(objective, ignored -> algorithmSummary(instance, alg, objective)));
            for (var item : alg.work) if (item.state == WorkState.SUCCEEDED || item.state == WorkState.FAILED) completed.add(item);
        }
        int resultCount = completed.size();
        var results = new ArrayList<ResultView>();
        for (var entry : sample(completed, objective, Math.max(5, 4000 / Math.max(1, algorithms.size())))) results.add(resultView(entry));
        return new InstanceDetail(runId, instance.revision, summary(instance), objective, List.copyOf(algorithms), List.copyOf(results), resultCount, results.size() < resultCount);
    }

    /** First/last and score extrema per bucket, retaining failures as visible gaps. Statistics remain exact. */
    private ArrayList<WorkEntry> sample(List<WorkEntry> results, String objective, int perAlgorithm) {
        var grouped = new LinkedHashMap<String, List<WorkEntry>>();
        for (var result : results) grouped.computeIfAbsent(result.algorithm.name, ignored -> new ArrayList<>()).add(result);
        var sampled = new ArrayList<WorkEntry>();
        for (var rows : grouped.values()) {
            if (rows.size() <= perAlgorithm) { sampled.addAll(rows); continue; }
            int bucketSize = Math.max(1, (int) Math.ceil(rows.size() / (double) Math.max(1, perAlgorithm / 5)));
            for (int from = 0; from < rows.size(); from += bucketSize) {
                int to = Math.min(rows.size(), from + bucketSize), min = from, max = from, failure = -1;
                for (int i = from; i < to; i++) {
                    var row = rows.get(i); var value = objectiveValue(row, objective);
                    if (row.state == WorkState.FAILED && failure == -1) failure = i;
                    if (value == null) continue;
                    var low = objectiveValue(rows.get(min), objective); var high = objectiveValue(rows.get(max), objective);
                    if (low == null || value < low) min = i; if (high == null || value > high) max = i;
                }
                var indices = new TreeSet<Integer>(List.of(from, to - 1, min, max));
                if (failure >= 0) indices.add(failure);
                for (var index : indices) sampled.add(rows.get(index));
            }
        }
        return sampled;
    }

    private Double objectiveValue(WorkEntry entry, String objective) {
        var value = resultStore.findResult(entry.id).orElseThrow().objectives().get(objective);
        return value != null && Double.isFinite(value) ? value : null;
    }

    private AlgorithmSummary algorithmSummary(InstanceEntry instance, AlgorithmEntry algorithm, String objective) {
        var scores = algorithm.scores.getOrDefault(objective, new StatisticsUtil.Accumulator());
        return new AlgorithmSummary(algorithm.name, algorithm.counter.view(), scores.summary(objectives.get(objective), instance.references.get(objective)),
                algorithm.runtimes.summary(FMode.MINIMIZE, null), algorithm.bestTimes.summary(FMode.MINIMIZE, null), algorithm.best.get(objective));
    }

    public synchronized Page<ResultView> results(String expectedRun, String instanceId, String algorithm, WorkState state, String search, ResultOrder order, int offset, int limit) {
        requireRun(expectedRun);
        validatePage(offset, limit);
        var items = new ArrayList<ResultView>();
        int total = 0;
        for (var entry : matchingWork(instanceId, algorithm, state, search, order)) {
            if (total >= offset && items.size() < limit) items.add(resultView(entry));
            total++;
        }
        return new Page<>(runId, revision, total, offset, limit, List.copyOf(items));
    }

    public synchronized List<ResultView> exportResults(String expectedRun, String instanceId, String algorithm, WorkState state, String search, ResultOrder order) {
        requireRun(expectedRun);
        var rows = new ArrayList<ResultView>();
        for (var entry : matchingWork(instanceId, algorithm, state, search, order)) rows.add(resultView(entry));
        return List.copyOf(rows);
    }

    private List<WorkEntry> matchingWork(String instanceId, String algorithm, WorkState state, String search, ResultOrder order) {
        var matches = new ArrayList<WorkEntry>();
        var source = new ArrayList<WorkEntry>();
        if (instanceId != null && !instanceId.isBlank()) {
            var instance = instances.get(instanceId);
            if (instance == null) return matches;
            for (var alg : instance.algorithms.values()) source.addAll(alg.work);
        } else source.addAll(work.values());
        String query = search == null ? "" : search.toLowerCase(Locale.ROOT);
        for (var entry : source) {
            if (algorithm != null && !algorithm.isBlank() && !entry.algorithm.name.equals(algorithm)) continue;
            if (state != null && entry.state != state) continue;
            if (!query.isBlank() && !(entry.instance.experiment + " " + entry.instance.name + " " + entry.algorithm.name).toLowerCase(Locale.ROOT).contains(query)) continue;
            matches.add(entry);
        }
        Comparator<WorkEntry> comparator = switch (order) {
            case PLANNED -> null;
            case INSTANCE -> Comparator.comparing(entry -> entry.instance.name);
            case ALGORITHM -> Comparator.comparing(entry -> entry.algorithm.name);
            case STATE -> Comparator.comparing(entry -> entry.state);
            case RUNTIME -> Comparator.comparing(entry -> {
                var result = resultStore.findResult(entry.id).orElse(null);
                return result == null || result.executionTime() < 0 ? null : result.executionTime();
            }, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        // List.sort is stable: equal keys retain planned order across pages and exports.
        if (comparator != null) matches.sort(comparator);
        return matches;
    }

    public synchronized ResultDetail result(String expectedRun, UUID id) {
        requireRun(expectedRun);
        var entry = work.get(id);
        if (entry == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown result");
        var relatedIssues = new ArrayList<Issue>();
        for (var issue : issues) if (id.equals(issue.resultId())) relatedIssues.add(issue);
        return new ResultDetail(runId, revision, resultView(entry), renderings.getOrDefault(id, new Rendering(
                renderer.getIfAvailable() == null ? "UNSUPPORTED" : "UNAVAILABLE", null, Map.of())), List.copyOf(relatedIssues));
    }

    public synchronized Page<Issue> issues(String expectedRun, String severity, int offset, int limit) {
        requireRun(expectedRun);
        var filtered = new ArrayList<Issue>();
        for (int i = issues.size() - 1; i >= 0; i--) {
            var item = issues.get(i);
            if (severity == null || severity.isBlank() || severity.equals(item.severity())) filtered.add(item);
        }
        return page(filtered, offset, limit);
    }

    public synchronized void requireRun(String expected) {
        if (runId == null || !runId.equals(expected)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Run changed; refresh status");
    }

    private <T> Page<T> page(List<T> items, int offset, int limit) {
        validatePage(offset, limit);
        int start = Math.min(offset, items.size());
        return new Page<>(runId, revision, items.size(), offset, limit, List.copyOf(items.subList(start, Math.min(items.size(), start + limit))));
    }

    private static void validatePage(int offset, int limit) {
        if (offset < 0 || limit < 1 || limit > 1000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page; limit must be 1..1000");
    }

    private InstanceSummary summary(InstanceEntry entry) {
        int finished = 0; for (var alg : entry.algorithms.values()) if (alg.finalized) finished++;
        return new InstanceSummary(entry.id, entry.experiment, entry.name, entry.counter.view(), entry.references, entry.algorithms.size(), finished, entry.finalized, entry.revision);
    }

    private ResultView resultView(WorkEntry entry) {
        var result = resultStore.findResult(entry.id).orElse(null);
        var finite = new LinkedHashMap<String, Double>();
        if (result != null) for (var item : result.objectives().entrySet()) if (Double.isFinite(item.getValue())) finite.put(item.getKey(), item.getValue());
        return new ResultView(entry.id, entry.instance.id, entry.instance.experiment, entry.instance.name, entry.algorithm.name,
                entry.iteration + 1, result == null ? String.valueOf(entry.iteration) : result.iteration(), entry.seed, entry.state, Collections.unmodifiableMap(finite),
                result == null ? Map.of() : result.solutionProperties(), result == null || result.executionTime() < 0 ? null : result.executionTime(),
                result == null || result.timeToTarget() < 0 ? null : result.timeToTarget(), entry.startedAt, entry.endedAt);
    }

    private void transition(WorkEntry entry, WorkState next) {
        totals.add(entry.state, -1); entry.instance.counter.add(entry.state, -1); entry.algorithm.counter.add(entry.state, -1);
        totals.add(next, 1); entry.instance.counter.add(next, 1); entry.algorithm.counter.add(next, 1);
        entry.state = next; entry.instance.revision = ++revision;
        entry.algorithm.cached.clear();
    }

    private record InstanceKey(String experiment, String path) {}
    private static class Counter {
        long pending, running, succeeded, failed, cancelled;
        void add(WorkState state, int amount) { switch (state) {
            case PENDING -> pending += amount; case RUNNING -> running += amount; case SUCCEEDED -> succeeded += amount;
            case FAILED -> failed += amount; case CANCELLED -> cancelled += amount;
        }}
        void merge(Counter other) { pending += other.pending; running += other.running; succeeded += other.succeeded; failed += other.failed; cancelled += other.cancelled; }
        Counts view() { return new Counts(pending + running + succeeded + failed + cancelled, pending, running, succeeded, failed, cancelled); }
    }
    private static class InstanceEntry {
        final String id, experiment; String name; long revision; boolean finalized;
        Map<String, Double> references = Map.of(); final Counter counter = new Counter(); final Map<String, AlgorithmEntry> algorithms = new LinkedHashMap<>();
        InstanceEntry(String id, String experiment, String path) { this.id = id; this.experiment = experiment; this.name = org.apache.commons.io.FilenameUtils.getName(path); }
    }
    private static class AlgorithmEntry {
        final String name; boolean finalized; final Counter counter = new Counter(); final List<WorkEntry> work = new ArrayList<>();
        final Map<String, UUID> best = new HashMap<>(); final Map<String, AlgorithmSummary> cached = new HashMap<>();
        final Map<String, StatisticsUtil.Accumulator> scores = new HashMap<>();
        final StatisticsUtil.Accumulator runtimes = new StatisticsUtil.Accumulator(), bestTimes = new StatisticsUtil.Accumulator();
        AlgorithmEntry(String name) { this.name = name; }
    }
    private static class WorkEntry {
        final UUID id; final InstanceEntry instance; final AlgorithmEntry algorithm; final int iteration; final long seed;
        WorkState state = WorkState.PENDING; Long startedAt, endedAt;
        WorkEntry(UUID id, InstanceEntry instance, AlgorithmEntry algorithm, int iteration, long seed) { this.id = id; this.instance = instance; this.algorithm = algorithm; this.iteration = iteration; this.seed = seed; }
    }
}
