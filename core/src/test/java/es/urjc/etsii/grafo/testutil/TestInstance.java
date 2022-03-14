package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Comparator;

public class TestInstance extends Instance {
    public TestInstance(String name) {
        super(name);
    }

    @Override
    public int compareTo(Instance o) {
        return Comparator.comparing(Instance::getPath).reversed().compare(this, o);
    }
}
