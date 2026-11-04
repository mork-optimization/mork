package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.FMode;
import tools.jackson.databind.JsonNode;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/** Reporting reads saved evidence only; it never loads generated classes or original instances. */
public final class AutoLlmPortableReport {
    private AutoLlmPortableReport() {}
    public record Selection(String objective, FMode mode) {
        public Selection {
            if (objective != null && objective.isBlank()) objective = null;
            if ((objective == null) != (mode == null)) throw new IllegalArgumentException("Select both objective and direction");
        }
    }
    public record Component(String name, String role, String description, boolean verified, boolean selected) {}
    public record Interval(String name, Long millis) {}
    public record Attempt(AutoLlmCampaign.Attempt saved, Long millis, List<Interval> substages,
                          JsonNode manifest, JsonNode usage, JsonNode model) {}
    public record Trial(AutoLlmCampaign.Trial saved, Long elapsed, long attemptMillis, int unknownDurations,
                        List<Interval> stages, List<Attempt> attempts, List<Component> components, Object winner,
                        AutoLlmComparisonReport.Run comparison, List<AutoLlmComparisonUtil.MeasuredInstance> partial,
                        String exclusion, List<String> warnings, List<String> evidence) {}
    public record Snapshot(AutoLlmCampaign campaign, String at, Selection selection, Map<String,FMode> objectives,
                           Long elapsed, String durationBasis, long attemptMillis, Map<String,Integer> statuses, Map<String,Integer> datasetSizes, JsonNode datasetOrigin,
                           int includedTrials, int wins, int ties, int losses, List<Trial> trials, List<String> warnings) {}
    private record Transition(String stage, Long at) {}

    static JsonNode read(Path file, List<String> warnings) {
        if (!AutoLlmEvidenceFiles.exists(file)) return null;
        try { return AutoLlmFileUtil.read(file, JsonNode.class); }
        catch (RuntimeException e) { warnings.add("Cannot read " + file.getFileName() + ": " + e.getMessage()); return null; }
    }
    static Long instant(String value) {
        try { return value == null ? null : Instant.parse(value).toEpochMilli(); }
        catch (DateTimeException e) { return null; }
    }
    static Long duration(Long start, Long end) { return start == null || end == null || end < start ? null : end - start; }
    private static Long min(Long a, Long b) { if (a == null) return b; if (b == null) return a; return Math.min(a,b); }
    private static Long max(Long a, Long b) { if (a == null) return b; if (b == null) return a; return Math.max(a,b); }
    private static Path path(Path root, String relative) { return AutoLlmFileUtil.inside(root, relative); }

    public static Map<String,FMode> objectives(AutoLlmCampaign campaign, Path root) {
        var result = new TreeMap<String,FMode>();
        for (var metadata : metadata(campaign, root)) {
            var node = read(metadata, new ArrayList<>());
            if (node != null) for (var field : node.path("objectives").properties()) {
                FMode mode = FMode.valueOf(field.getValue().asString());
                if (result.containsKey(field.getKey()) && result.get(field.getKey()) != mode)
                    throw new IllegalStateException("Conflicting saved objective directions: " + field.getKey());
                result.put(field.getKey(), mode);
            }
        }
        return result;
    }
    private static List<Path> metadata(AutoLlmCampaign c, Path root) {
        var paths = new ArrayList<Path>();
        if (c.localPreparation.directory != null) paths.add(path(root, c.localPreparation.directory + "/baseline-preflight/check/execution/reporting-metadata.json"));
        for (var target : c.targets.values()) if (target.directory != null)
            paths.add(path(root, target.directory + "/baseline-preflight/check/execution/reporting-metadata.json"));
        return paths;
    }
    public static Selection selection(AutoLlmCampaign c, Path root, Selection requested) {
        if (requested != null && requested.objective() != null) return requested;
        Path settings = root.resolve("reports/report-settings.json");
        if (Files.isRegularFile(settings)) return AutoLlmFileUtil.read(settings, Selection.class);
        var primary = new TreeSet<String>(); var modes = objectives(c, root);
        for (var file : metadata(c, root)) {
            var node = read(file, new ArrayList<>());
            if (node != null && node.path("primaryObjective").isString()) primary.add(node.path("primaryObjective").asString());
        }
        if (primary.size() == 1 && modes.containsKey(primary.first())) return new Selection(primary.first(), modes.get(primary.first()));
        return new Selection(null, null);
    }

