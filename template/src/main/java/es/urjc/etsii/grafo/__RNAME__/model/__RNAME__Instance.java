package es.urjc.etsii.grafo.__RNAME__.model;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Comparator;
import java.util.Map;

public class __RNAME__Instance extends Instance {

    public __RNAME__Instance(String name){
        super(name);
        // TODO Add all required fields and parameters
    }


//    /**
//     * How should instances be ordered, when listing and solving them.
//     * If not implemented, defaults to lexicographic sort by instance name
//     * @param other the other instance to be compared against this one
//     * @return comparison result
//     */
//    @Override
//    public int compareTo(Instance other) {
//        var otherInstance = (__RNAME__Instance) other;
//        return Integer.compare(this.size, otherInstance.size);
//    }

//    /**
//     * Define custom properties for the instance
//     * @return Map of properties, with each entry containing the property name and its value
//     */
//    @Override
//    public Map<String, Object> customProperties() {
//        var properties =  super.customProperties();
//        properties.put("MyInstancePropertyName", 7);
//        properties.put("MyInstanceProperty2Name", "Hello world");
//        return properties;
//    }
}
