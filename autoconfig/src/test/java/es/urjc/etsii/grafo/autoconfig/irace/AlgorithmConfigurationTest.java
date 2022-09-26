package es.urjc.etsii.grafo.autoconfig.irace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AlgorithmConfigurationTest {

    @Test
    void testFails(){
        // Fail due to duplicated key
        assertThrows(IllegalArgumentException.class,() -> new AlgorithmConfiguration(new String[]{
            "dup=1", "xyz=2", "dup=3"
        }));

        // Fail due to missing value
        assertThrows(IllegalArgumentException.class,() -> new AlgorithmConfiguration(new String[]{
                "asd=1", "xyz=2", "whatever-1", "aaqe=3"
        }));
    }

    @Test
    void testGetFromStringArray(){
        var config = new AlgorithmConfiguration(new String[]{
                "int=-51",
                "double=10e1",
                "string=eeeeeee"
        });
        Assertions.assertEquals(3, config.getConfig().size());

        Assertions.assertTrue(config.getValueAsInt("int").isPresent());
        Assertions.assertEquals(-51, config.getValueAsInt("int").get());
        Assertions.assertEquals(-51, config.getValueAsInt("int", 0));

        Assertions.assertTrue(config.getValueAsDouble("double").isPresent());
        Assertions.assertEquals(10e1, config.getValueAsDouble("double").get());
        Assertions.assertEquals(10e1, config.getValueAsDouble("double", 0));

        Assertions.assertTrue(config.getValue("string").isPresent());
        Assertions.assertEquals("eeeeeee", config.getValue("string").get());
        Assertions.assertEquals("eeeeeee", config.getValue("string", "defaultValue"));
    }

    // Copy pasted test for the other constructor method
    @Test
    void testGetFromStringMap(){
        var config = new AlgorithmConfiguration(Map.of(
                "int", "-51",
                "double", "10e1",
                "string", "eeeeeee"
        ));
        Assertions.assertEquals(3, config.getConfig().size());

        Assertions.assertTrue(config.getValueAsInt("int").isPresent());
        Assertions.assertEquals(-51, config.getValueAsInt("int").get());
        Assertions.assertEquals(-51, config.getValueAsInt("int", 0));

        Assertions.assertTrue(config.getValueAsDouble("double").isPresent());
        Assertions.assertEquals(10e1, config.getValueAsDouble("double").get());
        Assertions.assertEquals(10e1, config.getValueAsDouble("double", 0));

        Assertions.assertTrue(config.getValue("string").isPresent());
        Assertions.assertEquals("eeeeeee", config.getValue("string").get());
        Assertions.assertEquals("eeeeeee", config.getValue("string", "defaultValue"));
    }
}