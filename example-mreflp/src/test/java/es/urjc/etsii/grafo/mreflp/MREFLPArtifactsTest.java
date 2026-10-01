package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.mreflp.alg.*;
import es.urjc.etsii.grafo.mreflp.create.MREFLPConstructive;
import es.urjc.etsii.grafo.mreflp.improve.SwapDescent;
import es.urjc.etsii.grafo.mreflp.io.*;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static es.urjc.etsii.grafo.mreflp.MREFLPTestUtil.*;

class MREFLPArtifactsTest {
    @TempDir Path temporary;
    @BeforeEach void setup() { context(1234); }

    @Test void allRawCasesAndShortHeaders() throws Exception {
        int files = 0, cases = 0, shortHeaders = 0;
        var ids = new HashSet<String>();
        for (String category : BenchmarkPreparationUtil.CATEGORIES) {
            try (var entries = Files.newDirectoryStream(Path.of("instances", category), "*.txt")) {
                for (Path source : entries) {
                    files++;
                    for (int r = 2; r <= 5; r++) {
                        var i = MREFLPInstanceUtil.read(source, r);
                        assertTrue(ids.add(i.getId()));
                        assertTrue(i.n() <= i.capacity() * i.groups());
                        assertEquals(64, i.sourceHash().length());
                        if (r == 2 && i.missingWidths() > 0) shortHeaders++;
                        cases++;
                    }
                }
            }
        }
        assertEquals(143, files);
        assertEquals(572, cases);
        assertEquals(19, shortHeaders);
    }

    @Test void exactSmallGroupBoundsAndLargeBounds() {
        int[][] bounds = {{1,1,2,2,3,4,4,5,5,6,7,7,8,9,9,10},
                         {1,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8},
                         {1,1,1,1,2,2,2,3,3,4,4,4,5,5,5,6}};
        for (int r = 2; r <= 4; r++) for (int n = 1; n <= 16; n++) {
            assertEquals(bounds[r - 2][n - 1], MREFLPInstanceUtil.groupCount(n, r));
        }
        assertEquals(170, MREFLPInstanceUtil.groupCount(256, 2));
        assertEquals(39, MREFLPInstanceUtil.groupCount(60, 2));
        assertEquals(9, MREFLPInstanceUtil.groupCount(14, 2));
        assertEquals(41, MREFLPInstanceUtil.groupCount(100, 4));
        for (int n = 1; n <= 256; n++) for (int r = 2; r <= 5; r++) {
            assertTrue(r * MREFLPInstanceUtil.groupCount(n, r) >= n);
        }
        assertThrows(IllegalArgumentException.class, () -> MREFLPInstanceUtil.groupCount(0, 2));
    }

    @Test void triangularAndSymmetricInputAgreeAndMalformedDataIsRejected() throws Exception {
        Path upper = temporary.resolve("upper.txt"), symmetric = temporary.resolve("symmetric.txt");
        Files.writeString(upper, "3\n1 1 1\n\n0 2 3\n0 0 4\n0 0 0\n");
        Files.writeString(symmetric, "3\n1 1 1\n0 2 3\n2 0 4\n3 4 0\n");
        var first = MREFLPInstanceUtil.read(upper, 2);
        var second = MREFLPInstanceUtil.read(symmetric, 2);
        for (int u = 0; u < 3; u++) for (int v = 0; v < 3; v++) assertEquals(first.flow(u, v), second.flow(u, v));
        Path broken = temporary.resolve("broken.txt");
        for (String input : List.of("3\n1 1\n0 2 3\n0 0 4\n0 0 0\n",
                "3\n1 2 1\n0 2 3\n0 0 4\n0 0 0\n",
                "3\n1 1 1\n0 2 3\n1 0 4\n0 0 0\n",
                "3\n1 1 1\n0 -2 3\n0 0 4\n0 0 0\n",
                "3\n1 1 1\n0 2\n0 0 4\n0 0 0\n",
                "3\n1 1 1\n1 2 3\n0 0 4\n0 0 0\n",
                "3\n1 1 1\n0 2 3\n0 0 4\n0 0 0\nextra\n")) {
            Files.writeString(broken, input);
            assertThrows(IllegalArgumentException.class, () -> MREFLPInstanceUtil.read(broken, 2));
        }
        assertThrows(IllegalArgumentException.class, () -> new MREFLPInstance("bad", "test", 2, 2,
                new long[][]{{0, 1}, {1}}, 0, "fixture"));
    }

