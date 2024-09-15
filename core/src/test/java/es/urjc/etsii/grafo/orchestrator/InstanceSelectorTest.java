package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestInstanceImporter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InstanceSelectorTest {

    @BeforeAll
    public static void resetProps(){
        TestInstance.resetProperties();
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
        assertEquals(7, lines.size());
        assertArrayEquals(new String[]{"id", "awesomeness", "ble", "random", "size"}, lines.get(0).split(","));
        assertArrayEquals(new String[]{"instanceA.txt", "1.0", "-7.1", "346.0", "100.0"}, lines.get(1).split(","));
        assertArrayEquals(new String[]{"instanceB.txt", "2.0", "-5.1123", "132768.0","6.0"}, lines.get(2).split(","));
        assertArrayEquals(new String[]{"instanceC.txt", "3.0", "0.1123", "4129.0","121111.0"}, lines.get(3).split(","));
        assertArrayEquals(new String[]{"instanceD.txt", "4.0", "0.0", "918624.0","0.0"}, lines.get(4).split(","));
        assertArrayEquals(new String[]{"instanceE.txt", "5.0", "0.1123", "-123.0","-121111.0"}, lines.get(5).split(","));
        assertArrayEquals(new String[]{"instanceF.txt", "6.0", "0.1123", "-123.0","-121111.0"}, lines.get(6).split(","));
    }

    @AfterAll
    public static void deleteTempFiles(){
        try {
            new File(InstanceSelector.DEFAULT_OUTPUT_PATH).delete();
        } catch (Exception ignored){}
    }
}