    public static Snapshot build(AutoLlmCampaign c, Path root, Selection selection, Instant at) {
        var warnings = new ArrayList<String>();
        var transitions = transitions(root, warnings);
        var cache = new AutoLlmComparisonUtil.Cache();
        var trials = new ArrayList<Trial>(); var statuses = new TreeMap<String,Integer>();
        int included = 0, wins = 0, ties = 0, losses = 0; long effort = 0;
        Long first = null, last = null;
        for (var trial : c.trials) {
            var report = trial(c, trial, root, selection, at, transitions, cache);
            trials.add(report); statuses.merge(trial.status.name(), 1, Integer::sum); effort += report.attemptMillis();
            for (var attempt : trial.attempts) { first = min(first, instant(attempt.startedAt)); last = max(last, instant(attempt.finishedAt)); }
            if (report.comparison() != null) { included++; wins += report.comparison().wins(); ties += report.comparison().ties(); losses += report.comparison().losses(); }
        }
        trials.sort(Comparator.comparingInt(trial -> trial.saved().number));
        Long start = instant(c.startedAt), end = instant(c.stoppedAt);
        boolean approximate = start == null || end == null && !c.status.inProgress();
        if (start == null) start = first;
        if (end == null) {
            if (c.status.inProgress()) end = at.toEpochMilli();
            else end = last;
        }
        if (selection.objective() == null) warnings.add("No saved primary objective. Select an objective and direction to calculate comparison statistics.");
        return new Snapshot(c, at.toString(), selection, objectives(c, root), duration(start, end),
                approximate ? "Approximate span of recorded trial attempts; setup and unrecorded waits may be missing"
                        : "Campaign execution wall clock, including waits and pauses; excludes earlier screening and draft preparation",
                effort, statuses, datasetSizes(c, root), read(root.resolve("control/dataset-origin.json"), warnings), included, wins, ties, losses, trials, warnings);
    }

    private static Map<String,Integer> datasetSizes(AutoLlmCampaign c, Path root) {
        var sizes = new LinkedHashMap<String,Integer>();
        var directories = new ArrayList<Path>(); directories.add(root);
        if (c.localPreparation.directory != null) directories.add(path(root, c.localPreparation.directory));
        for (var target : c.targets.values()) if (target.directory != null) directories.add(path(root, target.directory));
        for (var trial : c.trials) if (trial.executionDirectory() != null) directories.add(path(root, trial.executionDirectory()));
        for (Path directory : directories) {
            var training = read(directory.resolve("data/training-manifest.json"), new ArrayList<>());
            var comparison = read(directory.resolve("data/comparison-manifest.json"), new ArrayList<>());
            if (training != null && training.isObject()) sizes.putIfAbsent("training", training.size());
            if (comparison != null && comparison.isObject()) sizes.putIfAbsent("comparison", comparison.size());
            if (training != null && comparison != null && training.isObject() && comparison.isObject()) {
                int overlap = 0;
                for (var input : training.properties()) if (comparison.has(input.getKey())) overlap++;
                sizes.put("overlap", overlap); break;
            }
        }
        return sizes;
    }

