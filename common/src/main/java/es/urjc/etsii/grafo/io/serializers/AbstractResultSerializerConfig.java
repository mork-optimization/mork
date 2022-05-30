package es.urjc.etsii.grafo.io.serializers;

/**
 * This class is used to configure the serializer by the properties specified in the application.yml
 * {@see application.yml}
 */
public abstract class AbstractResultSerializerConfig extends AbstractSerializerConfig{

    /**
     * When should result data be exported?
     */
    private Frequency frequency = Frequency.EXPERIMENT_END;

    /**
     * How frequently should data be exported?
     * @return export frequency
     */
    public Frequency getFrequency() {
        return frequency;
    }

    /**
     * How frequently should data be exported?
     * @param frequency export frequency
     */
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    /**
     * Defines export frequency.
     */
    public enum Frequency {
        /**
         * Try to export results using this serializer each time an instance completes executing.
         */
        PER_INSTANCE,

        /**
         * Try to export results only after the experiment completes.
         */
        EXPERIMENT_END
    }
}
