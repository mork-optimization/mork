package es.urjc.etsii.grafo.io.serializers.excel;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.solver.Mork;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static es.urjc.etsii.grafo.testutil.TestHelperFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ExcelSerializerTest {

    @BeforeAll
    public static void configurePOI() {
        // In order to read the file due to the size of the pivot table
        ZipSecureFile.setMinInflateRatio(0.001);
    }

    @BeforeEach
    public void cleanInstance() {
        Instance.resetProperties();
    }

    public ExcelSerializer<TestSolution, TestInstance> initExcel(boolean useJavaCalculation, Optional<ExcelCustomizer> customizer, Path p, List<ReferenceResultProvider> references, InstanceManager<TestInstance> instanceManager) {
        var config = new ExcelConfig();
        config.setFolder(p.toFile().getAbsolutePath());
        config.setCalculationMode(useJavaCalculation? ExcelConfig.CalculationMode.JAVA: ExcelConfig.CalculationMode.EXCEL);
        Mork.setSolvingMode(FMode.MAXIMIZE);
        return new ExcelSerializer<>(config, references, customizer, instanceManager);
    }

    public ExcelSerializer<TestSolution, TestInstance> initExcel(boolean useJavaCalculation, Optional<ExcelCustomizer> customizer, Path p, List<ReferenceResultProvider> references) {
        return initExcel(useJavaCalculation, customizer, p, references, TestHelperFactory.emptyInstanceManager());
    }

    public ExcelSerializer<TestSolution, TestInstance> initExcel(boolean useJavaCalculation, Optional<ExcelCustomizer> customizer, Path p) {
        return initExcel(useJavaCalculation, customizer, p, new ArrayList<>());
    }

    public void writeEmptyExcelParameters(boolean useJavaCalculation, Path temp, List<ReferenceResultProvider> references) {
        var excel = initExcel(useJavaCalculation, Optional.empty(), temp, references);
        var excelPath = temp.resolve("test.xlsx");
        excel.serializeResults("TestExperiment", new ArrayList<>(), excelPath);

        Assertions.assertTrue(Files.exists(excelPath));
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void writeEmptyCalcExcel(boolean useJavaCalculation, @TempDir Path temp) throws IOException {
        writeEmptyExcelParameters(useJavaCalculation, temp, new ArrayList<>());
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void writeEmptyExcelWithReferences(boolean useJavaCalculation, @TempDir Path temp) throws IOException {
        writeEmptyExcelParameters(useJavaCalculation, temp, referencesGenerator(10, 10));
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void writeEmptyCSVWithInvalidReference(boolean useJavaCalculation, @TempDir Path temp) {
        Assertions.assertDoesNotThrow(() -> writeEmptyExcelParameters(useJavaCalculation, temp, referencesGenerator(Double.NaN, Double.NaN)));
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void writeEmptyCSVInvalidPath(boolean useJavaCalculation) {
        Assertions.assertThrows(RuntimeException.class, () -> writeEmptyExcelParameters(useJavaCalculation, Path.of("/doesnotexist"), referencesGenerator(Double.NaN, Double.NaN)));
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void writeXLSXWithCustomizer(boolean useJavaCalculation, @TempDir Path temp) {
        var customizer = Mockito.mock(ExcelCustomizer.class);
        var excel = initExcel(useJavaCalculation, Optional.of(customizer), temp);
        var excelPath = temp.resolve("test2.xlsx");

        var data = solutionGenerator();

        excel.serializeResults("TestExperiment", data, excelPath);
        Mockito.verify(customizer, Mockito.atLeastOnce()).customize(any());
        verifyNoMoreInteractions(customizer);
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void writeXLSXWithReferences(boolean useJavaCalculation, @TempDir Path temp) throws IOException {
        var excel = initExcel(useJavaCalculation, Optional.empty(), temp, referencesGenerator(10.10, 10.10));
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

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void writeEmptyInstanceSheet(boolean useJavaCalculation, @TempDir Path temp) throws IOException {
        var excel = initExcel(useJavaCalculation, Optional.empty(), temp, referencesGenerator(10.10, 10.10));
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

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void writeInstanceSheet(boolean useJavaCalculation, @TempDir Path temp) throws IOException {
        var instance = new TestInstance("writeInstanceSheetTest");
        instance.setProperty("customProperty", 1234567);
        var excel = initExcel(useJavaCalculation, Optional.empty(), temp, referencesGenerator(10.10, 10.10), TestHelperFactory.simpleInstanceManager(instance));
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

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    public void writeExcelWithCustomProperties(boolean useJavaCalculation, @TempDir Path temp) throws IOException {
        var instance = new TestInstance("writeInstanceSheetTest");
        instance.setProperty("customProperty", 1234567);
        var excel = initExcel(useJavaCalculation, Optional.empty(), temp, referencesGenerator(10.10, 10.10), TestHelperFactory.simpleInstanceManager(instance));
        var excelPath = temp.resolve("instanceSheet.xlsx");
        var data = solutionWithCustomPropertiesGenerator();
        excel.serializeResults("TestExperiment", data, excelPath);
        File excelFile = excelPath.toFile();

        try (var wb = new XSSFWorkbook(new FileInputStream(excelFile))) {
            XSSFSheet instanceSheet = wb.getSheet(ExcelSerializer.RAW_SHEET);
            Iterator<Row> rowIterator = instanceSheet.iterator();

            Assertions.assertTrue(rowIterator.hasNext(), "Missing headers in raw result sheet");
            var header = rowIterator.next();
            Assertions.assertEquals(11, header.getLastCellNum(), "Missing headers in instance sheet");
            // Get the index of "prop1"
            var prop1Index = -1;
            for (int i = 0; i < header.getLastCellNum(); i++) {
                if (header.getCell(i).getStringCellValue().equals("prop1")) {
                    prop1Index = i;
                    break;
                }
            }
            Assertions.assertTrue(prop1Index >= 0, "Missing prop1 header in raw result sheet");
            // Get the index of "prop2"
            var prop2Index = -1;
            for (int i = 0; i < header.getLastCellNum(); i++) {
                if (header.getCell(i).getStringCellValue().equals("prop2")) {
                    prop2Index = i;
                    break;
                }
            }
            Assertions.assertTrue(prop2Index >= 0, "Missing prop2 header in raw result sheet");
            Assertions.assertTrue(rowIterator.hasNext(), "Missing values in raw result sheet");
            var firstData = rowIterator.next();
            Assertions.assertEquals(4, firstData.getCell(prop1Index).getNumericCellValue(), "Data of solution custom properties is not correct in raw result sheet");
            Assertions.assertEquals(1, firstData.getCell(prop2Index).getNumericCellValue(), "Data of solution custom properties is not correct in raw result sheet");
            var secondData = rowIterator.next();
            Assertions.assertEquals(6, secondData.getCell(prop1Index).getNumericCellValue(), "Data of solution custom properties is not correct in raw result sheet");
            Assertions.assertEquals(2, secondData.getCell(prop2Index).getNumericCellValue(), "Data of solution custom properties is not correct in raw result sheet");
        }
    }

    @Test
    public void serializerMode(){
        var config = new ExcelConfig();
        int defaultRowThreshold = config.getRowThreshold();
        Assertions.assertTrue(defaultRowThreshold > 0);

        config.setCalculationMode(ExcelConfig.CalculationMode.JAVA);
        var sheetWriter = ExcelSerializer.getRawSheetWriter(config, new ArrayList<>());
        Assertions.assertTrue(sheetWriter instanceof JavaCalculatedRawSheetWriter);

        config.setCalculationMode(ExcelConfig.CalculationMode.EXCEL);
        sheetWriter = ExcelSerializer.getRawSheetWriter(config, new ArrayList<>());
        Assertions.assertTrue(sheetWriter instanceof ExcelCalculatedRawSheetWriter);

        config.setCalculationMode(ExcelConfig.CalculationMode.AUTO);
        sheetWriter = ExcelSerializer.getRawSheetWriter(config, new ArrayList<>());
        Assertions.assertTrue(sheetWriter instanceof ExcelCalculatedRawSheetWriter);

        sheetWriter = ExcelSerializer.getRawSheetWriter(config, listOfN(1000));
        Assertions.assertTrue(sheetWriter instanceof ExcelCalculatedRawSheetWriter);

        sheetWriter = ExcelSerializer.getRawSheetWriter(config, listOfN(3000));
        Assertions.assertTrue(sheetWriter instanceof JavaCalculatedRawSheetWriter);

        sheetWriter = ExcelSerializer.getRawSheetWriter(config, listOfN(2000));
        Assertions.assertTrue(sheetWriter instanceof ExcelCalculatedRawSheetWriter);
    }

    private List<? extends SolutionGeneratedEvent<?,?>> listOfN(int n){
        var event = TestHelperFactory.solutionGenerated("fakeInstance", "fakeExp", "fakeAlg", -1, 2, 10, 8);
        var list = new ArrayList<SolutionGeneratedEvent<?,?>>(n);
        for (int i = 0; i < n; i++) {
            list.add(event);
        }
        return list;
    }
}
