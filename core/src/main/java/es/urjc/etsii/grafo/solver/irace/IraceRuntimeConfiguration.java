package es.urjc.etsii.grafo.solver.irace;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * <p>IraceRuntimeConfiguration class.</p>
 *
 */
public class IraceRuntimeConfiguration {

    //candidateConfiguration, instanceId, seed, instancePath, config, isMaximizing
    private final String candidateConfiguration;
    private final String instanceId;
    private final String seed;
    private final String instanceName;
    private final Map<String, String> alg;
    private final boolean isMaximizing;

    /**
     * <p>Constructor for IraceRuntimeConfiguration.</p>
     *
     * @param candidateConfiguration a {@link java.lang.String} object.
     * @param instanceId a {@link java.lang.String} object.
     * @param seed a {@link java.lang.String} object.
     * @param instanceName a {@link java.lang.String} object.
     * @param alg a {@link java.util.Map} object.
     * @param isMaximizing a boolean.
     */
    public IraceRuntimeConfiguration(String candidateConfiguration, String instanceId, String seed, String instanceName, Map<String, String> alg, boolean isMaximizing) {
        this.candidateConfiguration = candidateConfiguration;
        this.instanceId = instanceId;
        this.alg = alg;
        this.instanceName = instanceName;
        this.seed = seed;
        this.isMaximizing = isMaximizing;
    }

    /**
     * Get the value of a config property
     *
     * @param property config key
     * @return Optional with value if key was in config map, empty Optional if key was not in config map.
     */
    public Optional<String> getValue(String property){
        return Optional.ofNullable(alg.get(property));
    }

    /**
     * Get the value of a config property
     *
     * @param key config key
     * @param defaultValue value to return if key is not in config map
     * @return Config value if key present, default value otherwise
     */
    public String getValue(String key, String defaultValue){
        return alg.getOrDefault(key, defaultValue);
    }

    /**
     * Get the value of a config property and parse as integer value
     *
     * @param key config key
     * @return Optional with value if key was in config map, empty Optional if key was not in config map.
     */
    public Optional<Integer> getValueAsInt(String key){
        return getValue(key).map(Integer::parseInt);
    }

    /**
     * Get the value of a config property and parse as integer value
     *
     * @param key config key
     * @param defaultValue value to return if key is not in config map
     * @return Config value if key present, default value otherwise
     */
    public int getValueAsInt(String key, int defaultValue){
        return getValueAsInt(key).orElse(defaultValue);
    }

    /**
     * Get the value of a config property and parse as double value
     *
     * @param key config key
     * @return Optional with value if key was in config map, empty Optional if key was not in config map.
     */
    public Optional<Double> getValueAsDouble(String key){
        return getValue(key).map(Double::parseDouble);
    }

    /**
     * Get the value of a config property and parse as a double value
     *
     * @param key config key
     * @param defaultValue value to return if key is not in config map
     * @return Config value as double if key present, default value otherwise
     */
    public double getValueAsDouble(String key, double defaultValue){
        return getValueAsDouble(key).orElse(defaultValue);
    }

    /**
     * Get all config properties related to the irace algorithm
     *
     * @return all key/values as a map.
     */
    public Map<String, String> getConfig(){
        return Collections.unmodifiableMap(this.alg);
    }

    /**
     * <p>Getter for the field <code>instancePath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstanceName(){
        return instanceName;
    }

    /**
     * <p>Getter for the field <code>seed</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSeed(){
        return seed;
    }

    /**
     * <p>isMaximizing.</p>
     *
     * @return a boolean.
     */
    public boolean isMaximizing(){
        return this.isMaximizing;
    }

    /**
     * <p>Getter for the field <code>candidateConfiguration</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCandidateConfiguration() {
        return candidateConfiguration;
    }

    /**
     * <p>Getter for the field <code>instanceId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String toString() {
        return "IraceRuntimeConfiguration{" +
                "candidateConfiguration='" + candidateConfiguration + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", seed='" + seed + '\'' +
                ", instancePath='" + instanceName + '\'' +
                ", alg=" + alg +
                ", isMaximizing=" + isMaximizing +
                '}';
    }
}
