package es.urjc.etsii.grafo.io.serializers;

/**
 * Should we export each (instance, algorithm, iteration),
 * only the best solution for each (algorithm, instance),
 * or only the best solution for each instance?
 */
public enum SolutionExportFrequency {
    ALL(3),
    BEST_PER_ALG_INSTANCE(2),
    BEST_PER_INSTANCE(1);

    private final int ordering;

    SolutionExportFrequency(int ordering) {
        this.ordering = ordering;
    }

    public boolean matches(SolutionExportFrequency frequency){
        return this.ordering >= frequency.ordering;
    }

}