    @Test void incompleteConstructionIsInvalidAndAssignmentsAreDefensive() {
        var i = instance(4, 2, 3, 0);
        assertThrows(RuntimeException.class, () -> new MREFLPSolutionValidator().validate(new MREFLPSolution(i)).throwIfFail());
        var s = solution(i, 0, 0, 1, 1);
        s.assignments()[0] = 2;
        assertEquals(0, s.group(0));
        assertThrows(IllegalArgumentException.class, () -> s.assign(0, 1));
    }

    @Test void workbookMappingPreservesRawAnomaliesAndLargeNumbers() throws Exception {
        var references = WorkbookUtil.read(Path.of("results/results.xlsx"));
        int lmls = 0, optimal = 0, flagged = 0, flaggedLmls = 0;
        double largest = 0;
        for (var r : references) {
            assertFalse(r.caseId().startsWith("N-15-r"));
            if (r.method().equals("LMLS")) lmls++;
            if (r.method().equals("BKV") && r.optimal()) optimal++;
            if (r.flagged()) {
                flagged++;
                if (r.method().equals("LMLS")) {
                    flaggedLmls++;
                    assertEquals(109054, r.cost());
                    assertEquals("Table A.9", r.sheet());
                } else {
                    assertEquals("GRASP", r.method());
                    assertEquals("Table A.12", r.sheet());
                    assertEquals("E36", r.cell());
                    assertEquals(75542.1, r.cost(), 1e-8);
                }
            }
            largest = Math.max(largest, r.cost());
        }
        assertEquals(568, lmls);
        assertEquals(289, optimal);
        assertEquals(6, flagged);
        assertEquals(5, flaggedLmls);
        assertTrue(largest > Integer.MAX_VALUE);
        PublishedReference relaxed = null;
        for (var r : references) if (r.caseId().equals("tai256c-r5") && r.method().equals("LMLS_relaxed")) relaxed = r;
        assertNotNull(relaxed);
        assertNotNull(relaxed.averageCost());
        assertNotNull(relaxed.timeToBestSeconds());
        assertEquals("K7", relaxed.cell());
    }

    @Test void preparationGeneratesCompleteManifestWithoutChangingSources() throws Exception {
        Files.createSymbolicLink(temporary.resolve("instances"), Path.of("instances").toAbsolutePath());
        Files.createSymbolicLink(temporary.resolve("results"), Path.of("results").toAbsolutePath());
        Files.createSymbolicLink(temporary.resolve("paper.pdf"), Path.of("paper.pdf").toAbsolutePath());
        String hash = MREFLPInstanceUtil.sha256(Path.of("results/results.xlsx"));
        BenchmarkPreparationUtil.prepare(temporary);
        assertEquals(572, Files.readAllLines(temporary.resolve("benchmark/all.index")).size());
        assertEquals(4, Files.readAllLines(temporary.resolve("benchmark/pilot.index")).size());
        assertEquals(hash, MREFLPInstanceUtil.sha256(Path.of("results/results.xlsx")));
        String audit = Files.readString(temporary.resolve("benchmark/audit.md"));
        assertTrue(audit.contains("N-15-r2"));
        assertTrue(audit.contains("Table A.12!E36"));
    }

    @Test void checkpointValidationRejectsTamperingAndWrongSource() {
        var i = instance(4, 2, 3, 17);
        var r = record(i, "pilot-normal", 1234);
        assertDoesNotThrow(() -> ReproductionReportUtil.validate(r, i));
        r.assignments()[0] = 99;
        assertThrows(IllegalArgumentException.class, () -> ReproductionReportUtil.validate(r, i));
        var original = record(i, "pilot-normal", 1234);
        assertThrows(IllegalArgumentException.class, () -> ReproductionReportUtil.validate(original,
                new MREFLPInstance("fixture", "test", 2, 3, new long[4][4], 0, "different-hash")));
    }

