package es.urjc.etsii.grafo.mreflp.io;

import es.urjc.etsii.grafo.mreflp.model.MREFLPInstanceUtil;
import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class BenchmarkPreparationUtil {
    public static final List<String> CATEGORIES = List.of("small", "medium", "large", "realworld");
    public record BenchmarkCase(String caseId, String source, String category, int capacity, int groups,
                                int facilities, int missingWidths, String sourceHash) {}
    private BenchmarkPreparationUtil() {}

    public static void prepare(Path root) throws IOException {
        Path directory = root.resolve("benchmark");
        Path instances = root.resolve("instances");
        Files.createDirectories(directory);
        Files.createDirectories(instances.resolve("cases"));
        var cases = new ArrayList<BenchmarkCase>();
        var ids = new HashSet<String>();
        var index = new ArrayList<String>();
        var pilot = new ArrayList<String>();
        Set<String> pilotIds = Set.of("A-10-90-r2", "A-25-90-r3", "AKV70_set1-r4", "tai256c-r5");
        var audit = new StringBuilder("# Source artifact audit\n\n");
        for (String category : CATEGORIES) {
            var sources = new ArrayList<Path>();
            try (var entries = Files.newDirectoryStream(instances.resolve("raw").resolve(category), "*.txt")) {
                for (Path source : entries) sources.add(source);
            }
            Collections.sort(sources);
            for (Path source : sources) {
                for (int r = 2; r <= 5; r++) {
                    var instance = MREFLPInstanceUtil.read(source, r);
                    if (!ids.add(instance.getId())) throw new IllegalArgumentException("Duplicate case " + instance.getId());
                    String relative = root.toAbsolutePath().normalize().relativize(source.toAbsolutePath().normalize()).toString().replace('\\', '/');
                    var benchmarkCase = new BenchmarkCase(instance.getId(), relative, category, r, instance.groups(), instance.n(), instance.missingWidths(), instance.sourceHash());
                    cases.add(benchmarkCase);
                    String filename = instance.getId() + ".case";
                    AtomicFileUtil.write(instances.resolve("cases").resolve(filename), relative + "\n" + r + "\n");
                    index.add("cases/" + filename);
                    if (pilotIds.contains(instance.getId())) pilot.add("cases/" + filename);
                    if (r == 2 && instance.missingWidths() != 0) audit.append("- ").append(relative).append(": ").append(instance.missingWidths()).append(" missing unit-width entries; full matrix verified.\n");
                }
            }
        }
        var references = WorkbookUtil.read(root.resolve("results/results.xlsx"));
        var covered = new HashSet<String>();
        int optimal = 0, flagged = 0;
        for (var reference : references) {
            if (!ids.contains(reference.caseId())) throw new IllegalArgumentException("Reference without instance " + reference.caseId());
            if (reference.method().equals("LMLS")) covered.add(reference.caseId());
            if (reference.optimal() && reference.method().equals("BKV")) optimal++;
            if (reference.flagged()) {
                flagged++;
                audit.append("- Suspect reference ").append(reference.caseId()).append(" ").append(reference.method()).append(": ")
                        .append(reference.cost()).append(" at ").append(reference.sheet()).append("!").append(reference.cell()).append("; retained unchanged.\n");
            }
        }
        for (var benchmarkCase : cases) if (!covered.contains(benchmarkCase.caseId())) audit.append("- Missing published reference: ").append(benchmarkCase.caseId()).append(".\n");
        audit.append("\n").append(cases.size()).append(" cases, ").append(covered.size()).append(" published LMLS references, ")
                .append(optimal).append(" marked optima, ").append(flagged).append(" suspect values.\n")
                .append("\nDuplicate sko flow matrices are retained under their original case names. Workbook formatting/copy tables outside the primary tables are ignored.\n");
        var mapper = JsonMapper.builder().build();
        AtomicFileUtil.write(directory.resolve("cases.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cases));
        AtomicFileUtil.write(directory.resolve("references.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(references));
        var hashes = new LinkedHashMap<String, String>();
        for (String file : List.of("paper.pdf", "results/results.xlsx", "results/readme.md")) hashes.put(file, MREFLPInstanceUtil.sha256(root.resolve(file)));
        AtomicFileUtil.write(directory.resolve("source-hashes.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(hashes));
        AtomicFileUtil.write(instances.resolve("all.index"), String.join("\n", index) + "\n");
        AtomicFileUtil.write(instances.resolve("pilot.index"), String.join("\n", pilot) + "\n");
        AtomicFileUtil.write(directory.resolve("audit.md"), audit.toString());
        System.out.printf("Prepared %d cases; %d reference cases; %d known optima; %d flagged values.%n", cases.size(), covered.size(), optimal, flagged);
    }
}
