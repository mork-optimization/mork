package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.benchmark.BenchmarkReport;
import es.urjc.etsii.grafo.benchmark.BenchmarkResult;
import es.urjc.etsii.grafo.benchmark.BenchmarkService;
import es.urjc.etsii.grafo.executors.WorkUnit;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestHelperFactory;
import es.urjc.etsii.grafo.testutil.TestAlgorithm;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static es.urjc.etsii.grafo.testutil.TestHelperFactory.referencesGenerator;
import static es.urjc.etsii.grafo.testutil.TestHelperFactory.solutionGenerator;

public class ExcelSerializerTest {

    private static final Objective<TestMove, TestSolution, TestInstance> OBJ_MAX = Objective.of("Test", FMode.MAXIMIZE, TestSolution::getScore, TestMove::getScoreChange);

    @BeforeAll
    static void configureHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void cleanInstance() {
        Instance.resetProperties();
        Context.Configurator.setObjectives(OBJ_MAX);
    }

    ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p, List<ReferenceResultProvider> references, InstanceManager<TestInstance> instanceManager) {
        var config = new ExcelConfig();
        config.setFolder(p.toFile().getAbsolutePath());
        return new ExcelSerializer<>(config, references, customizer, instanceManager, new BenchmarkService());
    }

    ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p, List<ReferenceResultProvider> references) {
        return initExcel(customizer, p, references, TestHelperFactory.emptyInstanceManager());
    }

    ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p) {
        return initExcel(customizer, p, new ArrayList<>());
    }

    void writeEmptyExcelParameters(Path temp, List<ReferenceResultProvider> references) {
        var excel = initExcel(Optional.empty(), temp, references);
        var excelPath = temp.resolve("test.xlsx");
        excel.serializeResults("TestExperiment", new ArrayList<>(), excelPath);

        Assertions.assertTrue(Files.exists(excelPath));
    }

    @Test
    void benchmarkMetadataComesFromTheExecutionAndOfflineReportsHaveTheirOwnScope(@TempDir Path temp) throws IOException {
        var service = org.mockito.Mockito.mock(BenchmarkService.class);
        var config = new ExcelConfig();
        config.setFolder(temp.toString());
        var excel = new ExcelSerializer<TestSolution, TestInstance>(config, List.of(), Optional.empty(), TestHelperFactory.emptyInstanceManager(), service);
        var environment = Map.of("hostname", "solver-host", "java.runtime.version", "test-java");
        var protocol = new BenchmarkResult.Protocol("scimark-2.2-small", 1234, 2, 1024, 100, 1000, 5000, 100);
        var scores = new BenchmarkResult.Scores(10, 20, 30, 40, 50);
        var measured = new BenchmarkResult(1, protocol, "2026-11-04T00:00:00Z", environment, scores, 30);
        for (var status : List.of(BenchmarkReport.Status.MEASURED, BenchmarkReport.Status.REUSED,
                BenchmarkReport.Status.DISABLED, BenchmarkReport.Status.UNAVAILABLE)) {
            boolean hasScore = status == BenchmarkReport.Status.MEASURED || status == BenchmarkReport.Status.REUSED;
            var metadata = new BenchmarkReport(status, environment, hasScore ? measured : null, status.name());
            org.mockito.Mockito.when(service.report()).thenReturn(metadata);
            Path output = temp.resolve(status.name() + ".xlsx");
            excel.serializeResults("normal", List.of(), output);
            var values = otherData(output);
            Assertions.assertEquals(status.name(), values.get("Benchmark status"));
            Assertions.assertEquals("solver-host", values.get("hostname"));
            Assertions.assertEquals("test-java", values.get("java.runtime.version"));
            if (hasScore) {
                Assertions.assertEquals(30.0, values.get("Benchmark Score"));
                Assertions.assertEquals(measured.measuredAt(), values.get("Benchmark measured at"));
                Assertions.assertEquals(50.0, values.get("LU"));
            } else {
                Assertions.assertFalse(values.containsKey("Benchmark Score"));
            }
        }
        org.mockito.Mockito.when(service.report()).thenReturn(new BenchmarkReport(BenchmarkReport.Status.REUSED, environment, measured, "Reused"));
        org.mockito.Mockito.clearInvocations(service);
        Path offline = temp.resolve("offline.xlsx");
        excel.serializeCachedResults(List.of(), List.of(), offline,
                BenchmarkReport.notRecorded(Map.of("hostname", "remote-execution-host")));
        org.mockito.Mockito.verifyNoInteractions(service);
        var values = otherData(offline);
        Assertions.assertEquals("NOT_RECORDED", values.get("Benchmark status"));
        Assertions.assertEquals("Not recorded", values.get("Benchmark details"));
        Assertions.assertEquals("remote-execution-host", values.get("hostname"));
        Assertions.assertFalse(values.containsKey("Benchmark Score"));
        excel.serializeResults("normal", List.of(), temp.resolve("after-offline.xlsx"));
        Assertions.assertEquals(30.0, otherData(temp.resolve("after-offline.xlsx")).get("Benchmark Score"));
    }

    private Map<String, Object> otherData(Path path) throws IOException {
        var data = new HashMap<String, Object>();
        try (var workbook = new XSSFWorkbook(Files.newInputStream(path))) {
            for (var row : workbook.getSheet(ExcelSerializer.OTHER_DATA_SHEET)) {
                if (row.getCell(1) == null) continue;
                var cell = row.getCell(1);
                Object value = cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                        ? cell.getNumericCellValue() : cell.getStringCellValue();
                data.put(row.getCell(0).getStringCellValue(), value);
            }
        }
        return data;
    }

    @Test
    void writeEmptyCalcExcel(@TempDir Path temp) throws IOException {
        writeEmptyExcelParameters(temp, new ArrayList<>());

        try (var workbook = new XSSFWorkbook(new FileInputStream(temp.resolve("test.xlsx").toFile()))) {
            var table = workbook.getTable(ExcelSerializer.RAW_TABLE);
            Assertions.assertNotNull(table);
            Assertions.assertEquals("A1:I2", table.getArea().formatAsString());
            Assertions.assertEquals(2, workbook.getSheet(ExcelSerializer.RAW_SHEET).getPhysicalNumberOfRows());
            Assertions.assertNull(workbook.getSheet(ExcelSerializer.FAILURES_SHEET));
            var pivot = workbook.getSheet(ExcelSerializer.PIVOT_SHEET);
            Assertions.assertTrue(pivot.getPivotTables().isEmpty());
            Assertions.assertTrue(pivot.getRow(0).getCell(0).getStringCellValue().contains("No successful executions"));
        }
        Assertions.assertEquals(0.0, otherData(temp.resolve("test.xlsx")).get("Total executions"));
    }

    @Test
    void writeEmptyExcelWithReferences(@TempDir Path temp) {
        writeEmptyExcelParameters(temp, referencesGenerator(10, 10));
    }

    @Test
    void writeEmptyCSVWithInvalidReference(@TempDir Path temp) {
        Assertions.assertDoesNotThrow(() -> writeEmptyExcelParameters(temp, referencesGenerator(Double.NaN, Double.NaN)));
    }

    @Test
    void writeEmptyCSVInvalidPath() {
        var path = Path.of("/doesnotexist");
        var references = referencesGenerator(Double.NaN, Double.NaN);
        Assertions.assertThrows(RuntimeException.class, () -> writeEmptyExcelParameters(path, references));
    }

    @Test
    void writeXLSXWithCustomizer(@TempDir Path temp) throws IOException {
        var customizer = new RecordingExcelCustomizer();
        var excel = initExcel(Optional.of(customizer), temp);
        var excelPath = temp.resolve("test2.xlsx");

        var data = solutionGenerator();

        excel.serializeResults("TestExperiment", data, excelPath);
        Assertions.assertTrue(customizer.called);

        try (var workbook = new XSSFWorkbook(new FileInputStream(excelPath.toFile()))) {
            Assertions.assertEquals(
                    "Customized",
                    workbook.getSheet(ExcelSerializer.PIVOT_SHEET).getRow(10).getCell(0).getStringCellValue()
            );
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void failedReplacementPreservesExistingWorkbook(boolean cached, @TempDir Path temp) throws IOException {
        Path output = temp.resolve("report.xlsx");
        var data = solutionGenerator();
        export(initExcel(Optional.empty(), temp), data, output, cached);
        byte[] original = Files.readAllBytes(output);
        var failing = new ExcelCustomizer() {
            @Override
            public void customize(XSSFWorkbook workbook, List<? extends WorkUnitResult<?, ?>> results) {
                throw new IllegalStateException("Customizer failed after rows were written");
            }
        };
        var excel = initExcel(Optional.of(failing), temp);
        Assertions.assertThrows(RuntimeException.class, () -> export(excel, data, output, cached));
        Assertions.assertArrayEquals(original, Files.readAllBytes(output));
        try (var files = Files.newDirectoryStream(temp)) {
            for (Path file : files) Assertions.assertEquals(output, file, "Temporary export must be removed");
        }
        export(initExcel(Optional.empty(), temp), List.of(), output, cached);
        try (var workbook = new XSSFWorkbook(Files.newInputStream(output))) {
            Assertions.assertTrue(workbook.getSheet(ExcelSerializer.PIVOT_SHEET).getPivotTables().isEmpty());
        }
    }

    @ParameterizedTest
    @CsvSource({"MINIMIZE,-10,-8,0.2", "MAXIMIZE,-8,-10,0.25", "MINIMIZE,2,4,1", "MAXIMIZE,4,2,0.5",
            "MINIMIZE,0,2,", "MAXIMIZE,0,-2,", "MINIMIZE,0,0,0", "MAXIMIZE,0,0,0"})
    void comparisonsHandleSignedScoresAndMissingReferences(FMode mode, double best, double other, Double gap,
                                                         @TempDir Path temp) throws IOException {
        Context.Configurator.setObjectives(Objective.of("Test", mode, TestSolution::getScore, TestMove::getScoreChange));
        var references = new ArrayList<ReferenceResultProvider>();
        for (Double missing : java.util.Arrays.asList(null, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            references.add(new ReferenceResultProvider() {
                @Override public String getProviderName() { return "Reference " + missing; }
                @Override public ReferenceResult getValueFor(String instance) {
                    var result = new ReferenceResult().setTimeInSeconds(3.5);
                    if (missing != null) result.addScore("Test", missing);
                    return result;
                }
            });
        }
        var data = List.of(
                TestHelperFactory.solutionGenerated("instance", "experiment", "algorithm", 0, best, 10, 8),
                TestHelperFactory.solutionGenerated("instance", "experiment", "algorithm", 1, other, 10, 8));
        Path output = temp.resolve("comparison.xlsx");
        export(initExcel(Optional.empty(), temp, references), data, output, true);
        try (var workbook = new XSSFWorkbook(Files.newInputStream(output))) {
            var raw = workbook.getSheet(ExcelSerializer.RAW_SHEET);
            String bestFormula = raw.getRow(1).getCell(8).getCellFormula();
            Assertions.assertTrue(bestFormula.contains("RawResultsTable[Score]"));
            Assertions.assertTrue(bestFormula.contains("RawResultsTable[Instance Name]"));
            Assertions.assertTrue(bestFormula.endsWith("RawResultsTable[Score],\"<>\")"));
            // POI 5.5.1 Countif.StringMatcher rejects NumberEval for the <> criterion.
            // Check the exported best-score formula above; evaluate deviations against known best values.
            for (int i = 1; i < 7; i++) {
                raw.getRow(i).getCell(8).removeFormula();
                raw.getRow(i).getCell(8).setCellValue(best);
            }
            var evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Assertions.assertEquals(0, evaluator.evaluate(raw.getRow(1).getCell(7)).getNumberValue());
            var actualGap = evaluator.evaluate(raw.getRow(2).getCell(7));
            if (gap == null) Assertions.assertEquals("", actualGap.getStringValue());
            else Assertions.assertEquals(gap, actualGap.getNumberValue(), 1e-12);
            Assertions.assertEquals(ExcelSerializer.PERCENT_NUMBER_FORMAT, raw.getRow(2).getCell(7).getCellStyle().getDataFormatString());
            for (int rowIndex = 3; rowIndex < 7; rowIndex++) {
                var row = raw.getRow(rowIndex);
                Assertions.assertEquals(CellType.BLANK, row.getCell(3).getCellType());
                Assertions.assertEquals(3.5, row.getCell(4).getNumericCellValue());
                Assertions.assertEquals(CellType.BLANK, row.getCell(5).getCellType());
                Assertions.assertEquals("", evaluator.evaluate(row.getCell(6)).getStringValue());
                Assertions.assertEquals("", evaluator.evaluate(row.getCell(7)).getStringValue());
                Assertions.assertEquals(best, evaluator.evaluate(row.getCell(8)).getNumberValue());
            }
            // Table references must include a newly appended result after expanding the table.
            var added = raw.createRow(7);
            added.createCell(0).setCellValue("instance");
            double improved = mode == FMode.MINIMIZE ? best - 1 : best + 1;
            added.createCell(3).setCellValue(improved);
            workbook.getTable(ExcelSerializer.RAW_TABLE).setArea(new AreaReference("A1:I8", SpreadsheetVersion.EXCEL2007));
            var tokens = org.apache.poi.ss.formula.FormulaParser.parse(bestFormula,
                    org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook.create(workbook),
                    org.apache.poi.ss.formula.FormulaType.CELL, workbook.getSheetIndex(raw));
            int ranges = 0;
            for (var token : tokens) {
                if (token instanceof org.apache.poi.ss.formula.ptg.AreaPtgBase area) {
                    Assertions.assertEquals(1, area.getFirstRow());
                    Assertions.assertEquals(7, area.getLastRow());
                    ranges++;
                }
            }
            Assertions.assertEquals(3, ranges, "Score and instance table references must expand with the table");
        }
    }

    @Test
    void streamedRowsAndReportDataRemainAvailableAfterFlushing(@TempDir Path temp) throws IOException {
        var data = new ArrayList<WorkUnitResult<TestSolution, TestInstance>>();
        for (int i = 0; i < 500; i++) {
            data.add(TestHelperFactory.solutionGenerated("instance", "experiment", "algorithm", i, i, 10, 8).withoutSolution());
        }
        data.add(failedResult("instance", -1, 501));
        var customizer = new ExcelCustomizer() {
            @Override
            public void customize(XSSFWorkbook workbook, List<? extends WorkUnitResult<?, ?>> results) {
                Assertions.assertEquals(data, results);
                Assertions.assertThrows(UnsupportedOperationException.class, results::clear);
                var sheet = workbook.createSheet("Custom report");
                sheet.createRow(0).createCell(0).setCellValue(results.get(499).objectives().get("Test"));
                sheet.createRow(1).createCell(0).setCellValue(results.get(500).success());
            }
        };
        Path output = temp.resolve("streamed.xlsx");
        export(initExcel(Optional.of(customizer), temp), data, output, true);
        try (var workbook = new XSSFWorkbook(Files.newInputStream(output))) {
            var raw = workbook.getSheet(ExcelSerializer.RAW_SHEET);
            Assertions.assertEquals(501, raw.getPhysicalNumberOfRows());
            Assertions.assertEquals(0, raw.getRow(1).getCell(3).getNumericCellValue());
            Assertions.assertEquals(499, raw.getRow(500).getCell(3).getNumericCellValue());
            Assertions.assertEquals("A1:I501", workbook.getTable(ExcelSerializer.RAW_TABLE).getArea().formatAsString());
            var custom = workbook.getSheet("Custom report");
            Assertions.assertEquals(499, custom.getRow(0).getCell(0).getNumericCellValue());
            Assertions.assertFalse(custom.getRow(1).getCell(0).getBooleanCellValue());
            var counts = workbook.getSheet(ExcelSerializer.EXECUTION_SUMMARY_SHEET);
            long total = 0, successful = 0, failed = 0;
            for (var row : counts) {
                if (row.getRowNum() == 0) continue;
                total += (long) row.getCell(1).getNumericCellValue();
                successful += (long) row.getCell(2).getNumericCellValue();
                failed += (long) row.getCell(3).getNumericCellValue();
            }
            Assertions.assertEquals(501, total);
            Assertions.assertEquals(500, successful);
            Assertions.assertEquals(1, failed);
        }
    }

    @Test
    void writeXLSXWithReferences(@TempDir Path temp) throws IOException {
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10));
        var excelPath = temp.resolve("test2.xlsx");

        var data = solutionGenerator();

        excel.serializeResults("TestExperiment", data, excelPath);

        File excelFile = excelPath.toFile();
        try (var workbook = new XSSFWorkbook(new FileInputStream(excelFile))) {

            XSSFSheet rawResults = workbook.getSheet(ExcelSerializer.RAW_SHEET);
            Iterator<Row> rowIterator = rawResults.iterator();

            Assertions.assertEquals(data.size() * 2, rawResults.getPhysicalNumberOfRows() - 1); // There are headers

            var rawTable = workbook.getTable(ExcelSerializer.RAW_TABLE);
            Assertions.assertNotNull(rawTable);
            Assertions.assertEquals("A1:I7", rawTable.getArea().formatAsString());
            Assertions.assertEquals("TableStyleLight1", rawTable.getStyleName());
            Assertions.assertEquals("A1:I7", rawTable.getCTTable().getAutoFilter().getRef());

            var rawHeaderComment = rawResults.getRow(0).getCell(0).getCellComment();
            Assertions.assertNotNull(rawHeaderComment);
            Assertions.assertEquals(ExcelSerializer.RAW_TABLE_INSTRUCTIONS, rawHeaderComment.getString().getString());

            var pivotTables = workbook.getSheet(ExcelSerializer.PIVOT_SHEET).getPivotTables();
            Assertions.assertEquals(1, pivotTables.size());
            var cacheDefinition = pivotTables.getFirst().getPivotCacheDefinition().getCTPivotCacheDefinition();
            var source = cacheDefinition.getCacheSource().getWorksheetSource();
            Assertions.assertEquals(ExcelSerializer.RAW_TABLE, source.getName());
            Assertions.assertFalse(source.isSetRef());
            Assertions.assertTrue(cacheDefinition.getRefreshOnLoad());

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Assertions.assertEquals(row.getLastCellNum(), 9);
                if (row.getCell(1).getStringCellValue().equals("TestProvider")) {
                    Assertions.assertEquals(row.getCell(3).getNumericCellValue(), 10.10);
                    Assertions.assertEquals(row.getCell(4).getNumericCellValue(), 10.10); // Seconds to nanoseconds
                }
            }

            var appendedRow = rawResults.createRow(7);
            for (int i = 0; i < 9; i++) {
                appendedRow.createCell(i).setBlank();
            }
            rawTable.setArea(new AreaReference(
                    new CellReference(0, 0),
                    new CellReference(7, 8),
                    SpreadsheetVersion.EXCEL2007
            ));
            Assertions.assertEquals(ExcelSerializer.RAW_TABLE, source.getName());
            Assertions.assertFalse(source.isSetRef());
        }
    }

    @Test
    void writeEmptyInstanceSheet(@TempDir Path temp) throws IOException {
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10));
        var excelPath = temp.resolve("emptyInstanceSheet.xlsx");
        var data = solutionGenerator();
        excel.serializeResults("TestExperiment", data, excelPath);
        File excelFile = excelPath.toFile();

        try (var wb = new XSSFWorkbook(new FileInputStream(excelFile))) {
            XSSFSheet instanceSheet = wb.getSheet(ExcelSerializer.INSTANCE_SHEET);
            Iterator<Row> rowIterator = instanceSheet.iterator();

            Assertions.assertTrue(rowIterator.hasNext(), "If empty there must be an explanation on the first row");
            var explanation = rowIterator.next();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Assertions.assertEquals(row.getLastCellNum(), 0);
                // Rest should be empty
            }
        }
    }

    @Test
    void writeInstanceSheet(@TempDir Path temp) throws IOException {
        var instance = new TestInstance("writeInstanceSheetTest");
        instance.setProperty("customProperty", 1234567);
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10), TestHelperFactory.simpleInstanceManager(instance));
        var excelPath = temp.resolve("instanceSheet.xlsx");
        var data = solutionGenerator();
        excel.serializeResults("TestExperiment", data, excelPath);
        File excelFile = excelPath.toFile();

        try (var wb = new XSSFWorkbook(new FileInputStream(excelFile))) {
            XSSFSheet instanceSheet = wb.getSheet(ExcelSerializer.INSTANCE_SHEET);
            Iterator<Row> rowIterator = instanceSheet.iterator();

            Assertions.assertTrue(rowIterator.hasNext(), "Missing headers in instance sheet");
            var header = rowIterator.next();
            Assertions.assertEquals(2, header.getLastCellNum(), "Two columns, instance name and property name");
            var propertyName = header.getCell(1);
            Assertions.assertEquals("customProperty", propertyName.getStringCellValue());

            Assertions.assertTrue(rowIterator.hasNext(), "Missing values in instance sheet");
            var firstData = rowIterator.next();
            Assertions.assertEquals(2, header.getLastCellNum(), "Two columns, instance name and property name");
            Assertions.assertEquals("writeInstanceSheetTest", firstData.getCell(0).getStringCellValue());
            Assertions.assertEquals(1234567, firstData.getCell(1).getNumericCellValue());

        }
    }

    @Test
    void cachedReportsUseTheirOwnInputsWithoutChangingNormalInstanceScope(@TempDir Path temp) throws IOException {
        var original = new TestInstance("original"); original.setProperty("scope", "default");
        var target = new TestInstance("target"); target.setProperty("scope", "campaign");
        @SuppressWarnings("unchecked") var manager = (InstanceManager<TestInstance>) org.mockito.Mockito.mock(InstanceManager.class);
        org.mockito.Mockito.when(manager.getInstanceSolveOrder("normal")).thenReturn(List.of("original"));
        org.mockito.Mockito.when(manager.getInstance("original")).thenReturn(original);
        org.mockito.Mockito.when(manager.getInstance("target")).thenReturn(target);
        var excel = initExcel(Optional.empty(), temp, List.of(), manager);
        excel.serializeCachedResults(List.of(), List.of("target"), temp.resolve("target/cached.xlsx"), BenchmarkReport.notRecorded(Map.of()));
        org.mockito.Mockito.verify(manager, org.mockito.Mockito.never()).getInstanceSolveOrder(org.mockito.ArgumentMatchers.anyString());
        excel.serializeResults("normal", List.of(), temp.resolve("normal.xlsx"));
        try (var cached = new XSSFWorkbook(Files.newInputStream(temp.resolve("target/cached.xlsx")));
             var normal = new XSSFWorkbook(Files.newInputStream(temp.resolve("normal.xlsx")))) {
            Assertions.assertEquals("target", cached.getSheet(ExcelSerializer.INSTANCE_SHEET).getRow(1).getCell(0).getStringCellValue());
            Assertions.assertEquals("original", normal.getSheet(ExcelSerializer.INSTANCE_SHEET).getRow(1).getCell(0).getStringCellValue());
        }
    }

    @Test
    void writeExcelWithCustomProperties(@TempDir Path temp) throws IOException {
        var instance = new TestInstance("writeCustomPropsTest");
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10), TestHelperFactory.simpleInstanceManager(instance));
        var excelPath = temp.resolve("customProperties.xlsx");
        
        // Create events with custom properties
        var data = List.of(
                TestHelperFactory.solutionGenerated("writeCustomPropsTest", "TestExp", "Alg1", 1, 100.0, 1000, 500, Map.of("prop1", 4, "prop2", 1)),
                TestHelperFactory.solutionGenerated("writeCustomPropsTest", "TestExp", "Alg1", 2, 95.0, 1100, 600, Map.of("prop1", 6, "prop2", 2))
        );
        
        excel.serializeResults("TestExperiment", data, excelPath);
        File excelFile = excelPath.toFile();

        try (var wb = new XSSFWorkbook(new FileInputStream(excelFile))) {
            XSSFSheet rawSheet = wb.getSheet(ExcelSerializer.RAW_SHEET);
            Iterator<Row> rowIterator = rawSheet.iterator();

            Assertions.assertEquals("A1:K4", wb.getTable(ExcelSerializer.RAW_TABLE).getArea().formatAsString());

            Assertions.assertTrue(rowIterator.hasNext(), "Missing headers in raw result sheet");
            var header = rowIterator.next();
            Assertions.assertEquals(11, header.getLastCellNum(), "Should have 9 common headers + 2 custom property headers");
            
            // Find the index of "prop1"
            var prop1Index = -1;
            for (int i = 0; i < header.getLastCellNum(); i++) {
                if (header.getCell(i) != null && "prop1".equals(header.getCell(i).getStringCellValue())) {
                    prop1Index = i;
                    break;
                }
            }
            Assertions.assertTrue(prop1Index >= 0, "Missing prop1 header in raw result sheet");
            
            // Find the index of "prop2"
            var prop2Index = -1;
            for (int i = 0; i < header.getLastCellNum(); i++) {
                if (header.getCell(i) != null && "prop2".equals(header.getCell(i).getStringCellValue())) {
                    prop2Index = i;
                    break;
                }
            }
            Assertions.assertTrue(prop2Index >= 0, "Missing prop2 header in raw result sheet");
            
            Assertions.assertTrue(rowIterator.hasNext(), "Missing values in raw result sheet");
            var firstData = rowIterator.next();
            Assertions.assertEquals(4.0, firstData.getCell(prop1Index).getNumericCellValue(), "Data of solution custom properties is not correct in raw result sheet");
            Assertions.assertEquals(1.0, firstData.getCell(prop2Index).getNumericCellValue(), "Data of solution custom properties is not correct in raw result sheet");
            
            var secondData = rowIterator.next();
            Assertions.assertEquals(6.0, secondData.getCell(prop1Index).getNumericCellValue(), "Data of solution custom properties is not correct in raw result sheet");
            Assertions.assertEquals(2.0, secondData.getCell(prop2Index).getNumericCellValue(), "Data of solution custom properties is not correct in raw result sheet");
        }
    }

    @Test
    void rejectNonScalarCustomProperties(@TempDir Path temp) {
        var instance = new TestInstance("rejectNonScalarCustomPropertiesTest");
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10), TestHelperFactory.simpleInstanceManager(instance));
        var excelPath = temp.resolve("invalidCustomProperties.xlsx");

        var data = List.of(
                TestHelperFactory.solutionGenerated("rejectNonScalarCustomPropertiesTest", "TestExp", "Alg1", 1, 100.0, 1000, 500, Map.<String, Object>of("notScalar", List.of(1, 2, 3)))
        );

        var exception = Assertions.assertThrows(RuntimeException.class, () -> excel.serializeResults("TestExperiment", data, excelPath));
        Assertions.assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        Assertions.assertTrue(exception.getCause().getMessage().contains("notScalar"));
    }

    @Test
    void rejectNonScalarInstanceProperties(@TempDir Path temp) {
        var instance = new TestInstance("rejectNonScalarInstancePropertiesTest");
        instance.setProperty("test", new HashSet<>());
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10), TestHelperFactory.simpleInstanceManager(instance));
        var excelPath = temp.resolve("invalidInstanceProperties.xlsx");

        var data = solutionGenerator();

        var exception = Assertions.assertThrows(RuntimeException.class, () -> excel.serializeResults("TestExperiment", data, excelPath));
        Assertions.assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        String causeMessage = exception.getCause().getMessage();
        Assertions.assertTrue(causeMessage.contains("Instance property 'test'"));
        Assertions.assertTrue(causeMessage.contains("rejectNonScalarInstancePropertiesTest"));
        Assertions.assertTrue(causeMessage.contains("Instances"));
        Assertions.assertTrue(causeMessage.contains("HashSet"));
        Assertions.assertTrue(exception.getMessage().contains("Root cause: IllegalArgumentException"));
    }

    @ParameterizedTest(name = "{0}, cached={1}")
    @CsvSource({"MINIMIZE, false", "MINIMIZE, true", "MAXIMIZE, false", "MAXIMIZE, true"})
    void failedExecutionsStayOutOfPerformanceSummaries(FMode mode, boolean cached, @TempDir Path temp) throws IOException {
        var objective = Objective.of("Test", mode, TestSolution::getScore, TestMove::getScoreChange);
        Context.Configurator.setObjectives(objective);
        boolean maximizing = mode == FMode.MAXIMIZE;
        double sign = maximizing ? -1 : 1;
        var provider = org.mockito.Mockito.spy(referencesGenerator(sign, 0.5).getFirst());
        var references = List.of(provider);
        var knownFailure = failedResult("shared", 2_500_000_000L, 7);
        var unknownFailure = failedResult("failed-only", -1, 8);
        var data = List.of(
                knownFailure,
                TestHelperFactory.solutionGenerated("shared", "TestExperiment", "working", 0, sign * 5, 1_000_000_000L, 500_000_000L),
                TestHelperFactory.solutionGenerated("shared", "TestExperiment", "working", 1, sign * 2, 1_000_000_000L, 500_000_000L),
                unknownFailure);
        var customizer = new RecordingExcelCustomizer();
        var excel = initExcel(Optional.of(customizer), temp, references);
        Path output = temp.resolve(mode + "-" + cached + ".xlsx");
        export(excel, data, output, cached);
        Assertions.assertTrue(customizer.called);
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.times(1)).getValueFor("shared");
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.never()).getValueFor("failed-only");
        try (var workbook = new XSSFWorkbook(Files.newInputStream(output))) {
            var raw = workbook.getSheet(ExcelSerializer.RAW_SHEET);
            Assertions.assertEquals("A1:I4", workbook.getTable(ExcelSerializer.RAW_TABLE).getArea().formatAsString());
            Assertions.assertEquals(4, raw.getPhysicalNumberOfRows());
            Assertions.assertEquals(9, raw.getRow(0).getLastCellNum());
            Assertions.assertEquals(sign * 5, raw.getRow(1).getCell(3).getNumericCellValue());
            Assertions.assertEquals(sign * 2, raw.getRow(2).getCell(3).getNumericCellValue());
            Assertions.assertEquals(sign, raw.getRow(3).getCell(3).getNumericCellValue());
            Assertions.assertEquals("TestProvider", raw.getRow(3).getCell(1).getStringCellValue());
            for (int i = 1; i <= 3; i++) {
                var row = raw.getRow(i);
                Assertions.assertEquals("shared", row.getCell(0).getStringCellValue());
                Assertions.assertEquals(CellType.FORMULA, row.getCell(6).getCellType());
                Assertions.assertEquals(CellType.FORMULA, row.getCell(7).getCellType());
                Assertions.assertTrue(row.getCell(8).getCellFormula().startsWith("_xlfn." + (maximizing ? "MAXIFS" : "MINIFS")));
            }
            var pivot = workbook.getSheet(ExcelSerializer.PIVOT_SHEET).getPivotTables().getFirst();
            Assertions.assertEquals(maximizing ? "max" : "min",
                    pivot.getCTPivotTableDefinition().getDataFields().getDataFieldArray(0).getSubtotal().toString());
            Assertions.assertEquals(ExcelSerializer.RAW_TABLE,
                    pivot.getPivotCacheDefinition().getCTPivotCacheDefinition().getCacheSource().getWorksheetSource().getName());

            var failures = workbook.getSheet(ExcelSerializer.FAILURES_SHEET);
            Assertions.assertEquals(3, failures.getPhysicalNumberOfRows());
            var expectedHeaders = List.of("Result ID", "Instance Name", "Instance Path", "Algorithm Name", "Iteration", "Status", "Total Time (s)");
            for (int i = 0; i < expectedHeaders.size(); i++) {
                Assertions.assertEquals(expectedHeaders.get(i), failures.getRow(0).getCell(i).getStringCellValue());
            }
            var first = failures.getRow(1);
            Assertions.assertEquals(knownFailure.resultId().toString(), first.getCell(0).getStringCellValue());
            Assertions.assertEquals("shared", first.getCell(1).getStringCellValue());
            Assertions.assertEquals(knownFailure.instancePath(), first.getCell(2).getStringCellValue());
            Assertions.assertEquals("broken", first.getCell(3).getStringCellValue());
            Assertions.assertEquals("7", first.getCell(4).getStringCellValue());
            Assertions.assertEquals("FAILED", first.getCell(5).getStringCellValue());
            Assertions.assertEquals(2.5, first.getCell(6).getNumericCellValue());
            Assertions.assertEquals(ExcelSerializer.TIME_NUMBER_FORMAT, first.getCell(6).getCellStyle().getDataFormatString());
            Assertions.assertEquals("failed-only", failures.getRow(2).getCell(1).getStringCellValue());
            Assertions.assertEquals(CellType.BLANK, failures.getRow(2).getCell(6).getCellType());
        }
        var metadata = otherData(output);
        Assertions.assertEquals(4.0, metadata.get("Total executions"));
        Assertions.assertEquals(2.0, metadata.get("Successful executions"));
        Assertions.assertEquals(2.0, metadata.get("Failed executions"));
        Assertions.assertEquals("Successful executions only", metadata.get("Performance summaries"));
    }

    @ParameterizedTest(name = "cached={0}")
    @ValueSource(booleans = {false, true})
    void allFailedExecutionsProduceADiagnosticWorkbook(boolean cached, @TempDir Path temp) throws IOException {
        var failed = new ArrayList<WorkUnitResult<TestSolution, TestInstance>>();
        // Exceed the streaming row window so failures are also retained after rows are flushed.
        for (int i = 0; i < 150; i++) {
            failed.add(failedResult("failed-" + i, i == 0 ? -1 : 1_250_000_000L, i));
        }
        var customizer = new RecordingExcelCustomizer();
        var excel = initExcel(Optional.of(customizer), temp, referencesGenerator(123, 1));
        Path output = temp.resolve("all-failed-" + cached + ".xlsx");
        export(excel, failed, output, cached);
        Assertions.assertTrue(customizer.called);
        try (var workbook = new XSSFWorkbook(Files.newInputStream(output))) {
            Assertions.assertEquals("A1:I2", workbook.getTable(ExcelSerializer.RAW_TABLE).getArea().formatAsString());
            var raw = workbook.getSheet(ExcelSerializer.RAW_SHEET);
            Assertions.assertEquals(2, raw.getPhysicalNumberOfRows());
            for (var cell : raw.getRow(1)) Assertions.assertEquals(CellType.BLANK, cell.getCellType());
            var pivot = workbook.getSheet(ExcelSerializer.PIVOT_SHEET);
            Assertions.assertTrue(pivot.getPivotTables().isEmpty());
            Assertions.assertTrue(pivot.getRow(0).getCell(0).getStringCellValue().contains("Failures"));
            var failures = workbook.getSheet(ExcelSerializer.FAILURES_SHEET);
            Assertions.assertEquals(failed.size() + 1, failures.getPhysicalNumberOfRows());
            var summary = workbook.getSheet(ExcelSerializer.EXECUTION_SUMMARY_SHEET).getRow(1);
            Assertions.assertEquals(150, summary.getCell(1).getNumericCellValue());
            Assertions.assertEquals(0, summary.getCell(2).getNumericCellValue());
            Assertions.assertEquals(150, summary.getCell(3).getNumericCellValue());
            Assertions.assertEquals(0, summary.getCell(4).getNumericCellValue());
            Assertions.assertEquals(CellType.BLANK, failures.getRow(1).getCell(6).getCellType());
            for (int i = 0; i < failed.size(); i++) {
                var row = failures.getRow(i + 1);
                Assertions.assertEquals(failed.get(i).resultId().toString(), row.getCell(0).getStringCellValue());
                Assertions.assertEquals("FAILED", row.getCell(5).getStringCellValue());
                if (i > 0) Assertions.assertEquals(1.25, row.getCell(6).getNumericCellValue());
            }
        }
        var metadata = otherData(output);
        Assertions.assertEquals(150.0, metadata.get("Total executions"));
        Assertions.assertEquals(0.0, metadata.get("Successful executions"));
        Assertions.assertEquals(150.0, metadata.get("Failed executions"));
    }

    @Test
    void successfulResultsRequireAFiniteMainObjective(@TempDir Path temp) {
        var excel = initExcel(Optional.empty(), temp);
        var values = List.of(Map.<String, Double>of(), Map.of("Other", 1.0), Map.of("Test", Double.NaN),
                Map.of("Test", Double.POSITIVE_INFINITY), Map.of("Test", Double.NEGATIVE_INFINITY));
        for (var objectives : values) {
            var result = new WorkUnitResult<TestSolution, TestInstance>(UUID.randomUUID(), true,
                    "experiment", "instance.txt", "invalid-instance", new TestAlgorithm("invalid-algorithm"),
                    "7", null, objectives, Map.of(), 0, 0, null);
            var exception = Assertions.assertThrows(RuntimeException.class,
                    () -> excel.serializeResults("experiment", List.of(result), temp.resolve("invalid.xlsx")));
            Assertions.assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            var message = exception.getCause().getMessage();
            Assertions.assertTrue(message.contains("invalid-instance"));
            Assertions.assertTrue(message.contains("invalid-algorithm"));
            Assertions.assertTrue(message.contains("iteration '7'"));
            Assertions.assertTrue(message.contains("objective 'Test'"));
            Assertions.assertTrue(message.contains("finite"));
        }
    }

    private WorkUnitResult<TestSolution, TestInstance> failedResult(String instance, long time, int iteration) {
        var work = new WorkUnit<>(UUID.randomUUID(), "experiment", "instances/" + instance + ".txt",
                new TestAlgorithm("broken"), iteration);
        return WorkUnitResult.failure(work.resultId(), work, instance, time, -1);
    }

    private void export(ExcelSerializer<TestSolution, TestInstance> excel, List<WorkUnitResult<TestSolution, TestInstance>> results,
                        Path output, boolean cached) {
        if (cached) excel.serializeCachedResults(results, List.of(), output, BenchmarkReport.notRecorded(Map.of()));
        else excel.serializeResults("experiment", results, output);
    }

    @Test
    void rejectsMoreRowsThanExcelSupports() {
        int maxDataRows = SpreadsheetVersion.EXCEL2007.getMaxRows() - 1;
        for (var sheet : List.of(ExcelSerializer.RAW_SHEET, ExcelSerializer.FAILURES_SHEET)) {
            Assertions.assertDoesNotThrow(() -> ExcelSerializer.validateDataRowCount(sheet, maxDataRows));
            var exception = Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> ExcelSerializer.validateDataRowCount(sheet, (long) maxDataRows + 1));
            Assertions.assertTrue(exception.getMessage().contains(sheet));
            Assertions.assertTrue(exception.getMessage().contains(String.valueOf(maxDataRows)));
        }
    }

    private static final class RecordingExcelCustomizer extends ExcelCustomizer {
        private boolean called;

        @Override
        public void customize(XSSFWorkbook excelBook, List<? extends WorkUnitResult<?, ?>> results) {
            called = true;
            excelBook.getSheet(ExcelSerializer.PIVOT_SHEET).createRow(10).createCell(0).setCellValue("Customized");
        }
    }

}
