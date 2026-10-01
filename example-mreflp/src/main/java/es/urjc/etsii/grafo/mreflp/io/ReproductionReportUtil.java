package es.urjc.etsii.grafo.mreflp.io;

import es.urjc.etsii.grafo.mreflp.alg.LMLSVariant;
import es.urjc.etsii.grafo.mreflp.experiments.PaperExperiment;
import es.urjc.etsii.grafo.mreflp.model.*;
import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Reads raw instances and validates exported assignments independently of move caches. */
public final class ReproductionReportUtil {
    private static final Map<String, Number> PAPER_PARAMETERS = PaperExperiment.paperAlgorithm(LMLSVariant.LMLS, 0).parameters();
    public record ReportResult(int validRuns, int invalidRuns, int missingPaperRuns, boolean complete) {}
    private record Cohort(String caseId, String algorithm, String protocol) {}
    private static final class Summary {
        int cases, incomplete, wins, ties, losses, missing, flagged, optima;
        int gapCount;
        double gap;
    }
    private ReproductionReportUtil() {}

    public static void validate(RunRecord run, MREFLPInstance instance) {
        if (run.formatVersion() != 1 || instance == null || !instance.getId().equals(run.caseId())
                || !instance.sourceHash().equals(run.sourceHash())) throw new IllegalArgumentException("Unknown case or changed source hash");
        if (run.assignments() == null || run.assignments().length != instance.n()) throw new IllegalArgumentException("Wrong assignment length");
        var solution = new MREFLPSolution(instance);
        for (int v = 0; v < instance.n(); v++) solution.assign(v, run.assignments()[v]);
        if (solution.recalculateCost() != run.cost()) throw new IllegalArgumentException("Incorrect exported cost");
        if (run.cost() < 0 || run.runtimeNanos() < 0 || run.timeToBestNanos() < 0
                || run.timeToBestNanos() > run.runtimeNanos() || !Double.isFinite(run.budgetSeconds())
                || run.budgetSeconds() <= 0 || run.maxRestarts() < 0 || run.parameters() == null
                || run.algorithm() == null || run.protocol() == null || run.randomType() == null
                || run.artifactHash() == null) throw new IllegalArgumentException("Invalid run metadata");
        validateParameters(run.parameters());
    }

    private static void validateParameters(Map<String, Number> parameters) {
        if (!parameters.keySet().equals(PAPER_PARAMETERS.keySet())) {
            throw new IllegalArgumentException("Invalid parameter metadata");
        }
        for (Number value : parameters.values()) {
            if (value == null) throw new IllegalArgumentException("Missing parameter value");
        }
        double epsilon = parameters.get("epsilon").doubleValue();
        if (!(epsilon >= 0 && epsilon <= 1)) throw new IllegalArgumentException("Invalid epsilon");
        for (String name : List.of("maxIter", "tenure")) {
            double value = parameters.get(name).doubleValue();
            if (!(value >= 1 && value <= Integer.MAX_VALUE && value == Math.rint(value))) {
                throw new IllegalArgumentException("Invalid " + name);
            }
        }
        for (String name : List.of("alpha", "beta", "gamma", "rho")) {
            double value = parameters.get(name).doubleValue();
            if (!(value > 0 && value < 1)) throw new IllegalArgumentException("Invalid " + name);
        }
    }

    public static boolean isPaperRun(RunRecord r) {
        boolean seed = r.protocol().equals("normal") && r.seed() == 1234
                || r.protocol().equals("relaxed") && r.seed() >= 1235 && r.seed() <= 1244;
        return seed && r.algorithm().equals("LMLS") && r.budgetSeconds() == 600
                && r.maxRestarts() == 0 && PAPER_PARAMETERS.equals(r.parameters())
                && r.randomType().equals("Xoroshiro128PlusPlus") && r.warmedUp()
                && r.warmupRepetitions() == 5 && r.warmupMillis() == 1000
                && r.artifactHash().matches("[0-9a-f]{64}") && r.runtimeNanos() >= 590_000_000_000L;
    }

