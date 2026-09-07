package es.urjc.etsii.grafo.autoconfig.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class IraceExecuteConfig {
    @JsonProperty("id_configuration")
    private String name;

    @JsonProperty("id_instance")
    private int instanceId;

    @JsonProperty("instance")
    private String instance;

    @JsonProperty("seed")
    private int seed;

    @JsonProperty("configuration")
    private Map<String, String> configuration;

    public static IraceExecuteConfig of(String name, int instanceId, String instance, int seed, Map<String, String> configuration) {
        IraceExecuteConfig config = new IraceExecuteConfig();
        config.setName(name);
        config.setInstanceId(instanceId);
        config.setInstance(instance);
        config.setSeed(seed);
        config.setConfiguration(configuration);
        return config;
    }

    protected IraceExecuteConfig() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    public void checkValid() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Configuration ID cannot be blank");
        }
        if (instance == null || instance.isBlank()) {
            throw new IllegalArgumentException("Instance cannot be blank");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("Algorithm configuration cannot be null");
        }
    }

    @Override
    public String toString() {
        return "IraceExecuteConfig{" +
                "name='" + name + '\'' +
                ", instanceId=" + instanceId +
                ", instance='" + instance + '\'' +
                ", seed=" + seed +
                ", configuration=" + configuration +
                '}';
    }
}
