package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Comparator;
import java.util.Map;

public class TestInstance extends Instance {
    public TestInstance(String name) {
        super(name);
    }

    public TestInstance(String name, Map<String, Object> properties){
        super(name);
        for(var e: properties.entrySet()){
            this.setProperty(e.getKey(), e.getValue());
        }
    }

    @Override
    public int compareTo(Instance o) {
        return Comparator.comparing(Instance::getPath).reversed().compare(this, o);
    }
}
