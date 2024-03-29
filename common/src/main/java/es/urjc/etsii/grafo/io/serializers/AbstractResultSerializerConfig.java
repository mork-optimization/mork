package es.urjc.etsii.grafo.io.serializers;

/**
 * This class is used to configure the serializer by the properties specified in the application.yml
 * {@see application.yml}
 */
public abstract class AbstractResultSerializerConfig extends AbstractSerializerConfig {

    /**
     * When should result data be exported?
     */
    private ResultExportFrequency frequency = ResultExportFrequency.EXPERIMENT_END;

    /**
     * How frequently should data be exported?
     * @return export frequency
     */
    public ResultExportFrequency getFrequency() {
        return frequency;
    }

    /**
     * How frequently should data be exported?
     * @param frequency export frequency
     */
    public void setFrequency(ResultExportFrequency frequency) {
        this.frequency = frequency;
    }
}
