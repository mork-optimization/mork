package es.urjc.etsii.grafo.autoconfig.generator;

public class DefaultExplorationFilter implements IExplorationFilter {
    @Override
    public boolean reject(TreeContext context, Class<?> currentComponent) {
        return false;
    }
}
