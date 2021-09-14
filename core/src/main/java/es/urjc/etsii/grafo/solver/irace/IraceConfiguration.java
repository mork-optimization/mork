package es.urjc.etsii.grafo.solver.irace;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class IraceConfiguration {

    private final Map<String, String> alg;
    private final String instanceName;
    private final String seed;

    public IraceConfiguration(Map<String, String> alg, String instanceName, String seed) {
        this.alg = alg;
        this.instanceName = instanceName;
        this.seed = seed;
    }

    /**
     * Get the value of a config property
     * @param property config key
     * @return Optional with value if key was in config map, empty Optional if key was not in config map.
     */
    public Optional<String> getValue(String property){
        return Optional.ofNullable(alg.get(property));
    }

    /**
     * Get the value of a config property
     * @param key config key
     * @param defaultValue value to return if key is not in config map
     * @return Config value if key present, default value otherwise
     */
    public String getValue(String key, String defaultValue){
        return alg.getOrDefault(key, defaultValue);
    }

    /**
     * Get all config properties related to the irace algorithm
     * @return all key/values as a map.
     */
    public Map<String, String> getConfig(){
        return Collections.unmodifiableMap(this.alg);
    }

    public String getInstanceName(){
        return instanceName;
    }

    public String getSeed(){
        return seed;
    }

}
