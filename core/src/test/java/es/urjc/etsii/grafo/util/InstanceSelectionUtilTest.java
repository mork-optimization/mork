package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.testutil.TestInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstanceSelectionUtilTest {
    @TempDir Path temp;
    @Test void propertiesPreservePathIdentityAndQuoteNames() throws Exception {
        @SuppressWarnings("unchecked") InstanceManager<TestInstance> manager = mock(InstanceManager.class);
        var a = new TestInstance("same,\"id\"", Map.of("n", 10, "label", "text"));
        var b = new TestInstance("same,\"id\"", Map.of("n", 20));
        String first = temp.resolve("a.zip") + "!folder/instance.txt";
        String second = temp.resolve("b.zip") + "!folder/instance.txt";
        when(manager.getInstance(first)).thenReturn(a); when(manager.getInstance(second)).thenReturn(b);
        Path file = temp.resolve("properties.csv");
        InstanceSelectionUtil.writeProperties(file, List.of(first, second), manager);
        String csv = Files.readString(file);
        assertTrue(csv.contains("\"same,\"\"id\"\"\""));
        assertTrue(csv.contains(first)); assertTrue(csv.contains(second));
        assertFalse(csv.contains("\"label\"")); assertEquals(3, csv.lines().count());
    }
}
