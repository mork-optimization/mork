package es.urjc.etsii.grafo.io.serializers;

/**
 * Defines results export frequency.
 */
public enum ResultExportFrequency {
    /**
     * Try to export results using this serializer each time an instance completes executing.
     */
    PER_INSTANCE,

    /**
     * Try to export results only after the experiment completes.
     */
    EXPERIMENT_END
}
