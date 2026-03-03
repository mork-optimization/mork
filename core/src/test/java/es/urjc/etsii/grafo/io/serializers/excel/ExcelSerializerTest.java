package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestHelperFactory;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static es.urjc.etsii.grafo.testutil.TestHelperFactory.referencesGenerator;
import static es.urjc.etsii.grafo.testutil.TestHelperFactory.solutionGenerator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ExcelSerializerTest {

    private static final Objective<TestMove, TestSolution, TestInstance> OBJ_MAX = Objective.of("Test", FMode.MAXIMIZE, TestSolution::getScore, TestMove::getScoreChange);

    @BeforeAll
    static void configurePOI() {
        // In order to read the file due to the size of the pivot table
        ZipSecureFile.setMinInflateRatio(0.001);
        Context.Configurator.setObjectives(OBJ_MAX);
    }

    @BeforeEach
    void cleanInstance() {
        Instance.resetProperties();
    }

    ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p, List<ReferenceResultProvider> references, InstanceManager<TestInstance> instanceManager) {
        var config = new ExcelConfig();
        config.setFolder(p.toFile().getAbsolutePath());
        return new ExcelSerializer<>(config, references, customizer, instanceManager);
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
    void writeEmptyCalcExcel(@TempDir Path temp) {
        writeEmptyExcelParameters(temp, new ArrayList<>());
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
    void writeXLSXWithCustomizer(@TempDir Path temp) {
        var customizer = Mockito.mock(ExcelCustomizer.class);
        var excel = initExcel(Optional.of(customizer), temp);
        var excelPath = temp.resolve("test2.xlsx");

        var data = solutionGenerator();

        excel.serializeResults("TestExperiment", data, excelPath);
        Mockito.verify(customizer, Mockito.atLeastOnce()).customize(any());
        verifyNoMoreInteractions(customizer);
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

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Assertions.assertEquals(row.getLastCellNum(), 9);
                if (row.getCell(1).getStringCellValue().equals("TestProvider")) {
                    Assertions.assertEquals(row.getCell(3).getNumericCellValue(), 10.10);
                    Assertions.assertEquals(row.getCell(4).getNumericCellValue(), 10.10); // Seconds to nanoseconds
                }
            }
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
    void writeExcelWithCustomProperties(@TempDir Path temp) throws IOException {
        var instance = new TestInstance("writeCustomPropsTest");
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10), TestHelperFactory.simpleInstanceManager(instance));
        var excelPath = temp.resolve("customProperties.xlsx");
        
        // Create events with custom properties
        var data = Arrays.asList(
                TestHelperFactory.solutionGenerated("writeCustomPropsTest", "TestExp", "Alg1", 1, 100.0, 1000, 500, Map.of("prop1", 4, "prop2", 1)),
                TestHelperFactory.solutionGenerated("writeCustomPropsTest", "TestExp", "Alg1", 2, 95.0, 1100, 600, Map.of("prop1", 6, "prop2", 2))
        );
        
        excel.serializeResults("TestExperiment", data, excelPath);
        File excelFile = excelPath.toFile();

        try (var wb = new XSSFWorkbook(new FileInputStream(excelFile))) {
            XSSFSheet rawSheet = wb.getSheet(ExcelSerializer.RAW_SHEET);
            Iterator<Row> rowIterator = rawSheet.iterator();

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

}
