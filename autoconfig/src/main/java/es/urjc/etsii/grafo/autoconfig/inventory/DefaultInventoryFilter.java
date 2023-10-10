package es.urjc.etsii.grafo.autoconfig.inventory;

public class DefaultInventoryFilter implements IInventoryFilter {
    @Override
    public boolean include(Class<?> clazz) {
        return true;
    }
}
