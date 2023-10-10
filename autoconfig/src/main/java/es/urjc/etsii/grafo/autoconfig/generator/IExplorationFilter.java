package es.urjc.etsii.grafo.autoconfig.generator;

/**
 * Decides which branches of the tree should be explored, and which ones rejected
 */
public interface IExplorationFilter {
    boolean reject(TreeContext context, Class<?> currentComponent);
}