    private static Trial trial(AutoLlmCampaign c, AutoLlmCampaign.Trial t, Path root, Selection selection, Instant at,
                               Map<String,List<Transition>> transitions, AutoLlmComparisonUtil.Cache cache) {
        var warnings = new ArrayList<String>(); var evidence = new ArrayList<String>(); var attempts = new ArrayList<Attempt>();
        var stageTimes = new LinkedHashMap<String,Long>(); var unknownStages = new HashSet<String>();
        long sum = 0; int unknown = 0; Long first = null, last = null;
        JsonNode manifest = null; String generation = t.outputDirectory(AutoLlmCampaign.Stage.GENERATION);
        for (var a : t.attempts) {
            Long start = instant(a.startedAt), end = instant(a.finishedAt);
            if (end == null && a.status == AutoLlmCampaign.AttemptStatus.RUNNING && c.status.inProgress() && !t.status.terminal()) end = at.toEpochMilli();
            Long elapsed = duration(start, end);
            if (elapsed != null) { first = min(first, start); last = max(last, end); }
            stageTimes.putIfAbsent(a.stage.name(), 0L);
            if (elapsed == null) { unknown++; unknownStages.add(a.stage.name()); }
            else { sum += elapsed; stageTimes.merge(a.stage.name(), elapsed, Long::sum); }
            Path dir = path(root, a.directory);
            JsonNode entry = a.stage.generation() ? read(dir.resolve("manifest.json"), warnings) : null;
            if (entry != null && (generation == null || a.directory.equals(generation))) manifest = entry;
            var subtimes = new ArrayList<Interval>(); var events = transitions.getOrDefault(a.directory, List.of());
            for (int i = 0; i < events.size(); i++) {
                var event = events.get(i); Long stop = i+1 < events.size() ? events.get(i+1).at() : end;
                Long millis = duration(event.at(), stop);
                if (start == null || event.at() == null || event.at() < start || end != null && stop != null && stop > end) millis = null;
                subtimes.add(new Interval(event.stage(), millis));
            }
            attempts.add(new Attempt(a, elapsed, subtimes, entry, a.stage.generation() ? read(dir.resolve("usage.json"), warnings) : null,
                    a.stage.generation() ? read(dir.resolve("model-selection.json"), warnings) : null));
            evidence.add(a.directory);
        }
        if (c.status.inProgress() && !t.status.terminal() && t.status != AutoLlmCampaign.TrialStatus.PENDING && first != null) last = at.toEpochMilli();
        var stages = new ArrayList<Interval>();
        for (var entry : stageTimes.entrySet()) stages.add(new Interval(entry.getKey(), unknownStages.contains(entry.getKey()) ? null : entry.getValue()));
        Object winner = t.winner;
        String winnerDir = t.outputDirectory(AutoLlmCampaign.Stage.WINNER_EXPORT);
        if (winner == null && winnerDir != null) winner = read(path(root, winnerDir + "/execution/winner.json"), warnings);
        var components = components(t, root, manifest, winner, warnings);
        var partial = new ArrayList<AutoLlmComparisonUtil.MeasuredInstance>();
        AutoLlmComparisonReport.Run comparison = null; String exclusion = null;
        String comparisonDir = t.latestDirectory(AutoLlmCampaign.Stage.COMPARISON);
        if (selection.objective() == null) exclusion = "Objective and direction are not selected";
        else if (comparisonDir == null) exclusion = "Comparison has not started";
        else {
            String relative = comparisonDir + "/execution/events.jsonl"; Path journal = path(root, relative); evidence.add(relative);
            try {
                if (!AutoLlmEvidenceFiles.exists(journal)) throw new IllegalStateException("Comparison evidence is not collected");
                partial.addAll(cache.measured(journal, selection.objective(), selection.mode()));
                if (cache.omittedEvents(journal) != 0) throw new IllegalStateException("Comparison journal contains omitted or malformed events");
                if (t.status != AutoLlmCampaign.TrialStatus.SUCCEEDED) throw new IllegalStateException("Trial is " + t.status + "; partial results excluded from headline statistics");
                var expected = expectedInstances(root, t);
                var local = AutoLlmPoolUtil.localInstances(c, root, t);
                var seen = new HashSet<String>();
                for (var row : partial) {
                    String identity = expected.get(local.apply(row.instance()));
                    if (identity == null || !seen.add(identity)) throw new IllegalStateException("Comparison instances differ from the frozen approved set");
                }
                var identities = new HashSet<>(expected.values());
                if (!seen.equals(identities)) throw new IllegalStateException("Missing approved comparison instances");
                comparison = AutoLlmComparisonReport.run(t.number, t.targetId, c.config.comparison().repetitions(), identities.size(), relative, partial, selection.mode());
            } catch (RuntimeException e) { exclusion = e.getMessage(); }
        }
        if (unknown > 0) warnings.add(unknown + " attempt duration(s) unavailable; the summed duration includes known intervals only");
        if (t.executionDirectory() != null) for (String name : List.of("frozen/source-snapshot.json", "frozen/artifact.json", "frozen/source-hashes.json")) {
            Path file = path(root, t.executionDirectory() + "/" + name);
            var node = read(file, warnings); if (node != null) evidence.add(t.executionDirectory() + "/" + name + " · " + AutoLlmFileUtil.hash(file));
        }
        return new Trial(t, duration(first, last), sum, unknown, stages, attempts, components, winner, comparison, partial, exclusion, warnings, evidence);
    }

