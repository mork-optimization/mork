package es.urjc.etsii.grafo.solver.irace;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class IraceConfiguration {

    //candidateConfiguration, instanceId, seed, instanceName, config, isMaximizing
    private final String candidateConfiguration;
    private final String instanceId;
    private final String seed;
    private final String instanceName;
    private final Map<String, String> alg;
    private final boolean isMaximizing;

    public IraceConfiguration(String candidateConfiguration, String instanceId, String seed, String instanceName, Map<String, String> alg, boolean isMaximizing) {
        this.candidateConfiguration = candidateConfiguration;
        this.instanceId = instanceId;
        this.alg = alg;
        this.instanceName = instanceName;
        this.seed = seed;
        this.isMaximizing = isMaximizing;
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

    public boolean isMaximizing(){
        return this.isMaximizing;
    }

    public String getCandidateConfiguration() {
        return candidateConfiguration;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
