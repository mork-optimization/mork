package es.urjc.etsii.grafo.solver.create.grasp;

/**
 * Calculate alpha value in a GRASP like constructive
 */
public @FunctionalInterface
interface AlphaProvider {
    /**
     * Calculate alpha value to use for a given GRASP construction
     *
     * @return Next alpha value to use
     */
    double getAlpha();
}
