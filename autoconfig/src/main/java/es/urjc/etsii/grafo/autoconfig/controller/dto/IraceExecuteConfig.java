package es.urjc.etsii.grafo.autoconfig.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class IraceExecuteConfig {
    @JsonProperty("id.configuration")
    private String name;

    @JsonProperty("id.instance")
    private int instanceId;

    @JsonProperty("instance")
    private String instance;

    @JsonProperty("seed")
    private int seed;

    @JsonProperty("configuration")
    private Map<String, Object> configuration;

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

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
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