    public static ReportResult report(Path root, Path output, List<Path> inputs) throws IOException {
        var mapper = JsonMapper.builder().build();
        var instances = new TreeMap<String, MREFLPInstance>();
        for (String category : BenchmarkPreparationUtil.CATEGORIES) {
            try (var files = Files.newDirectoryStream(root.resolve("instances").resolve(category), "*.txt")) {
                for (Path file : files) for (int r = 2; r <= 5; r++) {
                    var instance = MREFLPInstanceUtil.read(file, r);
                    if (instances.put(instance.getId(), instance) != null) throw new IllegalArgumentException("Duplicate case");
                }
            }
        }
        var references = new HashMap<String, List<PublishedReference>>();
        for (var r : WorkbookUtil.read(root.resolve("results/results.xlsx"))) {
            references.computeIfAbsent(r.caseId(), ignored -> new ArrayList<>()).add(r);
        }
        var files = new TreeSet<Path>();
        for (Path input : inputs) {
            if (!Files.isDirectory(input)) throw new IllegalArgumentException("Missing input directory: " + input);
            try (var paths = Files.walk(input)) {
                var iterator = paths.iterator();
                while (iterator.hasNext()) {
                    Path path = iterator.next();
                    if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".run.json")) files.add(path.toRealPath());
                }
            }
        }
        var cohorts = new LinkedHashMap<Cohort, List<RunRecord>>();
        var seen = new HashSet<String>();
        var paperRuns = new HashSet<String>();
        var artifacts = new TreeSet<String>();
        var environments = new TreeSet<String>();
        var invalid = new ArrayList<String>();
        var warnings = new ArrayList<String>();
        int valid = 0;
        for (Path path : files) {
            RunRecord r;
            try {
                r = mapper.readValue(path.toFile(), RunRecord.class);
                validate(r, instances.get(r.caseId()));
            } catch (RuntimeException e) {
                invalid.add(path + ": " + e.getMessage());
                continue;
            }
            String key = key(r.caseId(), r.algorithm(), r.protocol(), r.seed());
            if (!seen.add(key)) throw new IllegalArgumentException("Duplicate run identity: " + key + " at " + path);
            valid++;
            cohorts.computeIfAbsent(new Cohort(r.caseId(), r.algorithm(), r.protocol()), ignored -> new ArrayList<>()).add(r);
            artifacts.add(r.artifactHash());
            environments.add(r.javaVersion() + " / " + r.javaVm() + " / " + r.os() + " / " + r.architecture() + " / available CPUs: " + r.availableProcessors());
            if (isPaperRun(r)) paperRuns.add(key);
            else if (r.protocol().equals("normal") || r.protocol().equals("relaxed")) warnings.add("Non-paper configuration or runtime: " + key);
            if (r.runtimeNanos() / 1e9 > r.budgetSeconds() + 10) warnings.add("Budget overrun: " + key);
        }
        Files.createDirectories(output);
        int missing = 0;
        try (var writer = Files.newBufferedWriter(output.resolve("coverage.csv"))) {
            writer.write("caseId,protocol,seed,status\n");
            for (String id : instances.keySet()) for (long seed = 1234; seed <= 1244; seed++) {
                String protocol = seed == 1234 ? "normal" : "relaxed";
                boolean present = paperRuns.contains(key(id, "LMLS", protocol, seed));
                if (!present) missing++;
                writer.write(csv(id, protocol, seed, present ? "complete" : "missing-or-nonpaper"));
            }
        }
        var summaries = new TreeMap<String, Summary>();
        try (var writer = Files.newBufferedWriter(output.resolve("comparisons.csv"))) {
            writer.write("caseId,category,capacity,algorithm,protocol,runs,expectedRuns,bestCost,averageCost,averageRuntimeSeconds,averageTimeToBestSeconds,referenceMethod,referenceCost,referenceAverageCost,referenceTimeToBestSeconds,absoluteGap,percentageGap,status,flagged,referenceLocation\n");
            for (var entry : cohorts.entrySet()) {
                Cohort c = entry.getKey();
                var runs = entry.getValue();
                var instance = instances.get(c.caseId());
                int expected = c.protocol().endsWith("relaxed") ? 10 : 1;
                long best = Long.MAX_VALUE;
                double average = 0, runtime = 0, ttb = 0;
                for (var r : runs) {
                    best = Math.min(best, r.cost());
                    average += r.cost();
                    runtime += r.runtimeNanos() / 1e9;
                    ttb += r.timeToBestNanos() / 1e9;
                }
                average /= runs.size(); runtime /= runs.size(); ttb /= runs.size();
                boolean completeCohort = runs.size() == expected;
                String referenceMethod = c.protocol().endsWith("relaxed") ? "LMLS_relaxed" : "LMLS";
                String summaryKey = c.algorithm() + " / " + c.protocol() + " / " + instance.category() + " / r=" + instance.capacity();
                var summary = summaries.computeIfAbsent(summaryKey, ignored -> new Summary());
                summary.cases++;
                if (!completeCohort) summary.incomplete++;
                boolean primaryFound = false;
                for (var ref : references.getOrDefault(c.caseId(), List.of())) {
                    double gap = best - ref.cost();
                    Double percent = ref.cost() == 0 ? null : 100 * gap / ref.cost();
                    String status = ref.flagged() ? "flagged-reference" : !completeCohort ? "incomplete-cohort" : gap < 0 ? "win" : gap == 0 ? "tie" : "loss";
                    writer.write(csv(c.caseId(), instance.category(), instance.capacity(), c.algorithm(), c.protocol(), runs.size(), expected,
                            best, average, runtime, ttb, ref.method(), ref.cost(), ref.averageCost(), ref.timeToBestSeconds(),
                            gap, percent, status, ref.flagged(), ref.sheet() + "!" + ref.cell()));
                    if (ref.optimal() && ref.method().equals("BKV")) {
                        if (best < ref.cost()) throw new IllegalArgumentException("Cost below marked optimum: " + c.caseId());
                        if (best == ref.cost()) summary.optima++;
                    }
                    if (ref.method().equals(referenceMethod)) {
                        primaryFound = true;
                        if (ref.flagged()) summary.flagged++;
                        else if (completeCohort) {
                            if (gap < 0) summary.wins++;
                            else if (gap == 0) summary.ties++;
                            else summary.losses++;
                            if (percent != null) { summary.gap += percent; summary.gapCount++; }
                        }
                    }
                }
                if (!primaryFound) {
                    summary.missing++;
                    writer.write(csv(c.caseId(), instance.category(), instance.capacity(), c.algorithm(), c.protocol(), runs.size(), expected,
                            best, average, runtime, ttb, referenceMethod, null, null, null, null, null, "missing-reference", false, null));
                }
            }
        }
        boolean complete = missing == 0 && invalid.isEmpty() && warnings.isEmpty() && artifacts.size() == 1;
        var result = new ReportResult(valid, invalid.size(), missing, complete);
        var report = new StringBuilder("# MREFLP reproduction report\n\nStatus: **")
                .append(complete ? "complete paper campaign" : "partial campaign").append("**.\n\n")
                .append(valid).append(" independently validated runs; ").append(paperRuns.size()).append(" of ")
                .append(instances.size() * 11).append(" required paper runs; ").append(missing).append(" missing or non-paper runs; ")
                .append(invalid.size()).append(" invalid checkpoints.\n\n")
                .append("Normal: seed 1234. Relaxed: seeds 1235–1244, best and mean over ten independent runs. Each paper run uses 600 seconds, the paper parameters, and a fresh learning matrix. A partial cohort is excluded from audited win/tie/loss counts. Runtime and time to best are reported separately; the workbook time columns are time to best.\n\n")
                .append("## Audited comparisons with published LMLS\n\n")
                .append("| Algorithm / protocol / category / capacity | Cases | Incomplete | Wins | Ties | Losses | Mean gap % | Missing reference | Flagged reference | Marked optima matched |\n")
                .append("|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n");
        for (var entry : summaries.entrySet()) {
            var s = entry.getValue();
            report.append("| ").append(entry.getKey()).append(" | ").append(s.cases).append(" | ").append(s.incomplete)
                    .append(" | ").append(s.wins).append(" | ").append(s.ties).append(" | ").append(s.losses)
                    .append(" | ").append(s.gapCount == 0 ? "" : String.format(Locale.ROOT, "%.4f", s.gap / s.gapCount))
                    .append(" | ").append(s.missing).append(" | ").append(s.flagged).append(" | ").append(s.optima).append(" |\n");
        }
        report.append("\n## Source and method audit\n\n")
                .append("- 143 raw instances × four capacities = 572 cases; 568 published LMLS reference cases. The four N-15 cases have no workbook references.\n")
                .append("- 289 workbook BKV entries are marked optimal. Matching an optimum is counted separately from matching an LMLS reference.\n")
                .append("- The five Table A.9 sko100 normal LMLS values (109054) conflict with the relaxed best values (189054). The raw values and cells remain in comparisons.csv; flagged values are excluded from audited counts.\n")
                .append("- GRASP Table A.12 E36 is fractional (75542.100000000006), despite integral instance costs; this sixth anomaly is also retained and flagged.\n")
                .append("- Nineteen sko headers omit five unit widths; full matrix rows are validated. Upper triangular matrices are mirrored; symmetric matrices are kept once. Replicate names with identical matrices remain separate cases.\n")
                .append("- Group counts use Anjos et al. (2018), Theorem 3 / Table 1. The paper does not provide a per-case group-count manifest. Seeded uniform ties, random facility order for greedy construction, facility tabu expiration, phase-best aspiration, idle iterations when all feasible moves are tabu, and one smoothing pass without row normalization are explicit implementation conventions.\n")
                .append("- Main LMLS and all six ablations are implemented. This report compares available runs with every available workbook method; external ILP/SDP/AMA/GRASP solvers are published references, not reimplemented baselines.\n\n")
                .append("## Provenance\n\n");
        for (String source : List.of("paper.pdf", "results/results.xlsx", "results/readme.md")) {
            report.append("- ").append(source).append(": ").append(MREFLPInstanceUtil.sha256(root.resolve(source))).append("\n");
        }
        for (String artifact : artifacts) report.append("- Executable SHA-256: ").append(artifact).append("\n");
        for (String environment : environments) report.append("- Environment: ").append(environment).append("\n");
        report.append("\nRetain campaign-manifest.json, failures.jsonl, and invocation logs alongside this report. They record CPU details, revision, launch settings, and interrupted/failed attempts. Different hardware prevents direct speed claims from time-to-best comparisons.\n\n")
                .append("## Invalid checkpoints and protocol warnings\n\n");
        if (invalid.isEmpty() && warnings.isEmpty()) report.append("None.\n");
        for (String message : invalid) report.append("- ").append(message.replace('\n', ' ')).append("\n");
        for (String message : warnings) report.append("- ").append(message).append("\n");
        if (artifacts.size() > 1) report.append("- Multiple executable hashes; review mixed versions before interpreting the campaign.\n");
        report.append("\n## Manual review\n\nReview source anomalies, coverage, assignment validity, gaps, and timing evidence before changing algorithms. A completed campaign does not imply that every stochastic objective matches the paper. No parameter tuning or algorithm changes are performed by this report.\n");
        Files.writeString(output.resolve("report.md"), report);
        mapper.writerWithDefaultPrettyPrinter().writeValue(output.resolve("status.json").toFile(), result);
        return result;
    }

    private static String key(String id, String algorithm, String protocol, long seed) {
        return id + "/" + algorithm + "/" + protocol + "/" + seed;
    }
    public static String csv(Object... values) {
        var result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) result.append(',');
            if (values[i] != null) result.append('"').append(values[i].toString().replace("\"", "\"\"")).append('"');
        }
        return result.append('\n').toString();
    }
}
