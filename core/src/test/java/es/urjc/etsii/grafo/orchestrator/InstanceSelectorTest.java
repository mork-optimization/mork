package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.io.InstanceImporter;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.testutil.TestInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class InstanceSelectorTest {
    private static Object tryParse(String s){
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e){
            return s;
        }
    }

    private class TestInstanceImporter extends InstanceImporter<TestInstance> {
        @Override
        public TestInstance importInstance(BufferedReader reader, String filename) {
            Map<String, Object> map = reader.lines()
                    .map(s -> s.split(","))
                    .collect(Collectors.toMap(s -> s[0], s -> tryParse(s[1])));
            return new TestInstance(filename, map);
        }
    }
    @Test
    public void checkCSV() throws IOException {
        var config = new InstanceConfiguration();
        config.setPath(Map.of("default", "src/test/resources/instances"));
        var instanceManager = new InstanceManager<>(config, new TestInstanceImporter());
        var eventMock = Mockito.mock(ApplicationEventPublisher.class);
        new EventPublisher(eventMock);

        var selector = new InstanceSelector<>(instanceManager, config); // The Python part is not actually tested!
        selector.run();
        var p = Path.of(InstanceSelector.DEFAULT_OUTPUT_PATH);
        var f = p.toFile();
        assertTrue(f.isFile());
        var lines = Files.readAllLines(p);
        assertEquals(6, lines.size());
        assertArrayEquals(new String[]{"id", "awesomeness", "ble", "random", "size"}, lines.get(0).split(","));
        assertArrayEquals(new String[]{"instanceA.txt", "1.0", "-7.1", "346.0", "100.0"}, lines.get(1).split(","));
        assertArrayEquals(new String[]{"instanceB.txt", "2.0", "-5.1123", "132768.0","6.0"}, lines.get(2).split(","));
        assertArrayEquals(new String[]{"instanceC.txt", "3.0", "0.1123", "4129.0","121111.0"}, lines.get(3).split(","));
        assertArrayEquals(new String[]{"instanceD.txt", "4.0", "0.0", "918624.0","0.0"}, lines.get(4).split(","));
        assertArrayEquals(new String[]{"instanceE.txt", "5.0", "0.1123", "-123.0","-121111.0"}, lines.get(5).split(","));
    }

    @AfterAll
    public static void deleteTempFiles(){
        try {
            new File(InstanceSelector.DEFAULT_OUTPUT_PATH).delete();
        } catch (Exception ignored){}
    }
}