    @Test void checkpointsRecordActualParametersAndRecognizePaperConfiguration() throws Exception {
        var i = instance(4, 2, 3, 17);
        var s = solution(i, 0, 0, 1, 1);
        var problem = new MREFLPConfig();
        problem.setProtocol("normal");
        var solver = new SolverConfig();
        solver.setSeed(1234);
        solver.setRandomType(RandomType.DEFAULT);
        solver.getWarmup().setEnabled(true);
        solver.getWarmup().setMaxMillis(1000);
        var exporter = new MREFLPResultExporter(new MREFLPResultConfig(), problem, solver);
        var paper = algorithm(LMLSVariant.LMLS, 0);
        var custom = new LMLS(LMLSVariant.LMLS, .8, 7, 2, .2, .3, .4, .5, 0);
        var generic = new SimpleAlgorithm<MREFLPSolution, MREFLPInstance>("generic",
                new MREFLPConstructive(LMLSVariant.RANDOM, .6), new SwapDescent(true));
        var mapper = JsonMapper.builder().build();
        String oldHash = System.getProperty("mreflp.artifact-hash");
        System.setProperty("mreflp.artifact-hash", "a".repeat(64));
        try {
            for (var configured : List.of(paper, custom, generic)) {
                var result = new WorkUnitResult<>(UUID.randomUUID(), true, "PaperExperiment", "fixture", i.getId(),
                        configured, "0", s, Map.<String, Double>of(), Map.<String, Object>of(),
                        600_000_000_000L, 20_000_000L, null);
                Path path = temporary.resolve(configured == paper ? "paper.run.json"
                        : configured == custom ? "custom.run.json" : "generic.run.json");
                exporter.export(temporary.toString(), path.getFileName().toString(), result);
                var run = mapper.readValue(path.toFile(), RunRecord.class);
                assertEquals(configured instanceof LMLS lmls ? lmls.parameters() : Map.of(), run.parameters());
                assertDoesNotThrow(() -> ReproductionReportUtil.validate(run, i));
                assertEquals(configured == paper, ReproductionReportUtil.isPaperRun(run));
                run.parameters().put("maxIter", 7.5);
                assertThrows(IllegalArgumentException.class, () -> ReproductionReportUtil.validate(run, i));
            }
        } finally {
            if (oldHash == null) System.clearProperty("mreflp.artifact-hash");
            else System.setProperty("mreflp.artifact-hash", oldHash);
        }
    }

    @Test void reportDoesNotPresentPilotOrIncompleteRelaxedAsFullCampaign() throws Exception {
        var i = MREFLPInstanceUtil.read(Path.of("instances/small/A-10-90.txt"), 2);
        Path input = temporary.resolve("runs");
        Files.createDirectories(input);
        var mapper = JsonMapper.builder().build();
        mapper.writeValue(input.resolve("first.run.json").toFile(), record(i, "pilot-relaxed", 1235));
        var result = ReproductionReportUtil.report(Path.of("."), temporary.resolve("report"), List.of(input));
        assertEquals(1, result.validRuns());
        assertEquals(6292, result.missingPaperRuns());
        assertFalse(result.complete());
        assertTrue(Files.readString(temporary.resolve("report/report.md")).contains("partial campaign"));
        assertTrue(Files.readString(temporary.resolve("report/comparisons.csv")).contains("incomplete-cohort"));
        mapper.writeValue(input.resolve("duplicate.run.json").toFile(), record(i, "pilot-relaxed", 1235));
        assertThrows(IllegalArgumentException.class, () -> ReproductionReportUtil.report(Path.of("."), temporary.resolve("duplicate-report"), List.of(input)));
    }

    @Test void invalidCheckpointIsCountedAndExcluded() throws Exception {
        Path input = temporary.resolve("runs");
        Files.createDirectories(input);
        Files.writeString(input.resolve("broken.run.json"), "{broken");
        var result = ReproductionReportUtil.report(Path.of("."), temporary.resolve("report"), List.of(input));
        assertEquals(0, result.validRuns());
        assertEquals(1, result.invalidRuns());
        assertFalse(result.complete());
    }

    @Test void csvEscapesFieldsAndLeavesUndefinedValuesEmpty() {
        assertEquals("\"A, B\",\"C\"\"D\",,\"0\"\n", ReproductionReportUtil.csv("A, B", "C\"D", null, 0));
    }

    @Test void concurrentReadersOnlySeeWholeAtomicFiles() throws Exception {
        Path target = temporary.resolve("shared.json");
        String first = "A".repeat(65536), second = "B".repeat(65536);
        AtomicFileUtil.write(target, first);
        try (var executor = Executors.newSingleThreadExecutor()) {
            var writer = executor.submit(() -> {
                for (int i = 0; i < 50; i++) AtomicFileUtil.write(target, i % 2 == 0 ? second : first);
                return null;
            });
            do {
                String value = Files.readString(target);
                assertTrue(value.equals(first) || value.equals(second));
            } while (!writer.isDone());
            writer.get();
        }
        try (var files = Files.newDirectoryStream(temporary, "*.tmp")) {
            assertFalse(files.iterator().hasNext());
        }
    }

    private static RunRecord record(MREFLPInstance i, String protocol, long seed) {
        int[] groups = new int[i.n()];
        for (int v = 0; v < groups.length; v++) groups[v] = v / i.capacity();
        var s = solution(i, groups);
        return new RunRecord(1, i.getId(), "LMLS", protocol, seed, .2, 0, algorithm(LMLSVariant.LMLS, 0).parameters(),
                "Xoroshiro128PlusPlus", i.sourceHash(), "a".repeat(64), s.cost(), 200_000_000L, 20_000_000L,
                groups, "25", "test", "test", "test", 1, true, 5, 50);
    }
}
