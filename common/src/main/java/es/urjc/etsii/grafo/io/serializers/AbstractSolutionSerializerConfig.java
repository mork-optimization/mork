package es.urjc.etsii.grafo.io.serializers;

/**
 * This class is used to configure the serializer by the properties specified in the application.yml
 * {@see application.yml}
 */
public abstract class AbstractSolutionSerializerConfig extends AbstractSerializerConfig {

    /**
     * When should result data be exported?
     */
    private SolutionExportFrequency frequency = SolutionExportFrequency.BEST_PER_ALG_INSTANCE;

    /**
     * How frequently should data be exported?
     * @return export frequency
     */
    public SolutionExportFrequency getFrequency() {
        return frequency;
    }

    /**
     * How frequently should data be exported?
     * @param frequency export frequency
     */
    public void setFrequency(SolutionExportFrequency frequency) {
        this.frequency = frequency;
    }
}
