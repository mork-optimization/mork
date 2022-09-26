package es.urjc.etsii.grafo.autoconfig.service.filter;

public class DefaultFilterStrategy implements InventoryFilterStrategy {
    @Override
    public boolean include(Class<?> clazz) {
        return true;
    }
}
