package es.urjc.etsii.grafo.autoconfig.inventory;


/**
 * Allows the user to filter algorithm components without having to comment or modify the components.
 * By default, two example implementations are provided that can be easily extended.
 * Users should extend either {@link WhitelistInventoryFilter} or {@link BlacklistInventoryFilter}.
 */
public interface IInventoryFilter {
    boolean include(Class<?> clazz);
}