    /** Alias to frozen identity; reading this never opens original instance files. */
    static Map<String,String> expectedInstances(Path root, AutoLlmCampaign.Trial t) {
        if (t.executionDirectory() == null) throw new IllegalStateException("Execution assignment is unavailable");
        Path file = path(root, t.executionDirectory() + "/data/comparison-manifest.json");
        if (!AutoLlmEvidenceFiles.exists(file)) throw new IllegalStateException("Frozen comparison membership is unavailable");
        var result = new LinkedHashMap<String,String>();
        var manifest = AutoLlmFileUtil.read(file, JsonNode.class);
        for (var field : manifest.properties()) {
            result.put(field.getKey(), field.getKey());
            String local = field.getValue().path("path").asString("");
            if (!local.isBlank()) result.put(local, field.getKey());
        }
        if (result.isEmpty()) throw new IllegalStateException("Frozen comparison membership is empty");
        return result;
    }

    private static List<Component> components(AutoLlmCampaign.Trial trial, Path root, JsonNode manifest, Object winner, List<String> warnings) {
        var names = new TreeSet<String>(); var verified = new HashSet<String>();
        String directory = trial.executionDirectory();
        if (directory != null) {
            var frozen = read(path(root, directory + "/frozen/components.json"), warnings);
            var baseline = read(path(root, directory + "/baseline/components.json"), warnings);
            if (frozen != null && baseline != null) {
                for (var item : frozen) names.add(item.asString());
                for (var item : baseline) names.remove(item.asString());
                verified.addAll(names);
            }
        }
        var catalog = new LinkedHashMap<String,JsonNode>(); var prose = new ArrayList<String>();
        if (manifest != null) {
            for (var entry : manifest.path("generatedComponents")) { String name = entry.path("className").asString(""); if (!name.isBlank()) { catalog.put(name, entry); names.add(name); } }
            for (String key : List.of("components", "metaheuristics")) for (var entry : manifest.path(key)) prose.add(entry.asString());
        }
        var selected = new HashSet<String>(); winnerComponents(AutoLlmFileUtil.JSON.valueToTree(winner), selected);
        var result = new ArrayList<Component>();
        for (String name : names) {
            var entry = catalog.get(name); String description = entry == null ? null : entry.path("description").asString(null);
            String simple = name.substring(name.lastIndexOf('.')+1);
            if (description == null) {
                var matches = new ArrayList<String>();
                for (String paragraph : prose) if (java.util.regex.Pattern.compile("(?<![\\w$])" + java.util.regex.Pattern.quote(simple) + "(?![\\w$])").matcher(paragraph).find()) matches.add(paragraph);
                if (matches.size() == 1) description = matches.getFirst();
            }
            result.add(new Component(name, entry == null ? "Unavailable" : entry.path("role").asString("Unavailable"),
                    description == null ? "Description unavailable in saved evidence" : description, verified.contains(name), selected.contains(name) || selected.contains(simple)));
        }
        if (manifest != null && names.isEmpty()) warnings.add("No structured or verified component inventory; original manifest descriptions appear under generation attempts");
        return result;
    }
    static void winnerComponents(JsonNode node, Set<String> result) {
        if (node == null) return;
        if (node.isObject()) for (var field : node.properties()) {
            if ((field.getKey().equals("$component") || field.getKey().equals("component")) && field.getValue().isString()) result.add(field.getValue().asString());
            else if (field.getKey().equals("_type") && field.getValue().isString()) result.add(field.getValue().asString());
            else winnerComponents(field.getValue(), result);
        }
        else if (node.isArray()) for (var item : node) winnerComponents(item, result);
    }
    private static Map<String,List<Transition>> transitions(Path root, List<String> warnings) {
        var result = new HashMap<String,List<Transition>>(); Path journal = root.resolve("campaign-events.jsonl");
        if (!AutoLlmEvidenceFiles.exists(journal)) return result;
        try {
            AutoLlmFileUtil.forEachEvent(journal, row -> {
                if (!"preparation-stage".equals(row.get("kind"))) return;
                var detail = AutoLlmFileUtil.objectMap(row.get("detail"));
                if (detail.containsKey("targetId")) detail = AutoLlmFileUtil.objectMap(detail.get("detail"));
                if (detail.get("attempt") instanceof String directory)
                    result.computeIfAbsent(directory, _ -> new ArrayList<>()).add(new Transition(Objects.toString(detail.get("stage")), instant(Objects.toString(row.get("timestamp"), null))));
            });
        } catch (RuntimeException e) { warnings.add("Preparation timing journal is incomplete: " + e.getMessage()); }
        return result;
    }
}
