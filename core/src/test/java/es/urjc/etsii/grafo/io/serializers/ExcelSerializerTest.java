package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.io.serializers.excel.ExcelConfig;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelCustomizer;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelSerializer;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.testutil.HelperFactory;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static es.urjc.etsii.grafo.io.serializers.SerializerHelper.referencesGenerator;
import static es.urjc.etsii.grafo.io.serializers.SerializerHelper.solutionGenerator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ExcelSerializerTest {

    public ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p, List<ReferenceResultProvider> references) {
        var config = new ExcelConfig();
        config.setFolder(p.toFile().getAbsolutePath());
        config.setCalculationMode(ExcelConfig.CalculationMode.AUTO);
        var solverConfig = new SolverConfig();
        solverConfig.setMaximizing(true);
        return new ExcelSerializer<>(config, solverConfig, references, customizer);
    }

    public ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p) {
        return initExcel(customizer, p, new ArrayList<>());
    }

    public void writeEmptyExcelParameters(Path temp, List<ReferenceResultProvider> references) {
        var excel = initExcel(Optional.empty(), temp, references);
        var excelPath = temp.resolve("test.xlsx");
        excel.serializeResults(new ArrayList<>(), excelPath);

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
    public void writeXLSXWithCustomizer(@TempDir Path temp) {
        var customizer = Mockito.mock(ExcelCustomizer.class);
        var excel = initExcel(Optional.of(customizer), temp);
        var excelPath = temp.resolve("test2.xlsx");

        var data = solutionGenerator();

        excel.serializeResults(data, excelPath);
        Mockito.verify(customizer, Mockito.atLeastOnce()).customize(any());
        verifyNoMoreInteractions(customizer);
    }

    @Test
    public void writeXLSXWithReferences(@TempDir Path temp) throws IOException {
        var excel = initExcel(Optional.empty(), temp, referencesGenerator(10.10, 10.10));
        var excelPath = temp.resolve("test2.xlsx");

        var data = solutionGenerator();

        excel.serializeResults(data, excelPath);

        File excelFile = excelPath.toFile();
        // In order to read the file due to the size of the pivot table
        ZipSecureFile.setMinInflateRatio(0.001);
        try (
                FileInputStream fis = new FileInputStream(excelFile);
        ) {
            XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
            XSSFSheet mySheet = myWorkBook.getSheetAt(1);
            Iterator<Row> rowIterator = mySheet.iterator();

            Assertions.assertEquals(data.size() * 2, mySheet.getPhysicalNumberOfRows() - 1); // There are headers

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
}
