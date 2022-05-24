package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelConfig;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelCustomizer;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelSerializer;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.testutil.TestHelperFactory;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static es.urjc.etsii.grafo.testutil.TestHelperFactory.referencesGenerator;
import static es.urjc.etsii.grafo.testutil.TestHelperFactory.solutionGenerator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ExcelSerializerTest {

    @BeforeAll
    public static void configurePOI(){
        // In order to read the file due to the size of the pivot table
        ZipSecureFile.setMinInflateRatio(0.001);
    }

    @BeforeEach
    public void cleanInstance(){
        Instance.resetProperties();
    }

    public ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p, List<ReferenceResultProvider> references, InstanceManager<TestInstance> instanceManager) {
        var config = new ExcelConfig();
        config.setFolder(p.toFile().getAbsolutePath());
        config.setCalculationMode(ExcelConfig.CalculationMode.AUTO);
        var solverConfig = new SolverConfig();
        solverConfig.setMaximizing(true);
        return new ExcelSerializer<>(config, solverConfig, references, customizer, instanceManager);
    }

    public ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p, List<ReferenceResultProvider> references) {
        return initExcel(customizer, p, references, TestHelperFactory.emptyInstanceManager());
    }

    public ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p) {
        return initExcel(customizer, p, new ArrayList<>());
    }

    public void writeEmptyExcelParameters(Path temp, List<ReferenceResultProvider> references) {
        var excel = initExcel(Optional.empty(), temp, references);
        var excelPath = temp.resolve("test.xlsx");
        excel.serializeResults("TestExperiment", new ArrayList<>(), excelPath);

        Assertions.assertTrue(Files.exists(excelPath));
    }

    @Test
    public void writeEmptyExcel(@TempDir Path temp) throws IOException {
        writeEmptyExcelParameters(temp, new ArrayList<>());
    }

    @Test
    public void writeEmptyExcelWithReferences(@TempDir Path temp) throws IOException {
        writeEmptyExcelParameters(temp, referencesGenerator(10, 10));
    }

    @Test
    public void writeEmptyCSVWithInvalidReference(@TempDir Path temp) {
        Assertions.assertDoesNotThrow(() -> writeEmptyExcelParameters(temp, referencesGenerator(Double.NaN,Double.NaN)));
    }

    @Test
    public void writeEmptyCSVInvalidPath() {
        Assertions.assertThrows(RuntimeException.class, () -> writeEmptyExcelParameters(Path.of("/doesnotexist"), referencesGenerator(Double.NaN,Double.NaN)));
    }

    @Test
    public void writeXLSXWithCustomizer(@TempDir Path temp) {
        var customizer = Mockito.mock(ExcelCustomizer.class);
        var excel = initExcel(Optional.of(customizer), temp);
        var excelPath = temp.resolve("test2.xlsx");

        var data = solutionGenerator();

        excel.serializeResults("TestExperiment", data, excelPath);
        Mockito.verify(customizer, Mockito.atLeastOnce()).customize(any());
        verifyNoMoreInteractions(customizer);
    }

    @Test
    public void writeXLSXWithReferences(@TempDir Path temp) throws IOException {
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10));
        var excelPath = temp.resolve("test2.xlsx");

        var data = solutionGenerator();

        excel.serializeResults("TestExperiment", data, excelPath);

        File excelFile = excelPath.toFile();
        try (var workbook = new XSSFWorkbook(new FileInputStream(excelFile))) {

            XSSFSheet rawResults = workbook.getSheetAt(1);
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
    public void writeEmptyInstanceSheet(@TempDir Path temp) throws IOException {
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10));
        var excelPath = temp.resolve("emptyInstanceSheet.xlsx");
        var data = solutionGenerator();
        excel.serializeResults("TestExperiment", data, excelPath);
        File excelFile = excelPath.toFile();

        try (var wb = new XSSFWorkbook(new FileInputStream(excelFile))) {
            XSSFSheet instanceSheet = wb.getSheetAt(2);
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
    public void writeInstanceSheet(@TempDir Path temp) throws IOException {
        var instance = new TestInstance("writeInstanceSheetTest");
        instance.setProperty("customProperty", 1234567);
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10), TestHelperFactory.simpleInstanceManager(instance));
        var excelPath = temp.resolve("instanceSheet.xlsx");
        var data = solutionGenerator();
        excel.serializeResults("TestExperiment", data, excelPath);
        File excelFile = excelPath.toFile();

        try (var wb = new XSSFWorkbook(new FileInputStream(excelFile))) {
            XSSFSheet instanceSheet = wb.getSheetAt(2);
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
}
