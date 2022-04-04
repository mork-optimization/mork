package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.io.serializers.csv.CSVConfig;
import es.urjc.etsii.grafo.io.serializers.csv.CSVSerializer;
import es.urjc.etsii.grafo.testutil.HelperFactory;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class CSVSerializerTest {

    public CSVSerializer<TestSolution, TestInstance> initCSV(char separator, Path p){
        var config = new CSVConfig();
        config.setSeparator(separator);
        config.setFolder(p.toFile().getAbsolutePath());
        return new CSVSerializer<>(config);
    }

    @Test
    public void writeEmptyCSV(@TempDir Path temp) throws IOException {
        char separator = ',';
        var csv = initCSV(separator, temp);
        var csvPath = temp.resolve("test.csv");
        csv.serializeResults(new ArrayList<>(), csvPath);

        Assertions.assertTrue(Files.exists(csvPath));
        String csvContent = Files.readString(csvPath);
        String[] cols = csvContent.split(String.valueOf(separator));
        // CSV must contains 6 columns: intance name, algorithm name, iteration, value, t, ttb
        Assertions.assertEquals(cols.length, 6);
    }

    @Test
    public void writeCSVDataWithCustomSeparator(@TempDir Path temp) throws IOException {
        char separator = ';';
        var csv = initCSV(separator, temp);
        var csvPath = temp.resolve("test2.csv");

        var data = Arrays.asList(
                HelperFactory.solutionGenerated("fakeInstance", "fakeExp", "fakeAlg", 1, 2, 10, 8),
                HelperFactory.solutionGenerated("fakeInstance2", "fakeExp2", "fakeAlg2", 2, 4, 12, 7),
                HelperFactory.solutionGenerated("fakeInstance3", "fakeExp3", "fakeAlg3", 3, 5, 14, 6)
        );

        csv.serializeResults(data, csvPath);
        Assertions.assertTrue(Files.exists(csvPath));
        var csvContent = Files.readAllLines(csvPath);
        Assertions.assertEquals(data.size(), csvContent.size() - 1); // CSV has an extra row, the header
        for(var line: csvContent){
            String[] cols = line.split(String.valueOf(separator));
            Assertions.assertEquals(cols.length, 6);
        }
    }
}
