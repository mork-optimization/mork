package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.testutil.TestInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class InstanceSelectorTest {

    private TestInstance instanceA = new TestInstance("instanceA", Map.of("size", 100, "ble", -7.1, "ignore", "ignored"));
    private TestInstance instanceB = new TestInstance("instanceB", Map.of("ble", -5.1123, "ignore", "ignored", "size", 6));
    @Test
    public void checkCSV() throws IOException {
        InstanceManager<TestInstance> mock = Mockito.mock(InstanceManager.class);
        when(mock.getInstanceSolveOrder(anyString(), eq(false))).thenReturn(List.of("instanceA", "instanceB"));
        when(mock.getInstance(anyString())).thenReturn(instanceA, instanceB);

        var selector = new InstanceSelector<>(mock, new InstanceConfiguration()); // The Python part is not actually tested!
        selector.run();
        var p = Path.of(InstanceSelector.DEFAULT_OUTPUT_PATH);
        var f = p.toFile();
        assertTrue(f.isFile());
        var lines = Files.readAllLines(p);
        assertEquals(3, lines.size());
        assertArrayEquals(new String[]{"id", "ble", "size"}, lines.get(0).split(","));
        assertArrayEquals(new String[]{"instanceA", "-7.1", "100.0"}, lines.get(1).split(","));
        assertArrayEquals(new String[]{"instanceB", "-5.1123", "6.0"}, lines.get(2).split(","));
    }

    @AfterAll
    public static void deleteTempFiles(){
        try {
            new File(InstanceSelector.DEFAULT_OUTPUT_PATH).delete();
        } catch (Exception ignored){}
    }
}