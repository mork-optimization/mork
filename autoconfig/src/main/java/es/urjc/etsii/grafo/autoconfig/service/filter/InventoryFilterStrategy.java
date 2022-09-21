package es.urjc.etsii.grafo.autoconfig.service.filter;


/**
 * Allows the user to filter algorithm components without having to comment or modify the components.
 * By default, two example implementations are provided that can be easily extended.
 * Users should extend either {@link WhitelistFilterStrategy} or {@link BlacklistFilterStrategy}.
 */
public interface InventoryFilterStrategy {
    boolean include(Class<?> clazz);
}
