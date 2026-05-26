package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;

/**
 * <p>Irace run configuration. Contains all data provided by irace for any given run</p>
 */
public class IraceRuntimeConfiguration {

    //candidateConfiguration, instanceId, seed, instancePath, config, isMaximizing
    private final String candidateConfiguration;
    private final String instanceId;
    private final long seed;
    private final String instanceName;
    private final AlgorithmConfiguration algorithmConfiguration;

    /**
     * <p>Constructor for IraceRuntimeConfiguration.</p>
     *
     * @param candidateConfiguration full configuration as a string
     * @param instanceId             instance id
     * @param seed                   seed for the random manager
     * @param instanceName           intance name
     * @param algorithmConfiguration                    algorithm configuration
     */
    public IraceRuntimeConfiguration(String candidateConfiguration, String instanceId, long seed, String instanceName, AlgorithmConfiguration algorithmConfiguration) {
        this.candidateConfiguration = candidateConfiguration;
        this.instanceId = instanceId;
        this.algorithmConfiguration = algorithmConfiguration;
        this.instanceName = instanceName;
        this.seed = seed;
    }

    public IraceRuntimeConfiguration(String name, int instanceId, long seed, String instance, AlgorithmConfiguration algorithmConfiguration) {
        this(name, String.valueOf(instanceId), seed, instance, algorithmConfiguration);
    }

    public IraceRuntimeConfiguration(IraceExecuteConfig config) {
        this.candidateConfiguration = config.getName();
        this.instanceId = String.valueOf(config.getInstanceId());
        this.instanceName = config.getInstance();
        this.seed = config.getSeed();
        this.algorithmConfiguration = new AlgorithmConfiguration(config.getConfiguration());
    }

    public AlgorithmConfiguration getAlgorithmConfig() {
        return algorithmConfiguration;
    }

    /**
     * <p>Getter for the field <code>instancePath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * <p>Getter for the field <code>seed</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public long getSeed() {
        return seed;
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
                ", alg=" + algorithmConfiguration +
                '}';
    }
}
