package es.urjc.etsii.grafo.autoconfig.service.filter;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.util.ClassUtil;

import java.util.Set;

/**
 * Includes ONLY the given classes/interfaces and their implementations, ignores the rest. By default, includes nothing
 */
@InheritedComponent
public abstract class WhitelistFilterStrategy implements InventoryFilterStrategy {

    protected final Set<Class<?>> whitelistedClasses;

    protected WhitelistFilterStrategy() {
        whitelistedClasses = getWhitelist();
    }

    public abstract Set<Class<?>> getWhitelist();

    @Override
    public boolean include(Class<?> clazz) {
        return ClassUtil.hierarchyContainsAny(clazz, whitelistedClasses);
    }
}
