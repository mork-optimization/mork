package es.urjc.etsii.grafo.mreflp.alg;

/** Table 1; immutable and shared only as configuration. */
public record LMLSParameters(double epsilon, int maxIter, int tenure, double alpha, double beta, double gamma, double rho) {
    public static final LMLSParameters PAPER = new LMLSParameters(0.6, 20, 3, 0.1, 0.2, 0.3, 0.3);
    public LMLSParameters {
        if (!(epsilon >= 0 && epsilon <= 1) || maxIter < 1 || tenure < 1) throw new IllegalArgumentException("Invalid search parameters");
        for (double factor : new double[]{alpha, beta, gamma, rho}) {
            if (!(factor > 0 && factor < 1)) throw new IllegalArgumentException("Learning factors must be in (0,1)");
        }
    }
}
