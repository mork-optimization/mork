package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.io.serializers.csv.CSVConfig;
import es.urjc.etsii.grafo.io.serializers.csv.CSVSerializer;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static es.urjc.etsii.grafo.testutil.TestHelperFactory.referencesGenerator;
import static es.urjc.etsii.grafo.testutil.TestHelperFactory.solutionGenerator;

public class CSVSerializerTest {


    public CSVSerializer<TestSolution, TestInstance> initCSV(char separator, Path p) {
        return initCSV(separator, p, new ArrayList<>());
    }

    public CSVSerializer<TestSolution, TestInstance> initCSV(char separator, Path p, List<ReferenceResultProvider> references) {
        var config = new CSVConfig();
        config.setSeparator(separator);
        config.setFolder(p.toFile().getAbsolutePath());
        return new CSVSerializer<>(config, references);
    }


    public void writeEmptyCSVParameters(Path temp, List<ReferenceResultProvider> providers) throws IOException {
        char separator = ',';
        var csv = initCSV(separator, temp, providers);
        var csvPath = temp.resolve("test.csv");
        csv.serializeResults(new ArrayList<>(), csvPath);

        Assertions.assertTrue(Files.exists(csvPath));
        String csvContent = Files.readString(csvPath);
        String[] cols = csvContent.split(String.valueOf(separator));
        // CSV must contain 6 columns: instance name, algorithm name, iteration, value, t, ttb
        Assertions.assertEquals(cols.length, 6);
    }

    @Test
    public void writeEmptyCSV(@TempDir Path temp) throws IOException {
        writeEmptyCSVParameters(temp, new ArrayList<>());
    }

    @Test
    public void writeEmptyCSVWithReferences(@TempDir Path temp) throws IOException {
        writeEmptyCSVParameters(temp, referencesGenerator(10,10));
    }

    @Test
    public void writeEmptyCSVWithInvalidReference(@TempDir Path temp) {
        Assertions.assertDoesNotThrow(() -> writeEmptyCSVParameters(temp, referencesGenerator(Double.NaN,Double.NaN)));
    }

    @Test
    public void writeEmptyCSVInvalidPath() {
        Assertions.assertThrows(RuntimeException.class, () -> writeEmptyCSVParameters(Path.of("/doesnotexist"), referencesGenerator(Double.NaN,Double.NaN)));
    }

    @Test
    public void writeCSVDataWithCustomSeparator(@TempDir Path temp) throws IOException {
        char separator = ';';
        var csv = initCSV(separator, temp);
        var csvPath = temp.resolve("test2.csv");
        var data = solutionGenerator();

        csv.serializeResults(data, csvPath);
        Assertions.assertTrue(Files.exists(csvPath));
        var csvContent = Files.readAllLines(csvPath);
        Assertions.assertEquals(data.size(), csvContent.size() - 1); // CSV has an extra row, the header
        for (var line : csvContent) {
            String[] cols = line.split(String.valueOf(separator));
            Assertions.assertEquals(cols.length, 6);
        }
    }

    @Test
    public void writeCSVDataWithCustomSeparatorWithReferences(@TempDir Path temp) throws IOException {
        char separator = ';';
        var references = referencesGenerator(10.10,10.10);
        var csv = initCSV(separator, temp, references);
        var csvPath = temp.resolve("test2.csv");
        var data = solutionGenerator();

        csv.serializeResults(data, csvPath);
        Assertions.assertTrue(Files.exists(csvPath));
        var csvContent = Files.readAllLines(csvPath);
        Assertions.assertEquals(data.size() * 2, csvContent.size() - 1); // CSV has an extra row, there are references
        for (var line : csvContent) {
            String[] cols = line.split(String.valueOf(separator));
            Assertions.assertEquals(cols.length, 6);
            if(cols[1].equals("TestProvider")){
                Assertions.assertEquals(Double.parseDouble(cols[3]), 10.10);
                Assertions.assertEquals(Long.parseLong(cols[4]), 10.10 * 1000000000L); // Seconds to nanoseconds

            }
        }
    }
}
