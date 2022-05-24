package es.urjc.etsii.grafo.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InstanceTests {

    @Test
    void customInstanceProperties(){
        var instance = new _Instance("fakeInstance");
        String key = "nNodes";
        Object value = 19;

        Assertions.assertThrows(IllegalArgumentException.class, () -> instance.getProperty(key));
        instance.setProperty(key, value);
        Assertions.assertEquals(value, instance.getProperty(key));
    }

    private static class _Instance extends Instance {

        /**
         * Creates a new instance
         *
         * @param id instance id or instance name
         */
        protected _Instance(String id) {
            super(id);
        }

    }
}
