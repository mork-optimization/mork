package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.io.serializers.excel.ExcelConfig;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelCustomizer;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelSerializer;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.testutil.HelperFactory;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ExcelSerializerTest {

    public ExcelSerializer<TestSolution, TestInstance> initExcel(Optional<ExcelCustomizer> customizer, Path p){
        var config = new ExcelConfig();
        config.setFolder(p.toFile().getAbsolutePath());
        config.setCalculationMode(ExcelConfig.CalculationMode.AUTO);
        var solverConfig = new SolverConfig();
        solverConfig.setMaximizing(true);
        return new ExcelSerializer<>(config, solverConfig, new ArrayList<>(), customizer);
    }

    @Test
    public void writeEmptyExcel(@TempDir Path temp) {
        var excel = initExcel(Optional.empty(), temp);
        var excelPath = temp.resolve("test.xlsx");
        excel.serializeResults(new ArrayList<>(), excelPath);

        Assertions.assertTrue(Files.exists(excelPath));
    }

    @Test
    public void writeXLSXWithCustomizer(@TempDir Path temp) {
        var customizer = Mockito.mock(ExcelCustomizer.class);
        var excel = initExcel(Optional.of(customizer), temp);
        var excelPath = temp.resolve("test2.xlsx");

        var data = Arrays.asList(
                HelperFactory.solutionGenerated("fakeInstance", "fakeExp", "fakeAlg", 1, 2, 10, 8),
                HelperFactory.solutionGenerated("fakeInstance2", "fakeExp2", "fakeAlg2", 2, 4, 12, 7),
                HelperFactory.solutionGenerated("fakeInstance3", "fakeExp3", "fakeAlg3", 3, 5, 14, 6)
        );

        excel.serializeResults(data, excelPath);
        Mockito.verify(customizer, Mockito.atLeastOnce()).customize(any());
        verifyNoMoreInteractions(customizer);
    }
}
